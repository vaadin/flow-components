/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntPredicate;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeUtil;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jspecify.annotations.Nullable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.spreadsheet.action.SpreadsheetDefaultActionHandler;
import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.client.MergedRegionUtil.MergedRegionContainer;
import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;
import com.vaadin.flow.component.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.flow.component.spreadsheet.framework.Action;
import com.vaadin.flow.component.spreadsheet.framework.ReflectTools;
import com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc;
import com.vaadin.flow.component.spreadsheet.shared.GroupingData;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.streams.AbstractDownloadHandler;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.shared.Registration;
import com.vaadin.pro.licensechecker.BuildType;
import com.vaadin.pro.licensechecker.Capabilities;
import com.vaadin.pro.licensechecker.Capability;
import com.vaadin.pro.licensechecker.LicenseChecker;

import tools.jackson.databind.JsonNode;

/**
 * Vaadin Spreadsheet is a component which allows displaying and interacting
 * with the contents of an Excel file. The Spreadsheet can be used in any Vaadin
 * application for enabling users to view and manipulate Excel files in their
 * web browsers.
 *
 * @author Vaadin Ltd.
 * @since 23.1
 */
@Tag("vaadin-spreadsheet")
@JsModule("./vaadin-spreadsheet/vaadin-spreadsheet.js")
// Need an explicit reference to Chart class, since the DefaultChartCreator
// class that references it is only accessed through reflection
@Uses(Chart.class)
public class Spreadsheet extends Component
        implements HasSize, HasStyle, Action.Container {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(Spreadsheet.class);

    static {
        VaadinService service = VaadinService.getCurrent();

        Properties properties = new Properties();
        try {
            properties.load(Spreadsheet.class
                    .getResourceAsStream("spreadsheet.properties"));
        } catch (Exception e) {
            LOGGER.warn("Unable to read Spreadsheet properties file", e);
            throw new ExceptionInInitializerError(e);
        }

        String version = properties.getProperty("spreadsheet.version");

        if (service != null) {
            if (!service.getDeploymentConfiguration().isProductionMode()) {
                LicenseChecker.checkLicenseFromStaticBlock(
                        "vaadin-spreadsheet-flow", version,
                        BuildType.DEVELOPMENT,
                        Capabilities.of(Capability.PRE_TRIAL));
            }
        }
    }

    @Override
    public Optional<String> getId() {
        return Optional.ofNullable(getElement().getProperty("id"));
    }

    @Override
    public void setId(String id) {
        getElement().setProperty("id", id);
    }

    // from SaredState

    private Map<String, String> resources = new HashMap<>();

    // spreadsheetState

    private final ViewportBufferConfig viewportBufferConfig = new ViewportBufferConfig(
            this);

    private final MirroredProperty<Integer> rows = MirroredProperty
            .integer(this, "rows", 0);

    private final MirroredProperty<Integer> cols = MirroredProperty
            .integer(this, "cols", 0);

    private final SheetGrouping sheetGrouping = new SheetGrouping(this);

    private final SheetSizes sheetSizes = new SheetSizes(this);

    /** should the sheet be reloaded on client side */
    private final ClientReloadFlag clientReloadFlag = new ClientReloadFlag();

    /** What the client has of each cell; every cell sent goes through it. */
    final ClientCellMirror clientCells = new ClientCellMirror(this);

    /** 1-based */
    private final MirroredProperty<Integer> sheetIndex = MirroredProperty
            .integer(this, "sheetIndex", 1);

    private final MirroredProperty<String[]> sheetNames = MirroredProperty
            .serialized(this, "sheetNames", null);

    /**
     * The CSS of each cell style of the workbook, by POI cell style index, as
     * sent to the client. Set when the styles of the active sheet are loaded.
     */
    protected HashMap<Integer, String> cellStyleToCSSStyle = null;

    private final SheetCssStyleMaps cssStyleMaps = new SheetCssStyleMaps(this);

    private final FreezePaneState freezePaneState = new FreezePaneState(this);

    private final MirroredProperty<Boolean> sheetProtected = MirroredProperty
            .bool(this, "sheetProtected", false);

    private final MirroredProperty<Boolean> showCustomEditorOnFocus = MirroredProperty
            .bool(this, "showCustomEditorOnFocus", false);
    private final MirroredProperty<Boolean> hasComponentFactory = MirroredProperty
            .bool(this, "hasComponentFactory", false);

    /**
     * The editor shown in each cell, by cell key. A cell keeps its editor
     * across the re-renders that keep the cell in view (scroll, selection,
     * resize), so what the user typed into it survives them. Cleared whenever
     * the editors are recreated from the factory, see
     * {@link #setCellKeyToEditorMap(Map)}.
     */
    private Map<String, Component> cellKeyToEditor = new HashMap<>();

    private final MirroredNullableMap<String, String> componentIDtoCellKeysMap = new MirroredNullableMap<>(
            this, "componentIDtoCellKeysMap");

    // Cell CSS key to link tooltip (usually same as address)
    private final MirroredProperty<HashMap<String, String>> hyperlinksTooltips = MirroredProperty
            .serialized(this, "hyperlinksTooltips", null);

    private final MirroredProperty<HashMap<String, String>> cellComments = MirroredProperty
            .serialized(this, "cellComments", null);
    private final MirroredProperty<HashMap<String, String>> cellCommentAuthors = MirroredProperty
            .serialized(this, "cellCommentAuthors", null);

    private final MirroredProperty<ArrayList<String>> visibleCellComments = MirroredProperty
            .serialized(this, "visibleCellComments", null);

    private final MirroredProperty<Set<String>> invalidFormulaCells = MirroredProperty
            .serialized(this, "invalidFormulaCells", null);

    private final MirroredProperty<HashMap<String, OverlayInfo>> overlays = MirroredProperty
            .serialized(this, "overlays", null);

    private final MirroredProperty<ArrayList<MergedRegion>> mergedRegions = MirroredProperty
            .serialized(this, "mergedRegions", null);

    private final MirroredProperty<String> infoLabelValue = MirroredProperty
            .string(this, "infoLabelValue", null);

    private final MirroredProperty<Boolean> workbookChangeToggle = MirroredProperty
            .bool(this, "workbookChangeToggle", false);

    private Locale locale;

    private Registration spreadsheetHandlerRegistration;

    private boolean insideCustomEditorCallback;

    int getCols() {
        return cols.get();
    }

    private List<GroupingData> getColGroupingData() {
        return sheetGrouping.getColGroupingData();
    }

    int getColGroupingMax() {
        return sheetGrouping.getColGroupingMax();
    }

    int getRowGroupingMax() {
        return sheetGrouping.getRowGroupingMax();
    }

    boolean isColGroupingInversed() {
        return sheetGrouping.isColGroupingInversed();
    }

    boolean isRowGroupingInversed() {
        return sheetGrouping.isRowGroupingInversed();
    }

    float getDefRowH() {
        return sheetSizes.defaultRowHeight();
    }

    int getDefColW() {
        return sheetSizes.defaultColumnWidth();
    }

    float[] getRowH() {
        return sheetSizes.rowHeights();
    }

    int[] getColW() {
        return sheetSizes.columnWidths();
    }

    int getSheetIndex() {
        return sheetIndex.get();
    }

    String[] getSheetNames() {
        return sheetNames.get();
    }

    HashMap<Integer, String> getCellStyleToCSSStyle() {
        return cellStyleToCSSStyle;
    }

    HashMap<Integer, Integer> getRowIndexToStyleIndex() {
        return cssStyleMaps.getRowIndexToStyleIndex();
    }

    HashMap<Integer, Integer> getColumnIndexToStyleIndex() {
        return cssStyleMaps.getColumnIndexToStyleIndex();
    }

    Set<Integer> getLockedColumnIndexes() {
        return cssStyleMaps.getLockedColumnIndexes();
    }

    Set<Integer> getLockedRowIndexes() {
        return cssStyleMaps.getLockedRowIndexes();
    }

    /**
     * The rules of the borders the cells draw on other cells than their own, as
     * the client is given them before the next response.
     */
    ArrayList<String> getShiftedCellBorderStyles() {
        return workbookScope.styler.borderRules();
    }

    HashMap<Integer, String> getConditionalFormattingStyles() {
        return cssStyleMaps.getConditionalFormattingStyles();
    }

    /** The columns hidden, counted from 1, as the client counts them. */
    ArrayList<Integer> getHiddenColumnIndexes() {
        return sheetSizes.hiddenColumnsFromOne();
    }

    /** The rows hidden, counted from 1, as the client counts them. */
    ArrayList<Integer> getHiddenRowIndexes() {
        return sheetSizes.hiddenRowsFromOne();
    }

    int[] getVerticalScrollPositions() {
        return freezePaneState.getVerticalScrollPositions();
    }

    int[] getHorizontalScrollPositions() {
        return freezePaneState.getHorizontalScrollPositions();
    }

    private boolean isSheetProtected() {
        return sheetProtected.get();
    }

    Map<String, Component> getCellKeyToEditorMap() {
        return cellKeyToEditor;
    }

    HashMap<String, String> getComponentIDtoCellKeysMap() {
        return componentIDtoCellKeysMap.get();
    }

    HashMap<String, String> getHyperlinksTooltips() {
        return hyperlinksTooltips.get();
    }

    HashMap<String, String> getCellComments() {
        return cellComments.get();
    }

    HashMap<String, String> getCellCommentAuthors() {
        return cellCommentAuthors.get();
    }

    ArrayList<String> getVisibleCellComments() {
        return visibleCellComments.get();
    }

    Set<String> getInvalidFormulaCells() {
        return invalidFormulaCells.get();
    }

    HashMap<String, OverlayInfo> getOverlays() {
        return overlays.get();
    }

    Map<String, String> getResources() {
        return resources;
    }

    ArrayList<MergedRegion> getMergedRegions() {
        return mergedRegions.get();
    }

    private int getVerticalSplitPosition() {
        return freezePaneState.getVerticalSplitPosition();
    }

    private int getHorizontalSplitPosition() {
        return freezePaneState.getHorizontalSplitPosition();
    }

    private String getInfoLabelValue() {
        return infoLabelValue.get();
    }

    private boolean isWorkbookChangeToggle() {
        return workbookChangeToggle.get();
    }

    void setRows(int rows) {
        this.rows.set(rows);
    }

    void setCols(int cols) {
        this.cols.set(cols);
    }

    void setColGroupingData(List<GroupingData> colGroupingData) {
        sheetGrouping.setColGroupingData(colGroupingData);
    }

    void setRowGroupingData(List<GroupingData> rowGroupingData) {
        sheetGrouping.setRowGroupingData(rowGroupingData);
    }

    void setColGroupingMax(int colGroupingMax) {
        sheetGrouping.setColGroupingMax(colGroupingMax);
    }

    void setRowGroupingMax(int rowGroupingMax) {
        sheetGrouping.setRowGroupingMax(rowGroupingMax);
    }

    void setColGroupingInversed(boolean colGroupingInversed) {
        sheetGrouping.setColGroupingInversed(colGroupingInversed);
    }

    void setRowGroupingInversed(boolean rowGroupingInversed) {
        sheetGrouping.setRowGroupingInversed(rowGroupingInversed);
    }

    void setRowH(float[] rowH) {
        sheetSizes.setRowHeights(rowH);
    }

    void setSheetIndex(int sheetIndex) {
        this.sheetIndex.set(sheetIndex);
    }

    void setSheetNames(String[] sheetNames) {
        this.sheetNames.set(sheetNames);
    }

    void setCellStyleToCSSStyle(HashMap<Integer, String> cellStyleToCSSStyle) {
        this.cellStyleToCSSStyle = cellStyleToCSSStyle;
        getElement().setProperty("cellStyleToCSSStyle",
                Serializer.serialize(cellStyleToCSSStyle));
    }

    void setRowIndexToStyleIndex(
            HashMap<Integer, Integer> rowIndexToStyleIndex) {
        cssStyleMaps.setRowIndexToStyleIndex(rowIndexToStyleIndex);
    }

    void setColumnIndexToStyleIndex(
            HashMap<Integer, Integer> columnIndexToStyleIndex) {
        cssStyleMaps.setColumnIndexToStyleIndex(columnIndexToStyleIndex);
    }

    void setLockedColumnIndexes(Set<Integer> lockedColumnIndexes) {
        cssStyleMaps.setLockedColumnIndexes(lockedColumnIndexes);
    }

    void setLockedRowIndexes(Set<Integer> lockedRowIndexes) {
        cssStyleMaps.setLockedRowIndexes(lockedRowIndexes);
    }

    void setShiftedCellBorderStyles(ArrayList<String> shiftedCellBorderStyles) {
        cssStyleMaps.setShiftedCellBorderStyles(shiftedCellBorderStyles);
    }

    void setConditionalFormattingStyles(
            HashMap<Integer, String> conditionalFormattingStyles) {
        cssStyleMaps
                .setConditionalFormattingStyles(conditionalFormattingStyles);
    }

    void setVerticalScrollPositions(int[] verticalScrollPositions) {
        freezePaneState.setVerticalScrollPositions(verticalScrollPositions);
    }

    void setHorizontalScrollPositions(int[] horizontalScrollPositions) {
        freezePaneState.setHorizontalScrollPositions(horizontalScrollPositions);
    }

    void setSheetProtected(boolean sheetProtected) {
        this.sheetProtected.set(sheetProtected);
    }

    private void setWorkbookProtected(boolean workbookProtected) {
        getElement().setProperty("workbookProtected", workbookProtected);
    }

    /**
     * Sets the editor of each cell, by cell key, and tells the client the node
     * id of each one. The ids are derived from the editors on every call rather
     * than kept: a node id is -1 until the node is attached, and changes when
     * it is attached again.
     * <p>
     * {@code null} drops every editor and the callback tracker, and clears the
     * map on the client.
     */
    void setCellKeyToEditorMap(Map<String, Component> cellKeyToEditor) {
        HashMap<String, String> editorIds = null;
        if (cellKeyToEditor == null) {
            // Every editor is gone — a sheet switch, the factory being
            // removed, a forced refresh. The next one to be selected is
            // newly displayed however it compares to the last.
            this.cellKeyToEditor = new HashMap<>();
            resetEditorCallbackTracker();
        } else {
            this.cellKeyToEditor = cellKeyToEditor;
            editorIds = new HashMap<>();
            for (Map.Entry<String, Component> entry : cellKeyToEditor
                    .entrySet()) {
                editorIds.put(entry.getKey(),
                        getComponentNodeId(entry.getValue()));
            }
        }
        getElement().setProperty("cellKeysToEditorIdMap",
                Serializer.serialize(editorIds));
    }

    /**
     * Cell key and editor that {@code onCustomEditorDisplayed} was last invoked
     * for, so the callback fires on a real selection change and not on the
     * re-renders that keep the same selection — scrolling, resizing, extending
     * a range. Re-firing would re-run the application's setValue and wipe
     * whatever the user had typed into the editor.
     */
    private String lastEditorCallbackKey;

    private Component lastEditorCallbackComponent;

    private void resetEditorCallbackTracker() {
        lastEditorCallbackKey = null;
        lastEditorCallbackComponent = null;
    }

    void setComponentIDtoCellKeysMap(
            HashMap<String, String> componentIDtoCellKeysMap) {
        this.componentIDtoCellKeysMap.set(componentIDtoCellKeysMap);
    }

    void setHyperlinksTooltips(HashMap<String, String> hyperlinksTooltips) {
        this.hyperlinksTooltips.set(hyperlinksTooltips);
    }

    void setCellComments(HashMap<String, String> cellComments) {
        this.cellComments.set(cellComments);
    }

    void setCellCommentAuthors(HashMap<String, String> cellCommentAuthors) {
        this.cellCommentAuthors.set(cellCommentAuthors);
    }

    void setVisibleCellComments(ArrayList<String> visibleCellComments) {
        this.visibleCellComments.set(visibleCellComments);
    }

    void setInvalidFormulaCells(Set<String> invalidFormulaCells) {
        this.invalidFormulaCells.set(invalidFormulaCells);
    }

    private void setHasActions(boolean hasActions) {
        getElement().setProperty("hasActions", hasActions);
    }

    void setOverlays(HashMap<String, OverlayInfo> overlays) {
        this.overlays.set(overlays);
    }

    void setMergedRegions(ArrayList<MergedRegion> mergedRegions) {
        this.mergedRegions.set(mergedRegions);
    }

    private void setDisplayGridlines(boolean displayGridlines) {
        getElement().setProperty("displayGridlines", displayGridlines);
    }

    private void setDisplayRowColHeadings(boolean displayRowColHeadings) {
        getElement().setProperty("displayRowColHeadings",
                displayRowColHeadings);
    }

    void setVerticalSplitPosition(int verticalSplitPosition) {
        freezePaneState.setVerticalSplitPosition(verticalSplitPosition);
    }

    void setHorizontalSplitPosition(int horizontalSplitPosition) {
        freezePaneState.setHorizontalSplitPosition(horizontalSplitPosition);
    }

    private void setInfoLabelValue(String infoLabelValue) {
        this.infoLabelValue.set(infoLabelValue);
    }

    private void setWorkbookChangeToggle(boolean workbookChangeToggle) {
        this.workbookChangeToggle.set(workbookChangeToggle);
    }

    void setLockFormatColumns(boolean lockFormatColumns) {
        getElement().setProperty("lockFormatColumns", lockFormatColumns);
    }

    void setLockFormatRows(boolean lockFormatRows) {
        getElement().setProperty("lockFormatRows", lockFormatRows);
    }

    void setNamedRanges(List<String> namedRanges) {
        getElement().setProperty("namedRanges",
                Serializer.serialize(namedRanges));
    }

    void onPopupButtonClick(int row, int column) {
        PopupButton popup = popupButtons()
                .get(SpreadsheetUtil.relativeToAbsolute(this,
                        new CellReference(getActiveSheet().getSheetName(),
                                row - 1, column - 1, false, false)));
        if (popup != null) {
            popup.openPopup();
        }
    }

    void onPopupClose(int row, int column) {
        PopupButton popup = popupButtons()
                .get(SpreadsheetUtil.relativeToAbsolute(this,
                        new CellReference(getActiveSheet().getSheetName(),
                                row - 1, column - 1, false, false)));

        if (popup != null) {
            popup.closePopup();
        }
    }

    /*
     * CLIENT RPC
     */

    // LOOK FOR THIS INSIDE CLIENTRPC VARIABLE

    /*
     * SERVER RPC
     */

    @DomEvent("spreadsheet-event")
    public static class SpreadsheetEvent extends ComponentEvent<Spreadsheet> {

        private final String type;
        private final JsonNode data;

        public SpreadsheetEvent(Spreadsheet source, boolean fromClient,
                @EventData("event.detail.type") String type,
                @EventData("event.detail.data") JsonNode data) {
            super(source, fromClient);
            this.type = type;
            this.data = data;
        }

        public String getType() {
            return type;
        }

        public JsonNode getData() {
            return data;
        }
    }

    /*
     * END OF FLOW RELATED STUFF
     */

    /**
     * This is a style which hides the top (address and formula) bar.
     */
    public static final String HIDE_FUNCTION_BAR_STYLE = "hidefunctionbar";

    /**
     * This is a style which hides the bottom (sheet selection) bar.
     */
    public static final String HIDE_TABSHEET_STYLE = "hidetabsheet";

    /**
     * A common formula evaluator for this Spreadsheet
     */

    /**
     * A common conditional formatting formula evaluator for this Spreadsheet
     * needed for proper value string conversions
     */

    private SpreadsheetClientRpc clientRpc = new SpreadsheetClientRpc() {
        @Override
        public void updateBottomRightCellValues(ArrayList<CellData> cellData) {
            clientCells.send(cellData);
        }

        @Override
        public void updateTopLeftCellValues(ArrayList<CellData> cellData) {
            clientCells.send(cellData);
        }

        @Override
        public void updateTopRightCellValues(ArrayList<CellData> cellData) {
            clientCells.send(cellData);
        }

        @Override
        public void updateBottomLeftCellValues(ArrayList<CellData> cellData) {
            clientCells.send(cellData);
        }

        @Override
        public void updateFormulaBar(String possibleName, int col, int row) {
            getElement().executeJs(
                    "queueMicrotask(() => this.updateFormulaBar($0, $1, $2));",
                    possibleName, col, row);
        }

        @Override
        public void invalidCellAddress() {
            getElement().callJsFunction("invalidCellAddress");
        }

        @Override
        public void showSelectedCell(String name, int col, int row,
                String cellValue, boolean function, boolean locked,
                boolean initialSelection) {
            selectionManager.onCellSelected(row, col, initialSelection);
            getElement().callJsFunction("showSelectedCell", name, col, row,
                    cellValue, function, locked, initialSelection,
                    selectionManager.isKeepingView());
        }

        @Override
        public void showActions(
                ArrayList<SpreadsheetActionDetails> actionDetails) {
            getElement().callJsFunction("showActions",
                    Serializer.serialize(actionDetails));
        }

        @Override
        public void setSelectedCellAndRange(String name, int col, int row,
                int c1, int c2, int r1, int r2, boolean scroll) {
            getElement().callJsFunction("setSelectedCellAndRange", name, col,
                    row, c1, c2, r1, r2,
                    scroll && !selectionManager.isKeepingView());
        }

        @Override
        public void cellsUpdated(ArrayList<CellData> cellData) {
            clientCells.send(cellData);
        }

        @Override
        public void refreshCellStyles() {
            clientCells.refreshStyles();
        }

        @Override
        public void editCellComment(int col, int row) {
            getElement().callJsFunction("editCellComment", col, row);
        }
    };

    /**
     * An interface for handling the edited cell value from user input.
     */
    public interface CellValueHandler extends Serializable {

        /**
         * Called if a cell value has been edited by the user by using the
         * default cell editor. Use
         * {@link Spreadsheet#setCellValueHandler(CellValueHandler)} to enable
         * it for the spreadsheet.
         *
         * @param cell
         *            The cell that has been edited, may be <code>null</code> if
         *            the cell doesn't yet exists
         * @param sheet
         *            The sheet the cell belongs to, the currently active sheet
         * @param colIndex
         *            Cell column index, 0-based
         * @param rowIndex
         *            Cell row index, 0-based
         * @param newValue
         *            The value user has entered
         * @param formulaEvaluator
         *            The {@link FormulaEvaluator} for this sheet
         * @param formatter
         *            The {@link DataFormatter} for this workbook
         * @param conditionalFormattingEvaluator
         *            The {@link ConditionalFormattingEvaluator} for this
         *            workbook
         * @return <code>true</code> if component default parsing should still
         *         be done, <code>false</code> if not
         */
        public boolean cellValueUpdated(Cell cell, Sheet sheet, int colIndex,
                int rowIndex, String newValue,
                FormulaEvaluator formulaEvaluator, DataFormatter formatter,
                ConditionalFormattingEvaluator conditionalFormattingEvaluator);
    }

    /**
     * An interface for handling cell deletion from user input.
     */
    public interface CellDeletionHandler extends Serializable {

        /**
         * Called if a cell value has been deleted by the user. Use
         * {@link Spreadsheet#setCellDeletionHandler(CellDeletionHandler)} to
         * enable it for the spreadsheet.
         *
         * @param cell
         *            The cell that has been deleted
         * @param sheet
         *            The sheet the cell belongs to, the currently active sheet
         * @param colIndex
         *            Cell column index, 0-based
         * @param rowIndex
         *            Cell row index, 0-based
         * @param formulaEvaluator
         *            The {@link FormulaEvaluator} for this sheet
         * @param formatter
         *            The {@link DataFormatter} for this workbook
         * @param conditionalFormattingEvaluator
         *            The {@link ConditionalFormattingEvaluator} for this
         *            workbook
         * @return <code>true</code> if component default deletion should still
         *         be done, <code>false</code> if not
         */
        public boolean cellDeleted(Cell cell, Sheet sheet, int colIndex,
                int rowIndex, FormulaEvaluator formulaEvaluator,
                DataFormatter formatter,
                ConditionalFormattingEvaluator conditionalFormattingEvaluator);

        /**
         * Called if individually selected cell values have been deleted by the
         * user. Use
         * {@link Spreadsheet#setCellDeletionHandler(CellDeletionHandler)} to
         * enable it for the spreadsheet.
         *
         * @param individualSelectedCells
         *            The cells that have been deleted
         * @param sheet
         *            The sheet the cells belong to, the currently active sheet
         * @param formulaEvaluator
         *            The {@link FormulaEvaluator} for this sheet
         * @param formatter
         *            The {@link DataFormatter} for this workbook
         * @param conditionalFormattingEvaluator
         *            The {@link ConditionalFormattingEvaluator} for this
         *            workbook
         * @return <code>true</code> if component default deletion should still
         *         be done, <code>false</code> if not
         */
        public boolean individualSelectedCellsDeleted(
                List<CellReference> individualSelectedCells, Sheet sheet,
                FormulaEvaluator formulaEvaluator, DataFormatter formatter,
                ConditionalFormattingEvaluator conditionalFormattingEvaluator);

        /**
         * Called if a cell range has been deleted by the user. Use
         * {@link Spreadsheet#setCellDeletionHandler(CellDeletionHandler)} to
         * enable it for the spreadsheet.
         *
         * @param cellRangeAddresses
         *            The range of cells that has been deleted
         * @param sheet
         *            The sheet the cells belongs to, the currently active sheet
         * @param formulaEvaluator
         *            The {@link FormulaEvaluator} for this sheet
         * @param formatter
         *            The {@link DataFormatter} for this workbook
         * @param conditionalFormattingEvaluator
         *            The {@link ConditionalFormattingEvaluator} for this
         *            workbook
         * @return <code>true</code> if component default deletion should still
         *         be done, <code>false</code> if not
         */
        public boolean cellRangeDeleted(
                List<CellRangeAddress> cellRangeAddresses, Sheet sheet,
                FormulaEvaluator formulaEvaluator, DataFormatter formatter,
                ConditionalFormattingEvaluator conditionalFormattingEvaluator);
    }

    /**
     * An interface for handling clicks on cells that contain a hyperlink.
     * <p>
     * Implement this interface and set it with
     * {@link Spreadsheet#setHyperlinkCellClickHandler(HyperlinkCellClickHandler)}
     * to customize the default behavior.
     */
    public interface HyperlinkCellClickHandler extends Serializable {

        /**
         * Called when a hyperlink cell has been clicked.
         *
         * Assumes the implementation knows which spreadsheet is in use if
         * needed, and how to navigate or perform some other action.
         *
         * @param cell
         *            The cell that contains the hyperlink
         * @param hyperlink
         *            The actual hyperlink
         */
        public void onHyperLinkCellClick(Cell cell, Hyperlink hyperlink);

        /**
         * @return link target for use as a tooltip
         */
        public String getHyperlinkFunctionTarget(Cell cell);
    }

    private HyperlinkCellClickHandler hyperlinkCellClickHandler;

    private final CellSelectionManager selectionManager = new CellSelectionManager(
            this);
    private final CellSelectionShifter cellShifter = new CellSelectionShifter(
            this);
    private final ContextMenuManager contextMenuManager = new ContextMenuManager(
            this);
    private final SpreadsheetHistoryManager historyManager = new SpreadsheetHistoryManager(
            this);

    /**
     * caches data, so it needs to be stable for the life of a given workbook
     */
    private CellValueManager valueManager;

    private final CellRangeNormalizer cellRangeNormalizer = new CellRangeNormalizer(
            this);

    private final CellMarkUpdater cellMarkUpdater = new CellMarkUpdater(this);

    private boolean chartsEnabled = true;

    /**
     * This is used for making sure the cells are sent to client side in when
     * the next cell data request comes. This is triggered when the client side
     * connector init() method is run.
     */
    private boolean reloadCellDataOnNextScroll;

    int defaultNewSheetRows = SpreadsheetFactory.DEFAULT_ROWS;
    int defaultNewSheetColumns = SpreadsheetFactory.DEFAULT_COLUMNS;

    private SpreadsheetDefaultActionHandler defaultActionHandler;

    protected int mergedRegionCounter;

    Workbook workbook;

    /** What lives as long as the workbook shown, made anew for each. */
    private WorkbookScope workbookScope;

    /** What lives as long as the sheet shown, made anew for each. */
    private SheetScope sheetScope = new SheetScope(this);

    private SpreadsheetComponentFactory componentFactory;

    private String defaultPercentageFormat = "0.00%";

    protected String initialSheetSelection = null;

    /**
     * Whether {@link #initialSheetSelection} is the selection the sheet was
     * left with, which it is shown with again without moving the view: the
     * client puts the sheet back where it was scrolled. A selection that a
     * hyperlink, a named range or an undo leads to is brought into view.
     */
    boolean initialSheetSelectionKeepsView;

    final CellCommentLoader cellCommentLoader = new CellCommentLoader(this);

    private final HyperlinkLoader hyperlinkLoader = new HyperlinkLoader(this);

    private final SheetOperations sheetOperations = new SheetOperations(this);

    private final RowShiftOrchestrator rowShiftOrchestrator = new RowShiftOrchestrator(
            this);

    /**
     * Container for merged regions for the currently active sheet. It always
     * reflects the current merged regions of the sheet.
     */
    protected final MergedRegionContainer mergedRegionContainer = new MergedRegionLookup(
            this::getMergedRegions);

    final MergedRegionOperations mergedRegionOperations = new MergedRegionOperations(
            this);

    /**
     * Minimum row height for rows containing components (in points).
     */
    private int minimumRowHeightForComponents = 30;

    /**
     * Creates a new Spreadsheet component using the newer Excel version format
     * {@link XSSFWorkbook}. Also creates one sheet using the default row
     * {@link SpreadsheetFactory#DEFAULT_ROWS} and column
     * {@link SpreadsheetFactory#DEFAULT_COLUMNS} counts.
     */
    public Spreadsheet() {
        this(SpreadsheetFactory.DEFAULT_ROWS,
                SpreadsheetFactory.DEFAULT_COLUMNS);
    }

    /**
     * Creates a new Spreadsheet component using the newer Excel version format
     * {@link XSSFWorkbook}. Also creates one sheet using the given row and
     * column counts. These counts will also be set as default for any new
     * sheets created later.
     *
     * @param defaultRowCount
     *            Default row count for new sheets
     * @param defaultColumnCount
     *            Default column count for new sheets
     */
    public Spreadsheet(int defaultRowCount, int defaultColumnCount) {
        // getUI().ifPresent(ui ->
        // ui.getPage().addDynamicImport("spreadsheet-lit-element/vaadin-spreadsheet.js"));
        init();
        setDefaultRowCount(defaultRowCount);
        setDefaultColumnCount(defaultColumnCount);
        SpreadsheetFactory.loadSpreadsheetWith(this, null, getDefaultRowCount(),
                getDefaultColumnCount());
    }

    /**
     * Creates a new Spreadsheet component and loads the given Workbook.
     *
     * @param workbook
     *            Workbook to load
     */
    public Spreadsheet(Workbook workbook) {
        init();
        SpreadsheetFactory.loadSpreadsheetWith(this, workbook,
                getDefaultRowCount(), getDefaultColumnCount());
    }

    /**
     * Creates a new Spreadsheet component and loads the given Excel file.
     *
     * @param file
     *            Excel file
     * @throws IOException
     *             If file has invalid format or there is no access to the file
     */
    public Spreadsheet(File file) throws IOException {
        init();
        SpreadsheetFactory.reloadSpreadsheetComponent(this, file);
    }

    /**
     * Creates a new Spreadsheet component based on the given input stream. The
     * expected format is that of an Excel file.
     *
     * @param inputStream
     *            Stream that provides Excel-formatted data.
     * @throws IOException
     *             If there is an error handling the stream, or if the data is
     *             in an invalid format.
     */
    public Spreadsheet(InputStream inputStream) throws IOException {
        init();
        SpreadsheetFactory.reloadSpreadsheetComponent(this, inputStream);
    }

    private void init() {
        updateAppId();
        valueManager = createCellValueManager();
        setSpreadsheetHandler(createDefaultHandler());
        defaultActionHandler = new SpreadsheetDefaultActionHandler();
        hyperlinkCellClickHandler = new DefaultHyperlinkCellClickHandler(this);
        addActionHandler(defaultActionHandler);
        setId(UUID.randomUUID().toString());
        customInit();
    }

    private void updateAppId() {
        Optional.ofNullable(UI.getCurrent()).ifPresent(ui -> {
            getElement().setProperty("appId", ui.getInternals().getAppId());
        });
    }

    /**
     * Register a Spreadsheet handler to listen to Spreadsheet Events. Removes
     * the default / previously set spreadsheet handler.
     *
     * @param spreadsheetHandler
     *            the spreadsheet handler
     * @since 24.9.2
     */
    public void setSpreadsheetHandler(
            SpreadsheetHandlerImpl spreadsheetHandler) {
        if (this.spreadsheetHandlerRegistration != null) {
            spreadsheetHandlerRegistration.remove();
        }
        spreadsheetHandlerRegistration = addListener(SpreadsheetEvent.class,
                new SpreadsheetEventListener(spreadsheetHandler));
    }

    /**
     * Create the default Spreadsheet handler.
     *
     * @return SpreadsheetHandlerImpl
     * @since 24.9.2
     */
    protected SpreadsheetHandlerImpl createDefaultHandler() {
        return new SpreadsheetHandlerImpl(this);
    }

    /**
     * Override if there are desired changes or temporary bug fixes, but be
     * careful - this class should cache values for performance.
     *
     * @return CellValueManager
     */
    protected CellValueManager createCellValueManager() {
        return new CellValueManager(this);
    }

    /**
     * Implement this to perform custom initialization in subclasses. Called
     * before loading any workbook, at the end of the required init() actions.
     */
    protected void customInit() {
        // do nothing by default
    }

    /**
     * Adds an action handler to the spreadsheet that handles the event produced
     * by the context menu (right click) on cells and row and column headers.
     * The action handler is component, not workbook, specific.
     * <p>
     * The parameters on the
     * {@link Action.Handler#handleAction(Action, Object, Object)} and
     * {@link Action.Handler#getActions(Object, Object)} depend on the actual
     * target of the right click.
     * <p>
     * The second parameter (sender) on
     * {@link Action.Handler#getActions(Object, Object)} is always the
     * spreadsheet component. In case of a cell, the first parameter (target) on
     * contains the latest {@link SelectionChangeEvent} for the spreadsheet. In
     * case of a row or a column header, the first parameter (target) is a
     * {@link CellRangeAddress}. To distinct between column / row header, you
     * can use {@link CellRangeAddress#isFullColumnRange()} and
     * {@link CellRangeAddress#isFullRowRange()}.
     * <p>
     * Similarly for {@link Action.Handler#handleAction(Action, Object, Object)}
     * the second parameter (sender) is always the spreadsheet component. The
     * third parameter (target) is the latest {@link SelectionChangeEvent} for
     * the spreadsheet, or the {@link CellRangeAddress} defining the selected
     * row / column header.
     */
    @Override
    public void addActionHandler(Action.Handler actionHandler) {
        contextMenuManager.addActionHandler(actionHandler);
        setHasActions(contextMenuManager.hasActionHandlers());
    }

    /**
     * Removes the spreadsheet's {@link SpreadsheetDefaultActionHandler} added
     * on {@link Spreadsheet#init()}
     */
    public void removeDefaultActionHandler() {
        removeActionHandler(defaultActionHandler);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vaadin.event.Action.Container#removeActionHandler(com.vaadin.event
     * .Action.Handler)
     */
    @Override
    public void removeActionHandler(Action.Handler actionHandler) {
        contextMenuManager.removeActionHandler(actionHandler);
        setHasActions(contextMenuManager.hasActionHandlers());
    }

    /**
     * Sets the {@link CellValueHandler} for this component (not workbook/sheet
     * specific). It is called when a cell's value has been updated by the user
     * by using the spreadsheet component's default editor (text input).
     *
     * @param customCellValueHandler
     *            New handler or <code>null</code> if none should be used
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
     * Sets the {@link CellDeletionHandler} for this component (not
     * workbook/sheet specific). It is called when a cell has been deleted by
     * the user.
     *
     * @param customCellDeletionHandler
     *            New handler or <code>null</code> if none should be used
     */
    public void setCellDeletionHandler(
            CellDeletionHandler customCellDeletionHandler) {
        getCellValueManager()
                .setCustomCellDeletionHandler(customCellDeletionHandler);
    }

    /**
     * See {@link CellDeletionHandler}.
     *
     * @return the current {@link CellDeletionHandler} for this component or
     *         <code>null</code> if none has been set
     */
    public CellDeletionHandler getCellDeletionHandler() {
        return getCellValueManager().getCustomCellDeletionHandler();
    }

    /**
     * Sets the {@link HyperlinkCellClickHandler} for this component (not
     * workbook/sheet specific). Called when the user clicks a cell that is a
     * hyperlink or uses the hyperlink function.
     *
     * @param handler
     *            new handler or <code>null</code> if none should be used
     * @see HyperlinkCellClickHandler
     * @see DefaultHyperlinkCellClickHandler
     */
    public void setHyperlinkCellClickHandler(
            HyperlinkCellClickHandler handler) {
        hyperlinkCellClickHandler = handler;
    }

    /**
     * See {@link HyperlinkCellClickHandler}.
     *
     * @return the current {@link HyperlinkCellClickHandler} for this component
     *         or <code>null</code> if none has been set
     */
    public HyperlinkCellClickHandler getHyperlinkCellClickHandler() {
        return hyperlinkCellClickHandler;
    }

    /**
     * Gets the ContextMenuManager for this Spreadsheet. This is component (not
     * workbook/sheet) specific.
     *
     * @return The ContextMenuManager
     */
    public ContextMenuManager getContextMenuManager() {
        return contextMenuManager;
    }

    /**
     * Gets the CellSelectionManager for this Spreadsheet. This is component
     * (not workbook/sheet) specific.
     *
     * @return The CellSelectionManager
     */
    public CellSelectionManager getCellSelectionManager() {
        return selectionManager;
    }

    /**
     * Gets the CellValueManager for this Spreadsheet. This is component (not
     * workbook/sheet) specific.
     *
     * @return The CellValueManager
     */
    public CellValueManager getCellValueManager() {
        return valueManager;
    }

    /**
     * Gets the CellShifter for this Spreadsheet. This is component (not
     * workbook/sheet) specific.
     *
     * @return The CellShifter
     */
    protected CellSelectionShifter getCellShifter() {
        return cellShifter;
    }

    /**
     * Gets the SpreadsheetHistoryManager for this Spreadsheet. This is
     * component (not workbook/sheet) specific.
     *
     * @return The SpreadsheetHistoryManager
     */
    public SpreadsheetHistoryManager getSpreadsheetHistoryManager() {
        return historyManager;
    }

    /**
     * Gets the MergedRegionContainer for this Spreadsheet. This is component
     * (not workbook/sheet) specific.
     *
     * @return The MergedRegionContainer
     */
    protected MergedRegionContainer getMergedRegionContainer() {
        return mergedRegionContainer;
    }

    /**
     * Returns the first visible column in the main scroll area (NOT freeze
     * pane)
     *
     * @return Index of first visible column, 1-based
     */
    public int getFirstColumn() {
        return sheetScope.viewport.getFirstColumn();
    }

    /**
     * Returns the last visible column in the main scroll area (NOT freeze pane)
     *
     * @return Index of last visible column, 1-based
     */
    public int getLastColumn() {
        return sheetScope.viewport.getLastColumn();
    }

    /**
     * Returns the first visible row in the scroll area (not freeze pane)
     *
     * @return Index of first visible row, 1-based
     */
    public int getFirstRow() {
        return sheetScope.viewport.getFirstRow();
    }

    /**
     * Returns the last visible row in the main scroll area (NOT freeze pane)
     *
     * @return Index of last visible row, 1-based
     */
    public int getLastRow() {
        return sheetScope.viewport.getLastRow();
    }

    /**
     * Returns the index the last frozen row (last row in top freeze pane).
     *
     * @return Last frozen row or 0 if none
     */
    public int getLastFrozenRow() {
        return getVerticalSplitPosition();
    }

    /**
     * Returns the index the last frozen column (last column in left freeze
     * pane).
     *
     * @return Last frozen column or 0 if none
     */
    public int getLastFrozenColumn() {
        return getHorizontalSplitPosition();
    }

    /**
     * Returns true if embedded charts are displayed
     *
     * @see #setChartsEnabled(boolean)
     * @return {@code true} if charts are enabled, {@code false} otherwise
     * @since 24.0
     */
    public boolean isChartsEnabled() {
        return chartsEnabled;
    }

    /**
     * Use this method to define whether embedded charts should be displayed in
     * the spreadsheet or not.
     *
     * @param chartsEnabled
     *            whether charts are enabled
     * @since 24.0
     */
    public void setChartsEnabled(boolean chartsEnabled) {
        this.chartsEnabled = chartsEnabled;
        sheetScope.overlays.clear();
        sheetScope.overlays.loadOrUpdate();
    }

    /**
     * Returns true if the component is being fully re-rendered after this
     * round-trip (sheet change etc.)
     *
     * @return true if re-render will happen, false otherwise
     */
    public boolean isRerenderPending() {
        return clientReloadFlag.isPending();
    }

    /**
     * Sets the visibility of the custom editor. By default, if a custom editor
     * is defined for a cell, then it is displayed on that cell. This behavior
     * can be changed by setting this property to true, which makes the custom
     * editor visible when the cell is focused.
     *
     * @param showCustomEditorOnFocus
     *            a boolean indicating whether the custom editor should be
     *            visible on focus (true) or not (false)
     * @since 24.8
     */
    public void setShowCustomEditorOnFocus(boolean showCustomEditorOnFocus) {
        this.showCustomEditorOnFocus.set(showCustomEditorOnFocus);
    }

    /**
     * Returns whether the custom editor is shown on focus or not.
     *
     * @return a boolean indicating whether the custom editor is visible on
     *         focus (true) or not (false)
     * @since 24.8
     */
    public boolean isShowCustomEditorOnFocus() {
        return showCustomEditorOnFocus.get();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vaadin.server.AbstractClientConnector#fireEvent(java.util.EventObject
     * )
     */
    @Override
    protected void fireEvent(ComponentEvent event) {
        super.fireEvent(event);
    }

    /**
     * The most rows that are not hidden one scroll loads. Well above what the
     * client asks for in the tallest window, with the rows it loads ahead.
     */
    private static final int MAX_SHOWN_ROWS_PER_SCROLL = 2000;

    /**
     * The most columns that are not hidden one scroll loads. See
     * {@link #MAX_SHOWN_ROWS_PER_SCROLL}.
     */
    private static final int MAX_SHOWN_COLUMNS_PER_SCROLL = 1000;

    /**
     * This method is called when the sheet is scrolled. It takes care of
     * sending newly revealed data to the client side.
     * <p>
     * A range that reaches outside the sheet is limited to the rows and columns
     * of the sheet, and to at most 2,000 rows and 1,000 columns that are not
     * hidden.
     *
     * @param firstRow
     *            Index of first visible row after the scroll, 1-based
     * @param firstColumn
     *            Index of first visible column after the scroll, 1-based
     * @param lastRow
     *            Index of last visible row after the scroll, 1-based
     * @param lastColumn
     *            Index of last visible column after the scroll, 1-based
     */
    protected void onSheetScroll(int firstRow, int firstColumn, int lastRow,
            int lastColumn) {
        // Every cell of this range is loaded below, so it can only cover
        // cells the sheet has.
        int maxRow = Math.max(1, getRows());
        int maxColumn = Math.max(1, getColumns());
        firstRow = Math.clamp(firstRow, 1, maxRow);
        lastRow = Math.clamp(lastRow, firstRow, maxRow);
        firstColumn = Math.clamp(firstColumn, 1, maxColumn);
        lastColumn = Math.clamp(lastColumn, firstColumn, maxColumn);
        // Every cell of the range is visited, so it is no bigger than the
        // client asks for at most. Hidden rows and columns do not count: a
        // filtered sheet loads the rows it shows however far apart they are.
        lastRow = lastWithShownCount(firstRow, lastRow,
                MAX_SHOWN_ROWS_PER_SCROLL, row -> isRowHidden(row - 1));
        lastColumn = lastWithShownCount(firstColumn, lastColumn,
                MAX_SHOWN_COLUMNS_PER_SCROLL,
                column -> isColumnHidden(column - 1));
        if (reloadCellDataOnNextScroll || sheetScope.viewport
                .hasChanged(firstRow, lastRow, firstColumn, lastColumn)) {
            sheetScope.viewport.update(firstRow, lastRow, firstColumn,
                    lastColumn);
            loadCells(firstRow, firstColumn, lastRow, lastColumn);
        }
        if (initialSheetSelection != null) {
            if (initialSheetSelectionKeepsView) {
                selectionManager.selectKeepingView(initialSheetSelection);
            } else {
                selectionManager.onSheetAddressChanged(initialSheetSelection,
                        true);
            }
            initialSheetSelection = null;
            initialSheetSelectionKeepsView = false;
        } else if (reloadCellDataOnNextScroll) {
            selectionManager.reloadCurrentSelection();
        }
        reloadCellDataOnNextScroll = false;
    }

    /**
     * The last index from {@code first} to {@code last} up to which no more
     * than {@code max} indexes are shown.
     */
    private static int lastWithShownCount(int first, int last, int max,
            IntPredicate hidden) {
        int shown = 0;
        for (int index = first; index <= last; index++) {
            if (!hidden.test(index) && ++shown == max) {
                return index;
            }
        }
        return last;
    }

    /**
     * Tells whether the given cell range is editable or not.
     *
     * @param cellRangeAddress
     *            Cell range to test
     * @return True if range is editable, false otherwise.
     */
    protected boolean isRangeEditable(CellRangeAddress cellRangeAddress) {
        return isRangeEditable(cellRangeAddress.getFirstRow(),
                cellRangeAddress.getFirstColumn(),
                cellRangeAddress.getLastRow(),
                cellRangeAddress.getLastColumn());
    }

    /**
     * Determines if the given cell range is editable or not.
     *
     * @param row1
     *            Index of starting row, 0-based
     * @param col1
     *            Index of starting column, 0-based
     * @param row2
     *            Index of ending row, 0-based
     * @param col2
     *            Index of ending column, 0-based
     *
     * @return True if the whole range is editable, false otherwise.
     */
    protected boolean isRangeEditable(int row1, int col1, int row2, int col2) {
        if (isActiveSheetProtected()) {
            for (int r = row1; r <= row2; r++) {
                for (int c = col1; c <= col2; c++) {
                    if (isCellLocked(new CellAddress(r, c))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Creates a CellRangeAddress from the given cell address string. Also
     * checks that the range is valid within the currently active sheet. If it
     * is not, the resulting range will be truncated to fit the active sheet.
     *
     * @param addressString
     *            Cell address string, e.g. "B3:C5"
     * @return A CellRangeAddress based on the given coordinates.
     */
    protected CellRangeAddress createCorrectCellRangeAddress(
            String addressString) {
        return cellRangeNormalizer.fromAddressString(addressString);
    }

    /**
     * Creates a CellRangeAddress from the given start and end coordinates. Also
     * checks that the range is valid within the currently active sheet. If it
     * is not, the resulting range will be truncated to fit the active sheet.
     *
     * @param row1
     *            Index of the starting row, 1-based
     * @param col1
     *            Index of the starting column, 1-based
     * @param row2
     *            Index of the ending row, 1-based
     * @param col2
     *            Index of the ending column, 1-based
     *
     * @return A CellRangeAddress based on the given coordinates.
     */
    protected CellRangeAddress createCorrectCellRangeAddress(int row1, int col1,
            int row2, int col2) {
        return cellRangeNormalizer.fromCoordinates(row1, col1, row2, col2);
    }

    /**
     * Set the Locale for the Spreadsheet. The locale is used for formatting
     * cell values.
     *
     * @param locale
     *            the locale set to the spreadsheet, cannot be null
     */
    public void setLocale(Locale locale) {
        Objects.requireNonNull(locale, "Locale must not be null.");
        this.locale = locale;
        valueManager.updateLocale(locale);
        refreshAllCellValues();
    }

    /**
     * Gets the Locale for this spreadsheet
     *
     * @return the locale used for spreadsheet
     */
    @Override
    public Locale getLocale() {
        if (locale != null) {
            return locale;
        } else {
            return super.getLocale();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        valueManager.updateLocale(getLocale());

        updateAppId();

        if (!resources.isEmpty()) {
            // A new element has none of the resource keys sent to the last.
            getElement().executeJs("this.resources=$0", resources.keySet());
        }

        if (overlays.get() != null) {
            // The node id's of component overlays attached as virtual children
            // may no longer be valid after a detach/attach. Remove all
            // overlays and reload them (with updated node id's).
            overlays.get().clear();
            sheetScope.overlays.loadOrUpdate();
        }

        if (!componentIDtoCellKeysMap.get().isEmpty()
                || !cellKeyToEditor.isEmpty()) {
            // The node id's of custom components may no longer be valid after a
            // detach/attach. Remove all custom components and reload them (with
            // updated node id's).
            loadCustomComponentsRecreatingEditors();
        }
    }

    /**
     * See {@link Workbook#setSheetHidden(int, boolean)}.
     * <p>
     * Gets the Workbook with {@link #getWorkbook()} and uses its API to access
     * status on currently visible/hidden/very hidden sheets.
     *
     * If the currently active sheet is set hidden, another sheet is set as
     * active sheet automatically. At least one sheet should be always visible.
     *
     * @param sheetPOIIndex
     *            Index of the target sheet within the POI model, 0-based
     * @param visibility
     *            Visibility state to set: visible, hidden, very hidden.
     * @throws IllegalArgumentException
     *             If the index or state is invalid, or if trying to hide the
     *             only visible sheet.
     */
    public void setSheetHidden(int sheetPOIIndex, SheetVisibility visibility)
            throws IllegalArgumentException {
        sheetOperations.setSheetHidden(sheetPOIIndex, visibility);
    }

    /**
     * Returns an array containing the names of the currently visible sheets.
     * Does not contain the names of hidden or very hidden sheets.
     * <p>
     * To get all of the current {@link Workbook}'s sheet names, you should
     * access the POI API with {@link #getWorkbook()}.
     *
     * @return Names of the currently visible sheets.
     */
    public String[] getVisibleSheetNames() {
        final String[] names = getSheetNames();
        return Arrays.copyOf(names, names.length);
    }

    /**
     * Sets a name for the sheet at the given visible sheet index.
     *
     * @param sheetIndex
     *            Index of the target sheet among the visible sheets, 0-based
     * @param sheetName
     *            New sheet name. Not null, empty nor longer than 31 characters.
     *            Must be unique within the Workbook.
     * @throws IllegalArgumentException
     *             If the index is invalid, or if the sheet name is invalid. See
     *             {@link WorkbookUtil#validateSheetName(String)}.
     */
    public void setSheetName(int sheetIndex, String sheetName)
            throws IllegalArgumentException {
        sheetOperations.setSheetName(sheetIndex, sheetName);
    }

    /**
     * Sets a name for the sheet at the given POI model index.
     *
     * @param sheetIndex
     *            Index of the target sheet within the POI model, 0-based
     * @param sheetName
     *            New sheet name. Not null, empty nor longer than 31 characters.
     *            Must be unique within the Workbook.
     * @throws IllegalArgumentException
     *             If the index is invalid, or if the sheet name is invalid. See
     *             {@link WorkbookUtil#validateSheetName(String)}.
     *
     */
    public void setSheetNameWithPOIIndex(int sheetIndex, String sheetName)
            throws IllegalArgumentException {
        sheetOperations.setSheetNameWithPOIIndex(sheetIndex, sheetName);
    }

    /**
     * Sets the protection enabled with the given password for the sheet at the
     * given index. <code>null</code> password removes the protection.
     *
     * @param sheetPOIIndex
     *            Index of the target sheet within the POI model, 0-based
     * @param password
     *            The password to set for the protection. Pass <code>null</code>
     *            to remove the protection.
     */
    public void setSheetProtected(int sheetPOIIndex, String password) {
        sheetOperations.setSheetProtected(sheetPOIIndex, password);
    }

    /**
     * Sets the protection enabled with the given password for the currently
     * active sheet. <code>null</code> password removes the protection.
     *
     * @param password
     *            The password to set for the protection. Pass <code>null</code>
     *            to remove the protection.
     */
    public void setActiveSheetProtected(String password) {
        sheetOperations.setActiveSheetProtected(password);
    }

    /**
     * Creates a new sheet as the last sheet and sets it as the active sheet.
     *
     * If the sheetName given is null, then the sheet name is automatically
     * generated by Apache POI in {@link Workbook#createSheet()}.
     *
     * @param sheetName
     *            Can be null, but not empty nor longer than 31 characters. Must
     *            be unique within the Workbook.
     * @param rows
     *            Number of rows the sheet should have
     * @param columns
     *            Number of columns the sheet should have
     * @throws IllegalArgumentException
     *             If the sheet name is empty or over 31 characters long or not
     *             unique.
     */
    public void createNewSheet(String sheetName, int rows, int columns)
            throws IllegalArgumentException {
        sheetOperations.createNewSheet(sheetName, rows, columns);
    }

    /**
     * Deletes the sheet with the given POI model index.
     *
     * Note: A workbook must contain at least one visible sheet.
     *
     * @param poiSheetIndex
     *            POI model index of the sheet to delete, 0-based, max value
     *            {@link Workbook#getNumberOfSheets()} -1.
     * @throws IllegalArgumentException
     *             In case there is only one visible sheet, or if the index is
     *             invalid.
     */
    public void deleteSheetWithPOIIndex(int poiSheetIndex)
            throws IllegalArgumentException {
        sheetOperations.deleteSheetWithPOIIndex(poiSheetIndex);
    }

    /**
     * Deletes the sheet at the given index.
     *
     * Note: A workbook must contain at least one visible sheet.
     *
     * @param sheetIndex
     *            Index of the sheet to delete among the visible sheets,
     *            0-based, maximum value {@link #getNumberOfVisibleSheets()} -1.
     * @throws IllegalArgumentException
     *             In case there is only one visible sheet, or if the given
     *             index is invalid.
     */
    public void deleteSheet(int sheetIndex) throws IllegalArgumentException {
        sheetOperations.deleteSheet(sheetIndex);
    }

    /**
     * Returns the number of currently visible sheets in the component. Doesn't
     * include the hidden or very hidden sheets in the POI model.
     *
     * @return Number of visible sheets.
     */
    public int getNumberOfVisibleSheets() {
        if (getSheetNames() != null) {
            return getSheetNames().length;
        } else {
            return 0;
        }
    }

    /**
     * Returns the total number of sheets in the workbook (includes hidden and
     * very hidden sheets).
     *
     * @return Total number of sheets in the workbook
     */
    public int getNumberOfSheets() {
        return workbook.getNumberOfSheets();
    }

    boolean isSheetNameExisting(String sheetName) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (workbook.getSheetName(i).equals(sheetName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index of the currently active sheet among the visible sheets
     * ( hidden or very hidden sheets not included).
     *
     * @return Index of the active sheet, 0-based
     */
    public int getActiveSheetIndex() {
        return getSheetIndex() - 1;
    }

    /**
     * Returns the POI model index of the currently active sheet (index among
     * all sheets including hidden and very hidden sheets).
     *
     * @return POI model index of the active sheet, 0-based
     */
    public int getActiveSheetPOIIndex() {
        return getVisibleSheetPOIIndex(getSheetIndex() - 1);
    }

    /**
     * Sets the currently active sheet within the sheets that are visible.
     *
     * @param sheetIndex
     *            Index of the target sheet (among the visible sheets), 0-based
     * @throws IllegalArgumentException
     *             If the index is invalid
     */
    public void setActiveSheetIndex(int sheetIndex)
            throws IllegalArgumentException {
        sheetOperations.setActiveSheetIndex(sheetIndex);
    }

    /**
     * Sets the currently active sheet. The sheet at the given index should be
     * visible (not hidden or very hidden).
     *
     * @param sheetIndex
     *            Index of sheet in the POI model (contains all sheets), 0-based
     * @throws IllegalArgumentException
     *             If the index is invalid, or if the sheet at the given index
     *             is hidden or very hidden.
     */
    public void setActiveSheetWithPOIIndex(int sheetIndex)
            throws IllegalArgumentException {
        sheetOperations.setActiveSheetWithPOIIndex(sheetIndex);
    }

    /**
     * This method will be called when a selected sheet change is requested.
     *
     * @param tabIndex
     *            Index of the sheet to select.
     * @param scrollLeft
     *            Current horizontal scroll position
     * @param scrollTop
     *            Current vertical scroll position
     */
    protected void onSheetSelected(int tabIndex, int scrollLeft,
            int scrollTop) {
        sheetOperations.onSheetSelected(tabIndex, scrollLeft, scrollTop);
    }

    /**
     * This method is called when the creation of a new sheet has been
     * requested.
     *
     * @param scrollLeft
     *            Current horizontal scroll position
     * @param scrollTop
     *            Current vertical scroll position
     */
    protected void onNewSheetCreated(int scrollLeft, int scrollTop) {
        sheetOperations.onNewSheetCreated(scrollLeft, scrollTop);
    }

    /**
     * This method is called when a request to rename a sheet has been made.
     *
     * @param sheetIndex
     *            Index of the sheet to rename (among visible sheets).
     * @param sheetName
     *            New name for the sheet.
     */
    protected void onSheetRename(int sheetIndex, String sheetName) {
        sheetOperations.onSheetRename(sheetIndex, sheetName);
    }

    /**
     * Get the number of columns in the currently active sheet, or if
     * {@link #setMaxColumns(int)} has been used, the current number of columns
     * the component shows (not the amount of columns in the actual sheet in the
     * POI model).
     *
     * @return Number of visible columns.
     */
    public int getColumns() {
        return getCols();
    }

    /**
     * Get the number of rows in the currently active sheet, or if
     * {@link #setMaxRows(int)} has been used, the current number of rows the
     * component shows (not the amount of rows in the actual sheet in the POI
     * model).
     *
     * @return Number of visible rows.
     */
    public int getRows() {
        return rows.get();
    }

    /**
     * Gets the current DataFormatter.
     *
     * @return The data formatter for this Spreadsheet.
     */
    public DataFormatter getDataFormatter() {
        return valueManager.getDataFormatter();
    }

    /**
     * Returns the Cell at the given address. If the cell is updated in outside
     * code, call {@link #refreshCells(Cell...)} AFTER ALL UPDATES (value, type,
     * formatting or style) to mark the cell as "dirty".
     *
     * @param cellAddress
     *            Address of the Cell to return, e.g. "A3"
     * @return The cell at the given address, or null if not defined
     */
    public @Nullable Cell getCell(String cellAddress) {
        CellReference ref = new CellReference(cellAddress);
        Row r = workbook.getSheetAt(workbook.getActiveSheetIndex())
                .getRow(ref.getRow());
        if (r != null) {
            return r.getCell(ref.getCol());
        } else {
            return null;
        }
    }

    /**
     * Returns the Cell at the given coordinates. If the cell is updated in
     * outside code, call {@link #refreshCells(Cell...)} AFTER ALL UPDATES
     * (value, type, formatting or style) to mark the cell as "dirty".
     *
     * @param row
     *            Row index of the cell to return, 0-based
     * @param col
     *            Column index of the cell to return, 0-based
     * @return The cell at the given coordinates, or null if not defined
     */
    public @Nullable Cell getCell(int row, int col) {
        Sheet sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
        return getCell(row, col, sheet);
    }

    /**
     * Returns the Cell at the given coordinates. If the cell is updated in
     * outside code, call {@link #refreshCells(Cell...)} AFTER ALL UPDATES
     * (value, type, formatting or style) to mark the cell as "dirty".
     *
     * @param row
     *            Row index of the cell to return, 0-based
     * @param col
     *            Column index of the cell to return, 0-based
     * @param sheet
     *            Sheet of the cell
     * @return The cell at the given coordinates, or null if not defined
     */
    public @Nullable Cell getCell(int row, int col, Sheet sheet) {
        Row r = sheet.getRow(row);
        if (r != null) {
            return r.getCell(col);
        } else {
            return null;
        }
    }

    /**
     * Returns the Cell corresponding to the given reference. If the cell is
     * updated in outside code, call {@link #refreshCells(Cell...)} AFTER ALL
     * UPDATES (value, type, formatting or style) to mark the cell as "dirty".
     *
     * @param cellReference
     *            Reference to the cell to return, may be null
     * @return The cell corresponding to the given reference, or null if not
     *         defined
     */
    public @Nullable Cell getCell(@Nullable CellReference cellReference) {
        return cellReference == null ? null
                : getCell(cellReference.getSheetName(), cellReference.getRow(),
                        cellReference.getCol());
    }

    /**
     * Returns the Cell at the given coordinates. If the cell is updated in
     * outside code, call {@link #refreshCells(Cell...)} AFTER ALL UPDATES
     * (value, type, formatting or style) to mark the cell as "dirty".
     *
     * @param sheetName
     *            Name of the sheet the cell is on, or current sheet if null
     * @param row
     *            Row index of the cell to return, 0-based
     * @param column
     *            Column index of the cell to return, 0-based
     * @return The cell at the given coordinates, or null if not defined
     */
    public @Nullable Cell getCell(@Nullable String sheetName, int row,
            int column) {
        if (sheetName == null)
            return getCell(row, column);
        return getCell(row, column, workbook.getSheet(sheetName));
    }

    /**
     * Returns the Cell corresponding to the given reference. If the cell is
     * updated in outside code, call {@link #refreshCells(Cell...)} AFTER ALL
     * UPDATES (value, type, formatting or style) to mark the cell as "dirty".
     *
     * @param cellReference
     *            Reference to the cell to return, may be null
     * @param sheet
     *            Sheet of the cell
     * @return The cell corresponding to the given reference, or null if not
     *         defined
     */
    public @Nullable Cell getCell(@Nullable CellReference cellReference,
            Sheet sheet) {
        return cellReference == null ? null
                : getCell(cellReference.getRow(), cellReference.getCol(),
                        sheet);
    }

    /**
     * Deletes the cell from the sheet and the underlying POI model as well.
     * This really deletes the cell, instead of just making it's value blank.
     *
     * @param row
     *            Row index of the cell to delete, 0-based
     * @param col
     *            Column index of the cell to delete, 0-based
     */
    public void deleteCell(int row, int col) {
        cellMarkUpdater.deleteCell(row, col);
    }

    /**
     * Refreshes the given cell(s). Should be called when the cell
     * value/formatting/style/etc. updating is done.
     *
     * NOTE: For optimal performance temporarily collect your updated cells and
     * call this method only once per update cycle. Calling this method
     * repeatedly for individual cells is not a good idea.
     *
     * @param cells
     *            Cell(s) to update
     */
    public void refreshCells(Cell... cells) {
        if (cells != null) {
            for (Cell cell : cells) {
                markCellAsUpdated(cell, true);
            }
            updateMarkedCells();
        }
    }

    /**
     * Refreshes the given cell(s). Should be called when the cell
     * value/formatting/style/etc. updating is done.
     *
     * NOTE: For optimal performance temporarily collect your updated cells and
     * call this method only once per update cycle. Calling this method
     * repeatedly for individual cells is not a good idea.
     *
     * @param cells
     *            A Collection of Cells to update
     */
    public void refreshCells(Collection<Cell> cells) {
        if (cells != null && !cells.isEmpty()) {
            for (Cell cell : cells) {
                markCellAsUpdated(cell, true);
            }
            updateMarkedCells();
        }
    }

    /**
     * Marks the cell as updated. Should be called when the cell
     * value/formatting/style/etc. updating is done.
     *
     * @param cellStyleUpdated
     *            True if the cell style has changed
     *
     * @param cell
     *            The updated cell
     */
    void markCellAsUpdated(Cell cell, boolean cellStyleUpdated) {
        cellMarkUpdater.markCellAsUpdated(cell, cellStyleUpdated);
    }

    /**
     * Marks the cell as deleted. This method should be called after removing a
     * cell from the {@link Workbook} using POI API.
     *
     * @param cellStyleUpdated
     *            True if the cell style has changed
     * @param cell
     *            The cell that has been deleted.
     */
    public void markCellAsDeleted(Cell cell, boolean cellStyleUpdated) {
        cellMarkUpdater.markCellAsDeleted(cell, cellStyleUpdated);
    }

    /**
     * Updates the content of the cells that have been marked for update with
     * {@link #markCellAsUpdated(Cell, boolean)}.
     * <p>
     * Does NOT update custom components (editors / always visible) for the
     * cells. For that, use {@link #reloadVisibleCellContents()}
     */
    void updateMarkedCells() {
        cellMarkUpdater.updateMarkedCells(false);
    }

    /**
     * As {@link #updateMarkedCells()}, but asking the factory for fresh
     * editors.
     * <p>
     * Only for updates that move cells, such as inserting or deleting rows: an
     * editor is cached against a cell key, so one left in place would end up on
     * the wrong cell.
     */
    void updateMarkedCellsRecreatingEditors() {
        cellMarkUpdater.updateMarkedCells(true);
    }

    /**
     * Creates a new Formula type cell with the given formula.
     *
     * After all editing is done, call {@link #refreshCells(Cell...)} or
     * {@link #refreshAllCellValues()} to make sure client side is updated.
     *
     * @param row
     *            Row index of the new cell, 0-based
     * @param col
     *            Column index of the new cell, 0-based
     * @param formula
     *            The formula to set to the new cell (should NOT start with "="
     *            nor "+")
     * @return The newly created cell
     * @throws IllegalArgumentException
     *             If columnIndex &lt; 0 or greater than the maximum number of
     *             supported columns (255 for *.xls, 1048576 for *.xlsx)
     */
    public Cell createFormulaCell(int row, int col, String formula)
            throws IllegalArgumentException {
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        Row r = activeSheet.getRow(row);
        if (r == null) {
            r = activeSheet.createRow(row);
        }
        Cell cell = r.getCell(col);
        if (cell == null) {
            cell = r.createCell(col, CellType.FORMULA);
        } else {
            final String key = SpreadsheetUtil.toKey(col + 1, row + 1);
            valueManager.clearCellCache(key);
        }
        cell.setCellFormula(formula);
        valueManager.cellUpdated(cell);
        return cell;
    }

    /**
     * Create a new cell (or replace existing) with the given value, the type of
     * the value parameter will define the type of the cell. The value may be of
     * the following types: Boolean, Calendar, Date, Double or String. The
     * default type will be String, value of ({@link #toString()} will be given
     * as the cell value.
     *
     * For formula cells, use {@link #createFormulaCell(int, int, String)}.
     *
     * After all editing is done, call {@link #refreshCells(Cell...)} or
     * {@link #refreshAllCellValues()} to make sure the client side is updated.
     *
     * @param row
     *            Row index of the new cell, 0-based
     * @param col
     *            Column index of the new cell, 0-based
     * @param value
     *            Object representing the type and value of the Cell
     * @return The newly created cell
     * @throws IllegalArgumentException
     *             If columnIndex &lt; 0 or greater than the maximum number of
     *             supported columns (255 for *.xls, 1048576 for *.xlsx)
     */
    public Cell createCell(int row, int col, Object value)
            throws IllegalArgumentException {
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
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
        if (value instanceof Double d) {
            cell.setCellValue(d);
        } else if (value instanceof Boolean b) {
            cell.setCellValue(b);
        } else if (value instanceof Date date) {
            cell.setCellValue(date);
        } else if (value instanceof Calendar cal) {
            cell.setCellValue(cal);
        } else if (value != null) {
            cell.setCellValue(value.toString());
        }
        valueManager.cellUpdated(cell);
        // if programmatically adding cells, need to make sure they display
        if (row > getRows()) {
            setMaxRows(row);
        }
        return cell;
    }

    /**
     * Forces recalculation and update to the client side for values of all of
     * the sheet's cells.
     *
     * Note: DOES NOT UPDATE STYLES; use {@link #refreshCells(Cell...)} when
     * cell styles change.
     */
    public void refreshAllCellValues() {
        cellMarkUpdater.refreshAllCellValues();
    }

    /**
     * Set the number of columns shown for the current sheet. Any null cells are
     * left empty. Any cells outside the given columns are hidden. Does not
     * update the actual POI-based model!
     *
     * The default value will be the actual size of the sheet from the POI
     * model.
     *
     * @param cols
     *            New maximum column count.
     */
    public void setMaxColumns(int cols) {
        if (getCols() != cols) {
            setCols(cols);
        }
    }

    /**
     * Set the number of rows shown for the current sheet. Any null cells are
     * left empty. Any cells outside the given rows are hidden. Does not update
     * the actual POI-based model!
     *
     * The default value will be the actual size of the sheet from the POI
     * model.
     *
     * @param rows
     *            New maximum row count.
     */
    public void setMaxRows(int rows) {
        if (getRows() != rows) {
            setRows(rows);
        }
    }

    /**
     * Does {@link #setMaxColumns(int)} and {@link #setMaxRows(int)} in one
     * method.
     *
     * @param rows
     *            Maximum row count
     * @param cols
     *            Maximum column count
     */
    public void setSheetMaxSize(int rows, int cols) {
        setCols(cols);
        setRows(rows);
    }

    /**
     * Gets the default column width: the width of the columns that have no
     * width of their own, on any sheet. It is 10 characters unless set with
     * {@link #setDefaultColumnWidth(int)}, whatever the default column width of
     * the sheet ({@link Sheet#getDefaultColumnWidth()}).
     *
     * @return The default column width in PX
     */
    public int getDefaultColumnWidth() {
        return getDefColW();
    }

    /**
     * Sets the default column width in pixels: the columns without a width of
     * their own are shown at it, on any sheet. This doesn't change the default
     * column width of the underlying sheet, returned by
     * {@link #getActiveSheet()} and {@link Sheet#getDefaultColumnWidth()}.
     *
     * @param widthPX
     *            The default column width in pixels
     */
    public void setDefaultColumnWidth(int widthPX) {
        if (widthPX <= 0) {
            throw new IllegalArgumentException(
                    "Default column width must be over 0, given value: "
                            + widthPX);
        }
        sheetSizes.setDefaultColumnWidth(widthPX);
    }

    /**
     * Gets the default row height in points. By default it should be the same
     * as {@link Sheet#getDefaultRowHeightInPoints()} for the currently active
     * sheet {@link #getActiveSheet()}.
     *
     * @return Default row height for the currently active sheet, in points.
     */
    public float getDefaultRowHeight() {
        return getDefRowH();
    }

    /**
     * Sets the default row height in points for this Spreadsheet and the
     * currently active sheet, returned by {@link #getActiveSheet()}: the rows
     * of the sheet without a height of their own are shown at it.
     *
     * @param heightPT
     *            New default row height in points.
     */
    public void setDefaultRowHeight(float heightPT) {
        if (heightPT <= 0.0f) {
            throw new IllegalArgumentException(
                    "Default row height must be over 0, given value: "
                            + heightPT);
        }
        sheetSizes.setDefaultRowHeight(heightPT);
    }

    /**
     * This method is called when column auto-fit has been initiated from the
     * browser by double-clicking the border of the target column header.
     *
     * @param columnIndex
     *            Index of the target column, 0-based
     */
    protected void onColumnAutofit(int columnIndex) {
        sheetSizes.onClientAutofit(columnIndex);
    }

    /**
     * Sets the column to automatically adjust the column width to fit the
     * largest cell content within the column. This is a POI feature, and is
     * meant to be called after all the data for the target column has been
     * written. See {@link Sheet#autoSizeColumn(int)}.
     * <p>
     * This does not take into account cells that have custom Vaadin components
     * inside them.
     *
     * @param columnIndex
     *            Index of the target column, 0-based
     */
    public void autofitColumn(int columnIndex) {
        sheetSizes.autofit(columnIndex);
    }

    /**
     * Shifts rows between startRow and endRow n number of rows. If you use a
     * negative number for n, the rows will be shifted upwards. This method
     * ensures that rows can't wrap around.
     * <p>
     * If you are adding / deleting rows, you might want to change the number of
     * visible rows rendered {@link #getRows()} with {@link #setMaxRows(int)}.
     * <p>
     * See {@link Sheet#shiftRows(int, int, int)}.
     *
     * @param startRow
     *            The first row to shift, 0-based
     * @param endRow
     *            The last row to shift, 0-based
     * @param n
     *            Number of rows to shift, positive numbers shift down, negative
     *            numbers shift up.
     */
    public void shiftRows(int startRow, int endRow, int n) {
        shiftRows(startRow, endRow, n, false, false);
    }

    /**
     * Shifts rows between startRow and endRow n number of rows. If you use a
     * negative number for n, the rows will be shifted upwards. This method
     * ensures that rows can't wrap around.
     * <p>
     * If you are adding / deleting rows, you might want to change the number of
     * visible rows rendered {@link #getRows()} with {@link #setMaxRows(int)}.
     * <p>
     * See {@link Sheet#shiftRows(int, int, int, boolean, boolean)}.
     *
     * @param startRow
     *            The first row to shift, 0-based
     * @param endRow
     *            The last row to shift, 0-based
     * @param n
     *            Number of rows to shift, positive numbers shift down, negative
     *            numbers shift up.
     * @param copyRowHeight
     *            True to copy the row height during the shift
     * @param resetOriginalRowHeight
     *            True to set the original row's height to the default
     */
    public void shiftRows(int startRow, int endRow, int n,
            boolean copyRowHeight, boolean resetOriginalRowHeight) {
        rowShiftOrchestrator.shift(startRow, endRow, n, copyRowHeight,
                resetOriginalRowHeight);
    }

    /**
     * @return the common {@link FormulaEvaluator} instance.
     */
    public FormulaEvaluator getFormulaEvaluator() {
        return workbookScope.formulaEvaluator;
    }

    /**
     * POI, as of 4.0.0, now accepts this as an argument to formula evaluation.
     * Some conditional formats can modify the display text of a cell.
     *
     * @return the common {@link ConditionalFormattingEvaluator} instance.
     */
    public ConditionalFormattingEvaluator getConditionalFormattingEvaluator() {
        return workbookScope.formattingEvaluator;
    }

    void updateMergedRegions() {
        mergedRegionOperations.reload();
    }

    /**
     * Deletes rows. See {@link Sheet#removeRow(Row)}. Removes all row content,
     * deletes cells and resets the sheet size.
     *
     * Does not shift rows up (!) - use
     * {@link #shiftRows(int, int, int, boolean, boolean)} for that.
     *
     * @param startRow
     *            Index of the starting row, 0-based
     * @param endRow
     *            Index of the ending row, 0-based
     */
    public void deleteRows(int startRow, int endRow) {
        Sheet sheet = getActiveSheet();
        for (int i = startRow; i <= endRow; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                // POI keeps a removed cell's link on the sheet, where a cell
                // made at the same address later picks it up.
                for (Cell cell : row) {
                    cell.removeHyperlink();
                    cell.removeCellComment();
                }
                sheet.removeRow(row);
            }
        }
        // The formula evaluator still holds the cells removed, and a cell
        // taken off its row fails when it is read.
        getFormulaEvaluator().clearAllCachedResultValues();
        // The rows deleted are empty, of the default height, and not hidden.
        sheetSizes.readRows(startRow, endRow);
        updateMergedRegions();
        // The cells deleted draw no borders, and those below a row hidden
        // draw theirs on it now.
        workbookScope.styler.reloadBorders();
        valueManager.updateDeletedRowsInClientCache(startRow + 1, endRow + 1);

        if (sheetScope.overlays.hasOverlays()) {
            sheetScope.overlays.markReloadFromPoi();
        }
        // Deleting rows moves cells, and an editor is cached against a cell
        // key — one left in place would end up on the wrong cell.
        updateMarkedCellsRecreatingEditors();
        // Nothing is selected until the client has shown the sheet.
        CellReference selectedCellReference = getSelectedCellReference();
        if (selectedCellReference != null
                && selectedCellReference.getRow() >= startRow
                && selectedCellReference.getRow() <= endRow) {
            selectionManager.reSelectSelectedCell();
        }
    }

    /**
     * Merges cells. See {@link Sheet#addMergedRegion(CellRangeAddress)}.
     *
     * @param selectionRange
     *            The cell range to merge, e.g. "B3:C5"
     */
    public void addMergedRegion(String selectionRange) {
        addMergedRegion(CellRangeAddress.valueOf(selectionRange));
    }

    /**
     * Merge cells. See {@link Sheet#addMergedRegion(CellRangeAddress)}.
     *
     * @param row1
     *            Index of the starting row of the merged region, 0-based
     * @param col1
     *            Index of the starting column of the merged region, 0-based
     * @param row2
     *            Index of the ending row of the merged region, 0-based
     * @param col2
     *            Index of the ending column of the merged region, 0-based
     */
    public void addMergedRegion(int row1, int col1, int row2, int col2) {
        addMergedRegion(new CellRangeAddress(row1, row2, col1, col2));
    }

    /**
     * Merges the given cells. See
     * {@link Sheet#addMergedRegion(CellRangeAddress)}.
     * <p>
     * If another existing merged region is completely inside the given range,
     * it is removed. If another existing region either encloses or overlaps the
     * given range, an error is thrown. See
     * {@link CellRangeUtil#intersect(CellRangeAddress, CellRangeAddress)}.
     * <p>
     * Note: POI doesn't seem to update the cells that are "removed" due to the
     * merge - the values for those cells still exist and continue being used in
     * possible formulas. If you need to make sure those values are removed,
     * just delete the cells before creating the merged region.
     * <p>
     * If the added region affects the currently selected cell, a new
     * {@link SelectionChangeEvent} is fired.
     *
     * @param region
     *            The range of cells to merge
     * @throws IllegalArgumentException
     *             If the given region overlaps with or encloses another
     *             existing region within the sheet.
     */
    public void addMergedRegion(CellRangeAddress region)
            throws IllegalArgumentException {
        mergedRegionOperations.addRegion(region);
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
     * If the removed region affects the currently selected cell, a new
     * {@link SelectionChangeEvent} is fired.
     *
     * @param index
     *            Position of the target merged region in the POI merged region
     *            array, 0-based
     */
    public void removeMergedRegion(int index) {
        mergedRegionOperations.removeRegion(index);
    }

    /**
     * Discards all current merged regions for the sheet and reloads them from
     * the POI model.
     * <p>
     * This can be used if you want to add / remove multiple merged regions
     * directly from the POI model and need to update the component.
     *
     * Note that you must also make sure that possible styles for the merged
     * regions are updated, if those were modified, by calling
     * {@link #reloadActiveSheetStyles()}.
     */
    public void reloadAllMergedRegions() {
        mergedRegionOperations.load();
    }

    /**
     * Reloads all the styles for the currently active sheet.
     */
    public void reloadActiveSheetStyles() {
        // For all its rows and columns: a sheet shown has its size by now.
        workbookScope.styler.reloadActiveSheetColumnRowStyles();
        workbookScope.styler.reloadActiveSheetCellStyles();
    }

    /**
     * Hides or shows the given column, see
     * {@link Sheet#setColumnHidden(int, boolean)}.
     *
     * @param columnIndex
     *            Index of the target column, 0-based
     * @param hidden
     *            True to hide the target column, false to show it.
     */
    public void setColumnHidden(int columnIndex, boolean hidden) {
        sheetSizes.setColumnHidden(columnIndex, hidden);
    }

    /**
     * Hides or shows the given columns. Used internally in order to defer the
     * style calculations until visibility of all columns are updated.
     *
     * @see #setColumnHidden(int, boolean)
     *
     * @param columnIndexToHidden
     *            Map of 0-based column indexes to the indicator whether the
     *            column should be hidden
     */
    void setColumnsHidden(Map<Integer, Boolean> columnIndexToHidden) {
        sheetSizes.setColumnsHidden(columnIndexToHidden);
    }

    /**
     * Gets the visibility state of the given column. See
     * {@link Sheet#isColumnHidden(int)}.
     *
     * @param columnIndex
     *            Index of the target column, 0-based
     * @return true if the target column is hidden, false if it is visible.
     */
    public boolean isColumnHidden(int columnIndex) {
        return getActiveSheet().isColumnHidden(columnIndex);
    }

    /**
     * Hides or shows the given row, see {@link Row#setZeroHeight(boolean)}.
     *
     * @param rowIndex
     *            Index of the target row, 0-based
     * @param hidden
     *            True to hide the target row, false to show it.
     */
    public void setRowHidden(int rowIndex, boolean hidden) {
        sheetSizes.setRowHidden(rowIndex, hidden);
    }

    /**
     * Hides or shows the given rows. Used internally in order to defer the
     * style calculations until visibility of all rows are updated.
     *
     * @see #setRowHidden(int, boolean)
     *
     * @param rowIndexToHidden
     *            Map of 0-based row indexes to the indicator whether the row
     *            should be hidden
     */
    void setRowsHidden(Map<Integer, Boolean> rowIndexToHidden) {
        sheetSizes.setRowsHidden(rowIndexToHidden);
    }

    /**
     * Gets the visibility state of the given row. A row is hidden when it has
     * zero height, see {@link Row#getZeroHeight()}.
     *
     * @param rowIndex
     *            Index of the target row, 0-based
     * @return true if the target row is hidden, false if it is visible.
     */
    public boolean isRowHidden(int rowIndex) {
        Row row = getActiveSheet().getRow(rowIndex);
        return row == null ? false : row.getZeroHeight();
    }

    /**
     * Reinitializes the component from the given Excel file.
     *
     * @param file
     *            Data source file. Excel format is expected.
     * @throws IOException
     *             If the file can't be read, or the file is of an invalid
     *             format.
     */
    public void read(File file) throws IOException {
        SpreadsheetFactory.reloadSpreadsheetComponent(this, file);
    }

    /**
     * Reinitializes the component from the given input stream. The expected
     * format is that of an Excel file.
     *
     * @param inputStream
     *            Data source input stream. Excel format is expected.
     * @throws IOException
     *             If handling the stream fails, or the data is in an invalid
     *             format.
     */
    public void read(InputStream inputStream) throws IOException {
        SpreadsheetFactory.reloadSpreadsheetComponent(this, inputStream);
    }

    /**
     * Exports current spreadsheet into a File with the given name.
     *
     * @param fileName
     *            The full name of the file. If the name doesn't end with '.xls'
     *            or '.xlsx', the appropriate one will be appended.
     * @return A File with the content of the current {@link Workbook}, In the
     *         file format of the original {@link Workbook}.
     * @throws FileNotFoundException
     *             If file name was invalid
     * @throws IOException
     *             If the file can't be written to for any reason
     */
    public File write(String fileName)
            throws FileNotFoundException, IOException {
        return SpreadsheetFactory.write(this, fileName);
    }

    /**
     * Exports current spreadsheet as an output stream.
     *
     * @param outputStream
     *            The target stream
     * @throws IOException
     *             If writing to the stream fails
     */
    public void write(OutputStream outputStream) throws IOException {
        SpreadsheetFactory.write(this, outputStream);
    }

    /**
     * The row buffer size determines the amount of content rendered outside the
     * top and bottom edges of the visible cell area, for smoother scrolling.
     * <p>
     * Size is in pixels, the default is 200.
     *
     * @return The current row buffer size
     */
    public int getRowBufferSize() {
        return viewportBufferConfig.getRowBufferSize();
    }

    /**
     * Sets the row buffer size. Comes into effect the next time sheet is
     * scrolled or reloaded.
     * <p>
     * The row buffer size determines the amount of content rendered outside the
     * top and bottom edges of the visible cell area, for smoother scrolling.
     *
     * @param rowBufferInPixels
     *            The amount of extra content rendered outside the top and
     *            bottom edges of the visible area.
     */
    public void setRowBufferSize(int rowBufferInPixels) {
        viewportBufferConfig.setRowBufferSize(rowBufferInPixels);
    }

    /**
     * The column buffer size determines the amount of content rendered outside
     * the left and right edges of the visible cell area, for smoother
     * scrolling.
     * <p>
     * Size is in pixels, the default is 200.
     *
     * @return The current column buffer size
     */
    public int getColBufferSize() {
        return viewportBufferConfig.getColumnBufferSize();
    }

    /**
     * Sets the column buffer size. Comes into effect the next time sheet is
     * scrolled or reloaded.
     * <p>
     * The column buffer size determines the amount of content rendered outside
     * the left and right edges of the visible cell area, for smoother
     * scrolling.
     *
     * @param colBufferInPixels
     *            The amount of extra content rendered outside the left and
     *            right edges of the visible area.
     */
    public void setColBufferSize(int colBufferInPixels) {
        viewportBufferConfig.setColumnBufferSize(colBufferInPixels);
    }

    /**
     * Gets the default row count for new sheets.
     *
     * @return The default row count for new sheets.
     */
    public int getDefaultRowCount() {
        return defaultNewSheetRows;
    }

    /**
     * Sets the default row count for new sheets.
     *
     * @param defaultRowCount
     *            The number of rows to give sheets that are created with the
     *            '+' button on the client side.
     */
    public void setDefaultRowCount(int defaultRowCount) {
        defaultNewSheetRows = defaultRowCount;
    }

    /**
     * Gets the default column count for new sheets.
     *
     * @return The default column count for new sheets.
     */
    public int getDefaultColumnCount() {
        return defaultNewSheetColumns;
    }

    /**
     * Sets the default column count for new sheets.
     *
     * @param defaultColumnCount
     *            The number of columns to give sheets that are created with the
     *            '+' button on the client side.
     */
    public void setDefaultColumnCount(int defaultColumnCount) {
        defaultNewSheetColumns = defaultColumnCount;
    }

    /**
     * Call this to force the spreadsheet to reload the currently viewed cell
     * contents. This forces reload of all: custom components (always visible
     * and editors) from {@link SpreadsheetComponentFactory}, hyperlinks, cells'
     * comments and cells' contents. Also updates styles for the visible area.
     * <p>
     * Any state entered into a custom editor is lost, as the editors are
     * replaced with new instances from the factory.
     */
    public void reloadVisibleCellContents() {
        // The documented contract of this method: state entered into a custom
        // editor is lost, because the editors are replaced with new instances
        // from the factory.
        reloadVisibleCellContents(true);
    }

    /**
     * Reload the currently viewed cell contents.
     *
     * @param recreateEditors
     *            {@code true} to ask the factory for new editors, {@code false}
     *            to keep the existing ones so their state survives
     */
    void reloadVisibleCellContents(boolean recreateEditors) {
        sheetScope.components.load(recreateEditors);
        updateRowAndColumnRangeCellData(sheetScope.viewport.getFirstRow(),
                sheetScope.viewport.getFirstColumn(),
                sheetScope.viewport.getLastRow(),
                sheetScope.viewport.getLastColumn());
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
    protected void setResource(String key, DownloadHandler resource) {
        if (resource == null) {
            resources.remove(key);
            getElement().removeAttribute("resource-" + key);
        } else {
            if (resource instanceof AbstractDownloadHandler<?> handler) {
                // change disposition to inline in pre-defined handlers,
                // where it is 'attachment' by default
                handler.inline();
            }
            resources.put(key, resource.toString());
            getElement().executeJs("this.resources=$0", resources.keySet());
            getElement().setAttribute("resource-" + key,
                    new StreamResourceRegistry.ElementStreamResource(resource,
                            this.getElement()));
        }
    }

    void clearSheetServerSide() {
        workbook = null;

        valueManager.clearCachedContent();
        selectionManager.clear();
        historyManager.clear();
    }

    void setInternalWorkbook(Workbook workbook) {
        this.workbook = workbook;
        if (workbookScope != null) {
            workbookScope.close();
        }
        workbookScope = new WorkbookScope(this, workbook);
        // Through the methods an application may override, once the scope is
        // in place for them to read, and before the sheet, which reads them,
        // is loaded.
        workbookScope.styler = createSpreadsheetStyleFactory();
        workbookScope.conditionalFormatter = createConditionalFormatter();

        if (workbook instanceof HSSFWorkbook hssf) {
            setWorkbookProtected(hssf.isWriteProtected());
        } else if (workbook instanceof XSSFWorkbook xssf) {
            setWorkbookProtected(xssf.isStructureLocked());
        }

        setWorkbookChangeToggle(!isWorkbookChangeToggle());
    }

    /**
     * Shows the sheet at the POI index, or the nearest one shown if it is
     * hidden. See {@link SheetOperations#showSheet(int)}.
     */
    void showSheet(int poiIndex) {
        sheetOperations.showSheet(poiIndex);
    }

    /** The client has no cells: it is reloaded, or new. */
    void forgetClientCells() {
        clientCells.reload();
    }

    /** The overlays of the sheet shown. */
    SheetOverlayManager overlays() {
        return sheetScope.overlays;
    }

    /** The custom components of the sheet shown. */
    CustomComponentLoader components() {
        return sheetScope.components;
    }

    /** The sizes of the rows and columns of the sheet shown. */
    SheetSizes sizes() {
        return sheetSizes;
    }

    /** The pop-up buttons of the workbook shown. */
    PopupButtonRegistry popupButtons() {
        return workbookScope.popupButtons;
    }

    /** The cells of the workbook shown marked as holding invalid formulas. */
    InvalidFormulaTracker invalidFormulas() {
        return workbookScope.invalidFormulas;
    }

    /** What each sheet of the workbook shown was left with. */
    SheetState sheetState() {
        return workbookScope.sheetState;
    }

    WorkbookScope workbookScope() {
        return workbookScope;
    }

    /**
     * Override this method to provide your own {@link ConditionalFormatter}
     * implementation. This method is called each time we open a workbook.
     *
     * @return A {@link ConditionalFormatter} that is tied to this spreadsheet.
     */
    protected ConditionalFormatter createConditionalFormatter() {
        return new ConditionalFormatter(this);
    }

    /**
     * Override this method to provide your own {@link SpreadsheetStyleFactory}
     * implementation. This method is called each time we open a workbook.
     *
     * @return A {@link SpreadsheetStyleFactory} that is tied to this
     *         Spreadsheet.
     */
    protected SpreadsheetStyleFactory createSpreadsheetStyleFactory() {
        return new SpreadsheetStyleFactory(this);
    }

    /**
     * Clears and reloads all data related to the currently active sheet.
     */
    protected void reloadActiveSheetData() {
        selectionManager.clear();
        valueManager.clearCachedContent();

        // What the client has of the last sheet goes with its scope.
        sheetScope.close();
        sheetScope = new SheetScope(this);

        clientReloadFlag.markPending(this);

        setSheetIndex(
                getSpreadsheetSheetIndex(workbook.getActiveSheetIndex()) + 1);
        setSheetProtected(getActiveSheet().getProtect());
        setCellKeyToEditorMap(null);
        setHyperlinksTooltips(null);
        setComponentIDtoCellKeysMap(null);
        setOverlays(null);
        setMergedRegions(null);
        setCellComments(null);
        setCellCommentAuthors(null);
        setVisibleCellComments(null);
        setInvalidFormulaCells(null);

        popupButtons().detachAll();

        // clear all tables, possible tables for new/changed sheet are added
        // after first round trip.
        workbookScope.tables.markNeedsReload();

        reloadSheetNames();
        updateMergedRegions();
        setDisplayGridlines(getActiveSheet().isDisplayGridlines());
        setDisplayRowColHeadings(getActiveSheet().isDisplayRowColHeadings());

        markAsDirty();
    }

    void markAsDirty() {
        getElement().setProperty("dirty", System.currentTimeMillis());
    }

    /**
     * This method should be always called when the selected cell has changed so
     * proper actions can be triggered for possible custom component inside the
     * cell.
     */
    protected void loadCustomEditorOnSelectedCell() {
        // Guard against reentrancy: if user code in onCustomEditorDisplayed
        // calls refreshCells() -> updateMarkedCells() ->
        // reloadVisibleCellContents() -> loadCells() ->
        // loadCustomEditorOnSelectedCell(), skip the recursive call.
        if (insideCustomEditorCallback) {
            return;
        }
        CellReference selectedCellReference = selectionManager
                .getSelectedCellReference();
        SpreadsheetComponentFactory factory = componentFactory;
        if (selectedCellReference != null && factory != null) {
            final short col = selectedCellReference.getCol();
            final int row = selectedCellReference.getRow();
            final String key = SpreadsheetUtil.toKey(col + 1, row + 1);
            Component editor = cellKeyToEditor.get(key);
            if (editor != null) {
                if (key.equals(lastEditorCallbackKey)
                        && editor == lastEditorCallbackComponent) {
                    // Same cell, same editor: nothing was newly displayed, so
                    // the callback has already run for it.
                    return;
                }
                lastEditorCallbackKey = key;
                lastEditorCallbackComponent = editor;
                insideCustomEditorCallback = true;
                try {
                    factory.onCustomEditorDisplayed(getCell(row, col), row, col,
                            this, getActiveSheet(), editor);
                } catch (Exception e) {
                    LOGGER.warn(
                            "Error in onCustomEditorDisplayed for cell ({}, {})",
                            col + 1, row + 1, e);
                } finally {
                    insideCustomEditorCallback = false;
                }
                return;
            }
            // Selected cell has no custom editor: forget the last-fired
            // editor so returning to an editor cell fires the callback again.
            resetEditorCallbackTracker();
        }
    }

    void reloadSheetNames() {
        final ArrayList<String> sheetNamesList = new ArrayList<String>();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!workbook.isSheetVeryHidden(i) && !workbook.isSheetHidden(i)) {
                sheetNamesList.add(workbook.getSheetName(i));
            }
        }
        setSheetNames(
                sheetNamesList.toArray(new String[sheetNamesList.size()]));
        workbookScope.scrollMemory.publish();
    }

    /**
     * Returns POI model based index for the given Spreadsheet sheet index.
     *
     * @param visibleSheetIndex
     *            Index of the sheet within this Spreadsheet, 0-based
     * @return Index of the sheet within the POI model, or -1 if something went
     *         wrong. 0-based.
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
     * Gets the Spreadsheet sheet-index for the sheet at the given POI index.
     * Index will be returned for a visible sheet only.
     *
     * @param poiSheetIndex
     *            Index of the target sheet within the POI model, 0-based
     * @return Index of the target sheet in the Spreadsheet, 0-based
     */
    int getSpreadsheetSheetIndex(int poiSheetIndex) {
        int ourIndex = -1;
        for (int i = 0; i <= poiSheetIndex; i++) {
            if (!workbook.isSheetVeryHidden(i) && !workbook.isSheetHidden(i)) {
                ourIndex++;
            }
        }
        return ourIndex;
    }

    /**
     * Gets the protection state of the sheet at the given POI index.
     *
     * @param poiSheetIndex
     *            Index of the target sheet within the POI model, 0-based
     * @return true if the target {@link Sheet} is protected, false otherwise.
     */
    public boolean isSheetProtected(int poiSheetIndex) {
        return workbook.getSheetAt(poiSheetIndex).getProtect();
    }

    /**
     * Gets the protection state of the current sheet.
     *
     * @return true if the current {@link Sheet} is protected, false otherwise.
     */
    public boolean isActiveSheetProtected() {
        return isSheetProtected();
    }

    /**
     * Whether the workbook's structure is protected, so that its sheets are not
     * added or renamed from the client, which also offers neither.
     */
    boolean isWorkbookStructureLocked() {
        Workbook workbook = getWorkbook();
        if (workbook instanceof XSSFWorkbook xssf) {
            return xssf.isStructureLocked();
        }
        return workbook instanceof HSSFWorkbook hssf && hssf.isWriteProtected();
    }

    /**
     * Whether the user may resize the active sheet's columns, or its rows, from
     * the client: not on a protected sheet that locks their formatting, by the
     * rule the client resizes by.
     */
    boolean mayResizeFromClient(boolean columns) {
        if (!isActiveSheetProtected()
                || !(getActiveSheet() instanceof XSSFSheet xssf)) {
            return true;
        }
        CTSheetProtection protection = xssf.getCTWorksheet()
                .getSheetProtection();
        return protection == null || !(columns ? protection.getFormatColumns()
                : protection.getFormatRows());
    }

    /**
     * Gets the visibility state of the given cell.
     *
     * @param cell
     *            The cell to check
     * @return true if the cell is hidden, false otherwise
     */
    public boolean isCellHidden(Cell cell) {
        return isActiveSheetProtected() && cell.getCellStyle().getHidden();
    }

    /**
     * Returns whether or not the cell at the given address in the active sheet
     * is locked.
     *
     * @param cellAddress
     *            The address of the cell to check
     * @return true if the cell is locked, false otherwise
     * @since 24.8.8
     */
    public boolean isCellLocked(CellAddress cellAddress) {
        // Locking cells only works if the sheet is protected
        if (!isActiveSheetProtected()) {
            return false;
        }

        Sheet sheet = getActiveSheet();
        Row row = sheet.getRow(cellAddress.getRow());
        Cell cell = row != null ? row.getCell(cellAddress.getColumn()) : null;

        // If there is a cell with a custom cell style, return its locked state
        if (cell != null && cell.getCellStyle().getIndex() != 0) {
            return cell.getCellStyle().getLocked();
        }

        // Otherwise inherit locked state from row or column styles
        // If neither is unlocked, the locked state is inherited from the sheet
        CellStyle rowStyle = row != null ? row.getRowStyle() : null;
        CellStyle columnStyle = sheet.getColumnStyle(cellAddress.getColumn());
        boolean rowLocked = rowStyle == null || rowStyle.getLocked();
        boolean columnLocked = columnStyle == null || columnStyle.getLocked();

        return rowLocked && columnLocked;
    }

    /**
     * Gets the RPC proxy for communication to the client side.
     *
     * @return Client RPC proxy instance
     */
    protected SpreadsheetClientRpc getRpcProxy() {
        return clientRpc;
    }

    /**
     * Gets the currently used style factory for this Spreadsheet.
     *
     * @return The current style factory.
     */
    public SpreadsheetStyleFactory getSpreadsheetStyleFactory() {
        return workbookScope.styler;
    }

    /**
     * Note that modifications done directly with the POI {@link Workbook} API
     * will not get automatically updated into the Spreadsheet component.
     * <p>
     * Use {@link #markCellAsDeleted(Cell, boolean)},
     * {@link #markCellAsUpdated(Cell, boolean)}, or
     * {@link #reloadVisibleCellContents()} to update content.
     *
     * @return The currently presented workbook
     */
    public Workbook getWorkbook() {
        return workbook;
    }

    /**
     * Reloads the component with the given Workbook.
     *
     * @param workbook
     *            New workbook to load
     */
    public void setWorkbook(Workbook workbook) {
        if (workbook == null) {
            throw new NullPointerException(
                    "Cannot open a null workbook with Spreadsheet component.");
        }
        SpreadsheetFactory.reloadSpreadsheetComponent(this, workbook);
    }

    /**
     * Note that modifications done directly with the POI {@link Sheet} API will
     * not get automatically updated into the Spreadsheet component.
     * <p>
     * Use {@link #markCellAsDeleted(Cell, boolean)},
     * {@link #markCellAsUpdated(Cell, boolean)}, or
     * {@link #reloadVisibleCellContents()} to update content.
     *
     * @return The currently active (= visible) sheet
     */
    public Sheet getActiveSheet() {
        return workbook.getSheetAt(workbook.getActiveSheetIndex());
    }

    /**
     * Updates the given range of cells. Takes frozen panes in to account.
     *
     * NOTE: Does not run style updates!
     */
    void updateRowAndColumnRangeCellData(int r1, int c1, int r2, int c2) {
        // FIXME should be optimized, should not go through all links, comments
        // etc. always
        hyperlinkLoader.load();
        cellCommentLoader.load();
        sheetScope.overlays.loadOrUpdate();
        loadPopupButtons();
        // custom components not updated here on purpose

        valueManager.loadCellData(r1, c1, r2, c2);
    }

    /**
     * Sends data of the given cell area to client side. Data is only sent once,
     * unless there are changes. Cells with custom components are skipped.
     *
     * @param firstRow
     *            Index of the starting row, 1-based
     * @param firstColumn
     *            Index of the starting column, 1-based
     * @param lastRow
     *            Index of the ending row, 1-based
     * @param lastColumn
     *            Index of the ending column, 1-based
     */
    protected void loadCells(int firstRow, int firstColumn, int lastRow,
            int lastColumn) {
        loadCustomComponents();
        hyperlinkLoader.load();
        cellCommentLoader.load();
        sheetScope.overlays.loadOrUpdate();
        loadTables();
        loadPopupButtons();
        valueManager.loadCellData(firstRow, firstColumn, lastRow, lastColumn);

        loadCustomEditorOnSelectedCell();
    }

    void onLinkCellClick(int row, int column) {
        Cell cell = getActiveSheet().getRow(row - 1).getCell(column - 1);
        if (hyperlinkCellClickHandler != null) {
            hyperlinkCellClickHandler.onHyperLinkCellClick(cell,
                    cell.getHyperlink());
        }
    }

    /** The user resized rows, counted from 1, as the client counts them. */
    void onRowResized(Map<Integer, Float> newRowSizes) {
        sheetSizes.onClientRowsResized(newRowSizes);
    }

    /**
     * Sets the row height for currently active sheet. Updates both POI model
     * and the visible sheet.
     *
     * @param index
     *            Index of target row, 0-based
     * @param height
     *            New row height in points
     */
    public void setRowHeight(int index, float height) {
        sheetSizes.setRowHeight(index, height);
    }

    /**
     * The user resized columns, counted from 1, as the client counts them.
     */
    void onColumnResized(Map<Integer, Integer> newColumnSizes) {
        sheetSizes.onClientColumnsResized(newColumnSizes);
    }

    /**
     * Sets the column width in pixels (using conversion) for the currently
     * active sheet. Updates both POI model and the visible sheet.
     *
     * @param index
     *            Index of target column, 0-based
     * @param width
     *            New column width in pixels
     */
    public void setColumnWidth(int index, int width) {
        sheetSizes.setColumnWidth(index, width);
    }

    void loadHyperLinks() {
        hyperlinkLoader.load();
    }

    /**
     * Loads the custom components for the currently viewed cells and clears
     * previous components that are not currently visible. A cell that already
     * has a custom editor keeps it, so its state survives the re-render; see
     * {@link #loadCustomComponentsRecreatingEditors()} to ask the factory for
     * new ones.
     */
    void loadCustomComponents() {
        sheetScope.components.load(false);
    }

    /**
     * Reload the custom components, asking the factory for fresh editors rather
     * than reusing the ones already shown. See
     * {@link CustomComponentLoader#load(boolean)}.
     */
    void loadCustomComponentsRecreatingEditors() {
        sheetScope.components.load(true);
    }

    static String getComponentNodeId(Component component) {
        return Integer.toString(component.getElement().getNode().getId());
    }

    /**
     * Determines if the cell at the given coordinates is currently visible
     * (rendered) in the browser.
     *
     * @param row
     *            Row index, 1-based
     * @param col
     *            Column index, 1-based
     *
     * @return True if the cell is visible, false otherwise
     */
    /** Scans the cells shown, pane by pane. */
    void forEachPaneShown(ViewportBounds.PaneScan scan) {
        sheetScope.viewport.forEachPane(getLastFrozenRow(),
                getLastFrozenColumn(), scan);
    }

    boolean isCellVisible(int row, int col) {
        int verticalSplitPosition = getLastFrozenRow();
        int horizontalSplitPosition = getLastFrozenColumn();
        int firstRow = sheetScope.viewport.getFirstRow();
        int lastRow = sheetScope.viewport.getLastRow();
        int firstColumn = sheetScope.viewport.getFirstColumn();
        int lastColumn = sheetScope.viewport.getLastColumn();
        return (col >= firstColumn && col <= lastColumn && row >= firstRow
                && row <= lastRow)
                || (col >= 1 && col <= horizontalSplitPosition && row >= 1
                        && row <= verticalSplitPosition)
                || (col >= firstColumn && col <= lastColumn && row >= 1
                        && row <= verticalSplitPosition)
                || (col >= 1 && col <= horizontalSplitPosition
                        && row >= firstRow && row <= lastRow);
    }

    void registerCustomComponent(Component component) {
        if (!getElement().equals(component.getElement().getParent())) {
            getElement().appendVirtualChild(component.getElement());
        }
    }

    void unRegisterCustomComponent(Component component) {
        Element element = component.getElement();
        if (element.isVirtualChild()
                && getElement().equals(element.getParent())) {
            getElement().removeVirtualChild(element);
        }
    }

    /**
     * Set a new component factory for this Spreadsheet. If a {@link Workbook}
     * has been set, all components will be reloaded.
     *
     * @param customComponentFactory
     *            The new component factory to use.
     * @since 24.0
     */
    public void setSpreadsheetComponentFactory(
            SpreadsheetComponentFactory customComponentFactory) {
        componentFactory = customComponentFactory;
        // The factory is asked about every cell loaded: the client then
        // loads only the cells it draws.
        hasComponentFactory.set(customComponentFactory != null);
        if (sheetScope.viewport.getFirstRow() != -1) {
            // A new factory produces its own editors, so recreate them rather
            // than reuse the previous factory's by cell key.
            loadCustomComponentsRecreatingEditors();
            loadCustomEditorOnSelectedCell();
        } else {
            sheetScope.components.unloadAll();
        }
    }

    /**
     * Gets the current SpreadsheetComponentFactory.
     *
     * @return The currently used component factory.
     */
    SpreadsheetComponentFactory getSpreadsheetComponentFactory() {
        return componentFactory;
    }

    /**
     * Sets a pop-up button to the given cell in the currently active sheet. If
     * there is already a pop-up button in the given cell, it will be replaced.
     * <p>
     * Note that if the active sheet is changed, all pop-up buttons are removed
     * from the spreadsheet.
     *
     * @param cellAddress
     *            address to the target cell, e.g. "C3"
     * @param popupButton
     *            PopupButton to set for the target cell. Passing null here
     *            removes the pop-up button for the target cell.
     */
    public void setPopup(String cellAddress, PopupButton popupButton) {
        setPopup(new CellReference(cellAddress), popupButton);
    }

    /**
     * Sets a pop-up button to the given cell in the currently active sheet. If
     * there is already a pop-up button in the given cell, it will be replaced.
     * <p>
     * Note that if the active sheet is changed, all pop-up buttons are removed
     * from the spreadsheet.
     *
     * @param row
     *            Row index of target cell, 0-based
     * @param col
     *            Column index of target cell, 0-based
     * @param popupButton
     *            PopupButton to set for the target cell. Passing null here
     *            removes the pop-up button for the target cell.
     */
    public void setPopup(int row, int col, PopupButton popupButton) {
        setPopup(new CellReference(getActiveSheet().getSheetName(), row, col,
                false, false), popupButton);
    }

    /**
     * Sets a pop-up button to the given cell in the currently active sheet. If
     * there is already a pop-up button in the given cell, it will be replaced.
     * <p>
     * Note that if the active sheet is changed, all pop-up buttons are removed
     * from the spreadsheet.
     *
     * @param cellReference
     *            Reference to the target cell
     * @param popupButton
     *            PopupButton to set for the target cell. Passing null here
     *            removes the pop-up button for the target cell.
     */
    public void setPopup(CellReference cellReference, PopupButton popupButton) {
        popupButtons().set(
                SpreadsheetUtil.relativeToAbsolute(this, cellReference),
                popupButton);
    }

    /**
     * Registers and unregister pop-up button components for the currently
     * visible cells.
     */
    void loadPopupButtons() {
        popupButtons().refresh();
    }

    /**
     * Registers the given table to this Spreadsheet, meaning that this table
     * will be reloaded when the active sheet changes to the sheet containing
     * the table.
     * <p>
     * Populating the table content (pop-up button and other content) is the
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
     *            The table to register
     */
    public void registerTable(SpreadsheetTable table) {
        workbookScope.tables.add(table);
        if (table instanceof SpreadsheetFilterTable filterTable) {
            sheetSizes.refreshAutofittedColumnsInTable(filterTable);
        }
    }

    /**
     * Unregisters the given table from this Spreadsheet - it will no longer get
     * reloaded when the sheet is changed back to the sheet containing the
     * table. This does not delete any table content, use
     * {@link #deleteTable(SpreadsheetTable)} to completely remove the table.
     * <p>
     * See {@link #registerTable(SpreadsheetTable)}.
     *
     * @param table
     *            The table to unregister
     */
    public void unregisterTable(SpreadsheetTable table) {
        workbookScope.tables.remove(table);
    }

    /**
     * Deletes the given table: removes it from "memory" (see
     * {@link #registerTable(SpreadsheetTable)}), clears and removes all
     * possible filters (if table is a {@link SpreadsheetFilterTable}), and
     * clears all table pop-up buttons and content.
     *
     * @param table
     *            The table to delete
     */
    public void deleteTable(SpreadsheetTable table) {
        // Through the component, so an override of it applies.
        unregisterTable(table);
        if (!table.isTableSheetCurrentlyActive()) {
            return;
        }
        for (PopupButton popupButton : table.getPopupButtons()) {
            setPopup(popupButton.getCellReference(), null);
        }
        if (table instanceof SpreadsheetFilterTable filterTable) {
            filterTable.clearAllFilters();
        }
        table.clear();
    }

    /**
     * Gets all the tables that have been registered to this Spreadsheet. See
     * {@link #registerTable(SpreadsheetTable)}.
     *
     * @return All tables for this spreadsheet
     */
    public HashSet<SpreadsheetTable> getTables() {
        return workbookScope.tables.getAll();
    }

    /**
     * Gets the tables that belong to the currently active sheet (
     * {@link #getActiveSheet()}). See {@link #registerTable(SpreadsheetTable)}.
     *
     * @return All tables for the currently active sheet
     */
    public List<SpreadsheetTable> getTablesForActiveSheet() {
        return workbookScope.tables.getForSheet(getActiveSheet());
    }

    /**
     * Reload tables for current sheet
     */
    private void loadTables() {
        workbookScope.tables.reloadForSheet(getActiveSheet());
    }

    /**
     * The text a copy puts on the clipboard for a range of the selection: the
     * values of its cells as they are shown, as the client writes the cells it
     * has. The selected cells are not sent to the client in case they are
     * copied, as they were: one column of 100,000 rows sent 10 MB, and
     * selecting everything kept the server busy for seconds.
     *
     * @param col1
     *            first column of the range, 1-based
     * @param row1
     *            first row of the range, 1-based
     * @param col2
     *            last column of the range, 1-based
     * @param row2
     *            last row of the range, 1-based
     * @return the text, or {@code null} for a range not selected
     */
    @ClientCallable
    String clipboardText(int col1, int row1, int col2, int row2) {
        if (col2 < col1 || row2 < row1 || !selectionManager.isSelectedRange(
                new CellRangeAddress(row1 - 1, row2 - 1, col1 - 1, col2 - 1))) {
            return null;
        }
        Sheet sheet = getActiveSheet();
        return ClipboardText.write(row2 - row1 + 1, col2 - col1 + 1,
                (row, column) -> {
                    Row poiRow = sheet.getRow(row1 - 1 + row);
                    Cell cell = poiRow == null ? null
                            : poiRow.getCell(col1 - 1 + column);
                    return cell == null ? null
                            : valueManager.createCellDataForCell(cell).value;
                });
    }

    /**
     * Returns the formatted value for the given cell, using the
     * {@link DataFormatter} with the current locale.
     *
     * See
     * {@link DataFormatter#formatCellValue(Cell, FormulaEvaluator, ConditionalFormattingEvaluator)}.
     *
     * @param cell
     *            Cell to get the value from
     * @return Formatted value
     */
    public final String getCellValue(Cell cell) {
        return valueManager.getDataFormatter().formatCellValue(cell,
                valueManager.getFormulaEvaluator(),
                getConditionalFormattingEvaluator());
    }

    /**
     * Gets grid line visibility for the currently active sheet.
     *
     * @return True if grid lines are visible, false if they are hidden
     */
    public boolean isGridlinesVisible() {
        if (getActiveSheet() != null) {
            return getActiveSheet().isDisplayGridlines();
        }
        return true;
    }

    /**
     * Sets grid line visibility for the currently active sheet.
     *
     * @param visible
     *            True to show grid lines, false to hide them
     */
    public void setGridlinesVisible(boolean visible) {
        if (getActiveSheet() == null) {
            throw new NullPointerException("no active sheet");
        }
        getActiveSheet().setDisplayGridlines(visible);
        setDisplayGridlines(visible);
    }

    /**
     * Gets row and column heading visibility for the currently active sheet.
     *
     * @return true if headings are visible, false if they are hidden
     */
    public boolean isRowColHeadingsVisible() {
        if (getActiveSheet() != null) {
            return getActiveSheet().isDisplayRowColHeadings();
        }
        return true;
    }

    /**
     * Sets row and column heading visibility for the currently active sheet.
     *
     * @param visible
     *            true to show headings, false to hide them
     */
    public void setRowColHeadingsVisible(boolean visible) {
        if (getActiveSheet() == null) {
            throw new NullPointerException("no active sheet");
        }
        getActiveSheet().setDisplayRowColHeadings(visible);
        setDisplayRowColHeadings(visible);
    }

    /**
     * This is a parent class for value change events.
     */
    public abstract static class ValueChangeEvent
            extends ComponentEvent<Component> {
        private final Set<CellReference> changedCells;

        public ValueChangeEvent(Component source,
                Set<CellReference> changedCells) {
            super(source, false);
            this.changedCells = new CellSet(changedCells);
        }

        /**
         * Gets the changed cells.
         * <p>
         * When using {@link Set#contains(Object)}, you should only use
         * {@link CellReference}s with sheet names. Otherwise, it will throw an
         * {@link IllegalArgumentException}.
         *
         * @return the changed cells
         * @see CellSet
         */
        public Set<CellReference> getChangedCells() {
            return changedCells;
        }
    }

    /**
     * This event is fired when cell value changes.
     */
    public static class CellValueChangeEvent extends ValueChangeEvent {

        public CellValueChangeEvent(Component source,
                Set<CellReference> changedCells) {
            super(source, changedCells);
        }

    }

    /**
     * This event is fired when the value of a cell referenced by a formula cell
     * changes making the formula value change
     */
    public static class FormulaValueChangeEvent extends ValueChangeEvent {

        public FormulaValueChangeEvent(Component source,
                Set<CellReference> changedCells) {
            super(source, changedCells);
        }
    }

    /**
     * This event is fired when cell selection changes.
     */
    public static class SelectionChangeEvent extends ComponentEvent<Component> {

        private final CellReference selectedCellReference;
        private final List<CellReference> individualSelectedCells;
        private final CellRangeAddress selectedCellMergedRegion;
        private final List<CellRangeAddress> cellRangeAddresses;

        /**
         * Creates a new selection change event.
         *
         * @param source
         *            Source Spreadsheet
         * @param selectedCellReference
         *            see {@link #getSelectedCellReference()}
         * @param individualSelectedCells
         *            see {@link #getIndividualSelectedCells()}
         * @param selectedCellMergedRegion
         *            see {@link #getSelectedCellMergedRegion()}
         * @param cellRangeAddresses
         *            see {@link #getCellRangeAddresses()}
         */
        public SelectionChangeEvent(Component source,
                CellReference selectedCellReference,
                List<CellReference> individualSelectedCells,
                CellRangeAddress selectedCellMergedRegion,
                List<CellRangeAddress> cellRangeAddresses) {
            super(source, false);
            this.selectedCellReference = selectedCellReference;
            this.individualSelectedCells = individualSelectedCells;
            this.selectedCellMergedRegion = selectedCellMergedRegion;
            this.cellRangeAddresses = cellRangeAddresses;
        }

        /**
         * Gets the Spreadsheet where this event happened.
         *
         * @return Source Spreadsheet
         */
        public Spreadsheet getSpreadsheet() {
            return (Spreadsheet) getSource();
        }

        /**
         * Returns reference to the currently selected single cell OR in case of
         * multiple selections the last cell clicked OR in case of area select
         * the cell from which the area selection was started.
         *
         * @return CellReference to the single selected cell, or the last cell
         *         selected manually (e.g. with ctrl+mouseclick)
         */
        public CellReference getSelectedCellReference() {
            return selectedCellReference;
        }

        /**
         * Gets all the individually selected single cells in the current
         * selection.
         *
         * @return All non-contiguously selected cells (e.g. with
         *         ctrl+mouseclick)
         */
        public List<CellReference> getIndividualSelectedCells() {
            return individualSelectedCells;
        }

        /**
         * Gets the merged region the single selected cell is a part of, if
         * applicable.
         *
         * @return The {@link CellRangeAddress} described the merged region the
         *         single selected cell is part of, if any.
         */
        public CellRangeAddress getSelectedCellMergedRegion() {
            return selectedCellMergedRegion;
        }

        /**
         * Gets all separately selected cell ranges.
         *
         * @return All separately selected cell ranges (e.g. with
         *         ctrl+shift+mouseclick)
         */
        public List<CellRangeAddress> getCellRangeAddresses() {
            return cellRangeAddresses;
        }

        /**
         * Gets a combination of all selected cells.
         * <p>
         * When using {@link Set#contains(Object)}, you should only use
         * {@link CellReference}s with sheet names. Otherwise, it will throw an
         * {@link IllegalArgumentException}.
         *
         * @return A combination of all selected cells, regardless of selection
         *         mode. Doesn't contain duplicates.
         * @see CellSet
         */
        public Set<CellReference> getAllSelectedCells() {
            return new CellSet(
                    Spreadsheet.getAllSelectedCells(selectedCellReference,
                            individualSelectedCells, cellRangeAddresses));
        }
    }

    private static Set<CellReference> getAllSelectedCells(
            CellReference selectedCellReference,
            List<CellReference> individualSelectedCells,
            List<CellRangeAddress> cellRangeAddresses) {
        Set<CellReference> cells = new HashSet<>(individualSelectedCells);
        cells.add(selectedCellReference);

        if (cellRangeAddresses != null) {
            for (CellRangeAddress a : cellRangeAddresses) {

                for (int x = a.getFirstColumn(); x <= a.getLastColumn(); x++) {
                    for (int y = a.getFirstRow(); y <= a.getLastRow(); y++) {
                        cells.add(new CellReference(
                                selectedCellReference.getSheetName(), y, x,
                                false, false));
                    }
                }
            }
        }
        return cells;
    }

    /**
     * Used for knowing when a user has changed the cell selection in any way.
     */
    public interface SelectionChangeListener extends Serializable {
        public static final Method SELECTION_CHANGE_METHOD = ReflectTools
                .findMethod(SelectionChangeListener.class, "onSelectionChange",
                        SelectionChangeEvent.class);

        /**
         * This is called when user changes cell selection.
         *
         * @param event
         *            SelectionChangeEvent that happened
         */
        public void onSelectionChange(SelectionChangeEvent event);
    }

    /**
     * Used for knowing when a user has changed the cell value in Spreadsheet
     * UI.
     */
    public interface CellValueChangeListener extends Serializable {
        public static final Method CELL_VALUE_CHANGE_METHOD = ReflectTools
                .findMethod(CellValueChangeListener.class, "onCellValueChange",
                        CellValueChangeEvent.class);

        /**
         * This is called when user changes the cell value in Spreadsheet.
         *
         * @param event
         *            CellValueChangeEvent that happened
         */
        public void onCellValueChange(CellValueChangeEvent event);
    }

    /**
     * Used for knowing when a cell referenced by a formula cell has changed in
     * the Spreadsheet UI making the formula value change
     */
    public interface FormulaValueChangeListener extends Serializable {
        public static final Method FORMULA_VALUE_CHANGE_METHOD = ReflectTools
                .findMethod(FormulaValueChangeListener.class,
                        "onFormulaValueChange", FormulaValueChangeEvent.class);

        /**
         * This is called when user changes the cell value in Spreadsheet.
         *
         * @param event
         *            FormulaValueChangeEvent that happened
         */
        public void onFormulaValueChange(FormulaValueChangeEvent event);
    }

    /**
     * Adds the given SelectionChangeListener to this Spreadsheet.
     *
     * @param listener
     *            Listener to add.
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addSelectionChangeListener(
            SelectionChangeListener listener) {
        return addListener(SelectionChangeEvent.class,
                listener::onSelectionChange);
    }

    /**
     * Adds the given CellValueChangeListener to this Spreadsheet.
     *
     * @param listener
     *            Listener to add.
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addCellValueChangeListener(
            CellValueChangeListener listener) {
        return addListener(CellValueChangeEvent.class,
                listener::onCellValueChange);
    }

    /**
     * Adds the given FormulaValueChangeListener to this Spreadsheet.
     *
     * @param listener
     *            Listener to add.
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addFormulaValueChangeListener(
            FormulaValueChangeListener listener) {
        return addListener(FormulaValueChangeEvent.class,
                listener::onFormulaValueChange);
    }

    /**
     * An event that is fired when an attempt to modify a locked cell has been
     * made.
     */
    public static class ProtectedEditEvent extends ComponentEvent<Component> {

        public ProtectedEditEvent(Component source) {
            super(source, false);
        }
    }

    /**
     * A listener for when an attempt to modify a locked cell has been made.
     */
    public interface ProtectedEditListener extends Serializable {
        public static final Method SELECTION_CHANGE_METHOD = ReflectTools
                .findMethod(ProtectedEditListener.class, "writeAttempted",
                        ProtectedEditEvent.class);

        /**
         * Called when the SpreadSheet detects that the client tried to edit a
         * locked cell (usually by pressing a key). Method is not called for
         * each such event; instead, the SpreadSheet waits a second before
         * sending a new event. This is done to give the user time to react to
         * the results of this call (e.g. showing a notification).
         *
         * @param event
         *            ProtectedEditEvent that happened
         */
        public void writeAttempted(ProtectedEditEvent event);
    }

    /**
     * Add listener for when an attempt to modify a locked cell has been made.
     *
     * @param listener
     *            The listener to add.
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addProtectedEditListener(
            ProtectedEditListener listener) {
        return addListener(ProtectedEditEvent.class, listener::writeAttempted);
    }

    /**
     * Creates or removes a freeze pane from the currently active sheet.
     *
     * If both colSplit and rowSplit are zero then the existing freeze pane is
     * removed.
     *
     * @param rowSplit
     *            Vertical position of the split, 1-based row index
     * @param colSplit
     *            Horizontal position of the split, 1-based column index
     */
    public void createFreezePane(int rowSplit, int colSplit) {
        getActiveSheet().createFreezePane(colSplit, rowSplit);
        SpreadsheetFactory.loadFreezePane(this);
        reloadActiveSheetData();
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
            reloadActiveSheetData();
        }
    }

    /**
     * Gets a reference to the current single selected cell.
     *
     * @return Reference to the currently selected single cell.
     *         <p>
     *         <em>NOTE:</em> other cells might also be selected: use
     *         {@link #addSelectionChangeListener(SelectionChangeListener)} to
     *         get notified for all selection changes or call
     *         {@link #getSelectedCellReferences()}.
     */
    public CellReference getSelectedCellReference() {
        return selectionManager.getSelectedCellReference();
    }

    /**
     * Gets all the currently selected cells.
     *
     * @return References to all currently selected cells.
     */
    public Set<CellReference> getSelectedCellReferences() {
        SelectionChangeEvent event = selectionManager.getLatestSelectionEvent();
        if (event == null) {
            return new HashSet<>();
        }
        return event.getAllSelectedCells();
    }

    /**
     * An event that is fired to registered listeners when the selected sheet
     * has been changed.
     */
    public static class SheetChangeEvent extends ComponentEvent<Component> {

        private final Sheet newSheet;
        private final Sheet previousSheet;
        private final int newSheetVisibleIndex;
        private final int newSheetPOIIndex;

        /**
         * Creates a new SheetChangeEvent.
         *
         * @param source
         *            Spreadsheet that triggered the event
         * @param newSheet
         *            New selection
         * @param previousSheet
         *            Previous selection
         * @param newSheetVisibleIndex
         *            New visible index of selection
         * @param newSheetPOIIndex
         *            New POI index of selection
         */
        public SheetChangeEvent(Component source, Sheet newSheet,
                Sheet previousSheet, int newSheetVisibleIndex,
                int newSheetPOIIndex) {
            super(source, false);
            this.newSheet = newSheet;
            this.previousSheet = previousSheet;
            this.newSheetVisibleIndex = newSheetVisibleIndex;
            this.newSheetPOIIndex = newSheetPOIIndex;
        }

        /**
         * Gets the newly selected sheet.
         *
         * @return The new selection
         */
        public Sheet getNewSheet() {
            return newSheet;
        }

        /**
         * Gets the sheet that was previously selected.
         *
         * @return The previous selection
         */
        public Sheet getPreviousSheet() {
            return previousSheet;
        }

        /**
         * Gets the index of the newly selected sheet among all visible sheets.
         *
         * @return Index of new selection among visible sheets
         */
        public int getNewSheetVisibleIndex() {
            return newSheetVisibleIndex;
        }

        /**
         * Gets the POI index of the newly selected sheet.
         *
         * @return POI index of new selection
         */
        public int getNewSheetPOIIndex() {
            return newSheetPOIIndex;
        }
    }

    /**
     * A listener for when a sheet is selected.
     */
    public interface SheetChangeListener extends Serializable {
        public static final Method SHEET_CHANGE_METHOD = ReflectTools
                .findMethod(SheetChangeListener.class, "onSheetChange",
                        SheetChangeEvent.class);

        /**
         * This method is called an all registered listeners when the selected
         * sheet has changed.
         *
         * @param event
         *            Sheet selection event
         */
        public void onSheetChange(SheetChangeEvent event);
    }

    /**
     * Adds the given SheetChangeListener to this Spreadsheet.
     *
     * @param listener
     *            Listener to add
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addSheetChangeListener(SheetChangeListener listener) {
        return addListener(SheetChangeEvent.class, listener::onSheetChange);
    }

    void fireSheetChangeEvent(Sheet previousSheet, Sheet newSheet) {
        int newSheetPOIIndex = workbook.getActiveSheetIndex();

        fireEvent(new SheetChangeEvent(this, newSheet, previousSheet,
                getSpreadsheetSheetIndex(newSheetPOIIndex), newSheetPOIIndex));
    }

    /**
     * This is called when the client-side connector has been initialized.
     */
    protected void onConnectorInit() {
        reloadCellDataOnNextScroll = true;
        valueManager.clearCachedContent();
        popupButtons().forgetClient();
        clientCells.reload();
    }

    /**
     * Reloads all data from the current spreadsheet and performs a full
     * re-render.
     * <p>
     * Functionally same as calling {@link #setWorkbook(Workbook)} with
     * {@link #getWorkbook()} parameter.
     */
    public void reload() {
        setWorkbook(getWorkbook());
    }

    /**
     * Sets the content of the status label.
     *
     * @param value
     *            The new content. Can not be HTML.
     */
    public void setStatusLabelValue(String value) {
        setInfoLabelValue(value);
    }

    /**
     * Gets the content of the status label
     *
     * @return Current content of the status label.
     */
    public String getStatusLabelValue() {
        return getInfoLabelValue();
    }

    /**
     * Selects the cell at the given coordinates
     *
     * @param row
     *            Row index, 0-based
     * @param col
     *            Column index, 0-based
     */
    public void setSelection(int row, int col) {
        setSelectionRange(row, col, row, col);
    }

    /**
     * Selects the given range, using the cell at row1 and col1 as an anchor.
     *
     * @param row1
     *            Index of the first row of the area, 0-based
     * @param col1
     *            Index of the first column of the area, 0-based
     * @param row2
     *            Index of the last row of the area, 0-based
     * @param col2
     *            Index of the last column of the area, 0-based
     */
    public void setSelectionRange(int row1, int col1, int row2, int col2) {
        CellRangeAddress cra = new CellRangeAddress(row1, row2, col1, col2);
        selectionManager.handleCellRangeSelection(cra);
    }

    /**
     * Selects the cell(s) at the given coordinates
     *
     * Coordinates can be simple "A1" style addresses or ranges, named ranges,
     * or a formula. Note that scatter charts, if present, use formulas that may
     * contain named ranges.
     *
     * @param selectionRange
     *            The wanted range, e.g. "A3" or "B3:C5"
     */
    public void setSelection(String selectionRange) {
        selectionManager.handleCellRangeSelection(SpreadsheetUtil
                .getRangeForReference(selectionRange, this, true));
    }

    /**
     * Gets the ConditionalFormatter
     *
     * @return the {@link ConditionalFormatter} used by this {@link Spreadsheet}
     */
    public ConditionalFormatter getConditionalFormatter() {
        return workbookScope.conditionalFormatter;
    }

    /**
     * Disposes the current {@link Workbook}, if any, and loads a new empty XSLX
     * Workbook.
     *
     * Note: Discards all data. Be sure to write out the old Workbook if needed.
     */
    public void reset() {
        SpreadsheetFactory.loadNewXLSXSpreadsheet(this);
    }

    private CommentAuthorProvider commentAuthorProvider;

    /**
     * Returns the formatting string that is used when a user enters percentages
     * into the Spreadsheet.
     * <p>
     * Default is "0.00%".
     *
     * @return The formatting applied to percentage values when entered by the
     *         user
     */
    public String getDefaultPercentageFormat() {
        return defaultPercentageFormat;
    }

    /**
     * Sets the formatting string that is used when a user enters percentages
     * into the Spreadsheet.
     * <p>
     * Default is "0.00%".
     */
    public void setDefaultPercentageFormat(String defaultPercentageFormat) {
        this.defaultPercentageFormat = defaultPercentageFormat;
    }

    /**
     * This interface can be implemented to provide the comment author name set
     * to new comments in cells.
     */
    public interface CommentAuthorProvider extends Serializable {

        /**
         * Gets the author name for a new comment about to be added to the cell
         * at the given cell reference.
         *
         * @param targetCell
         *            Reference to the target cell
         * @return Comment author name
         */
        public String getAuthorForComment(CellReference targetCell);
    }

    /**
     * Sets the given CommentAuthorProvider to this Spreadsheet.
     *
     * @param commentAuthorProvider
     *            New provider
     */
    public void setCommentAuthorProvider(
            CommentAuthorProvider commentAuthorProvider) {
        this.commentAuthorProvider = commentAuthorProvider;
    }

    /**
     * Gets the CommentAuthorProvider currently set to this Spreadsheet.
     *
     * @return Current provider or null if not set.
     */
    public @Nullable CommentAuthorProvider getCommentAuthorProvider() {
        return commentAuthorProvider;
    }

    /**
     * Triggers editing of the cell comment in the given cell reference. Note
     * that the cell must have a previously set cell comment in order to be able
     * to edit it.
     *
     * @param cr
     *            Reference to the cell containing the comment to edit
     */
    public void editCellComment(CellReference cr) {
        getRpcProxy().editCellComment(cr.getCol(), cr.getRow());
    }

    /**
     * Sets the visibility of the top function bar. By default the bar is
     * visible.
     *
     * @param functionBarVisible
     *            True to show the top bar, false to hide it.
     */
    public void setFunctionBarVisible(boolean functionBarVisible) {
        if (functionBarVisible) {
            removeClassName(HIDE_FUNCTION_BAR_STYLE);
        } else {
            addClassName(HIDE_FUNCTION_BAR_STYLE);
        }
    }

    /**
     * Gets the visibility of the top function bar. By default the bar is
     * visible.
     *
     * @return True if the function bar is visible, false otherwise.
     */
    public boolean isFunctionBarVisible() {
        return !getClassNames().contains(HIDE_FUNCTION_BAR_STYLE);
    }

    /**
     * Sets the visibility of the bottom sheet selection bar. By default the bar
     * is visible.
     *
     * @param sheetSelectionBarVisible
     *            True to show the sheet selection bar, false to hide it.
     */
    public void setSheetSelectionBarVisible(boolean sheetSelectionBarVisible) {
        if (sheetSelectionBarVisible) {
            removeClassName(HIDE_TABSHEET_STYLE);
        } else {
            addClassName(HIDE_TABSHEET_STYLE);
        }
    }

    /**
     * Gets the visibility of the bottom sheet selection bar. By default the bar
     * is visible.
     *
     * @return True if the sheet selection bar is visible, false otherwise.
     */
    public boolean isSheetSelectionBarVisible() {
        return !getClassNames().contains(HIDE_TABSHEET_STYLE);
    }

    /**
     * Enables or disables the report style. When enabled, the top and bottom
     * bars of Spreadsheet will be hidden.
     *
     * @param reportStyle
     *            True to hide both toolbars, false to show them.
     */
    public void setReportStyle(boolean reportStyle) {
        setFunctionBarVisible(!reportStyle);
        setSheetSelectionBarVisible(!reportStyle);
    }

    /**
     * Gets the state of the report style.
     *
     * @return True if report style is enabled, false otherwise.
     */
    public boolean isReportStyle() {
        return !isSheetSelectionBarVisible() && !isFunctionBarVisible();
    }

    public void setInvalidFormulaErrorMessage(
            String invalidFormulaErrorMessage) {
        getElement().setProperty("invalidFormulaErrorMessage",
                invalidFormulaErrorMessage);
    }

    /**
     * Controls if a column group is collapsed or not.
     *
     * @param isCols
     *            <code>true</code> when collapsing columns, <code>false</code>
     *            when collapsing rows
     * @param index
     *            A column that is part of the group, 0-based
     * @param collapsed
     *            If the group should be collapsed or not
     */
    protected void setGroupingCollapsed(boolean isCols, int index,
            boolean collapsed) {

        XSSFSheet activeSheet = (XSSFSheet) getActiveSheet();
        if (isCols) {
            if (collapsed) {
                GroupingUtil.collapseColumn(activeSheet, index);
            } else {
                short expandLevel = GroupingUtil.expandColumn(activeSheet,
                        index);
                updateExpandedRegion(activeSheet, index, expandLevel);
            }
        } else {
            if (collapsed) {
                GroupingUtil.collapseRow(activeSheet, index);
            } else {
                GroupingUtil.expandRow(activeSheet, index);
            }
        }
        SpreadsheetFactory.loadGrouping(this);
        // The rows and columns the group hid or showed are read again.
        sheetSizes.groupToggled();
    }

    private void updateExpandedRegion(XSSFSheet sheet, int columnIndex,
            int expandLevel) {
        if (expandLevel < 0) {
            return;
        }
        int endIndex = -1;
        for (GroupingData data : getColGroupingData()) {
            if (data.level == expandLevel && data.startIndex <= columnIndex
                    && columnIndex <= data.endIndex) {
                endIndex = data.endIndex;
                break;
            }
        }
        if (endIndex < 0) {
            return;
        }
        // update the style for the region cells, effects region + 1 row&col
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        for (int r = firstRowNum; r <= lastRowNum; r++) {
            Row row = sheet.getRow(r);
            if (row != null) {
                for (int c = columnIndex; c <= endIndex; c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null) {
                        valueManager.markCellForUpdate(cell);
                    }
                }
            }
        }
    }

    /**
     * Called when a grouping level header is clicked
     *
     * @param isCols
     *            true if the user clicked on cols, false for row level headers
     * @param level
     *            which level the user clicked
     */
    protected void levelHeaderClicked(boolean isCols, int level) {

        /*
         * A click on a header should change groupings so that all levels above
         * the selected are expanded, and the selected level is all collapsed
         * (which hides any levels underneath this).
         */

        if (!(getActiveSheet() instanceof XSSFSheet xsheet)) {
            return;
        }
        CTWorksheet ctWorksheet = xsheet.getCTWorksheet();

        if (isCols) {

            CTCols ctCols = ctWorksheet.getColsList().get(0);
            List<CTCol> colList = ctCols.getColList();
            for (CTCol col : colList) {
                short l = col.getOutlineLevel();

                // It's a lot easier to not call expand/collapse

                if (l >= 0 && l < level) {
                    // expand
                    if (col.isSetHidden()) {
                        col.unsetHidden();
                    }
                } else {
                    // collapse
                    col.setHidden(true);
                }
            }

        } else {

            /*
             * Groups are more complicated than cols, use existing
             * collapse/expand functionality.
             */

            int lastlevel = 0;
            for (int i = 0; i < getRows(); i++) {

                XSSFRow row = xsheet.getRow(i);
                if (row == null) {
                    lastlevel = 0;
                    continue;
                }

                short l = row.getCTRow().getOutlineLevel();
                if (l != lastlevel) {

                    // group starts here

                    int end = (int) GroupingUtil.findEndOfRowGroup(this, i, l);
                    long uniqueIndex = GroupingUtil.findUniqueRowIndex(this, i,
                            end, l);

                    if (l > 0 && l < level) {
                        // expand
                        GroupingUtil.expandRow(xsheet, (int) uniqueIndex);

                    } else if (l >= level) {
                        // collapse
                        GroupingUtil.collapseRow(xsheet, (int) uniqueIndex);
                    }

                    lastlevel = l;
                }

            }

        }

        SpreadsheetFactory.reloadSpreadsheetComponent(this, workbook);
    }

    /**
     * Flags the cell as holding a formula the server could not parse, which is
     * what draws the invalid-formula marker on it.
     * <p>
     * The undo commands put the flag back on a cell whose text they restore,
     * through {@link InvalidFormulaMarks}: the flag lives beside the workbook
     * rather than in it, so restoring the text alone would leave a broken cell
     * looking fine.
     *
     * @param col
     *            column index, 1-based
     * @param row
     *            row index, 1-based
     */
    void markInvalidFormula(int col, int row) {
        invalidFormulas().mark(col, row);
    }

    /**
     * Whether the cell holds a formula the server could not parse.
     *
     * @param col
     *            column index, 1-based
     * @param row
     *            row index, 1-based
     * @return {@code true} when the cell carries the invalid-formula flag
     * @see #markInvalidFormula(int, int)
     */
    boolean isMarkedAsInvalidFormula(int col, int row) {
        return invalidFormulas().isMarked(col, row);
    }

    void removeInvalidFormulaMark(int col, int row) {
        invalidFormulas().removeMark(col, row);
    }

    public void addSheetOverlay(SheetOverlayWrapper image) {
        sheetScope.overlays.add(image);
    }

    /**
     * Get the minimum row height in points for the rows that contain custom
     * components
     *
     * @return the minimum row heights in points
     */
    public int getMinimumRowHeightForComponents() {
        return minimumRowHeightForComponents;
    }

    /***
     * Set the minimum row height in points for the rows that contain custom
     * components. If set to a small value, it might cause some components like
     * checkboxes to be cut off
     *
     * @param minimumRowHeightForComponents
     *            the minimum row height in points
     */
    public void setMinimumRowHeightForComponents(
            final int minimumRowHeightForComponents) {
        this.minimumRowHeightForComponents = minimumRowHeightForComponents;
    }

    /**
     * This event is fired when the border of a row header is double clicked
     **/
    public static class RowHeaderDoubleClickEvent
            extends ComponentEvent<Component> {
        private final int rowIndex;

        public RowHeaderDoubleClickEvent(Component source, int row) {
            super(source, false);
            rowIndex = row;
        }

        public int getRowIndex() {
            return rowIndex;
        }
    }

    /**
     * Interface for listening a {@link RowHeaderDoubleClickEvent} event
     **/
    public interface RowHeaderDoubleClickListener extends Serializable {
        Method ON_ROW_ON_ROW_HEADER_DOUBLE_CLICK = ReflectTools.findMethod(
                RowHeaderDoubleClickListener.class, "onRowHeaderDoubleClick",
                RowHeaderDoubleClickEvent.class);

        /**
         * This method is called when the user doubleclicks on the border of a
         * row header
         *
         * @param event
         *            The RowHeaderDoubleClilckEvent that happened
         **/
        void onRowHeaderDoubleClick(RowHeaderDoubleClickEvent event);
    }

    /**
     * This method is called when rowIndex auto-fit has been initiated from the
     * browser by double-clicking the border of the target rowIndex header.
     *
     * @param rowIndex
     *            Index of the target rowIndex, 0-based
     */
    protected void onRowHeaderDoubleClick(int rowIndex) {
        fireRowHeaderDoubleClick(rowIndex);
    }

    private void fireRowHeaderDoubleClick(int rowIndex) {
        fireEvent(new RowHeaderDoubleClickEvent(this, rowIndex));
    }

    /**
     * adds a {@link RowHeaderDoubleClickListener} to the Spreadsheet
     *
     * @param listener
     *            The listener to add
     * @return a {@link Registration} for removing the event listener
     **/
    public Registration addRowHeaderDoubleClickListener(
            RowHeaderDoubleClickListener listener) {
        return addListener(RowHeaderDoubleClickEvent.class,
                listener::onRowHeaderDoubleClick);
    }

    /**
     * Define the theme of the Spreadsheet.
     *
     * @param theme
     *            SpreadsheetTheme
     * @since 24.5
     */
    public void setTheme(SpreadsheetTheme theme) {
        getElement().setAttribute("theme", theme.getThemeName());
    }

    /**
     * Themes for the Spreadsheet.
     * 
     * @since 24.5
     */
    public enum SpreadsheetTheme {
        LUMO("lumo"), VALO("");

        private final String theme;

        SpreadsheetTheme(String theme) {
            this.theme = theme;
        }

        String getThemeName() {
            return theme;
        }
    }
}
