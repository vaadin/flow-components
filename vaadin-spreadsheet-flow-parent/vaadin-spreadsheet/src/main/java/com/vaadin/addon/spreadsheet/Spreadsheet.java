package com.vaadin.addon.spreadsheet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.converter.ExcelToHtmlUtils;
import org.apache.poi.hssf.record.cf.CellRangeUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;

import com.vaadin.addon.spreadsheet.client.ImageInfo;
import com.vaadin.addon.spreadsheet.client.MergedRegion;
import com.vaadin.addon.spreadsheet.client.MergedRegionUtil.MergedRegionContainer;
import com.vaadin.addon.spreadsheet.client.SpreadsheetClientRpc;
import com.vaadin.addon.spreadsheet.client.SpreadsheetState;
import com.vaadin.addon.spreadsheet.command.SizeChangeCommand;
import com.vaadin.addon.spreadsheet.command.SizeChangeCommand.Type;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.util.ReflectTools;

@SuppressWarnings("serial")
public class Spreadsheet extends AbstractComponent implements HasComponents,
        Action.Container {

    private static final Logger LOGGER = Logger.getLogger(Spreadsheet.class
            .getName());

    /**
     * An interface for handling the edited cell value from user input.
     */
    public interface CellValueHandler {

        /**
         * Called if a cell value has been edited by the user by using the
         * default cell editor. Use
         * {@link Spreadsheet#setCellValueHandler(CellValueHandler)} to enable
         * it for the spreadsheet.
         * 
         * @param cell
         *            the cell that has been edited, may be <code>null</code> if
         *            the cell doesn't yet exists
         * @param sheet
         *            the sheet the cell belongs to, the currently active sheet
         * @param colIndex
         *            0-based cell column index
         * @param rowIndex
         *            0-based cell row index
         * @param newValue
         *            the value user has entered
         * @param formulaEvaluator
         *            the {@link FormulaEvaluator} for this sheet
         * @param formatter
         *            the {@link DataFormatter} for this workbook
         * @return <code>true</code> if component default parsing should still
         *         be done, <code>false</code> if not
         */
        public boolean cellValueUpdated(Cell cell, Sheet sheet, int colIndex,
                int rowIndex, String newValue,
                FormulaEvaluator formulaEvaluator, DataFormatter formatter);
    }

    /**
     * An interface for handling clicks on cells that contain a hyperlink.
     * <p>
     * Implement this interface and use
     * {@link Spreadsheet#setHyperlinkCellClickHandler(HyperlinkCellClickHandler)}
     * to enable it for the spreadsheet.
     */
    public interface HyperlinkCellClickHandler {

        /**
         * Called when a hyperlink cell has been clicked.
         * 
         * @param cell
         *            the cell that contains the hyperlink
         * @param hyperlink
         *            the actual hyperlink
         * @param spreadsheet
         *            the component
         */
        public void onHyperLinkCellClick(Cell cell, Hyperlink hyperlink,
                Spreadsheet spreadsheet);
    }

    private SpreadsheetStyleFactory styler;
    private HyperlinkCellClickHandler hyperlinkCellClickHandler;
    private SpreadsheetComponentFactory customComponentFactory;

    private final CellSelectionManager selectionManager = new CellSelectionManager(
            this);
    private final CellValueManager valueManager = new CellValueManager(this);
    private final CellShifter cellShifter = new CellShifter(this);
    private final ContextMenuManager contextMenuManager = new ContextMenuManager(
            this);
    private final SpreadsheetHistoryManager historyManager = new SpreadsheetHistoryManager(
            this);
    private ConditionalFormatter conditionalFormatter;

    private int firstRow;
    private int lastRow;
    private int firstColumn;
    private int lastColumn;

    /**
     * This is used for making sure the cells are sent to client side in when
     * the next cell data request comes. This is triggered when the client side
     * connector init() method is run.
     */
    private boolean reloadCellDataOnNextScroll;

    private int defaultNewSheetRows = SpreadsheetFactory.DEFAULT_ROWS;
    private int defaultNewSheetColumns = SpreadsheetFactory.DEFAULT_COLUMNS;

    private boolean topLeftCellCommentsLoaded;
    private boolean topLeftCellHyperlinksLoaded;

    protected int mergedRegionCounter;

    private Workbook workbook;

    /** true if the component sheet should be reloaded on client side. */
    private boolean reload;

    /** are tables for currently active sheet loaded */
    private boolean tablesLoaded;

    /** image sizes need to be recalculated on column/row resizing s */
    private boolean reloadImageSizesFromPOI;

    protected String initialSheetSelection = null;

    private HashSet<Component> customComponents;

    private HashSet<PopupButton> sheetPopupButtons;

    protected HashSet<SheetImageWrapper> sheetImages;

    private HashSet<SpreadsheetTable> tables;

    protected final MergedRegionContainer mergedRegionContainer = new MergedRegionContainer() {

        @Override
        public MergedRegion getMergedRegionStartingFrom(int column, int row) {
            List<MergedRegion> mergedRegions = getState(false).mergedRegions;
            if (mergedRegions != null) {
                for (MergedRegion region : mergedRegions) {
                    if (region.col1 == column && region.row1 == row) {
                        return region;
                    }
                }
            }
            return null;
        }

        @Override
        public MergedRegion getMergedRegion(int column, int row) {
            List<MergedRegion> mergedRegions = getState(false).mergedRegions;
            if (mergedRegions != null) {
                for (MergedRegion region : mergedRegions) {
                    if (region.col1 <= column && region.row1 <= row
                            && region.col2 >= column && region.row2 >= row) {
                        return region;
                    }
                }
            }
            return null;
        }
    };

    /**
     * Creates a new Spreadsheet component using the newer Excel version
     * {@link XSSFWorkbook} and default {@link SpreadsheetFactory#DEFAULT_ROWS}
     * and {@link SpreadsheetFactory#DEFAULT_COLUMNS}.
     */
    public Spreadsheet() {
        sheetImages = new HashSet<SheetImageWrapper>();
        tables = new HashSet<SpreadsheetTable>();

        registerRpc(new SpreadsheetHandlerImpl(this));
        setSizeFull(); // Default to full size

        SpreadsheetFactory.loadSpreadsheetWith(this, null);
    }

    /**
     * Creates a new Spreadsheet component and loads the given Workbook.
     * 
     * @param workbook
     */
    public Spreadsheet(Workbook workbook) {
        sheetImages = new HashSet<SheetImageWrapper>();
        tables = new HashSet<SpreadsheetTable>();

        registerRpc(new SpreadsheetHandlerImpl(this));
        setSizeFull(); // Default to full size

        SpreadsheetFactory.loadSpreadsheetWith(this, workbook);

    }

    /**
     * Creates a new Spreadsheet component and loads the given Excel file.
     * 
     * @param file
     * @throws InvalidFormatException
     * @throws IOException
     */
    public Spreadsheet(File file) throws InvalidFormatException, IOException {
        this(WorkbookFactory.create(file));
    }

    /**
     * Adds an action handler to the spreadsheet that handles the event produced
     * by the context menu (right click) on cells and row and column headers.
     * The action handler is component, not workbook, specific.
     * <p>
     * The parameters on the
     * {@link Handler#handleAction(Action, Object, Object)} and
     * {@link Handler#getActions(Object, Object)} depend on the actual target of
     * the right click.
     * <p>
     * The second parameter (sender) on
     * {@link Handler#getActions(Object, Object)} is always the spreadsheet
     * component. In case of a cell, the first parameter (target) on contains
     * the latest {@link SelectionChangeEvent} for the spreadsheet. In case of a
     * row or a column header, the first parameter (target) is a
     * {@link CellRangeAddress}. To distinct between column / row header, you
     * can use {@link CellRangeAddress#isFullColumnRange()} and
     * {@link CellRangeAddress#isFullRowRange()}.
     * <p>
     * Similarly for {@link Handler#handleAction(Action, Object, Object)} the
     * second parameter (sender) is always the spreadsheet component. The third
     * parameter (target) is the latest {@link SelectionChangeEvent} for the
     * spreadsheet, or the {@link CellRangeAddress} defining the selected row /
     * column header.
     */
    @Override
    public void addActionHandler(Handler actionHandler) {
        contextMenuManager.addActionHandler(actionHandler);
        getState().hasActions = contextMenuManager.hasActionHandlers();
    }

    /**
     * Removes a previously registered action handler from this spreadsheet.
     */
    @Override
    public void removeActionHandler(Handler actionHandler) {
        contextMenuManager.removeActionHandler(actionHandler);
        getState().hasActions = contextMenuManager.hasActionHandlers();
    }

    /**
     * Sets the {@link CellValueHandler} for this component (not workbook/sheet
     * specific). It is called when a cell's value has been updated by the user
     * by using the spreadsheet component's default editor (text input).
     * 
     * @param customCellValueHandler
     *            or <code>null</code> if none should be used
     */
    public void setCellValueHandler(CellValueHandler customCellValueHandler) {
        getCellValueManager().setCustomCellValueHandler(customCellValueHandler);
    }

    /**
     * See {@link CellValueHandler}.
     * 
     * @return the current {@link CellValueHandler} for this component or
     *         <code>null</code> if none has been set
     */
    public CellValueHandler getCellValueHandler() {
        return getCellValueManager().getCustomCellValueHandler();
    }

    /**
     * Sets the {@link HyperlinkCellClickHandler} for this component (not
     * workbook/sheet specific). It's called when the user click a cell that is
     * a hyperlink.
     * 
     * @param hyperLinkCellClickHandler
     *            or <code>null</code> if none should be used
     */
    public void setHyperlinkCellClickHandler(
            HyperlinkCellClickHandler hyperLinkCellClickHandler) {
        hyperlinkCellClickHandler = hyperLinkCellClickHandler;
    }

    /**
     * see {@link HyperlinkCellClickHandler}.
     * 
     * @return the current {@link HyperlinkCellClickHandler} for this component
     *         or <code>null</code> if none has been set
     */
    public HyperlinkCellClickHandler getHyperlinkCellClickHandler() {
        return hyperlinkCellClickHandler;
    }

    /**
     * 
     * @return the contextMenuManager
     */
    public ContextMenuManager getContextMenuManager() {
        return contextMenuManager;
    }

    /**
     * @return the selectionManager
     */
    public CellSelectionManager getCellSelectionManager() {
        return selectionManager;
    }

    public CellValueManager getCellValueManager() {
        return valueManager;
    }

    protected CellShifter getCellShifter() {
        return cellShifter;
    }

    /**
     * @return the historyManager
     */
    public SpreadsheetHistoryManager getSpreadsheetHistoryManager() {
        return historyManager;
    }

    protected MergedRegionContainer getMergedRegionContainer() {
        return mergedRegionContainer;
    }

    /**
     * Returns the first visible column in the scroll area (not freeze pane)
     * 
     * @return 1-based
     */
    public int getFirstColumn() {
        return firstColumn;
    }

    /**
     * Returns the last visible column in the scroll area
     * 
     * @return 1-based
     */
    public int getLastColumn() {
        return lastColumn;
    }

    /**
     * Returns the first visible row in the scroll area (not freeze pane)
     * 
     * @return 1-based
     */
    public int getFirstRow() {
        return firstRow;
    }

    /**
     * Returns the last visible row in the scroll area
     * 
     * @return 1-based
     */
    public int getLastRow() {
        return lastRow;
    }

    /**
     * Returns the position of the vertical split (freeze pane). NOTE: this is
     * the opposite from POI, this is the last ROW that is frozen.
     * 
     * @return last frozen row or 0 if none
     */
    public int getVerticalSplitPosition() {
        return getState(false).verticalSplitPosition;
    }

    /**
     * Returns the position of the horizontal split (freeze pane). NOTE: this is
     * the opposite from POI, this is the last COLUMN that is frozen.
     * 
     * @return last frozen column or 0 if none
     */
    public int getHorizontalSplitPosition() {
        return getState(false).horizontalSplitPosition;
    }

    /**
     * Returns true if the component is being re-rendered after this roundtrip
     * (sheet change etc.)
     * 
     * @return
     */
    public boolean isRealoadingOnThisRoundtrip() {
        return reload;
    }

    @Override
    protected void fireEvent(EventObject event) {
        super.fireEvent(event);
    }

    protected void onSheetScroll(int firstRow, int lastRow, int firstColumn,
            int lastColumn) {
        if (reloadCellDataOnNextScroll || this.firstRow != firstRow
                || this.lastRow != lastRow || this.firstColumn != firstColumn
                || this.lastColumn != lastColumn) {
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstColumn = firstColumn;
            this.lastColumn = lastColumn;
            loadCells(firstRow, lastRow, firstColumn, lastColumn);
        }
        if (initialSheetSelection != null) {
            selectionManager.onSheetAddressChanged(initialSheetSelection);
            initialSheetSelection = null;
        } else if (reloadCellDataOnNextScroll) {
            selectionManager.reloadCurrentSelection();
        }
        reloadCellDataOnNextScroll = false;
    }

    protected boolean isRangeEditable(CellRangeAddress cellRangeAddress) {
        return isRangeEditable(cellRangeAddress.getFirstColumn(),
                cellRangeAddress.getLastColumn(),
                cellRangeAddress.getFirstRow(), cellRangeAddress.getLastRow());
    }

    /**
     * 
     * @param col1
     *            0-based
     * @param col2
     *            0-based
     * @param row1
     *            0-based
     * @param row2
     *            0-based
     * @return
     */
    protected boolean isRangeEditable(int col1, int col2, int row1, int row2) {
        if (isSheetProtected()) {
            for (int r = row1; r <= row2; r++) {
                final Row row = getActiveSheet().getRow(r);
                if (row != null) {
                    for (int c = col1; c <= col2; c++) {
                        final Cell cell = row.getCell(c);
                        if (isCellLocked(cell)) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    protected CellRangeAddress createCorrectCellRangeAddress(
            String addressString) {
        final String[] split = addressString.split(":");
        final CellReference cr1 = new CellReference(split[0]);
        final CellReference cr2 = new CellReference(split[1]);
        int r1 = cr1.getRow() > cr2.getRow() ? cr2.getRow() : cr1.getRow();
        int r2 = cr1.getRow() > cr2.getRow() ? cr1.getRow() : cr2.getRow();
        int c1 = cr1.getCol() > cr2.getCol() ? cr2.getCol() : cr1.getCol();
        int c2 = cr1.getCol() > cr2.getCol() ? cr1.getCol() : cr2.getCol();
        if (r1 >= getState().rows) {
            r1 = getState().rows - 1;
        }
        if (r2 >= getState().rows) {
            r2 = getState().rows - 1;
        }
        if (c1 >= getState().cols) {
            c1 = getState().cols - 1;
        }
        if (c2 >= getState().cols) {
            c2 = getState().cols - 1;
        }
        return new CellRangeAddress(r1, r2, c1, c2);
    }

    /**
     * 
     * @param col1
     *            1-based
     * @param col2
     *            1-based
     * @param row1
     *            1-based
     * @param row2
     *            1-based
     * @return
     */
    protected CellRangeAddress createCorrectCellRangeAddress(int col1,
            int col2, int row1, int row2) {
        int r1 = row1 > row2 ? row2 : row1;
        int r2 = row1 > row2 ? row1 : row2;
        int c1 = col1 > col2 ? col2 : col1;
        int c2 = col1 > col2 ? col1 : col2;
        if (r1 >= getState().rows) {
            r1 = getState().rows;
        }
        if (r2 >= getState().rows) {
            r2 = getState().rows;
        }
        if (c1 >= getState().cols) {
            c1 = getState().cols;
        }
        if (c2 >= getState().cols) {
            c2 = getState().cols;
        }
        return new CellRangeAddress(r1 - 1, r2 - 1, c1 - 1, c2 - 1);
    }

    @Override
    protected SpreadsheetState getState() {
        return (SpreadsheetState) super.getState();
    }

    @Override
    protected SpreadsheetState getState(boolean markAsDirty) {
        return (SpreadsheetState) super.getState(markAsDirty);
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        valueManager.updateFormatter(locale);
        updatedAndRecalculateAllCellValues();
    }

    /**
     * See {@link Workbook#setSheetHidden(int, int)}.
     * <p>
     * Get the Workbook with {@link #getWorkbook()} and use its API to access
     * status on currently visible/hidden/very hidden sheets.
     * 
     * If the currently active sheet is set hidden, another sheet is set as
     * active sheet automatically. At least one sheet should be always visible.
     * 
     * @param hidden
     *            0-visible, 1-hidden, 2-very hidden
     * @param sheetPOIIndex
     *            0-based
     * @throws IllegalArgumentException
     *             if the index or state is invalid, or if trying to hide the
     *             only visible sheet
     */
    public void setSheetHidden(int sheetPOIIndex, int hidden)
            throws IllegalArgumentException {
        // POI allows user to hide all sheets ...
        if (hidden != 0
                && SpreadsheetUtil.getNumberOfVisibleSheets(workbook) == 1) {
            throw new IllegalArgumentException(
                    "At least one sheet should be always visible.");
        }
        boolean isHidden = workbook.isSheetHidden(sheetPOIIndex);
        boolean isVeryHidden = workbook.isSheetVeryHidden(sheetPOIIndex);
        int activeSheetIndex = workbook.getActiveSheetIndex();
        workbook.setSheetHidden(sheetPOIIndex, hidden);

        // skip component reload if "nothing changed"
        if (hidden == 0 && (isHidden || isVeryHidden) || hidden != 0
                && !(isHidden && isVeryHidden)) {
            if (sheetPOIIndex != activeSheetIndex) {
                reloadSheetNames();
                getState().sheetIndex = getSpreadsheetSheetIndex(activeSheetIndex) + 1;
            } else { // the active sheet can be only set as hidden
                int oldVisibleSheetIndex = getState().sheetIndex - 1;
                if (hidden != 0
                        && activeSheetIndex == (workbook.getNumberOfSheets() - 1)) {
                    // hiding the active sheet, and it was the last sheet
                    oldVisibleSheetIndex--;
                }
                int newActiveSheetIndex = getVisibleSheetPOIIndex(oldVisibleSheetIndex);
                workbook.setActiveSheet(newActiveSheetIndex);
                reloadActiveSheetData();
                SpreadsheetFactory
                        .reloadSpreadsheetData(this, getActiveSheet());
            }
        }
    }

    /**
     * Returns an array containing the currently visible sheets' names. Does not
     * contain hidden or very hidden sheets.
     * <p>
     * To get all of the current {@link Workbook}'s sheet names, you access the
     * Apache POI API with {@link #getWorkbook()}.
     * 
     * @return the currently visible sheets' names
     */
    public String[] getVisibleSheetNames() {
        final String[] names = getState(false).sheetNames;
        return Arrays.copyOf(names, names.length);
    }

    /**
     * 
     * @param sheetIndex
     *            0-based, visible sheets index
     * @param sheetName
     *            not null, nor empty nor longer than 31 characters. must be
     *            unique
     * @throws IllegalArgumentException
     *             if the index is invalid, or if the sheet name invalid see
     *             {@link WorkbookUtil#validateSheetName(String)}
     */
    public void setSheetName(int sheetIndex, String sheetName)
            throws IllegalArgumentException {
        if (sheetIndex < 0 || sheetIndex >= getState().sheetNames.length) {
            throw new IllegalArgumentException("Invalid Sheet index given.");
        }
        int poiSheetIndex = getVisibleSheetPOIIndex(sheetIndex);
        setSheetNameWithPOIIndex(poiSheetIndex, sheetName);
    }

    /**
     * 
     * @param sheetIndex
     *            0-based, Apache POI based index (includes hidden & very hidden
     *            sheets)
     * @param sheetName
     *            not null, empty nor longer than 31 characters. must be unique
     * @throws IllegalArgumentException
     *             if the index is invalid, or if the sheet name invalid see
     *             {@link WorkbookUtil#validateSheetName(String)}
     * 
     */
    public void setSheetNameWithPOIIndex(int sheetIndex, String sheetName)
            throws IllegalArgumentException {
        if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException("Invalid POI Sheet index given.");
        }
        if (sheetName == null || sheetName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sheet Name cannot be null or an empty String, or contain backslash \\.");
        }
        if (isSheetNameExisting(sheetName)) {
            throw new IllegalArgumentException(
                    "Sheet name must be unique within the workbook.");
        }
        workbook.setSheetName(sheetIndex, sheetName);
        if (!workbook.isSheetVeryHidden(sheetIndex)) {
            int ourIndex = getSpreadsheetSheetIndex(sheetIndex);
            getState().sheetNames[ourIndex] = sheetName;
        }
    }

    /**
     * Sets the protection enabled as well as the password for the sheet at the
     * given index. <code>null</code> password removes the protection.
     * 
     * @param sheetPOIIndex
     *            0-based, the POI index (contains hidden and very hidden
     *            sheets) of the sheet to protect
     * @param password
     *            to set for protection. Pass <code>null</code> to remove
     *            protection
     */
    public void setSheetProtected(int sheetPOIIndex, String password) {
        if (sheetPOIIndex < 0 || sheetPOIIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException("Invalid POI Sheet index given.");
        }
        workbook.getSheetAt(sheetPOIIndex).protectSheet(password);
        getState().sheetProtected = getActiveSheet().getProtect();
        // if the currently active sheet was protected, the protection for the
        // currently selected cell might have changed
        if (sheetPOIIndex == workbook.getActiveSheetIndex()) {
            loadCustomComponents();
            selectionManager.reSelectSelectedCell();
        }
    }

    /**
     * Sets the protection enabled as well as the password for the currently
     * active sheet. <code>null</code> password removes the protection.
     * 
     * @param password
     *            to set for protection. Pass <code>null</code> to remove
     *            protection
     */
    public void setActiveSheetProtected(String password) {
        setSheetProtected(workbook.getActiveSheetIndex(), password);
    }

    /**
     * Creates a new sheet as the last sheet, sets it as the active sheet.
     * 
     * If the sheetName given is null, then the sheet name is automatically
     * generated by Apache POI in {@link Workbook#createSheet()}.
     * 
     * @param sheetName
     *            can be null, but not empty nor longer than 31 characters. must
     *            be unique
     * @param rows
     *            number of rows the sheet should have
     * @param columns
     *            number of columns the sheet should have
     * @throws IllegalArgumentException
     *             if the index is invalid, or if the sheet name is null or
     *             empty or the sheet already contains a sheet by that name
     */
    public void createNewSheet(String sheetName, int rows, int columns)
            throws IllegalArgumentException {
        if (sheetName != null && sheetName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sheet Name cannot be an empty String.");
        }
        if (sheetName != null && sheetName.length() > 31) {
            throw new IllegalArgumentException(
                    "Sheet Name cannot be longer than 31 characters");
        }
        if (sheetName != null && isSheetNameExisting(sheetName)) {
            throw new IllegalArgumentException(
                    "Sheet name must be unique within the workbook.");
        }
        final Sheet previousSheet = getActiveSheet();
        SpreadsheetFactory
                .addNewSheet(this, workbook, sheetName, rows, columns);
        fireSelectedSheetChangeEvent(previousSheet, getActiveSheet());
    }

    /**
     * Deletes the sheet. A workbook must contain at least one visible sheet.
     * 
     * @param sheetIndex
     *            0-based, max value {@link Workbook#getNumberOfSheets()} -1
     * @throws IllegalArgumentException
     *             in case there is only one visible sheet, or if index is
     *             invalid
     */
    public void deleteSheetWithPOIIndex(int pOISheetIndex)
            throws IllegalArgumentException {
        if (getNumberOfVisibleSheets() < 2) {
            throw new IllegalArgumentException(
                    "A workbook must contain at least one visible worksheet");
        }
        int removedVisibleIndex = getSpreadsheetSheetIndex(pOISheetIndex);
        workbook.removeSheetAt(pOISheetIndex);

        // POI doesn't seem to shift the active sheet index ...
        int oldIndex = getState().sheetIndex - 1;
        if (removedVisibleIndex <= oldIndex) { // removed before current
            if (oldIndex == (getNumberOfVisibleSheets())) {
                // need to shift index backwards if the current sheet is last
                workbook.setActiveSheet(getVisibleSheetPOIIndex(oldIndex - 1));
            } else {
                workbook.setActiveSheet(getVisibleSheetPOIIndex(oldIndex));
            }
        }
        // need to reload everything because there is a ALWAYS chance that the
        // removed sheet effects the currently visible sheet (via cell formulas
        // etc.)
        reloadActiveSheetData();
    }

    /**
     * Deletes the sheet. A workbook must contain at least one visible sheet.
     * 
     * @param sheetIndex
     *            0-based, max value {@link #getNumberOfVisibleSheets()} -1
     * @throws IllegalArgumentException
     *             in case there is only one visible sheet, or if index is
     *             invalid
     */
    public void deleteSheet(int sheetIndex) throws IllegalArgumentException {
        if (getNumberOfVisibleSheets() < 2) {
            throw new IllegalArgumentException(
                    "A workbook must contain at least one visible worksheet");
        }
        deleteSheetWithPOIIndex(getVisibleSheetPOIIndex(sheetIndex));
    }

    /**
     * Returns the number of currently visible sheets in the component. Doesn't
     * include the hidden or very hidden sheets in the POI model.
     * 
     * @return
     */
    public int getNumberOfVisibleSheets() {
        if (getState().sheetNames != null) {
            return getState().sheetNames.length;
        } else {
            return 0;
        }
    }

    /**
     * Returns the total number of sheets in the workbook (includes hidden and
     * very hidden sheets).
     * 
     * @return total number of sheets in the workbook
     */
    public int getNumberOfSheets() {
        return workbook.getNumberOfSheets();
    }

    private boolean isSheetNameExisting(String sheetName) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (workbook.getSheetName(i).equals(sheetName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the currently active sheet's index among the visible sheets (no
     * hidden or very hidden sheets).
     * 
     * @return 0-based index
     */
    public int getActiveSheetIndex() {
        return getState(false).sheetIndex - 1;
    }

    /**
     * Returns the currently active sheet's index among all sheets (including
     * hidden and very hidden sheets).
     * 
     * @return 0-based index
     */
    public int getActiveSheetPOIIndex() {
        return getVisibleSheetPOIIndex(getState(false).sheetIndex - 1);
    }

    /**
     * Sets the currently active sheet within the sheets that are visible.
     * 
     * @param sheetIndex
     *            0-based visible sheets index
     * @throws IllegalArgumentException
     *             if invalid index
     */
    public void setActiveSheetIndex(int sheetIndex)
            throws IllegalArgumentException {
        if (sheetIndex < 0 || sheetIndex >= getState().sheetNames.length) {
            throw new IllegalArgumentException("Invalid Sheet index given.");
        }
        int POISheetIndex = getVisibleSheetPOIIndex(sheetIndex);
        setActiveSheetWithPOIIndex(POISheetIndex);
    }

    /**
     * Sets the currently active sheet. The sheet at the given index should be
     * visible (not hidden or very hidden), or
     * 
     * @param sheetIndex
     *            0-based POI sheets index (all sheets)
     * @throws IllegalArgumentException
     *             if invalid index, or if the sheet is hidden or very hidden.
     */
    public void setActiveSheetWithPOIIndex(int sheetIndex)
            throws IllegalArgumentException {
        if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException("Invalid POI Sheet index given.");
        }
        if (workbook.isSheetHidden(sheetIndex)
                || workbook.isSheetVeryHidden(sheetIndex)) {
            throw new IllegalArgumentException(
                    "Cannot set a hidden or very hidden sheet as the active sheet. Given index: "
                            + sheetIndex);
        }
        workbook.setActiveSheet(sheetIndex);
        reloadActiveSheetData();
        SpreadsheetFactory.reloadSpreadsheetData(this,
                workbook.getSheetAt(sheetIndex));
        reloadActiveSheetStyles();
    }

    protected void onSheetSelected(int tabIndex, int scrollLeft, int scrollTop) {
        // this is for the very rare occasion when the sheet has been
        // selected and the selected sheet value is still negative
        int oldIndex = Math.abs(getState().sheetIndex) - 1;
        getState().verticalScrollPositions[oldIndex] = scrollTop;
        getState().horizontalScrollPositions[oldIndex] = scrollLeft;
        Sheet oldSheet = getActiveSheet();
        setActiveSheetIndex(tabIndex);
        Sheet newSheet = getActiveSheet();
        fireSelectedSheetChangeEvent(oldSheet, newSheet);
    }

    protected void onNewSheetCreated(int scrollLeft, int scrollTop) {
        getState().verticalScrollPositions[getState().sheetIndex - 1] = scrollTop;
        getState().horizontalScrollPositions[getState().sheetIndex - 1] = scrollLeft;
        createNewSheet(null, defaultNewSheetRows, defaultNewSheetColumns);
    }

    protected void onSheetRename(int sheetIndex, String sheetName) {
        // if excel doesn't keep these in history, neither will we
        setSheetNameWithPOIIndex(getVisibleSheetPOIIndex(sheetIndex), sheetName);
    }

    /**
     * Get the number of columns in the spreadsheet, or if
     * {@link #setMaximumColumns(int)} has been used, the current number of
     * columns the component shows (not the amount of columns in the actual
     * sheet).
     */
    public int getColumns() {
        return getState().cols;
    }

    /**
     * Get the number of rows in the spreadsheet, or if
     * {@link #setMaximumRows(int)} has been used, the current number of rows
     * the component shows (not the amout of rows in the actual sheet).
     */
    public int getRows() {
        return getState().rows;
    }

    /**
     * 
     * @return the formatter for this sheet
     */
    public DataFormatter getDataFormatter() {
        return valueManager.getDataFormatter();
    }

    /**
     * Returns Cell. If the cell is updated, call
     * {@link #markCellAsUpdated(Cell)} AFTER ALL UPDATES (value, type,
     * formatting or style) to mark the cell as "dirty" and when all updates are
     * done, remember to call {@link #updateMarkedCells()} to make sure client
     * side is updated.
     * 
     * @param row
     *            0-based
     * @param col
     *            0-based
     * @return the cell or null if not defined
     */
    public Cell getCell(int row, int col) {
        Row r = workbook.getSheetAt(workbook.getActiveSheetIndex()).getRow(row);
        if (r != null) {
            return r.getCell(col);
        } else {
            return null;
        }
    }

    /**
     * Deletes the cell from the sheet.
     * 
     * No need to call mark cell as updated with
     * {@link #markCellAsUpdated(Cell)}.
     * 
     * After editing is done, call {@link #updateMarkedCells()} to update the
     * values to the client.
     * 
     * This really deletes the cell, instead of just making it's value blank.
     * 
     * @param row
     *            0-based
     * @param col
     *            0-based
     */
    public void deleteCell(int row, int col) {
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        final Cell cell = activeSheet.getRow(row).getCell(col);
        if (cell != null) {
            // cell.setCellStyle(null); //TODO NPE on HSSF
            styler.cellStyleUpdated(cell, true);
            activeSheet.getRow(row).removeCell(cell);
            valueManager.cellDeleted(cell);
        }
    }

    /**
     * Marks the cell as updated. Should be called when the cell
     * value/formatting/style/etc. updating is done.
     * <p>
     * When all cell updating is done, remember to call
     * {@link #updateMarkedCells()} to publish the updates.
     * 
     * @param cellStyleUpdated
     *            has the cell style changed
     * 
     * @param cell
     */
    public void markCellAsUpdated(Cell cell, boolean cellStyleUpdated) {
        valueManager.cellUpdated(cell);
        if (cellStyleUpdated) {
            styler.cellStyleUpdated(cell, true);
        }
    }

    /**
     * Marks the cell as deleted. Should be called after removing a cell from
     * the {@link Workbook}.
     * <p>
     * When all cell updating is done, remember to call
     * {@link #updateMarkedCells()} to publish the updates.
     * 
     * @param cellStyleUpdated
     *            has the cell style changed
     * 
     * @param cell
     */
    public void markCellAsDeleted(Cell cell, boolean cellStyleUpdated) {
        valueManager.cellDeleted(cell);
        if (cellStyleUpdated) {
            styler.cellStyleUpdated(cell, true);
        }
    }

    /**
     * Updates the content of the cells that have been marked for update with
     * {@link #markCellAsUpdated(Cell, boolean)}. Also updates style selectors
     * for currently visible cells (but not the style content,
     * {@link #markCellAsUpdated(Cell, boolean)} should be used for that).
     * <p>
     * Does NOT update custom components (editors / always visible) for the
     * cells. For that, use {@link #reloadVisibleCellContents()}
     */
    public void updateMarkedCells() {
        // update conditional formatting in case styling has changed. New values
        // are fetched in ValueManager (below).
        conditionalFormatter.createConditionalFormatterRules();
        // FIXME should be optimized, should not go through all links, comments
        // etc. always
        valueManager.updateMarkedCellValues();
        // if the selected cell is of type formula, there is a change that the
        // formula has been changed.
        selectionManager.reSelectSelectedCell();
    }

    /**
     * Create a Formula type cell with the given formula.
     * 
     * After all editing is done, call {@link #updateMarkedCells()} or
     * {@link #updatedAndRecalculateAllCellValues()} to make sure client side is
     * updated. No need to call {@link #markCellAsUpdated(Cell)}, UNLESS the
     * cell style is changed.
     * 
     * @param row
     *            0-based
     * @param col
     *            0-based
     * @param formula
     *            the formula (should NOT start with "=")
     * @return the created cell
     * @throws IllegalArgumentException
     *             if columnIndex < 0 or greater than the maximum number of
     *             supported columns (255 for *.xls, 1048576 for *.xlsx)
     */
    public Cell createFormulaCell(int row, int col, String formula)
            throws IllegalArgumentException {
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        Row r = activeSheet.getRow(row);
        if (r == null) {
            r = activeSheet.createRow(row);
        }
        Cell cell = r.getCell(col);
        if (cell == null) {
            cell = r.createCell(col, Cell.CELL_TYPE_FORMULA);
        } else {
            final String key = SpreadsheetUtil.toKey(col + 1, row + 1);
            valueManager.clearCellCache(key);
            cell.setCellType(Cell.CELL_TYPE_FORMULA);
        }
        cell.setCellFormula(formula);
        valueManager.cellUpdated(cell);
        return cell;
    }

    /**
     * Create a new cell (or replace existing) with the given value, the type of
     * the value parameter will define the type of the cell. Thus value may be
     * of types: Boolean, Calendar, Date, Double or String. The default type
     * will be String, value of ({@link #toString()} will be given as the cell
     * value.
     * 
     * For formula cells, use {@link #createFormulaCell(int, int, String)}.
     * 
     * After all editing is done, call {@link #updateMarkedCells()} or
     * {@link #updatedAndRecalculateAllCellValues()} to make sure client side is
     * updated. No need to call {@link #markCellAsUpdated(Cell)}, UNLESS the
     * cell style is changed.
     * 
     * @param row
     *            0-based
     * @param col
     *            0-based
     * @param value
     *            object representing the type of the Cell
     * @return the created cell
     * @throws IllegalArgumentException
     *             if columnIndex < 0 or greater than the maximum number of
     *             supported columns (255 for *.xls, 1048576 for *.xlsx)
     */
    public Cell createCell(int row, int col, Object value)
            throws IllegalArgumentException {
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        Row r = activeSheet.getRow(row);
        if (r == null) {
            r = activeSheet.createRow(row);
        }
        Cell cell = r.getCell(col);
        if (cell == null) {
            cell = r.createCell(col);
        } else {
            final String key = SpreadsheetUtil.toKey(col + 1, row + 1);
            valueManager.clearCellCache(key);
        }
        if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if (value instanceof Calendar) {
            cell.setCellValue((Calendar) value);
        } else {
            cell.setCellValue(value.toString());
        }
        valueManager.cellUpdated(cell);
        return cell;
    }

    /**
     * Forces recalculation and update to client side for all of the sheet's
     * cells. DOES NOT UPDATE STYLES; use {@link #markCellAsUpdated(Cell)} when
     * cell styles change.
     */
    public void updatedAndRecalculateAllCellValues() {
        valueManager.clearEvaluatorCache();
        valueManager.clearCachedContent();
        updateRowAndColumnRangeCellData(1, getRows(), 1, getColumns());
        // if the selected cell is of type formula, there is a change that the
        // formula has been changed.
        selectionManager.reSelectSelectedCell();
    }

    /**
     * Set the number of columns shown for the current sheet. Any unset cells
     * are left empty. Any cells outside the given columns are hidden. Does not
     * update the actual POI-based model!
     * 
     * The default value will be the actual size of the sheet (from POI).
     * 
     */
    public void setMaximumColumns(int cols) {
        if (getState().cols != cols) {
            getState().cols = cols;
        }
    }

    /**
     * Set the number of rows shown for the current sheet. Any unset cells are
     * left empty. Any cells outside the given rows are hidden. Does not update
     * the actual POI-based model!
     * 
     * The default value will be the actual size of the sheet (from POI).
     */
    public void setMaximumRows(int rows) {
        if (getState().rows != rows) {
            getState().rows = rows;
        }
    }

    /**
     * Does {@link #setMaximumColumns(int)} & {@link #setMaximumRows(int)} in
     * one method.
     * 
     * @param cols
     * @param rows
     */
    public void setSheetMaximumSize(int cols, int rows) {
        getState().cols = cols;
        getState().rows = rows;
    }

    /**
     * This is the value that the component uses, it is derived from the active
     * sheets ({@link #getActiveSheet()}) default column width (Sheet
     * {@link #getDefaultColumnWidth()}).
     * 
     * @return the default column width in PX
     */
    public int getDefaultColumnWidth() {
        return getState().defColW;
    }

    /**
     * Set the default column width in pixels that the component uses, this
     * doesn't change the default column width of the underlying sheet, returned
     * by {@link #getActiveSheet()} and {@link Sheet#getDefaultColumnWidth()}.
     * 
     * @param widthPX
     */
    public void setDefaultColumnWidth(int widthPX) {
        if (widthPX <= 0) {
            throw new IllegalArgumentException(
                    "Default column width must be over 0, given value: "
                            + widthPX);
        }
        getState().defColW = widthPX;
    }

    /**
     * This is the default row height in points, by default it should be the
     * same as {@link Sheet#getDefaultRowHeightInPoints()} for current sheet
     * {@link #getActiveSheet()}.
     * 
     * @return
     */
    public float getDefaultRowHeightInPoints() {
        return getState().defRowH;
    }

    /**
     * Set the default row height in points for the component and the currently
     * active sheet, returned by {@link #getActiveSheet()}.
     * 
     * @param heightPT
     */
    public void setDefaultRowHeightInPoints(float heightPT) {
        if (heightPT <= 0.0f) {
            throw new IllegalArgumentException(
                    "Default row height must be over 0, given value: "
                            + heightPT);
        }
        getActiveSheet().setDefaultRowHeightInPoints(heightPT);
        getState().defRowH = heightPT;
    }

    /**
     * 
     * @param columnIndex
     *            0-based
     */
    protected void onColumnAutofit(int columnIndex) {
        SizeChangeCommand command = new SizeChangeCommand(this, Type.COLUMN);
        command.captureValues(new Integer[] { columnIndex + 1 });
        autofitColumn(columnIndex);
        historyManager.addCommand(command);
    }

    /**
     * Sets the column to automatically adjust the column width to fit to the
     * largest cell content. This is a POI feature, and is ment to be called
     * after all the data for that column has been written. See
     * {@link Sheet#autoSizeColumn(int)}.
     * <p>
     * This does not take into account cells that have custom Vaadin components
     * inside them.
     * 
     * @param columnIndex
     *            0-based
     */
    public void autofitColumn(int columnIndex) {
        final Sheet activeSheet = getActiveSheet();
        activeSheet.autoSizeColumn(columnIndex);
        getState().colW[columnIndex] = ExcelToHtmlUtils
                .getColumnWidthInPx(activeSheet.getColumnWidth(columnIndex));
        getCellValueManager().clearCacheForColumn(columnIndex + 1);
        getCellValueManager().loadCellData(firstRow, lastRow, columnIndex + 1,
                columnIndex + 1);
        if (sheetImages != null) {
            reloadImageSizesFromPOI = true;
            loadImages();
        }
    }

    /**
     * Shifts rows between startRow and endRow n number of rows. If you use a
     * negative number, it will shift rows up. Code ensures that rows don't wrap
     * around.
     * <p>
     * If you are adding / deleting rows, you might want to change the number of
     * visible rows rendered {@link #getRows()} with
     * {@link #setMaximumRows(int)}.
     * <p>
     * See {@link Sheet#shiftRows(int, int, int)}.
     * 
     * @param startRow
     *            The first row to shift (0-based)
     * @param endRow
     *            The last row to shift (0-based)
     * @param n
     *            number of rows to shift, positive shifts down, negative shifts
     *            up.
     */
    public void shiftRows(int startRow, int endRow, int n) {
        shiftRows(startRow, endRow, n, false, false);
    }

    /**
     * Shifts rows between startRow and endRow n number of rows. If you use a
     * negative number, it will shift rows up. Code ensures that rows don't wrap
     * around
     * <p>
     * If you are adding / deleting rows, you might want to change the number of
     * visible rows rendered {@link #getRows()} with
     * {@link #setMaximumRows(int)}.
     * <p>
     * See {@link Sheet#shiftRows(int, int, int, boolean, boolean)}.
     * 
     * @param startRow
     *            The first row to shift (0-based)
     * @param endRow
     *            The last row to shift (0-based)
     * @param n
     *            number of rows to shift, positive shifts down, negative shifts
     *            up.
     * @param copyRowHeight
     *            whether to copy the row height during the shift
     * @param resetOriginalRowHeight
     *            whether to set the original row's height to the default
     */
    public void shiftRows(int startRow, int endRow, int n,
            boolean copyRowHeight, boolean resetOriginalRowHeight) {
        Sheet sheet = getActiveSheet();
        sheet.shiftRows(startRow, endRow, n, copyRowHeight,
                resetOriginalRowHeight);
        // need to resend the cell values to client
        // remove all cached cell data that is now empty
        int start = n < 0 ? endRow + n + 1 : startRow;
        int end = n < 0 ? endRow : startRow + n - 1;
        valueManager.updateDeletedRowsInClientCache(start, end);
        // updateDeletedRowsInClientCache(start + 1, end + 1); this was a bug?
        int firstEffectedRow = n < 0 ? startRow + n : startRow;
        int lastEffectedRow = n < 0 ? endRow : endRow + n;
        if (copyRowHeight || resetOriginalRowHeight) {
            // might need to increase the size of the row heights array
            int oldLength = getState(false).rowH.length;
            int neededLength = endRow + n + 1;
            if (n > 0 && oldLength < neededLength) {
                getState().rowH = Arrays.copyOf(getState().rowH, neededLength);
            }
            for (int i = firstEffectedRow; i <= lastEffectedRow; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    if (row.getZeroHeight()) {
                        getState().rowH[i] = 0f;
                    } else {
                        getState().rowH[i] = row.getHeightInPoints();
                    }
                } else {
                    getState().rowH[i] = sheet.getDefaultRowHeightInPoints();
                }
            }
        }
        if (sheetImages != null) {
            reloadImageSizesFromPOI = true;
        }
        // need to shift the cell styles, clear and update
        // need to go -1 and +1 because of shifted borders..
        final ArrayList<Cell> cellsToUpdate = new ArrayList<Cell>();
        for (int r = (firstEffectedRow - 1); r <= (lastEffectedRow + 1); r++) {
            if (r < 0) {
                r = 0;
            }
            Row row = sheet.getRow(r);
            final Integer rowIndex = new Integer(r + 1);
            if (row == null) {
                if (getState(false).hiddenRowIndexes.contains(rowIndex)) {
                    getState().hiddenRowIndexes.remove(rowIndex);
                }
                for (int c = 0; c < getState().cols; c++) {
                    styler.clearCellStyle(c, r);
                }
            } else {
                if (row.getZeroHeight()) {
                    getState().hiddenRowIndexes.add(rowIndex);
                } else if (getState(false).hiddenRowIndexes.contains(rowIndex)) {
                    getState().hiddenRowIndexes.remove(rowIndex);
                }
                for (int c = 0; c < getState().cols; c++) {
                    Cell cell = row.getCell(c);
                    if (cell == null) {
                        styler.clearCellStyle(c, r);
                    } else {
                        cellsToUpdate.add(cell);
                    }
                }
            }
        }
        for (Cell cell : cellsToUpdate) {
            styler.cellStyleUpdated(cell, false);
        }
        styler.loadCustomBorderStylesToState();

        updateMarkedCells(); // deleted and formula cells and style selectors
        updateRowAndColumnRangeCellData(firstRow, lastRow, firstColumn,
                lastColumn); // shifted area values

        CellReference selectedCellReference = selectionManager
                .getSelectedCellReference();
        if (selectedCellReference.getRow() >= firstEffectedRow
                && selectedCellReference.getRow() <= lastEffectedRow) {
            selectionManager.onSheetAddressChanged(selectedCellReference
                    .formatAsString());
        }
    }

    protected void updateMergedRegions() {
        int regions = getActiveSheet().getNumMergedRegions();
        if (regions > 0) {
            getState().mergedRegions = new ArrayList<MergedRegion>();
            for (int i = 0; i < regions; i++) {
                final CellRangeAddress region = getActiveSheet()
                        .getMergedRegion(i);
                try {
                    final MergedRegion mergedRegion = new MergedRegion();
                    mergedRegion.col1 = region.getFirstColumn() + 1;
                    mergedRegion.col2 = region.getLastColumn() + 1;
                    mergedRegion.row1 = region.getFirstRow() + 1;
                    mergedRegion.row2 = region.getLastRow() + 1;
                    mergedRegion.id = mergedRegionCounter++;
                    getState().mergedRegions.add(i, mergedRegion);
                } catch (IndexOutOfBoundsException ioobe) {
                    createMergedRegionIntoSheet(region);
                }
            }
            while (regions < getState(false).mergedRegions.size()) {
                getState().mergedRegions.remove(getState(false).mergedRegions
                        .size() - 1);
            }
        } else {
            getState().mergedRegions = null;
        }
    }

    /**
     * Removes rows. See {@link Sheet#removeRow(Row)}. Removes all row content,
     * deletes cell and resets size. Does not shift rows up (!) - use
     * {@link #shiftRows(int, int, int, boolean, boolean)} for that.
     * 
     * @param startRow
     *            0-based
     * @param endRow
     *            0-based
     */
    public void removeRows(int startRow, int endRow) {
        Sheet sheet = getActiveSheet();
        for (int i = startRow; i <= endRow; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                getActiveSheet().removeRow(row);
            }
        }
        for (int i = startRow; i <= endRow; i++) {
            getState(false).rowH[i] = sheet.getDefaultRowHeightInPoints();
        }
        updateMergedRegions();
        valueManager.updateDeletedRowsInClientCache(startRow + 1, endRow + 1);
        if (sheetImages != null) {
            reloadImageSizesFromPOI = true;
        }
        updateMarkedCells();
        CellReference selectedCellReference = getSelectedCellReference();
        if (selectedCellReference.getRow() >= startRow
                && selectedCellReference.getRow() <= endRow) {
            selectionManager.reSelectSelectedCell();
        }

    }

    /**
     * Merge cells. See {@link Sheet#addMergedRegion(CellRangeAddress)}.
     * <p>
     * Parameters 0-based
     * 
     * @param col1
     * @param col2
     * @param row1
     * @param row2
     */
    public void addMergedRegion(int col1, int col2, int row1, int row2) {
        addMergedRegion(new CellRangeAddress(row1, row2, col1, col2));
    }

    /**
     * Merge cells. See {@link Sheet#addMergedRegion(CellRangeAddress)}.
     * <p>
     * If another existing merged region is completely inside the given range,
     * it is removed. If another existing region either encloses or overlaps the
     * given range, an error is thrown. See
     * {@link CellRangeUtil#intersect(CellRangeAddress, CellRangeAddress)}.
     * <p>
     * Note: POI doesn't seem to update the cells that are "removed" - the
     * values for those cells are still existing and being used in possible
     * formulas. If you need to make sure those values are removed, just delete
     * the cells before creating the merged region.
     * <p>
     * If the added region effects the currently selected cell, a new
     * {@link SelectionChangeEvent} is fired.
     * 
     * @param region
     * @throws IllegalArgumentException
     *             if the given region overlaps or encloses another existing
     *             region within the sheet
     */
    public void addMergedRegion(CellRangeAddress region)
            throws IllegalArgumentException {
        final Sheet sheet = getActiveSheet();
        // need to check if there are merged regions already inside the given
        // range, otherwise very bad inconsistencies appear.
        int index = 0;
        while (index < sheet.getNumMergedRegions()) {
            CellRangeAddress existingRegion = sheet.getMergedRegion(index);
            int intersect = CellRangeUtil.intersect(region, existingRegion);
            if (intersect == CellRangeUtil.INSIDE) {
                deleteMergedRegion(index);
            } else if (intersect == CellRangeUtil.OVERLAP
                    || intersect == CellRangeUtil.ENCLOSES) {
                throw new IllegalArgumentException("An existing region "
                        + existingRegion
                        + " "
                        + (intersect == CellRangeUtil.OVERLAP ? "overlaps "
                                : "encloses ") + "the given region " + region);
            } else {
                index++;
            }
        }
        createMergedRegionIntoSheet(region);
        selectionManager.mergedRegionAdded(region);
    }

    private void createMergedRegionIntoSheet(CellRangeAddress region) {
        Sheet sheet = getActiveSheet();
        int addMergedRegionIndex = sheet.addMergedRegion(region);
        MergedRegion mergedRegion = new MergedRegion();
        mergedRegion.col1 = region.getFirstColumn() + 1;
        mergedRegion.col2 = region.getLastColumn() + 1;
        mergedRegion.row1 = region.getFirstRow() + 1;
        mergedRegion.row2 = region.getLastRow() + 1;
        mergedRegion.id = mergedRegionCounter++;
        if (getState().mergedRegions == null) {
            getState().mergedRegions = new ArrayList<MergedRegion>();
        }
        getState().mergedRegions.add(addMergedRegionIndex - 1, mergedRegion);
        // update the style & data for the region cells, effects region + 1
        // FIXME POI doesn't seem to care that the other cells inside the merged
        // region should be removed; the values those cells have are still used
        // in formulas..
        for (int r = mergedRegion.row1; r <= (mergedRegion.row2 + 1); r++) {
            Row row = sheet.getRow(r - 1);
            for (int c = mergedRegion.col1; c <= (mergedRegion.col2 + 1); c++) {
                if (row != null) {
                    Cell cell = row.getCell(c - 1);
                    if (cell != null) {
                        styler.cellStyleUpdated(cell, false);
                        if ((c != mergedRegion.col1 || r != mergedRegion.row1)
                                && c <= mergedRegion.col2
                                && r <= mergedRegion.row2) {
                            getCellValueManager().markCellForRemove(cell);
                        }
                    }
                }
            }
        }
        styler.loadCustomBorderStylesToState();
        updateMarkedCells();
    }

    /**
     * Removes a merged region with the given index. Current merged regions can
     * be inspected within the currently active sheet with
     * {@link #getActiveSheet()} and {@link Sheet#getMergedRegion(int)} and
     * {@link Sheet#getNumMergedRegions()}.
     * <p>
     * Note that in POI after removing a merged region at index n, all regions
     * added after the removed region will get a new index (index-1).
     * <p>
     * If the removed region effects the currently selected cell, a new
     * {@link SelectionChangeEvent} is fired.
     * 
     * @param index
     *            0-based position in the POI merged region array
     */
    public void removeMergedRegion(int index) {
        final CellRangeAddress removedRegion = getActiveSheet()
                .getMergedRegion(index);
        deleteMergedRegion(index);
        updateMarkedCells();
        // update selection if removed region overlaps
        selectionManager.mergedRegionRemoved(removedRegion);
    }

    private void deleteMergedRegion(int index) {
        final Sheet sheet = getActiveSheet();
        sheet.removeMergedRegion(index);
        MergedRegion mergedRegion = getState().mergedRegions.remove(index);
        // update the style for the region cells, effects region + 1 row&col
        for (int r = mergedRegion.row1; r <= (mergedRegion.row2 + 1); r++) {
            Row row = sheet.getRow(r - 1);
            if (row != null) {
                for (int c = mergedRegion.col1; c <= (mergedRegion.col2 + 1); c++) {
                    Cell cell = row.getCell(c - 1);
                    if (cell != null) {
                        styler.cellStyleUpdated(cell, false);
                        valueManager.markCellForUpdate(cell);
                    } else {
                        styler.clearCellStyle(c, r);
                    }
                }
            }
        }
        styler.loadCustomBorderStylesToState();
    }

    /**
     * Discards all current merged regions for the sheet and reloads them from
     * POI model.
     * <p>
     * This can be used if you want to add / remove multiple merged regions
     * directly from the POI model and need to update the component.
     * 
     * Note that you must also make sure that possible styles for the merged
     * regions are updated, if those were modified, by calling
     * {@link #reloadActiveSheetStyles()}.
     */
    public void reloadAllMergedRegions() {
        SpreadsheetFactory.loadMergedRegions(this);
    }

    /**
     * Reloads the active sheets styles.
     */
    public void reloadActiveSheetStyles() {
        styler.reloadActiveSheetCellStyles();
    }

    /**
     * Hides or unhides the column, see
     * {@link Sheet#setColumnHidden(int, boolean)}.
     * 
     * @param columnIndex
     *            0-based
     * @param hidden
     */
    public void setColumnHidden(int columnIndex, boolean hidden) {
        getActiveSheet().setColumnHidden(columnIndex, hidden);
        if (hidden && !getState().hiddenColumnIndexes.contains(columnIndex + 1)) {
            getState().hiddenColumnIndexes.add(columnIndex + 1);
            getState().colW[columnIndex] = 0;
        } else if (!hidden
                && getState().hiddenColumnIndexes.contains(columnIndex + 1)) {
            getState().hiddenColumnIndexes
                    .remove(getState().hiddenColumnIndexes
                            .indexOf(columnIndex + 1));
            getState().colW[columnIndex] = ExcelToHtmlUtils
                    .getColumnWidthInPx(getActiveSheet().getColumnWidth(
                            columnIndex));
            getCellValueManager().clearCacheForColumn(columnIndex + 1);
            getCellValueManager().loadCellData(firstRow, lastRow,
                    columnIndex + 1, columnIndex + 1);
        }
        if (sheetImages != null) {
            reloadImageSizesFromPOI = true;
            loadImages();
        }
    }

    /**
     * See {@link Sheet#isColumnHidden(int)}.
     * 
     * @param columnIndex
     *            0-based
     * @return
     */
    public boolean isColumnHidden(int columnIndex) {
        return getActiveSheet().isColumnHidden(columnIndex);
    }

    /**
     * Hides or unhides the row, see {@link Row#setZeroHeight(boolean)}.
     * 
     * @param rowIndex
     *            0-based
     * @param hidden
     */
    public void setRowHidden(int rowIndex, boolean hidden) {
        final Sheet activeSheet = getActiveSheet();
        Row row = activeSheet.getRow(rowIndex);
        if (row == null) {
            row = activeSheet.createRow(rowIndex);
        }
        row.setZeroHeight(hidden);
        if (hidden && !getState().hiddenRowIndexes.contains(rowIndex + 1)) {
            getState().hiddenRowIndexes.add(rowIndex + 1);
            getState().rowH[rowIndex] = 0.0F;
        } else if (!hidden
                && getState().hiddenRowIndexes.contains(rowIndex + 1)) {
            getState().hiddenRowIndexes.remove(getState().hiddenRowIndexes
                    .indexOf(rowIndex + 1));
            getState().rowH[rowIndex] = row.getHeightInPoints();
        }
        if (sheetImages != null) {
            reloadImageSizesFromPOI = true;
            loadImages();
        }
    }

    /**
     * Row is hidden when it has zero height, see {@link Row#getZeroHeight()}.
     * 
     * @param rowIndex
     *            0-based
     * @return
     */
    public boolean isRowHidden(int rowIndex) {
        Row row = getActiveSheet().getRow(rowIndex);
        return row == null ? false : row.getZeroHeight();
    }

    /**
     * Reloads the component from the given Excel file.
     * 
     * @param file
     * @throws InvalidFormatException
     * @throws IOException
     */
    public void reloadSpreadsheetFrom(File file) throws InvalidFormatException,
            IOException {
        SpreadsheetFactory.reloadSpreadsheetComponent(this, file);
    }

    /**
     * Disposes the current {@link Workbook}, if any, and loads a new empty XSLX
     * Workbook.
     */
    public void reloadSpreadsheetWithNewWorkbook() {
        SpreadsheetFactory.loadNewXLSXSpreadsheet(this);
    }

    /**
     * Exports current spreadsheet into a File with the given name.
     * 
     * @param fileName
     *            The full name of the file. If the name doesn't end with '.xls'
     *            or '.xlsx', the approriate one will be appended.
     * @return A File with the content of the current {@link Workbook}, In the
     *         file format of the original {@link Workbook}.
     * @throws FileNotFoundException
     *             If file name was invalid
     * @throws IOException
     *             If file can't be written
     */
    public File writeSpreadsheetIntoFile(String fileName)
            throws FileNotFoundException, IOException {
        return SpreadsheetFactory.write(this, fileName);
    }

    /**
     * The row buffer size determines the amount of content rendered above and
     * below the visible cell area, for smoother scrolling.
     * <p>
     * Size is in pixels, default is 200.
     * 
     * @return the current row buffer size
     */
    public int getRowBufferSize() {
        return getState().rowBufferSize;
    }

    /**
     * Sets the row buffer size. Comes into effect the next time sheet is
     * scrolled or reloaded.
     * <p>
     * The row buffer size determines the amount of content rendered above and
     * below the visible cell area, for smoother scrolling.
     * 
     * @param rowBufferInPixels
     *            the amount of extra room rendered both above and below the
     *            visible area.
     */
    public void setRowBufferSize(int rowBufferInPixels) {
        getState().rowBufferSize = rowBufferInPixels;
    }

    /**
     * The column buffer size determines the amount of content rendered to the
     * left and right of the visible cell area, for smoother scrolling.
     * <p>
     * Size is in pixels, default is 200.
     * 
     * @return the current column buffer size
     */
    public int getColBufferSize() {
        return getState().columnBufferSize;
    }

    /**
     * Sets the column buffer size. Comes into effect the next time sheet is
     * scrolled or reloaded.
     * <p>
     * The column buffer size determines the amount of content rendered to the
     * left and right of the visible cell area, for smoother scrolling.
     * 
     * @param colBufferInPixels
     *            the amount of extra room rendered both to the left and right
     *            of the visible area.
     */
    public void setColBufferSize(int colBufferInPixels) {
        getState().columnBufferSize = colBufferInPixels;
    }

    /**
     * @return the defaultNewSheetRows
     */
    public int getDefaultNewSheetRows() {
        return defaultNewSheetRows;
    }

    /**
     * @param defaultNewSheetRows
     *            the number of rows to give sheets that are created with the
     *            '+' button on the client side.
     */
    public void setDefaultNewSheetRows(int defaultNewSheetRows) {
        this.defaultNewSheetRows = defaultNewSheetRows;
    }

    /**
     * @return the defaultNewSheetColumns
     */
    public int getDefaultNewSheetColumns() {
        return defaultNewSheetColumns;
    }

    /**
     * @param defaultNewSheetColumns
     *            the number of columns to give sheets that are created with the
     *            '+' button on the client side.
     */
    public void setDefaultNewSheetColumns(int defaultNewSheetColumns) {
        this.defaultNewSheetColumns = defaultNewSheetColumns;
    }

    /**
     * Call this to force the spreadsheet to reload the currently viewed cell
     * contents. This forces reload of all: custom components (always visible &
     * editors) from {@link SpreadsheetComponentFactory}, hyperlinks, cells'
     * comments and cells' contents. Updates styles for the visible area.
     */
    public void reloadVisibleCellContents() {
        loadCustomComponents();
        updateRowAndColumnRangeCellData(firstRow, lastRow, firstColumn,
                lastColumn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vaadin.server.AbstractClientConnector#setResource(java.lang.String,
     * com.vaadin.server.Resource)
     * 
     * Provides package visibility.
     */
    @Override
    protected void setResource(String key, Resource resource) {
        super.setResource(key, resource);
    }

    /** clears server side spread sheet content */
    protected void clearSheetServerSide() {
        workbook = null;
        styler = null;

        valueManager.clearCachedContent();
        selectionManager.clear();
        historyManager.clear();

        for (SheetImageWrapper image : sheetImages) {
            setResource(image.resourceKey, null);
        }
        sheetImages.clear();
    }

    protected void setInternalWorkbook(Workbook workbook) {
        this.workbook = workbook;
        valueManager.updateEvaluator();
        styler = createSpreadsheetStyleFactory();

        reloadActiveSheetData();
        if (workbook instanceof HSSFWorkbook) {
            getState().workbookProtected = ((HSSFWorkbook) workbook)
                    .isWriteProtected();
        } else if (workbook instanceof XSSFWorkbook) {
            getState().workbookProtected = ((XSSFWorkbook) workbook)
                    .isStructureLocked();
        }
        // clear all tables from memory
        tables.clear();

        getState().verticalScrollPositions = new int[getState().sheetNames.length];
        getState().horizontalScrollPositions = new int[getState().sheetNames.length];

        conditionalFormatter = createConditionalFormatter();
    }

    /**
     * Override this method to provide your own {@link ConditionalFormatter}
     * implementation. Called each time we open a workbook.
     * 
     * @return A {@link ConditionalFormatter} that links to this spreadsheet.
     */
    protected ConditionalFormatter createConditionalFormatter() {
        return new ConditionalFormatter(this);
    }

    /**
     * Override this method to provide your own {@link SpreadsheetStyleFactory}
     * implementation. Called each time we open a workbook.
     * 
     * @return A {@link SpreadsheetStyleFactory} that links to this spreadsheet.
     */
    protected SpreadsheetStyleFactory createSpreadsheetStyleFactory() {
        return new SpreadsheetStyleFactory(this);
    }

    protected void reloadActiveSheetData() {
        selectionManager.clear();
        valueManager.clearCachedContent();

        firstColumn = lastColumn = firstRow = lastRow = -1;
        for (SheetImageWrapper image : sheetImages) {
            setResource(image.resourceKey, null);
        }
        sheetImages.clear();
        topLeftCellCommentsLoaded = false;
        topLeftCellHyperlinksLoaded = false;

        reload = true;
        getState().sheetIndex = getSpreadsheetSheetIndex(workbook
                .getActiveSheetIndex()) + 1;
        getState().sheetProtected = getActiveSheet().getProtect();
        getState().cellKeysToEditorIdMap = null;
        getState().hyperlinksTooltips = null;
        getState().componentIDtoCellKeysMap = null;
        getState().resourceKeyToImage = null;
        getState().mergedRegions = null;
        getState().cellComments = null;
        getState().visibleCellComments = null;
        if (customComponents != null && !customComponents.isEmpty()) {
            for (Component c : customComponents) {
                unRegisterCustomComponent(c);
            }
            customComponents.clear();
        }
        if (sheetPopupButtons != null && !sheetPopupButtons.isEmpty()) {
            for (PopupButton sf : sheetPopupButtons) {
                unRegisterCustomComponent(sf);
            }
            sheetPopupButtons.clear();
        }
        // clear all tables, possible tables for new/changed sheet are added
        // after first round trip.
        tablesLoaded = false;

        reloadSheetNames();

        getState().displayGridlines = getActiveSheet().isDisplayGridlines();
        getState().displayRowColHeadings = getActiveSheet()
                .isDisplayRowColHeadings();
        markAsDirty();
    }

    /**
     * This method should be always called when the selected cell has changed so
     * proper actions can be triggered for possible custom component inside the
     * cell.
     */
    protected void loadCustomEditorOnSelectedCell() {
        CellReference selectedCellReference = selectionManager
                .getSelectedCellReference();
        if (selectedCellReference != null && customComponentFactory != null) {
            final short col = selectedCellReference.getCol();
            final int row = selectedCellReference.getRow();
            final String key = SpreadsheetUtil.toKey(col + 1, row + 1);
            Map<String, String> cellKeysToEditorIdMap = getState(false).cellKeysToEditorIdMap;
            if (cellKeysToEditorIdMap != null
                    && cellKeysToEditorIdMap.containsKey(key)
                    && customComponents != null) {
                String componentId = getState(false).cellKeysToEditorIdMap
                        .get(key);
                for (Component c : customComponents) {
                    if (c.getConnectorId().equals(componentId)) {
                        customComponentFactory.onCustomEditorDisplayed(
                                getCell(row, col), row, col, this,
                                getActiveSheet(), c);
                        return;
                    }
                }
            }
        }
    }

    private void reloadSheetNames() {
        final ArrayList<String> sheetNamesList = new ArrayList<String>();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!workbook.isSheetVeryHidden(i) && !workbook.isSheetHidden(i)) {
                sheetNamesList.add(workbook.getSheetName(i));
            }
        }
        getState().sheetNames = sheetNamesList
                .toArray(new String[sheetNamesList.size()]);
    }

    /**
     * Returns Apache POI model based index for the visible sheet at the given
     * index.
     * 
     * @param visibleSheetIndex
     *            0-based
     * @return 0-based, the sheet index inside POI, or -1 if something went
     *         wrong
     */
    public int getVisibleSheetPOIIndex(int visibleSheetIndex) {
        int realIndex = -1;
        int i = -1;
        do {
            realIndex++;
            if (!workbook.isSheetVeryHidden(realIndex)
                    && !workbook.isSheetHidden(realIndex)) {
                i++;
            }
        } while (i < visibleSheetIndex
                && realIndex < (workbook.getNumberOfSheets() - 1));
        return realIndex;
    }

    /**
     * 
     * @param pOISheetIndex
     *            0-based
     * @return 0-based visible sheet index
     */
    private int getSpreadsheetSheetIndex(int pOISheetIndex) {
        int ourIndex = -1;
        for (int i = 0; i <= pOISheetIndex; i++) {
            if (!workbook.isSheetVeryHidden(i) && !workbook.isSheetHidden(i)) {
                ourIndex++;
            }
        }
        return ourIndex;
    }

    /**
     * 
     * @return if the current {@link Sheet} is protected or not.
     */
    public boolean isSheetProtected() {
        return getState().sheetProtected;
    }

    /**
     * @param cell
     * @return if the given cell is hidden or not.
     */
    public boolean isCellHidden(Cell cell) {
        return isSheetProtected() && cell.getCellStyle().getHidden();
    }

    /**
     * @param cell
     * @return if the current cell is locked or not.
     */
    public boolean isCellLocked(Cell cell) {
        return isSheetProtected()
                && (cell == null || cell.getCellStyle().getLocked());
    }

    protected SpreadsheetClientRpc getSpreadsheetRpcProxy() {
        return getRpcProxy(SpreadsheetClientRpc.class);
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);
        if (reload) {
            reload = false;
            getState().reload = true;
            if (initialSheetSelection == null) {
                initialSheetSelection = "A1";
            }
        } else {
            getState().reload = false;
        }
    }

    /**
     * 
     * @return the current style factory.
     */
    public SpreadsheetStyleFactory getSpreadsheetStyleFactory() {
        return styler;
    }

    /**
     * Note that modifications done directly with the {@link Workbook}'s API
     * will not get automatically updated into the Spreadsheet component.
     * <p>
     * Use {@link #markCellAsDeleted(Cell, boolean)},
     * {@link #markCellAsUpdated(Cell, boolean)}, or
     * {@link #reloadVisibleCellContents()} to update content.
     * 
     * @return the currently presented workbook
     */
    public Workbook getWorkbook() {
        return workbook;
    }

    /**
     * Reloads the component with the given Workbook.
     * 
     * @param workbook
     */
    public void setWorkbook(Workbook workbook) {
        if (workbook == null) {
            throw new NullPointerException(
                    "Cannot open a null workbook with Spreadsheet component.");
        }
        SpreadsheetFactory.reloadSpreadsheetComponent(this, workbook);
    }

    /**
     * Note that modifications done directly with the {@link Sheet}'s API will
     * not get automatically updated into the Spreadsheet component.
     * <p>
     * Use {@link #markCellAsDeleted(Cell, boolean)},
     * {@link #markCellAsUpdated(Cell, boolean)}, or
     * {@link #reloadVisibleCellContents()} to update content.
     * 
     * @return the currently active sheet in the component.
     */
    public Sheet getActiveSheet() {
        return workbook.getSheetAt(workbook.getActiveSheetIndex());
    }

    /**
     * @return if a {@link Workbook} has been given to the component or not.
     */
    public boolean hasSheetData() {
        return workbook != null;
    }

    /**
     * Updates the given range of cells. Takes frozen panes in to account. NOTE:
     * Does not run style updates!
     */
    protected void updateRowAndColumnRangeCellData(int r1, int r2, int c1,
            int c2) {
        // FIXME should be optimized, should not go through all links, comments
        // etc. always
        loadHyperLinks();
        loadCellComments();
        loadImages();
        loadPopupButtons();
        // custom components not updated here on purpose

        valueManager.loadCellData(r1, r2, c1, c2);
    }

    /**
     * Sends cells' data to client side. Data is only sent once, unless there
     * are changes. Cells with custom components are skipped.
     * 
     * @param firstRow
     *            1-based
     * @param lastRow
     *            1-based
     * @param firstColumn
     *            1-based
     * @param lastColumn
     *            1-based
     */
    private void loadCells(int firstRow, int lastRow, int firstColumn,
            int lastColumn) {
        loadCustomComponents();
        loadHyperLinks();
        loadCellComments();
        loadImages();
        loadTables();
        loadPopupButtons();
        valueManager.loadCellData(firstRow, lastRow, firstColumn, lastColumn);
    }

    protected void onLinkCellClick(int column, int row) {
        Cell cell = getActiveSheet().getRow(row - 1).getCell(column - 1);
        if (hyperlinkCellClickHandler != null) {
            hyperlinkCellClickHandler.onHyperLinkCellClick(cell,
                    cell.getHyperlink(), Spreadsheet.this);
        } else {
            DefaultHyperlinkCellClickHandler.get().onHyperLinkCellClick(cell,
                    cell.getHyperlink(), Spreadsheet.this);
        }
    }

    protected void onRowResized(Map<Integer, Float> newRowSizes, int col1,
            int col2, int row1, int row2) {
        SizeChangeCommand command = new SizeChangeCommand(this, Type.ROW);
        command.captureValues(newRowSizes.keySet().toArray(
                new Integer[newRowSizes.size()]));
        historyManager.addCommand(command);
        for (Entry<Integer, Float> entry : newRowSizes.entrySet()) {
            int index = entry.getKey();
            float height = entry.getValue();
            setRowHeight(index - 1, height);
        }
        if (sheetImages != null) {
            reloadImageSizesFromPOI = true;
        }
        loadCells(row1, row2, col1, col2);
    }

    /**
     * Sets the row height for currently active sheet. Updates POI and visible
     * sheet.
     * 
     * @param index
     *            0-based
     * @param height
     *            in points
     */
    public void setRowHeight(int index, float height) {
        if (height == 0.0F) {
            setRowHidden(index, true);
        } else {
            Row row = getActiveSheet().getRow(index);
            if (getState().hiddenRowIndexes
                    .contains(Integer.valueOf(index + 1))) {
                getState().hiddenRowIndexes.remove(Integer.valueOf(index + 1));
                if (row != null && row.getZeroHeight()) {
                    row.setZeroHeight(false);
                }
            }
            getState().rowH[index] = height;
            if (row == null) {
                row = getActiveSheet().createRow(index);
            }
            row.setHeightInPoints(height);
        }
    }

    protected void onColumnResized(Map<Integer, Integer> newColumnSizes,
            int col1, int col2, int row1, int row2) {
        SizeChangeCommand command = new SizeChangeCommand(this, Type.COLUMN);
        command.captureValues(newColumnSizes.keySet().toArray(
                new Integer[newColumnSizes.size()]));
        historyManager.addCommand(command);
        for (Entry<Integer, Integer> entry : newColumnSizes.entrySet()) {
            int index = entry.getKey();
            int width = entry.getValue();
            setColumnWidth(index - 1, width);
        }
        if (sheetImages != null) {
            reloadImageSizesFromPOI = true;
        }
        loadCells(row1, row2, col1, col2);
    }

    /**
     * Sets the column width in pixels (using conversion) for the currently
     * active sheet. Updates POI and visible sheet.
     * 
     * @param index
     *            0-based
     * @param width
     *            in pixels
     */
    public void setColumnWidth(int index, int width) {
        if (width == 0) {
            setColumnHidden(index, true);
        } else {
            if (getState().hiddenColumnIndexes.contains(Integer
                    .valueOf(index + 1))) {
                getState().hiddenColumnIndexes.remove(Integer
                        .valueOf(index + 1));
            }
            if (getActiveSheet().isColumnHidden(index)) {
                getActiveSheet().setColumnHidden(index, false);
            }
            getState().colW[index] = width;
            getActiveSheet().setColumnWidth(index,
                    SpreadsheetFactory.pixel2WidthUnits(width));
            getCellValueManager().clearCacheForColumn(index + 1);
            getCellValueManager().loadCellData(firstRow, lastRow, index + 1,
                    index + 1);
        }
    }

    private void loadHyperLinks() {
        if (getState(false).hyperlinksTooltips == null) {
            getState(false).hyperlinksTooltips = new HashMap<String, String>();
        } else {
            getState().hyperlinksTooltips.clear();
        }
        if (getVerticalSplitPosition() > 0 && getHorizontalSplitPosition() > 0
                && !topLeftCellHyperlinksLoaded) {
            loadHyperLinks(1, getVerticalSplitPosition(), 1,
                    getHorizontalSplitPosition());
        }
        if (getVerticalSplitPosition() > 0) {
            loadHyperLinks(1, getVerticalSplitPosition(), firstColumn,
                    lastColumn);
        }
        if (getHorizontalSplitPosition() > 0) {
            loadHyperLinks(firstRow, lastRow, 1, getHorizontalSplitPosition());
        }
        loadHyperLinks(firstRow, lastRow, firstColumn, lastColumn);
    }

    private void loadHyperLinks(int r1, int r2, int c1, int c2) {
        for (int r = r1 - 1; r < r2; r++) {
            final Row row = getActiveSheet().getRow(r);
            if (row != null) {
                for (int c = c1 - 1; c < c2; c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null) {
                        try {
                            Hyperlink link = cell.getHyperlink();
                            if (link != null) {
                                if (link instanceof XSSFHyperlink) {
                                    String tooltip = ((XSSFHyperlink) link)
                                            .getTooltip();
                                    // Show address if no defined tooltip (like
                                    // in
                                    // excel)
                                    if (tooltip == null) {
                                        tooltip = link.getAddress();
                                    }
                                    getState().hyperlinksTooltips
                                            .put(SpreadsheetUtil.toKey(c + 1,
                                                    r + 1), tooltip);
                                } else {
                                    getState().hyperlinksTooltips
                                            .put(SpreadsheetUtil.toKey(c + 1,
                                                    r + 1), link.getAddress());
                                }
                            } else {
                                // Check if the cell has HYPERLINK function
                                if (DefaultHyperlinkCellClickHandler
                                        .isHyperlinkFormulaCell(cell)) {
                                    getState().hyperlinksTooltips
                                            .put(SpreadsheetUtil.toKey(c + 1,
                                                    r + 1),
                                                    DefaultHyperlinkCellClickHandler
                                                            .getHyperlinkFunctionCellAddress(cell));
                                }
                            }
                        } catch (XmlValueDisconnectedException exc) {
                            LOGGER.log(Level.FINEST, exc.getMessage(), exc);
                        }
                    }
                }
            }
        }
    }

    /**
     * Triggers image reload from POI model (only if there are images present)
     */
    protected void triggerImageReload() {
        if (sheetImages != null) {
            reloadImageSizesFromPOI = true;
        }
    }

    private void loadImages() {
        if (sheetImages.isEmpty()) {
            getState().resourceKeyToImage = null;
        } else {
            if (getState(false).resourceKeyToImage == null) {
                getState(false).resourceKeyToImage = new HashMap<String, ImageInfo>();
            }
            // reload images from POI because row / column sizes have changed
            // currently doesn't effect anything because POI doesn't update the
            // image anchor data after resizing
            if (reloadImageSizesFromPOI) {
                for (SheetImageWrapper image : sheetImages) {
                    if (image.visible) {
                        getState().resourceKeyToImage.remove(image.resourceKey);
                        setResource(image.resourceKey, null);
                    }
                }
                sheetImages.clear();
                SpreadsheetFactory.loadSheetImages(this);
            }
            for (final SheetImageWrapper image : sheetImages) {
                if (isImageVisible(image)) {
                    if (!getState(false).resourceKeyToImage
                            .containsKey(image.resourceKey)) {
                        ImageInfo imageInfo = new ImageInfo();
                        generateImageInfo(image, imageInfo);
                        getState().resourceKeyToImage.put(image.resourceKey,
                                imageInfo);
                        if (image.resource == null) {
                            StreamSource streamSource = new StreamSource() {

                                @Override
                                public InputStream getStream() {
                                    return new ByteArrayInputStream(image.data);
                                }
                            };
                            StreamResource resource = new StreamResource(
                                    streamSource, image.resourceKey);
                            resource.setMIMEType(image.MIMEType);
                            setResource(image.resourceKey, resource);
                            image.resource = resource;
                        }
                        image.visible = true;
                    } else {
                        generateImageInfo(image,
                                getState(false).resourceKeyToImage
                                        .get(image.resourceKey));
                    }
                } else if (image.visible) {
                    getState().resourceKeyToImage.remove(image.resourceKey);
                    image.visible = false;
                }
            }
        }
        reloadImageSizesFromPOI = false;
    }

    private boolean isImageVisible(SheetImageWrapper image) {
        int horizontalSplitPosition = getHorizontalSplitPosition();
        int verticalSplitPosition = getVerticalSplitPosition();
        return (horizontalSplitPosition > 0 && verticalSplitPosition > 0 && image
                .isVisible(1, horizontalSplitPosition, 1, verticalSplitPosition))
                || (horizontalSplitPosition > 0 && image.isVisible(1,
                        horizontalSplitPosition, firstRow, lastRow))
                || (verticalSplitPosition > 0 && image.isVisible(firstColumn,
                        lastColumn, 1, verticalSplitPosition))
                || image.isVisible(firstColumn, lastColumn, firstRow, lastRow);

    }

    private void generateImageInfo(final SheetImageWrapper image,
            final ImageInfo info) {
        Sheet sheet = getActiveSheet();

        int col = image.anchor.getCol1();
        while (sheet.isColumnHidden(col) && col < (getState(false).cols - 1)) {
            col++;
        }
        int row = image.anchor.getRow1();
        Row r = sheet.getRow(row);
        while (r != null && r.getZeroHeight()) {
            row++;
            r = sheet.getRow(row);
        }

        info.col = col + 1; // 1-based
        info.row = row + 1; // 1-based
        info.height = image.getHeight(sheet, getState(false).rowH);
        info.width = image.getWidth(sheet, getState(false).colW,
                getState(false).defColW);
        info.dx = image.getDx1(sheet);
        info.dy = image.getDy1(sheet);
    }

    private void loadCellComments() {
        if (getState(false).cellComments == null) {
            getState(false).cellComments = new HashMap<String, String>();
        } else {
            getState().cellComments.clear();
        }
        if (getState(false).visibleCellComments == null) {
            getState(false).visibleCellComments = new ArrayList<String>();
        } else {
            getState().visibleCellComments.clear();
        }
        if (getVerticalSplitPosition() > 0 && getHorizontalSplitPosition() > 0
                && !topLeftCellCommentsLoaded) {
            loadCellComments(1, getVerticalSplitPosition(), 1,
                    getHorizontalSplitPosition());
        }
        if (getVerticalSplitPosition() > 0) {
            loadCellComments(1, getVerticalSplitPosition(), firstColumn,
                    lastColumn);
        }
        if (getHorizontalSplitPosition() > 0) {
            loadCellComments(firstRow, lastRow, 1, getHorizontalSplitPosition());
        }
        loadCellComments(firstRow, lastRow, firstColumn, lastColumn);
    }

    private void loadCellComments(int r1, int r2, int c1, int c2) {
        Sheet sheet = getActiveSheet();
        for (int r = r1 - 1; r < r2; r++) {
            Row row = sheet.getRow(r);
            if (row != null && row.getZeroHeight()) {
                continue;
            }
            for (int c = c1 - 1; c < c2; c++) {
                if (sheet.isColumnHidden(c)) {
                    continue;
                }
                MergedRegion region = mergedRegionContainer.getMergedRegion(
                        c + 1, r + 1);
                // do not add comments that are "below" merged regions.
                // client side handles cases where comment "moves" (because
                // shifting etc.) from merged cell into basic or vice versa.
                if (region == null || region.col1 == (c + 1)
                        && region.row1 == (r + 1)) {
                    Comment comment = sheet.getCellComment(r, c);
                    if (comment != null) {
                        // by default comments are shown when mouse is over the
                        // red
                        // triangle on the cell's top right corner. the comment
                        // position is calculated so that it is completely
                        // visible.
                        String key = SpreadsheetUtil.toKey(c + 1, r + 1);
                        getState().cellComments.put(key, comment.getString()
                                .getString());
                        if (comment.isVisible()) {
                            getState().visibleCellComments.add(key);
                        }
                    }
                } else {
                    c = region.col2 - 1;
                }
            }
        }
    }

    /**
     * loads the custom components for the currently viewed cells and clears
     * previous components that are not currently visible.
     */
    private void loadCustomComponents() {
        if (customComponentFactory != null) {
            if (getState().cellKeysToEditorIdMap == null) {
                getState().cellKeysToEditorIdMap = new HashMap<String, String>();
            } else {
                getState().cellKeysToEditorIdMap.clear();
            }
            if (getState().componentIDtoCellKeysMap == null) {
                getState().componentIDtoCellKeysMap = new HashMap<String, String>();
            } else {
                getState().componentIDtoCellKeysMap.clear();
            }
            if (customComponents == null) {
                customComponents = new HashSet<Component>();
            }
            HashSet<Component> newCustomComponents = new HashSet<Component>();
            // iteration indexes 0-based
            for (int r = firstRow - 1; r < lastRow; r++) {
                final Row row = getActiveSheet().getRow(r);
                for (int c = firstColumn - 1; c < lastColumn; c++) {
                    // Cells that are inside a merged region are skipped:
                    MergedRegion region = mergedRegionContainer
                            .getMergedRegion(c + 1, r + 1);
                    if (region == null
                            || (region.col1 == (c + 1) && region.row1 == (r + 1))) {
                        Cell cell = null;
                        if (row != null) {
                            cell = row.getCell(c);
                        }
                        // check if the cell has a custom component
                        Component customComponent = customComponentFactory
                                .getCustomComponentForCell(cell, r, c, this,
                                        getActiveSheet());
                        if (customComponent != null) {
                            final String key = SpreadsheetUtil.toKey(c + 1,
                                    r + 1);
                            if (!customComponents.contains(customComponent)) {
                                registerCustomComponent(customComponent);
                            }
                            getState().componentIDtoCellKeysMap.put(
                                    customComponent.getConnectorId(), key);
                            newCustomComponents.add(customComponent);
                        } else if (!isCellLocked(cell)) {
                            // no custom component and not locked, check if
                            // the cell has a custom editor
                            Component customEditor = customComponentFactory
                                    .getCustomEditorForCell(cell, r, c, this,
                                            getActiveSheet());
                            if (customEditor != null) {
                                final String key = SpreadsheetUtil.toKey(c + 1,
                                        r + 1);
                                if (!newCustomComponents.contains(customEditor)
                                        && !customComponents
                                                .contains(customEditor)) {
                                    registerCustomComponent(customEditor);
                                }
                                getState().cellKeysToEditorIdMap.put(key,
                                        customEditor.getConnectorId());
                                newCustomComponents.add(customEditor);
                            }
                        }
                    }
                    if (region != null) {
                        c = region.col2;
                    }
                }
            }
            // unregister old
            for (Iterator<Component> i = customComponents.iterator(); i
                    .hasNext();) {
                Component c = i.next();
                if (!newCustomComponents.contains(c)) {
                    unRegisterCustomComponent(c);
                    i.remove();
                }
            }
            customComponents = newCustomComponents;
        } else {
            getState().cellKeysToEditorIdMap = null;
            getState().componentIDtoCellKeysMap = null;
            if (customComponents != null && !customComponents.isEmpty()) {
                for (Component c : customComponents) {
                    unRegisterCustomComponent(c);
                }
                customComponents.clear();
            }
        }
    }

    private boolean isCellVisible(int col, int row) {
        int verticalSplitPosition = getVerticalSplitPosition();
        int horizontalSplitPosition = getHorizontalSplitPosition();
        return (col >= firstColumn && col <= lastColumn && row >= firstRow && row <= lastRow)
                || (col >= 1 && col <= horizontalSplitPosition && row >= 1 && row <= verticalSplitPosition)
                || (col >= firstColumn && col <= lastColumn && row >= 1 && row <= verticalSplitPosition)
                || (col >= 1 && col <= horizontalSplitPosition
                        && row >= firstRow && row <= lastRow);
    }

    private void registerCustomComponent(Component component) {
        if (!equals(component.getParent())) {
            component.setParent(this);
        }
    }

    private void unRegisterCustomComponent(Component component) {
        component.setParent(null);
    }

    /**
     * Set a new component factory for the Spreadsheet. If a {@link Workbook}
     * has been set, we reload all components.
     * 
     * @param customComponentFactory
     */
    public void setSpreadsheetComponentFactory(
            SpreadsheetComponentFactory customComponentFactory) {
        this.customComponentFactory = customComponentFactory;
        if (firstRow != -1) {
            loadCustomComponents();
            loadCustomEditorOnSelectedCell();
        } else {
            getState().cellKeysToEditorIdMap = null;
            if (customComponents != null && !customComponents.isEmpty()) {
                for (Component c : customComponents) {
                    unRegisterCustomComponent(c);
                }
                customComponents.clear();
            }
        }
    }

    /**
     * @return the currently used component factory
     */
    public SpreadsheetComponentFactory getSpreadsheetComponentFactory() {
        return customComponentFactory;
    }

    /**
     * Adds a pop-up button to the spreadsheet. The button is added to the cell
     * that is defined in pop-up button settings (
     * {@link PopupButton#getCellReference()}).
     * <p>
     * Note that if the active sheet is changed, all pop-up buttons are removed
     * from the spreadsheet.
     * 
     * @param popupButton
     *            the pop-up button to add
     */
    public void addPopupButton(PopupButton popupButton) {
        if (sheetPopupButtons == null) {
            sheetPopupButtons = new HashSet<PopupButton>();
        }
        if (!sheetPopupButtons.contains(popupButton)) {
            sheetPopupButtons.add(popupButton);
        }
        int column = popupButton.getColumn() + 1;
        int row = popupButton.getRow() + 1;
        if (isCellVisible(column, row)) {
            registerCustomComponent(popupButton);
            markAsDirty();
        }
    }

    /**
     * Removes the pop-up button from the spreadsheet.
     * <p>
     * Note that if the active sheet is changed, all pop-up buttons are removed
     * from the spreadsheet.
     * 
     * @param popupButton
     *            the pop-up button to remove
     */
    public void removePopup(PopupButton popupButton) {
        if (sheetPopupButtons.contains(popupButton)) {
            sheetPopupButtons.remove(popupButton);
            int column = popupButton.getColumn() + 1;
            int row = popupButton.getRow() + 1;
            if (column >= firstColumn && column <= lastColumn
                    && row >= firstRow && row <= lastRow) {
                unRegisterCustomComponent(popupButton);
                markAsDirty();
            }
        }
    }

    /**
     * Registers and unregister pop-up button components for the currently
     * visible cells.
     */
    private void loadPopupButtons() {
        if (sheetPopupButtons != null) {
            for (PopupButton popupButton : sheetPopupButtons) {
                int column = popupButton.getColumn() + 1;
                int row = popupButton.getRow() + 1;
                if (isCellVisible(column, row)) {
                    registerCustomComponent(popupButton);
                } else {
                    unRegisterCustomComponent(popupButton);
                }
            }
        }
    }

    /**
     * Adds a table to "memory", meaning that this table will be reloaded when
     * the active sheet changes to the sheet containing the table.
     * <p>
     * Populating the table "content" (pop-up button & content) is the
     * responsibility of the table, with {@link SpreadsheetTable#reload()}.
     * <p>
     * When the sheet is changed to a different sheet than the one that the
     * table belongs to, the table contents are cleared with
     * {@link SpreadsheetTable#clear()}. If the table is a filtering table, the
     * filters are NOT cleared (can be done with
     * {@link SpreadsheetFilterTable#clearAllFilters()}.
     * <p>
     * The pop-up buttons are always removed by the spreadsheet when the sheet
     * changes.
     * 
     * @param table
     *            the table to add to memory
     */
    public void addTableToMemory(SpreadsheetTable table) {
        tables.add(table);
    }

    /**
     * Removes a table from the "memory", it will no longer get reloaded when
     * the sheet is changed back to the sheet containing the table. Does not
     * delete any table content, use {@link #deleteTable(SpreadsheetTable)} for
     * "complete removal" table.
     * <p>
     * See {@link #addTableToMemory(SpreadsheetTable)}.
     * 
     * @param table
     *            the table to remove from memory
     */
    public void removeTableFromMemory(SpreadsheetTable table) {
        tables.remove(table);
    }

    /**
     * Deletes the table: removes it from "memory" (see
     * {@link #addTableToMemory(SpreadsheetTable)}), clears and removes all
     * possible filters (if table is {@link SpreadsheetFilterTable}), and clears
     * all table pop-up buttons and content.
     * 
     * @param table
     *            to delete
     */
    public void deleteTable(SpreadsheetTable table) {
        removeTableFromMemory(table);
        if (table.isTableSheetCurrentlyActive()) {
            for (PopupButton popupButton : table.getPopupButtons()) {
                removePopup(popupButton);
            }
            if (table instanceof SpreadsheetFilterTable) {
                ((SpreadsheetFilterTable) table).clearAllFilters();
            }
            table.clear();
        }
    }

    /**
     * Gets all the tables that for the spreadsheet. See
     * {@link #addTableToMemory(SpreadsheetTable)}.
     * 
     * @return all tables for the spreadsheet
     */
    public HashSet<SpreadsheetTable> getTables() {
        return tables;
    }

    /**
     * Gets the tables that belong to the currently active sheet (
     * {@link #getActiveSheet()}). See
     * {@link #addTableToMemory(SpreadsheetTable)}.
     * 
     * @return tables for current sheet
     */
    public List<SpreadsheetTable> getTablesForActiveSheet() {
        List<SpreadsheetTable> temp = new ArrayList<SpreadsheetTable>();
        for (SpreadsheetTable table : tables) {
            if (table.getSheet().equals(getActiveSheet())) {
                temp.add(table);
            }
        }
        return temp;
    }

    /** reload tables for current sheet */
    private void loadTables() {
        if (!tablesLoaded) {
            for (SpreadsheetTable table : tables) {
                if (table.getSheet().equals(getActiveSheet())) {
                    table.reload();
                }
            }
            tablesLoaded = true;
        }
    }

    /**
     * Returns the formatted value for the cell, using the {@link DataFormatter}
     * with the current locale. See
     * {@link DataFormatter#formatCellValue(Cell, FormulaEvaluator)}.
     * 
     * @param cell
     * @return formatted value
     */
    public final String getCellValue(Cell cell) {
        return valueManager.getDataFormatter().formatCellValue(cell,
                valueManager.getFormulaEvaluator());
    }

    /**
     * @return if we display grid lines for the active {@link Sheet} or not.
     */
    public boolean isDisplayGridLines() {
        if (getActiveSheet() != null) {
            return getActiveSheet().isDisplayGridlines();
        }
        return true;
    }

    /**
     * Set if we should display grid lines for the active sheet or not.
     * 
     * @param displayGridlines
     */
    public void setDisplayGridlines(boolean displayGridlines) {
        if (getActiveSheet() == null) {
            throw new NullPointerException("no active sheet");
        }
        getActiveSheet().setDisplayGridlines(displayGridlines);
        getState().displayGridlines = displayGridlines;
    }

    /**
     * @return if we display row and column headers for the active sheet
     */
    public boolean isDisplayRowColHeadings() {
        if (getActiveSheet() != null) {
            return getActiveSheet().isDisplayRowColHeadings();
        }
        return true;
    }

    /**
     * Set if we should display row and column headers for the active sheet
     * 
     * @param displayRowColHeadings
     */
    public void setDisplayRowColHeadings(boolean displayRowColHeadings) {
        if (getActiveSheet() == null) {
            throw new NullPointerException("no active sheet");
        }
        getActiveSheet().setDisplayRowColHeadings(displayRowColHeadings);
        getState().displayRowColHeadings = displayRowColHeadings;
    }

    /**
     * Fired when cell selection changes.
     */
    public static class SelectionChangeEvent extends Component.Event {

        private final CellReference selectedCellReference;
        private final CellReference[] individualSelectedCells;
        private final CellRangeAddress selectedCellMergedRegion;
        private final CellRangeAddress[] cellRangeAddresses;

        public SelectionChangeEvent(Component source,
                CellReference selectedCellReference,
                CellReference[] individualSelectedCells,
                CellRangeAddress selectedCellMergedRegion,
                CellRangeAddress... cellRangeAddresses) {
            super(source);
            this.selectedCellReference = selectedCellReference;
            this.individualSelectedCells = individualSelectedCells;
            this.selectedCellMergedRegion = selectedCellMergedRegion;
            this.cellRangeAddresses = cellRangeAddresses;
        }

        public Spreadsheet getSpreadsheet() {
            return (Spreadsheet) getSource();
        }

        /**
         * @return single selected cell, or the last cell selected manually
         *         (e.g. with ctrl+mouseclick)
         */
        public CellReference getSelectedCellReference() {
            return selectedCellReference;
        }

        /**
         * @return all non-contiguously selected cells (e.g. with
         *         ctrl+mouseclick)
         */
        public CellReference[] getIndividualSelectedCells() {
            return individualSelectedCells;
        }

        /**
         * @return the {@link CellRangeAddress} of the merged region the single
         *         selected cell is part of, if any.
         */
        public CellRangeAddress getSelectedCellMergedRegion() {
            return selectedCellMergedRegion;
        }

        /**
         * @return all separately selected cell ranges (e.g. with
         *         ctrl+shift+mouseclick)
         */
        public CellRangeAddress[] getCellRangeAddresses() {
            return cellRangeAddresses;
        }

        /**
         * @return a combination of all selected cells, regardless of selection
         *         mode. Doesn't contain duplicates.
         */
        public CellReference[] getAllSelectedCells() {
            Set<CellReference> cells = new HashSet<CellReference>();
            for (CellReference r : getIndividualSelectedCells()) {
                cells.add(r);
            }
            cells.add(getSelectedCellReference());

            if (getCellRangeAddresses() != null) {
                for (CellRangeAddress a : getCellRangeAddresses()) {

                    for (int x = a.getFirstColumn(); x <= a.getLastColumn(); x++) {
                        for (int y = a.getFirstRow(); y <= a.getLastRow(); y++) {
                            cells.add(new CellReference(y, x));
                        }
                    }
                }
            }

            CellReference[] refs = new CellReference[cells.size()];
            int i = 0;
            for (CellReference r : cells) {
                refs[i++] = r;
            }
            return refs;

        }
    }

    /**
     * Used for knowing when user has changed the cell selection in any way.
     */
    public interface SelectionChangeListener extends Serializable {
        public static final Method SELECTION_CHANGE_METHOD = ReflectTools
                .findMethod(SelectionChangeListener.class, "onSelectionChange",
                        SelectionChangeEvent.class);

        /**
         * Called when user changes cell selection.
         * 
         * @param event
         */
        public void onSelectionChange(SelectionChangeEvent event);
    }

    public void addSelectionChangeListener(SelectionChangeListener listener) {
        addListener(SelectionChangeEvent.class, listener,
                SelectionChangeListener.SELECTION_CHANGE_METHOD);
    }

    public void removeSelectionChangeListener(SelectionChangeListener listener) {
        removeListener(SelectionChangeEvent.class, listener,
                SelectionChangeListener.SELECTION_CHANGE_METHOD);
    }

    public static class ProtectedCellWriteAttemptedEvent extends
            Component.Event {

        public ProtectedCellWriteAttemptedEvent(Component source) {
            super(source);
        }
    }

    /**
     * A listener for when user tries to modify a locked cell.
     */
    public interface ProtectedCellWriteAttemptedListener extends Serializable {
        public static final Method SELECTION_CHANGE_METHOD = ReflectTools
                .findMethod(ProtectedCellWriteAttemptedListener.class,
                        "writeAttempted",
                        ProtectedCellWriteAttemptedEvent.class);

        /**
         * Called when the SpreadSheet detects that the client tried to edit a
         * locked cell (usually by pressing a key). Method is not called for
         * each such event; instead, the SpreadSheet waits a second before
         * sending a new event. This is done to give the user time to react to
         * the results of this call (e.g. showing a notification).
         * 
         * @param event
         */
        public void writeAttempted(ProtectedCellWriteAttemptedEvent event);
    }

    /**
     * Add listener for when user tries to modify a locked cell.
     * 
     * @param listener
     */
    public void addProtectedCellWriteAttemptedListener(
            ProtectedCellWriteAttemptedListener listener) {
        addListener(ProtectedCellWriteAttemptedEvent.class, listener,
                ProtectedCellWriteAttemptedListener.SELECTION_CHANGE_METHOD);
    }

    /**
     * Removes the specified listener.
     * 
     * @param listener
     */
    public void removeProtectedCellWriteAttemptedListener(
            ProtectedCellWriteAttemptedListener listener) {
        removeListener(ProtectedCellWriteAttemptedEvent.class, listener,
                ProtectedCellWriteAttemptedListener.SELECTION_CHANGE_METHOD);
    }

    /**
     * Creates or removes a freeze pane from the currently active sheet.
     * 
     * If both colSplit and rowSplit are zero then the existing freeze pane is
     * removed
     * 
     * @param colSplit
     * @param rowSplit
     */
    public void createFreezePane(int colSplit, int rowSplit) {
        getActiveSheet().createFreezePane(colSplit, rowSplit);
        SpreadsheetFactory.loadFreezePane(this);
    }

    /**
     * Removes the freeze pane from the currently active sheet if one is
     * present.
     */
    public void removeFreezePane() {
        PaneInformation paneInformation = getActiveSheet().getPaneInformation();
        if (paneInformation != null && paneInformation.isFreezePane()) {
            getActiveSheet().createFreezePane(0, 0);
            SpreadsheetFactory.loadFreezePane(this);
        }
    }

    /**
     * 
     * @return the reference to the currently selected single cell.
     *         <p>
     *         <em>NOTE:</em> other cells migh also be selected: use
     *         {@link #addSelectionChangeListener(SelectionChangeListener)} to
     *         get notified for all selection changes.
     */
    public CellReference getSelectedCellReference() {
        return selectionManager.getSelectedCellReference();
    }

    public static class SelectedSheetChangeEvent extends Component.Event {

        private final Sheet newSheet;
        private final Sheet previousSheet;
        private final int newSheetVisibleIndex;
        private final int newSheetPOIIndex;

        public SelectedSheetChangeEvent(Component source, Sheet newSheet,
                Sheet previousSheet, int newSheetVisibleIndex,
                int newSheetPOIIndex) {
            super(source);
            this.newSheet = newSheet;
            this.previousSheet = previousSheet;
            this.newSheetVisibleIndex = newSheetVisibleIndex;
            this.newSheetPOIIndex = newSheetPOIIndex;
        }

        /**
         * @return the newSheet
         */
        public Sheet getNewSheet() {
            return newSheet;
        }

        /**
         * @return the previousSheet
         */
        public Sheet getPreviousSheet() {
            return previousSheet;
        }

        /**
         * @return the newSheetVisibleIndex
         */
        public int getNewSheetVisibleIndex() {
            return newSheetVisibleIndex;
        }

        /**
         * @return the newSheetPOIIndex
         */
        public int getNewSheetPOIIndex() {
            return newSheetPOIIndex;
        }
    }

    /**
     * A listener for when a sheet is selected.
     */
    public interface SelectedSheetChangeListener extends Serializable {
        public static final Method SELECTED_SHEET_CHANGE_METHOD = ReflectTools
                .findMethod(SelectedSheetChangeListener.class,
                        "onSelectedSheetChange", SelectedSheetChangeEvent.class);

        public void onSelectedSheetChange(SelectedSheetChangeEvent event);
    }

    public void addSelectedSheetChangeListener(
            SelectedSheetChangeListener listener) {
        addListener(SelectedSheetChangeEvent.class, listener,
                SelectedSheetChangeListener.SELECTED_SHEET_CHANGE_METHOD);
    }

    public void removeSelectedSheetChangeListener(
            SelectedSheetChangeListener listener) {
        removeListener(SelectedSheetChangeEvent.class, listener,
                SelectedSheetChangeListener.SELECTED_SHEET_CHANGE_METHOD);
    }

    private void fireSelectedSheetChangeEvent(Sheet previousSheet,
            Sheet newSheet) {
        int newSheetPOIIndex = workbook.getActiveSheetIndex();

        fireEvent(new SelectedSheetChangeEvent(this, newSheet, previousSheet,
                getSpreadsheetSheetIndex(newSheetPOIIndex), newSheetPOIIndex));
    }

    @Override
    public Iterator<Component> iterator() {
        if (customComponents == null && sheetPopupButtons == null) {
            List<Component> emptyList = Collections.emptyList();
            return emptyList.iterator();
        } else {
            return new SpreadsheetIterator<Component>(customComponents,
                    sheetPopupButtons);
        }
    }

    public static class SpreadsheetIterator<E extends Component> implements
            Iterator<Component> {
        private final Iterator<Component> customComponentIterator;
        private final Iterator<PopupButton> sheetPopupButtonIterator;
        /** true for customComponentIterator, false for sheetPopupButtonIterator */
        private boolean currentIteratorPointer;

        public SpreadsheetIterator(Set<Component> customComponents,
                Set<PopupButton> sheetPopupButtons) {
            customComponentIterator = customComponents == null ? null
                    : customComponents.iterator();
            sheetPopupButtonIterator = sheetPopupButtons == null ? null
                    : sheetPopupButtons.iterator();
            currentIteratorPointer = true;
        }

        @Override
        public boolean hasNext() {
            return (customComponentIterator != null && customComponentIterator
                    .hasNext())
                    || (sheetPopupButtonIterator != null && sheetPopupButtonIterator
                            .hasNext());
        }

        @Override
        public Component next() {
            if (customComponentIterator != null
                    && customComponentIterator.hasNext()) {
                return customComponentIterator.next();
            }
            if (sheetPopupButtonIterator != null
                    && sheetPopupButtonIterator.hasNext()) {
                currentIteratorPointer = false;
                return sheetPopupButtonIterator.next();
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            if (currentIteratorPointer && customComponentIterator != null) {
                customComponentIterator.remove();
            } else if (sheetPopupButtonIterator != null) {
                sheetPopupButtonIterator.remove();
            }
        }

    }

    protected void onConnectorInit() {
        reloadCellDataOnNextScroll = true;
        valueManager.clearCachedContent();
    }

    /**
     * Reloads all data from the current spreadsheet and performs a full
     * re-render. <br/>
     * Functionally same as calling {@link #setWorkbook(Workbook)} with
     * {@link #getWorkbook()} parameter.
     */
    public void resetSpreadsheetFromData() {
        setWorkbook(getWorkbook());
    }

    /**
     * Sets the content of the info label.
     * 
     * @param value
     *            the new content. Can not be HTML.
     */
    public void setInfoLabelValue(String value) {
        getState().infoLabelValue = value;
    }

    /**
     * @return current content of the info label.
     */
    public String getInfoLabelValue() {
        return getState().infoLabelValue;
    }

    /**
     * Selects the cell at the given coordinates
     * 
     * @param row
     * @param col
     */
    public void setSelection(int row, int col) {

        CellReference ref = new CellReference(row, col);
        selectionManager.handleCellSelection(ref);
    }

    /**
     * Selects the given range, using row1 and col1 and anchor.
     * 
     * @param row1
     * @param row2
     * @param col1
     * @param col2
     */
    public void setSelectionRange(int row1, int row2, int col1, int col2) {

        CellReference ref = new CellReference(row1, col1);

        if (row1 == row2 && col1 == col2) {
            selectionManager.handleCellSelection(ref);
        } else {
            CellRangeAddress cra = new CellRangeAddress(row1, row2, col1, col2);
            selectionManager.handleCellRangeSelection(ref, cra);
        }
    }

    /**
     * Selects the cell(s) at the given coordinates
     * 
     * @param selectionRange
     *            The wanted range, e.g. "A3" or "B3:C5"
     */
    public void setSelection(String selectionRange) {

        CellReference ref = new CellReference(selectionRange);
        selectionManager.handleCellSelection(ref);
    }

    /**
     * @return the {@link ConditionalFormatter} used by this {@link Spreadsheet}
     */
    public ConditionalFormatter getConditionalFormatter() {
        return conditionalFormatter;
    }
}
