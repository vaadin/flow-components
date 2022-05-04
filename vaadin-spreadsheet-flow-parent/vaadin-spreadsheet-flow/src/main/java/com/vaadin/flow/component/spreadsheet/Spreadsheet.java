package com.vaadin.flow.component.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.BaseFormulaEvaluator;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Comment;
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
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.spreadsheet.SheetOverlayWrapper.OverlayChangeListener;
import com.vaadin.flow.component.spreadsheet.action.SpreadsheetDefaultActionHandler;
import com.vaadin.flow.component.spreadsheet.client.CellData;
import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.client.MergedRegionUtil.MergedRegionContainer;
import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;
import com.vaadin.flow.component.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.flow.component.spreadsheet.command.SizeChangeCommand;
import com.vaadin.flow.component.spreadsheet.command.SizeChangeCommand.Type;
import com.vaadin.flow.component.spreadsheet.framework.Action;
import com.vaadin.flow.component.spreadsheet.framework.ReflectTools;
import com.vaadin.flow.component.spreadsheet.rpc.SpreadsheetClientRpc;
import com.vaadin.flow.component.spreadsheet.shared.ContentMode;
import com.vaadin.flow.component.spreadsheet.shared.ErrorLevel;
import com.vaadin.flow.component.spreadsheet.shared.GroupingData;
import com.vaadin.flow.component.spreadsheet.shared.PopupButtonState;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.pro.licensechecker.LicenseChecker;

import elemental.json.JsonValue;

/**
 * Vaadin Spreadsheet is a Vaadin Add-On Component which allows displaying and
 * interacting with the contents of an Excel file. The Spreadsheet can be used
 * in any Vaadin application for enabling users to view and manipulate Excel
 * files in their web browsers.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-spreadsheet")
@JsModule("./vaadin-spreadsheet/vaadin-spreadsheet.js")
@SuppressWarnings("serial")
public class Spreadsheet extends Component implements HasComponents, HasSize,
        HasStyle, Action.Container, Focusable {

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
            if (service.getDeploymentConfiguration().isProductionMode()) {
                LicenseChecker.checkLicenseFromStaticBlock(
                        "vaadin-spreadsheet-flow", version);
            }
        }
    }

    private SpreadsheetHandlerImpl spreadsheetHandler;

    /*
     * FLOW RELATED STUFF
     */

    /*
     * SHARED STATE PROPERTIES
     */

    // from TabIndexState

    public int tabIndex = 0;

    // from AbstractComponentState

    private String height = "100%";
    private String width = "100%";

    @Override
    public void setId(String id) {
        this.id = id;
        getElement().setProperty("id", id);
    }

    @Override
    public void setHeight(String height) {
        this.height = height;
        getElement().setProperty("height", height);
    }

    @Override
    public void setWidth(String width) {
        this.width = width;
        getElement().setProperty("width", width);
    }

    private String description = "";
    private ContentMode descriptionContentMode = ContentMode.PREFORMATTED;
    // Note: for the caption, there is a difference between null and an empty
    // string!
    private String caption = null;
    private List<String> styles = null;
    private String id = null;
    private String primaryStyleName = null;

    /** HTML formatted error message for the component. */
    private String errorMessage = null;

    /**
     * Level of error.
     *
     * @since 8.2
     */
    private ErrorLevel errorLevel = null;

    private boolean captionAsHtml = false;

    // from SaredState

    // private Map<String, URLReference> resources = new HashMap<>();
    private Map<String, String> resources = new HashMap<>();

    private boolean enabled = true;

    /**
     * A set of event identifiers with registered listeners.
     */
    private Set<String> registeredEventListeners;

    // spreadsheetState

    private int rowBufferSize = 200;

    private int columnBufferSize = 200;

    private int rows;

    private int cols;

    private List<GroupingData> colGroupingData;
    private List<GroupingData> rowGroupingData;

    private int colGroupingMax;
    private int rowGroupingMax;

    private boolean colGroupingInversed;
    private boolean rowGroupingInversed;

    private float defRowH;
    private int defColW;

    private float[] rowH;
    private int[] colW;

    /** should the sheet be reloaded on client side */
    private boolean reload;

    /** 1-based */
    private int sheetIndex = 1;

    private String[] sheetNames = null;

    protected HashMap<Integer, String> cellStyleToCSSStyle = null;
    private HashMap<Integer, Integer> rowIndexToStyleIndex = null;
    private HashMap<Integer, Integer> columnIndexToStyleIndex = null;
    private Set<Integer> lockedColumnIndexes = null;
    private Set<Integer> lockedRowIndexes = null;

    private ArrayList<String> shiftedCellBorderStyles = null;

    /**
     * All conditional formatting styles for this sheet.
     */
    private HashMap<Integer, String> conditionalFormattingStyles = null;

    /** 1-based */
    private ArrayList<Integer> hiddenColumnIndexes = null;

    /** 1-based */
    private ArrayList<Integer> hiddenRowIndexes = null;

    private int[] verticalScrollPositions;

    private int[] horizontalScrollPositions;

    private boolean sheetProtected;

    private boolean workbookProtected;

    private HashMap<String, String> cellKeysToEditorIdMap;

    private HashMap<String, String> componentIDtoCellKeysMap;

    // Cell CSS key to link tooltip (usually same as address)
    private HashMap<String, String> hyperlinksTooltips;

    private HashMap<String, String> cellComments;
    private HashMap<String, String> cellCommentAuthors;

    private ArrayList<String> visibleCellComments;

    private Set<String> invalidFormulaCells;

    private boolean hasActions;

    private HashMap<String, OverlayInfo> overlays;

    private ArrayList<MergedRegion> mergedRegions;

    private boolean displayGridlines = true;

    private boolean displayRowColHeadings = true;

    private int verticalSplitPosition = 0;
    private int horizontalSplitPosition = 0;

    private String infoLabelValue;

    private boolean workbookChangeToggle;

    private String invalidFormulaErrorMessage = "Invalid formula";

    private boolean lockFormatColumns = true;

    private boolean lockFormatRows = true;

    private List<String> namedRanges;

    public String getDescription() {
        return description;
    }

    public ContentMode getDescriptionContentMode() {
        return descriptionContentMode;
    }

    public String getCaption() {
        return caption;
    }

    public List<String> getStyles() {
        return styles;
    }

    public String getPrimaryStyleName() {
        return primaryStyleName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ErrorLevel getErrorLevel() {
        return errorLevel;
    }

    public boolean isCaptionAsHtml() {
        return captionAsHtml;
    }

    public Map<String, String> getResources() {
        return resources;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Set<String> getRegisteredEventListeners() {
        return registeredEventListeners;
    }

    public int getColumnBufferSize() {
        return columnBufferSize;
    }

    public int getCols() {
        return cols;
    }

    public List<GroupingData> getColGroupingData() {
        return colGroupingData;
    }

    public List<GroupingData> getRowGroupingData() {
        return rowGroupingData;
    }

    public int getColGroupingMax() {
        return colGroupingMax;
    }

    public int getRowGroupingMax() {
        return rowGroupingMax;
    }

    public boolean isColGroupingInversed() {
        return colGroupingInversed;
    }

    public boolean isRowGroupingInversed() {
        return rowGroupingInversed;
    }

    public float getDefRowH() {
        return defRowH;
    }

    public int getDefColW() {
        return defColW;
    }

    public float[] getRowH() {
        return rowH;
    }

    public int[] getColW() {
        return colW;
    }

    public boolean isReload() {
        return reload;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public String[] getSheetNames() {
        return sheetNames;
    }

    public HashMap<Integer, String> getCellStyleToCSSStyle() {
        return cellStyleToCSSStyle;
    }

    public HashMap<Integer, Integer> getRowIndexToStyleIndex() {
        return rowIndexToStyleIndex;
    }

    public HashMap<Integer, Integer> getColumnIndexToStyleIndex() {
        return columnIndexToStyleIndex;
    }

    public Set<Integer> getLockedColumnIndexes() {
        return lockedColumnIndexes;
    }

    public Set<Integer> getLockedRowIndexes() {
        return lockedRowIndexes;
    }

    public ArrayList<String> getShiftedCellBorderStyles() {
        return shiftedCellBorderStyles;
    }

    public HashMap<Integer, String> getConditionalFormattingStyles() {
        return conditionalFormattingStyles;
    }

    public ArrayList<Integer> getHiddenColumnIndexes() {
        return hiddenColumnIndexes;
    }

    public ArrayList<Integer> getHiddenRowIndexes() {
        return hiddenRowIndexes;
    }

    public int[] getVerticalScrollPositions() {
        return verticalScrollPositions;
    }

    public int[] getHorizontalScrollPositions() {
        return horizontalScrollPositions;
    }

    public boolean isSheetProtected() {
        return sheetProtected;
    }

    public boolean isWorkbookProtected() {
        return workbookProtected;
    }

    public HashMap<String, String> getCellKeysToEditorIdMap() {
        return cellKeysToEditorIdMap;
    }

    public HashMap<String, String> getComponentIDtoCellKeysMap() {
        return componentIDtoCellKeysMap;
    }

    public HashMap<String, String> getHyperlinksTooltips() {
        return hyperlinksTooltips;
    }

    public HashMap<String, String> getCellComments() {
        return cellComments;
    }

    public HashMap<String, String> getCellCommentAuthors() {
        return cellCommentAuthors;
    }

    public ArrayList<String> getVisibleCellComments() {
        return visibleCellComments;
    }

    public Set<String> getInvalidFormulaCells() {
        return invalidFormulaCells;
    }

    public boolean isHasActions() {
        return hasActions;
    }

    public HashMap<String, OverlayInfo> getOverlays() {
        return overlays;
    }

    public ArrayList<MergedRegion> getMergedRegions() {
        return mergedRegions;
    }

    public boolean isDisplayGridlines() {
        return displayGridlines;
    }

    public boolean isDisplayRowColHeadings() {
        return displayRowColHeadings;
    }

    public int getVerticalSplitPosition() {
        return verticalSplitPosition;
    }

    public int getHorizontalSplitPosition() {
        return horizontalSplitPosition;
    }

    public String getInfoLabelValue() {
        return infoLabelValue;
    }

    public boolean isWorkbookChangeToggle() {
        return workbookChangeToggle;
    }

    public String getInvalidFormulaErrorMessage() {
        return invalidFormulaErrorMessage;
    }

    public boolean isLockFormatColumns() {
        return lockFormatColumns;
    }

    public boolean isLockFormatRows() {
        return lockFormatRows;
    }

    public List<String> getNamedRanges() {
        return namedRanges;
    }

    public void _setRowBufferSize(int rowBufferSize) {
        setRowBufferSize(rowBufferSize);
    }

    public void setColumnBufferSize(int columnBufferSize) {
        this.columnBufferSize = columnBufferSize;
        getElement().setProperty("columnBufferSize", columnBufferSize);
    }

    public void setRows(int rows) {
        this.rows = rows;
        getElement().setProperty("rows", rows);
    }

    public void setCols(int cols) {
        this.cols = cols;
        getElement().setProperty("cols", cols);
    }

    public void setColGroupingData(List<GroupingData> colGroupingData) {
        this.colGroupingData = colGroupingData;
        getElement().setProperty("colGroupingData",
                Serializer.serialize(colGroupingData));
    }

    public void setRowGroupingData(List<GroupingData> rowGroupingData) {
        this.rowGroupingData = rowGroupingData;
        getElement().setProperty("rowGroupingData",
                Serializer.serialize(rowGroupingData));
    }

    public void setColGroupingMax(int colGroupingMax) {
        this.colGroupingMax = colGroupingMax;
        getElement().setProperty("colGroupingMax", colGroupingMax);
    }

    public void setRowGroupingMax(int rowGroupingMax) {
        this.rowGroupingMax = rowGroupingMax;
        getElement().setProperty("rowGroupingMax", rowGroupingMax);
    }

    public void setColGroupingInversed(boolean colGroupingInversed) {
        this.colGroupingInversed = colGroupingInversed;
        getElement().setProperty("colGroupingInversed", colGroupingInversed);
    }

    public void setRowGroupingInversed(boolean rowGroupingInversed) {
        this.rowGroupingInversed = rowGroupingInversed;
        getElement().setProperty("rowGroupingInversed", rowGroupingInversed);
    }

    public void setDefRowH(float defRowH) {
        this.defRowH = defRowH;
        getElement().setProperty("defRowH", defRowH);
    }

    public void setDefColW(int defColW) {
        this.defColW = defColW;
        getElement().setProperty("defColW", defColW);
    }

    public void setRowH(float[] rowH) {
        this.rowH = rowH;
        getElement().setProperty("rowH", Serializer.serialize(rowH));
    }

    public void setColW(int[] colW) {
        this.colW = colW;
        getElement().setProperty("colW", Serializer.serialize(colW));
    }

    public void setReload(boolean reload) {
        if (reload)
            getElement().setProperty("reload", System.currentTimeMillis());
    }

    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
        getElement().setProperty("sheetIndex", sheetIndex);
    }

    public void setSheetNames(String[] sheetNames) {
        this.sheetNames = sheetNames;
        getElement().setProperty("sheetNames",
                Serializer.serialize(sheetNames));
    }

    public void setCellStyleToCSSStyle(
            HashMap<Integer, String> cellStyleToCSSStyle) {
        this.cellStyleToCSSStyle = cellStyleToCSSStyle;
        getElement().setProperty("cellStyleToCSSStyle",
                Serializer.serialize(cellStyleToCSSStyle));
    }

    public void setRowIndexToStyleIndex(
            HashMap<Integer, Integer> rowIndexToStyleIndex) {
        this.rowIndexToStyleIndex = rowIndexToStyleIndex;
        getElement().setProperty("rowIndexToStyleIndex",
                Serializer.serialize(rowIndexToStyleIndex));
    }

    public void setColumnIndexToStyleIndex(
            HashMap<Integer, Integer> columnIndexToStyleIndex) {
        this.columnIndexToStyleIndex = columnIndexToStyleIndex;
        getElement().setProperty("columnIndexToStyleIndex",
                Serializer.serialize(columnIndexToStyleIndex));
    }

    public void setLockedColumnIndexes(Set<Integer> lockedColumnIndexes) {
        this.lockedColumnIndexes = lockedColumnIndexes;
        getElement().setProperty("lockedColumnIndexes",
                Serializer.serialize(lockedColumnIndexes));
    }

    public void setLockedRowIndexes(Set<Integer> lockedRowIndexes) {
        this.lockedRowIndexes = lockedRowIndexes;
        getElement().setProperty("lockedRowIndexes",
                Serializer.serialize(lockedRowIndexes));
    }

    public void setShiftedCellBorderStyles(
            ArrayList<String> shiftedCellBorderStyles) {
        this.shiftedCellBorderStyles = shiftedCellBorderStyles;
        getElement().setProperty("shiftedCellBorderStyles",
                Serializer.serialize(shiftedCellBorderStyles));
    }

    public void setConditionalFormattingStyles(
            HashMap<Integer, String> conditionalFormattingStyles) {
        this.conditionalFormattingStyles = conditionalFormattingStyles;
        getElement().setProperty("conditionalFormattingStyles",
                Serializer.serialize(conditionalFormattingStyles));
    }

    public void setHiddenColumnIndexes(ArrayList<Integer> hiddenColumnIndexes) {
        this.hiddenColumnIndexes = hiddenColumnIndexes;
        getElement().setProperty("hiddenColumnIndexes",
                Serializer.serialize(hiddenColumnIndexes));
    }

    public void setHiddenRowIndexes(ArrayList<Integer> hiddenRowIndexes) {
        this.hiddenRowIndexes = hiddenRowIndexes;
        getElement().setProperty("hiddenRowIndexes",
                Serializer.serialize(hiddenRowIndexes));
    }

    public void setVerticalScrollPositions(int[] verticalScrollPositions) {
        this.verticalScrollPositions = verticalScrollPositions;
        getElement().setProperty("verticalScrollPositions",
                Serializer.serialize(verticalScrollPositions));
    }

    public void setHorizontalScrollPositions(int[] horizontalScrollPositions) {
        this.horizontalScrollPositions = horizontalScrollPositions;
        getElement().setProperty("horizontalScrollPositions",
                Serializer.serialize(horizontalScrollPositions));
    }

    public void setSheetProtected(boolean sheetProtected) {
        this.sheetProtected = sheetProtected;
        getElement().setProperty("sheetProtected", sheetProtected);
    }

    public void setWorkbookProtected(boolean workbookProtected) {
        this.workbookProtected = workbookProtected;
        getElement().setProperty("workbookProtected", workbookProtected);
    }

    public void setCellKeysToEditorIdMap(
            HashMap<String, String> cellKeysToEditorIdMap) {
        this.cellKeysToEditorIdMap = cellKeysToEditorIdMap;
        getElement().setProperty("cellKeysToEditorIdMap",
                Serializer.serialize(cellKeysToEditorIdMap));
    }

    public void setComponentIDtoCellKeysMap(
            HashMap<String, String> componentIDtoCellKeysMap) {
        this.componentIDtoCellKeysMap = componentIDtoCellKeysMap;
        getElement().setProperty("componentIDtoCellKeysMap",
                Serializer.serialize(componentIDtoCellKeysMap));
    }

    public void setHyperlinksTooltips(
            HashMap<String, String> hyperlinksTooltips) {
        this.hyperlinksTooltips = hyperlinksTooltips;
        getElement().setProperty("hyperlinksTooltips",
                Serializer.serialize(hyperlinksTooltips));
    }

    public void setCellComments(HashMap<String, String> cellComments) {
        this.cellComments = cellComments;
        getElement().setProperty("cellComments",
                Serializer.serialize(cellComments));
    }

    public void setCellCommentAuthors(
            HashMap<String, String> cellCommentAuthors) {
        this.cellCommentAuthors = cellCommentAuthors;
        getElement().setProperty("cellCommentAuthors",
                Serializer.serialize(cellCommentAuthors));
    }

    public void setVisibleCellComments(ArrayList<String> visibleCellComments) {
        this.visibleCellComments = visibleCellComments;
        getElement().setProperty("visibleCellComments",
                Serializer.serialize(visibleCellComments));
    }

    public void setInvalidFormulaCells(Set<String> invalidFormulaCells) {
        this.invalidFormulaCells = invalidFormulaCells;
        getElement().setProperty("invalidFormulaCells",
                Serializer.serialize(invalidFormulaCells));
    }

    public void setHasActions(boolean hasActions) {
        this.hasActions = hasActions;
        getElement().setProperty("hasActions", hasActions);
    }

    public void setOverlays(HashMap<String, OverlayInfo> overlays) {
        this.overlays = overlays;
        getElement().setProperty("overlays", Serializer.serialize(overlays));
    }

    public void setMergedRegions(ArrayList<MergedRegion> mergedRegions) {
        this.mergedRegions = mergedRegions;
        getElement().setProperty("mergedRegions",
                Serializer.serialize(mergedRegions));
    }

    public void setDisplayGridlines(boolean displayGridlines) {
        this.displayGridlines = displayGridlines;
        getElement().setProperty("displayGridlines", displayGridlines);
    }

    public void setDisplayRowColHeadings(boolean displayRowColHeadings) {
        this.displayRowColHeadings = displayRowColHeadings;
        getElement().setProperty("displayRowColHeadings",
                displayRowColHeadings);
    }

    public void setVerticalSplitPosition(int verticalSplitPosition) {
        this.verticalSplitPosition = verticalSplitPosition;
        getElement().setProperty("verticalSplitPosition",
                verticalSplitPosition);
    }

    public void setHorizontalSplitPosition(int horizontalSplitPosition) {
        this.horizontalSplitPosition = horizontalSplitPosition;
        getElement().setProperty("horizontalSplitPosition",
                horizontalSplitPosition);
    }

    public void setInfoLabelValue(String infoLabelValue) {
        this.infoLabelValue = infoLabelValue;
        getElement().setProperty("infoLabelValue", infoLabelValue);
    }

    public void setWorkbookChangeToggle(boolean workbookChangeToggle) {
        this.workbookChangeToggle = workbookChangeToggle;
        getElement().setProperty("workbookChangeToggle", workbookChangeToggle);
    }

    public void setLockFormatColumns(boolean lockFormatColumns) {
        this.lockFormatColumns = lockFormatColumns;
        getElement().setProperty("lockFormatColumns", lockFormatColumns);
    }

    public void setLockFormatRows(boolean lockFormatRows) {
        this.lockFormatRows = lockFormatRows;
        getElement().setProperty("lockFormatRows", lockFormatRows);
    }

    public void setNamedRanges(List<String> namedRanges) {
        this.namedRanges = namedRanges;
        getElement().setProperty("namedRanges",
                Serializer.serialize(namedRanges));
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
        private final JsonValue data;

        public SpreadsheetEvent(Spreadsheet source, boolean fromClient,
                @EventData("event.detail.type") String type,
                @EventData("event.detail.data") JsonValue data) {
            super(source, fromClient);
            this.type = type;
            this.data = data;
        }

        public String getType() {
            return type;
        }

        public JsonValue getData() {
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
    private FormulaEvaluator formulaEvaluator;

    /**
     * A common conditional formatting formula evaluator for this Spreadsheet
     * needed for proper value string conversions
     */
    private ConditionalFormattingEvaluator conditionalFormattingEvaluator;

    /**
     * Pixel width of the filter popup button
     */
    private static final int FILTER_BUTTON_PIXEL_WIDTH = 14;

    /**
     * Extra padding (in pixels) to add between the filter popup button and cell
     * text when autofitting a column.
     */
    private static final int FILTER_BUTTON_PIXEL_PADDING = 2;

    /**
     * Map of autofitted column widths in points
     */
    private Map<CellReference, Integer> autofittedColumnWidths = new WeakHashMap<>();

    private SpreadsheetClientRpc clientRpc = new SpreadsheetClientRpc() {
        @Override
        public void updateBottomRightCellValues(ArrayList<CellData> cellData) {
            getElement().callJsFunction("updateBottomRightCellValues",
                    Serializer.serialize(cellData));
        }

        @Override
        public void updateTopLeftCellValues(ArrayList<CellData> cellData) {
            getElement().callJsFunction("updateTopLeftCellValues",
                    Serializer.serialize(cellData));
        }

        @Override
        public void updateTopRightCellValues(ArrayList<CellData> cellData) {
            getElement().callJsFunction("updateTopRightCellValues",
                    Serializer.serialize(cellData));
        }

        @Override
        public void updateBottomLeftCellValues(ArrayList<CellData> cellData) {
            getElement().callJsFunction("updateBottomLeftCellValues",
                    Serializer.serialize(cellData));
        }

        @Override
        public void updateFormulaBar(String possibleName, int col, int row) {
            getElement().callJsFunction("updateFormulaBar", possibleName, col,
                    row);
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
                    cellValue, function, locked, initialSelection);
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
                    row, c1, c2, r1, r2, scroll);
        }

        @Override
        public void cellsUpdated(ArrayList<CellData> cellData) {
            getElement().callJsFunction("cellsUpdated",
                    Serializer.serialize(cellData));
        }

        @Override
        public void refreshCellStyles() {
            getElement().callJsFunction("refreshCellStyles");
        }

        @Override
        public void editCellComment(int col, int row) {
            getElement().callJsFunction("editCellComment", col, row);
        }
    };
    private Locale locale;

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

    private SpreadsheetStyleFactory styler;
    private HyperlinkCellClickHandler hyperlinkCellClickHandler;
    private SpreadsheetComponentFactory customComponentFactory;

    private final CellSelectionManager selectionManager = new CellSelectionManager(
            this);
    private final CellSelectionShifter cellShifter = new CellSelectionShifter(
            this);
    private final ContextMenuManager contextMenuManager = new ContextMenuManager(
            this);
    private final SpreadsheetHistoryManager historyManager = new SpreadsheetHistoryManager(
            this);
    private ConditionalFormatter conditionalFormatter;

    /**
     * caches data, so it needs to be stable for the life of a given workbook
     */
    private CellValueManager valueManager;

    /** The first visible row in the scroll area **/
    private int firstRow;
    /** The last visible row in the scroll area **/
    private int lastRow;
    /** The first visible column in the scroll area **/
    private int firstColumn;
    /** The last visible column in the scroll area **/
    private int lastColumn;

    /** Spreadsheet Flow does not support charts yet **/
    private boolean chartsEnabled = false;

    /**
     * This is used for making sure the cells are sent to client side in when
     * the next cell data request comes. This is triggered when the client side
     * connector init() method is run.
     */
    private boolean reloadCellDataOnNextScroll;

    private int defaultNewSheetRows = SpreadsheetFactory.DEFAULT_ROWS;
    private int defaultNewSheetColumns = SpreadsheetFactory.DEFAULT_COLUMNS;

    private boolean topLeftCellCommentsLoaded;

    private SpreadsheetDefaultActionHandler defaultActionHandler;

    protected int mergedRegionCounter;

    private Workbook workbook;

    /** true if the component sheet should be reloaded on client side. */
    // todo: already defined in shared state. Check!
    // private boolean reload;

    /** are tables for currently active sheet loaded */
    private boolean tablesLoaded;

    private SheetState sheetState = new SheetState(this);

    /** image sizes need to be recalculated on column/row resizing */
    private boolean reloadImageSizesFromPOI;

    private String defaultPercentageFormat = "0.00%";

    protected String initialSheetSelection = null;

    private Set<Component> customComponents = new HashSet<Component>();

    /* Disable buttons until table support #826 */
    private static final boolean popupButtonsEnabled = false;

    private Map<CellReference, PopupButton> sheetPopupButtons = new HashMap<CellReference, PopupButton>();

    private HashSet<PopupButton> attachedPopupButtons = new HashSet<PopupButton>();

    /**
     * Set of images contained in the currently active sheet.
     */
    private HashSet<SheetOverlayWrapper> sheetOverlays;

    private Set<Component> overlayComponents = new HashSet<Component>();

    private HashSet<SpreadsheetTable> tables;

    private final Map<Integer, HashSet<String>> invalidFormulas = new HashMap<Integer, HashSet<String>>();

    /**
     * Container for merged regions for the currently active sheet.
     */
    protected final MergedRegionContainer mergedRegionContainer = new MergedRegionContainer() {

        /*
         * (non-Javadoc)
         *
         * @see com.vaadin.flow.component.spreadsheet.client.MergedRegionUtil.
         * MergedRegionContainer#getMergedRegionStartingFrom(int, int)
         */
        @Override
        public MergedRegion getMergedRegionStartingFrom(int column, int row) {
            List<MergedRegion> mergedRegions = getMergedRegions();
            if (mergedRegions != null) {
                for (MergedRegion region : mergedRegions) {
                    if (region.col1 == column && region.row1 == row) {
                        return region;
                    }
                }
            }
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see com.vaadin.flow.component.spreadsheet.client.MergedRegionUtil.
         * MergedRegionContainer#getMergedRegion(int, int)
         */
        @Override
        public MergedRegion getMergedRegion(int column, int row) {
            List<MergedRegion> mergedRegions = getMergedRegions();
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

    private Set<Integer> rowsWithComponents;

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
        valueManager = createCellValueManager();
        sheetOverlays = new HashSet<SheetOverlayWrapper>();
        tables = new HashSet<SpreadsheetTable>();
        registerRpc(new SpreadsheetHandlerImpl(this));
        setSizeFull(); // Default to full size
        defaultActionHandler = new SpreadsheetDefaultActionHandler();
        hyperlinkCellClickHandler = new DefaultHyperlinkCellClickHandler(this);
        addActionHandler(defaultActionHandler);
        setId(UUID.randomUUID().toString());
        customInit();
    }

    private void registerRpc(SpreadsheetHandlerImpl spreadsheetHandler) {
        LOGGER.info("Spreadsheet.registerRpc()");
        this.spreadsheetHandler = spreadsheetHandler;
        addListener(SpreadsheetEvent.class,
                new SpreadsheetEventListener(spreadsheetHandler));
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
        return firstColumn;
    }

    /**
     * Returns the last visible column in the main scroll area (NOT freeze pane)
     *
     * @return Index of last visible column, 1-based
     */
    public int getLastColumn() {
        return lastColumn;
    }

    /**
     * Returns the first visible row in the scroll area (not freeze pane)
     *
     * @return Index of first visible row, 1-based
     */
    public int getFirstRow() {
        return firstRow;
    }

    /**
     * Returns the last visible row in the main scroll area (NOT freeze pane)
     *
     * @return Index of last visible row, 1-based
     */
    public int getLastRow() {
        return lastRow;
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
     * @return
     */
    public boolean isChartsEnabled() {
        return chartsEnabled;
    }

    /**
     * Use this method to define whether embedded charts should be displayed in
     * the spreadsheet or not.
     *
     * @param chartsEnabled
     */
    public void setChartsEnabled(boolean chartsEnabled) {
        this.chartsEnabled = chartsEnabled;
        clearSheetOverlays();
        loadOrUpdateOverlays();
    }

    /**
     * Returns true if the component is being fully re-rendered after this
     * round-trip (sheet change etc.)
     *
     * @return true if re-render will happen, false otherwise
     */
    public boolean isRerenderPending() {
        return reload;
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
     * This method is called when the sheet is scrolled. It takes care of
     * sending newly revealed data to the client side.
     *
     * @param firstRow
     *            Index of first visible row after the scroll, 1-based
     * @param firstColumn
     *            Index of first visible column after the scroll, 1-based
     * @param lastRow
     *            Index of last visible row after the scroll, 1-based
     * @param lastColumn
     *            Index of first visible column after the scroll, 1-based
     */
    protected void onSheetScroll(int firstRow, int firstColumn, int lastRow,
            int lastColumn) {
        if (reloadCellDataOnNextScroll || this.firstRow != firstRow
                || this.lastRow != lastRow || this.firstColumn != firstColumn
                || this.lastColumn != lastColumn) {
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstColumn = firstColumn;
            this.lastColumn = lastColumn;
            loadCells(firstRow, firstColumn, lastRow, lastColumn);
        }
        if (initialSheetSelection != null) {
            selectionManager.onSheetAddressChanged(initialSheetSelection, true);
            initialSheetSelection = null;
        } else if (reloadCellDataOnNextScroll) {
            selectionManager.reloadCurrentSelection();
        }
        reloadCellDataOnNextScroll = false;
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
        final String[] split = addressString.split(":");
        final CellReference cr1 = new CellReference(split[0]);
        final CellReference cr2 = new CellReference(split[1]);
        int r1 = cr1.getRow() > cr2.getRow() ? cr2.getRow() : cr1.getRow();
        int r2 = cr1.getRow() > cr2.getRow() ? cr1.getRow() : cr2.getRow();
        int c1 = cr1.getCol() > cr2.getCol() ? cr2.getCol() : cr1.getCol();
        int c2 = cr1.getCol() > cr2.getCol() ? cr1.getCol() : cr2.getCol();
        if (r1 >= getRows()) {
            r1 = getRows() - 1;
        }
        if (r2 >= getRows()) {
            r2 = getRows() - 1;
        }
        if (c1 >= getCols()) {
            c1 = getCols() - 1;
        }
        if (c2 >= getCols()) {
            c2 = getCols() - 1;
        }
        return new CellRangeAddress(r1, r2, c1, c2);
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
        int r1 = row1 > row2 ? row2 : row1;
        int r2 = row1 > row2 ? row1 : row2;
        int c1 = col1 > col2 ? col2 : col1;
        int c2 = col1 > col2 ? col1 : col2;
        if (r1 >= getRows()) {
            r1 = getRows();
        }
        if (r2 >= getRows()) {
            r2 = getRows();
        }
        if (c1 >= getCols()) {
            c1 = getCols();
        }
        if (c2 >= getCols()) {
            c2 = getCols();
        }
        return new CellRangeAddress(r1 - 1, r2 - 1, c1 - 1, c2 - 1);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.ui.AbstractComponent#setLocale(java.util.Locale)
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
        valueManager.updateLocale(locale);
        refreshAllCellValues();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        valueManager.updateLocale(getLocale());
        if (!FeatureFlags
                .get(UI.getCurrent().getSession().getService().getContext())
                .isEnabled(FeatureFlags.SPREADSHEET_COMPONENT)) {
            throw new RuntimeException("\n\n--------------\n\n"
                    + "The spreadsheet component is currently an experimental feature and needs to be explicitly enabled. "
                    + "The component can be enabled by using the Vaadin dev-mode Gizmo, in the experimental features tab, "
                    + "or by adding a `src/main/resources/vaadin-featureflags.properties` file with the following content: "
                    + "`com.vaadin.experimental.spreadsheetComponent=true`"
                    + "\n\n--------------\n\n");
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
        // POI allows user to hide all sheets ...
        if (visibility != SheetVisibility.VISIBLE
                && SpreadsheetUtil.getNumberOfVisibleSheets(workbook) == 1
                && !(workbook.isSheetHidden(sheetPOIIndex)
                        || workbook.isSheetVeryHidden(sheetPOIIndex))) {
            throw new IllegalArgumentException(
                    "At least one sheet should be always visible.");
        }
        boolean isHidden = workbook.isSheetHidden(sheetPOIIndex);
        boolean isVeryHidden = workbook.isSheetVeryHidden(sheetPOIIndex);
        int activeSheetIndex = workbook.getActiveSheetIndex();

        workbook.setSheetVisibility(sheetPOIIndex, visibility);

        // skip component reload if "nothing changed"
        if ((visibility == SheetVisibility.VISIBLE
                && (isHidden || isVeryHidden))
                || (visibility != SheetVisibility.VISIBLE
                        && !(isHidden || isVeryHidden))) {
            if (sheetPOIIndex != activeSheetIndex) {
                reloadSheetNames();
                setSheetIndex(getSpreadsheetSheetIndex(activeSheetIndex) + 1);
            } else { // the active sheet can be only set as hidden
                int oldVisibleSheetIndex = getSheetIndex() - 1;
                if (visibility != SheetVisibility.VISIBLE
                        && activeSheetIndex == (workbook.getNumberOfSheets()
                                - 1)) {
                    // hiding the active sheet, and it was the last sheet
                    oldVisibleSheetIndex--;
                }
                int newActiveSheetIndex = getVisibleSheetPOIIndex(
                        oldVisibleSheetIndex);
                workbook.setActiveSheet(newActiveSheetIndex);
                reloadActiveSheetData();
                SpreadsheetFactory.reloadSpreadsheetData(this,
                        getActiveSheet());
            }
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
     * @param hidden
     *            Visibility state to set: 0-visible, 1-hidden, 2-very hidden.
     * @param sheetPOIIndex
     *            Index of the target sheet within the POI model, 0-based
     * @throws IllegalArgumentException
     *             If the index or state is invalid, or if trying to hide the
     *             only visible sheet.
     * @deprecated use {@link #setSheetHidden(int, SheetVisibility)}
     */
    @Deprecated
    public void setSheetHidden(int sheetPOIIndex, int hidden)
            throws IllegalArgumentException {
        setSheetHidden(sheetPOIIndex, SheetVisibility.values()[hidden]);
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
        if (sheetIndex < 0 || sheetIndex >= getSheetNames().length) {
            throw new IllegalArgumentException("Invalid Sheet index given.");
        }
        int poiSheetIndex = getVisibleSheetPOIIndex(sheetIndex);
        setSheetNameWithPOIIndex(poiSheetIndex, sheetName);
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
        if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException(
                    "Invalid POI Sheet index given.");
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
        if (!workbook.isSheetVeryHidden(sheetIndex)
                && !workbook.isSheetHidden(sheetIndex)) {
            int ourIndex = getSpreadsheetSheetIndex(sheetIndex);
            // todo: comprobar si esto es así
            String[] _sheetNames = Arrays.copyOf(getSheetNames(),
                    getSheetNames().length);
            _sheetNames[ourIndex] = sheetName;
            setSheetNames(_sheetNames);
        }
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
        if (sheetPOIIndex < 0
                || sheetPOIIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException(
                    "Invalid POI Sheet index given.");
        }
        workbook.getSheetAt(sheetPOIIndex).protectSheet(password);
        setSheetProtected(getActiveSheet().getProtect());
        // if the currently active sheet was protected, the protection for the
        // currently selected cell might have changed
        if (sheetPOIIndex == workbook.getActiveSheetIndex()) {
            loadCustomComponents();
            selectionManager.reSelectSelectedCell();
        }
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
        setSheetProtected(workbook.getActiveSheetIndex(), password);
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
        SpreadsheetFactory.addNewSheet(this, workbook, sheetName, rows,
                columns);
        fireSheetChangeEvent(previousSheet, getActiveSheet());
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
        if (getNumberOfVisibleSheets() < 2) {
            throw new IllegalArgumentException(
                    "A workbook must contain at least one visible worksheet");
        }
        int removedVisibleIndex = getSpreadsheetSheetIndex(poiSheetIndex);
        workbook.removeSheetAt(poiSheetIndex);

        // POI doesn't seem to shift the active sheet index ...
        int oldIndex = getSheetIndex() - 1;
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

    private boolean isSheetNameExisting(String sheetName) {
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
        if (sheetIndex < 0 || sheetIndex >= getSheetNames().length) {
            throw new IllegalArgumentException("Invalid Sheet index given.");
        }
        int POISheetIndex = getVisibleSheetPOIIndex(sheetIndex);
        setActiveSheetWithPOIIndex(POISheetIndex);
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
        if (sheetIndex < 0 || sheetIndex >= workbook.getNumberOfSheets()) {
            throw new IllegalArgumentException(
                    "Invalid POI Sheet index given.");
        }
        if (workbook.isSheetHidden(sheetIndex)
                || workbook.isSheetVeryHidden(sheetIndex)) {
            throw new IllegalArgumentException(
                    "Cannot set a hidden or very hidden sheet as the active sheet. Given index: "
                            + sheetIndex);
        }
        workbook.setActiveSheet(sheetIndex);
        // assume since the UI doesn't allow multiple sheet selections
        // active sheet == selected tab
        workbook.setSelectedTab(sheetIndex);

        // formulas defined relative to the sheet may need recalculation
        getFormulaEvaluator().clearAllCachedResultValues();
        getConditionalFormattingEvaluator().clearAllCachedValues();

        reloadActiveSheetData();
        SpreadsheetFactory.reloadSpreadsheetData(this,
                workbook.getSheetAt(sheetIndex));
        reloadActiveSheetStyles();
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
        // this is for the very rare occasion when the sheet has been
        // selected and the selected sheet value is still negative
        int oldIndex = Math.abs(getSheetIndex()) - 1;
        int[] _verticalScrollPositions = Arrays.copyOf(
                getVerticalScrollPositions(),
                getVerticalScrollPositions().length);
        _verticalScrollPositions[oldIndex] = scrollTop;
        setVerticalScrollPositions(_verticalScrollPositions);
        int[] _horizontalScrollPositions = Arrays.copyOf(
                getHorizontalScrollPositions(),
                getHorizontalScrollPositions().length);
        _horizontalScrollPositions[oldIndex] = scrollLeft;
        setHorizontalScrollPositions(_horizontalScrollPositions);
        Sheet oldSheet = getActiveSheet();
        setActiveSheetIndex(tabIndex);
        Sheet newSheet = getActiveSheet();
        fireSheetChangeEvent(oldSheet, newSheet);
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
        int[] _verticalScrollPositions = Arrays.copyOf(
                getVerticalScrollPositions(),
                getVerticalScrollPositions().length);
        _verticalScrollPositions[getSheetIndex() - 1] = scrollTop;
        setVerticalScrollPositions(_verticalScrollPositions);
        int[] _horizontalScrollPositions = Arrays.copyOf(
                getHorizontalScrollPositions(),
                getHorizontalScrollPositions().length);
        _horizontalScrollPositions[getSheetIndex() - 1] = scrollLeft;
        setHorizontalScrollPositions(_horizontalScrollPositions);
        createNewSheet(null, defaultNewSheetRows, defaultNewSheetColumns);
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
        // if excel doesn't keep these in history, neither will we
        setSheetNameWithPOIIndex(getVisibleSheetPOIIndex(sheetIndex),
                sheetName);
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
        return rows;
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
    public Cell getCell(String cellAddress) {
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
    public Cell getCell(int row, int col) {
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
    public Cell getCell(int row, int col, Sheet sheet) {
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
     *            Reference to the cell to return
     * @return The cell corresponding to the given reference, or null if not
     *         defined
     */
    public Cell getCell(CellReference cellReference) {
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
    public Cell getCell(String sheetName, int row, int column) {
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
     *            Reference to the cell to return
     * @param sheet
     *            Sheet of the cell
     * @return The cell corresponding to the given reference, or null if not
     *         defined
     */
    public Cell getCell(CellReference cellReference, Sheet sheet) {
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
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        final Cell cell = activeSheet.getRow(row).getCell(col);
        if (cell != null) {
            // cell.setCellStyle(null); // TODO NPE on HSSF
            styler.cellStyleUpdated(cell, true);
            activeSheet.getRow(row).removeCell(cell);
            valueManager.cellDeleted(cell);
            refreshCells(cell);
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
        valueManager.cellUpdated(cell);
        if (cellStyleUpdated) {
            styler.cellStyleUpdated(cell, true);
        }
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
        valueManager.cellDeleted(cell);
        if (cellStyleUpdated) {
            styler.cellStyleUpdated(cell, true);
        }
        refreshCells(cell);
    }

    /**
     * Updates the content of the cells that have been marked for update with
     * {@link #markCellAsUpdated(Cell, boolean)}.
     * <p>
     * Does NOT update custom components (editors / always visible) for the
     * cells. For that, use {@link #reloadVisibleCellContents()}
     */
    void updateMarkedCells() {
        // update conditional formatting in case styling has changed. New values
        // are fetched in ValueManager (below).
        conditionalFormatter.createConditionalFormatterRules();
        // FIXME should be optimized, should not go through all links, comments
        // etc. always
        valueManager.updateMarkedCellValues();
        // if the selected cell is of type formula, there is a change that the
        // formula has been changed.
        selectionManager.reSelectSelectedCell();
        // Update the cell comments as well to show them instantly after adding
        // them
        loadCellComments();

        // update custom components, editors
        reloadVisibleCellContents();
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
        if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if (value instanceof Calendar) {
            cell.setCellValue((Calendar) value);
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

        getFormulaEvaluator().clearAllCachedResultValues();
        getConditionalFormattingEvaluator().clearAllCachedValues();
        valueManager.clearCachedContent();

        // only reload if the cells have been loaded once previously
        if (firstColumn == -1) {
            // client will request cells soon, no need for reload now
            return;
        }
        updateRowAndColumnRangeCellData(1, 1, getRows(), getColumns());
        // if the selected cell is of type formula, there is a change that the
        // formula has been changed.
        selectionManager.reSelectSelectedCell();
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
     * Gets the default column width for the currently active sheet. This is
     * derived from the active sheet's ({@link #getActiveSheet()}) default
     * column width (Sheet {@link #getDefaultColumnWidth()}).
     *
     * @return The default column width in PX
     */
    public int getDefaultColumnWidth() {
        return getDefColW();
    }

    /**
     * Sets the default column width in pixels that the component uses, this
     * doesn't change the default column width of the underlying sheet, returned
     * by {@link #getActiveSheet()} and {@link Sheet#getDefaultColumnWidth()}.
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
        setDefColW(widthPX);
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
     * currently active sheet, returned by {@link #getActiveSheet()}.
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
        getActiveSheet().setDefaultRowHeightInPoints(heightPT);
        setDefRowH(heightPT);
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
     **/
    public void addRowHeaderDoubleClickListener(
            RowHeaderDoubleClickListener listener) {
        addListener(RowHeaderDoubleClickEvent.class,
                listener::onRowHeaderDoubleClick); // ,
                                                   // RowHeaderDoubleClickListener.ON_ROW_ON_ROW_HEADER_DOUBLE_CLICK);
    }

    /**
     * This method is called when column auto-fit has been initiated from the
     * browser by double-clicking the border of the target column header.
     *
     * @param columnIndex
     *            Index of the target column, 0-based
     */
    protected void onColumnAutofit(int columnIndex) {
        SizeChangeCommand command = new SizeChangeCommand(this, Type.COLUMN);
        command.captureValues(new Integer[] { columnIndex + 1 });
        autofitColumn(columnIndex);
        historyManager.addCommand(command);
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
        final Sheet activeSheet = getActiveSheet();
        try {
            activeSheet.autoSizeColumn(columnIndex);
        } catch (NullPointerException e) {
            // NullPointerException is being thrown in POI. Catch to prevent
            // breaking the UI.
            LOGGER.trace(
                    "Poi threw NullPointerException when trying to autofit column",
                    e);
            return;
        }
        int columnPixelWidth = getColumnAutofitPixelWidth(columnIndex,
                (int) activeSheet.getColumnWidthInPixels(columnIndex));

        int[] _colW = Arrays.copyOf(getColW(), getColW().length);
        _colW[columnIndex] = columnPixelWidth;
        setColW(_colW);

        getCellValueManager().clearCacheForColumn(columnIndex + 1);
        getCellValueManager().loadCellData(firstRow, columnIndex + 1, lastRow,
                columnIndex + 1);

        if (hasSheetOverlays()) {
            reloadImageSizesFromPOI = true;
            loadOrUpdateOverlays();
        }
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
        Sheet sheet = getActiveSheet();
        int lastNonBlankRow = getLastNonBlankRow(sheet);
        sheet.shiftRows(startRow, endRow, n, copyRowHeight,
                resetOriginalRowHeight);
        // need to re-send the cell values to client
        // remove all cached cell data that is now empty
        getFormulaEvaluator().clearAllCachedResultValues();
        getConditionalFormattingEvaluator().clearAllCachedValues();
        int start = n < 0 ? Math.max(lastNonBlankRow, startRow) : startRow;
        int end = n < 0 ? endRow : startRow + n - 1;
        valueManager.updateDeletedRowsInClientCache(start + 1, end + 1);
        int firstAffectedRow = n < 0 ? startRow + n : startRow;
        int lastAffectedRow = n < 0 ? endRow : endRow + n;
        if (copyRowHeight || resetOriginalRowHeight) {
            // might need to increase the size of the row heights array
            int oldLength = getRowH().length;
            int neededLength = endRow + n + 1;
            float[] _rowH = Arrays.copyOf(getRowH(), getRowH().length);
            if (n > 0 && oldLength < neededLength) {
                _rowH = Arrays.copyOf(_rowH, neededLength);
            }
            for (int i = firstAffectedRow; i <= lastAffectedRow; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    if (row.getZeroHeight()) {
                        _rowH[i] = 0f;
                    } else {
                        _rowH[i] = row.getHeightInPoints();
                    }
                } else {
                    _rowH[i] = sheet.getDefaultRowHeightInPoints();
                }
            }
            setRowH(_rowH);
        }

        if (hasSheetOverlays()) {
            reloadImageSizesFromPOI = true;
        }
        // need to shift the cell styles, clear and update
        // need to go -1 and +1 because of shifted borders..
        final ArrayList<Cell> cellsToUpdate = new ArrayList<Cell>();
        for (int r = (firstAffectedRow - 1); r <= (lastAffectedRow + 1); r++) {
            if (r < 0) {
                r = 0;
            }
            Row row = sheet.getRow(r);
            final Integer rowIndex = new Integer(r + 1);
            ArrayList<Integer> _hiddenRowIndexes = new ArrayList<>(
                    getHiddenRowIndexes());
            if (row == null) {
                valueManager.updateDeletedRowsInClientCache(rowIndex, rowIndex);
                if (_hiddenRowIndexes.contains(rowIndex)) {
                    _hiddenRowIndexes.remove(rowIndex);
                }
                for (int c = 0; c < getCols(); c++) {
                    styler.clearCellStyle(r, c);
                }
            } else {
                if (row.getZeroHeight()) {
                    _hiddenRowIndexes.add(rowIndex);
                } else if (_hiddenRowIndexes.contains(rowIndex)) {
                    _hiddenRowIndexes.remove(rowIndex);
                }
                for (int c = 0; c < getCols(); c++) {
                    Cell cell = row.getCell(c);
                    if (cell == null) {
                        styler.clearCellStyle(r, c);
                        if (r <= lastNonBlankRow + n) {
                            // There might be a pre-shift value for this cell in
                            // client-side and should be overwritten
                            cell = row.createCell(c);
                            cellsToUpdate.add(cell);
                        }
                    } else {
                        cellsToUpdate.add(cell);
                    }
                }
            }
            setHiddenRowIndexes(_hiddenRowIndexes);
        }
        rowsMoved(firstAffectedRow, lastAffectedRow, n);

        for (Cell cell : cellsToUpdate) {
            styler.cellStyleUpdated(cell, false);
            markCellAsUpdated(cell, false);
        }
        styler.loadCustomBorderStylesToState();

        updateMarkedCells(); // deleted and formula cells and style selectors
        updateRowAndColumnRangeCellData(firstRow, firstColumn, lastRow,
                lastColumn); // shifted area values
        updateMergedRegions();

        CellReference selectedCellReference = selectionManager
                .getSelectedCellReference();
        if (selectedCellReference != null) {
            if (selectedCellReference.getRow() >= firstAffectedRow
                    && selectedCellReference.getRow() <= lastAffectedRow) {
                selectionManager.onSheetAddressChanged(
                        selectedCellReference.formatAsString(), false);
            }
        }
    }

    private boolean hasSheetOverlays() {
        return sheetOverlays != null && sheetOverlays.size() > 0;
    }

    /**
     * Checks if the current column has a filter popup button and calculates
     * extra width to accommodate when to include it in autofit.
     *
     * @param columnIndex
     *            Index of the target column, 0 based
     * @param autofitWidth
     *            The autofit width without the button, in pixels
     * @return Pixel width of the column
     */
    private int getColumnAutofitPixelWidth(int columnIndex, int autofitWidth) {
        List<SpreadsheetTable> tablesForActiveSheet = getTablesForActiveSheet();
        CellReference cr = new CellReference(getActiveSheet().getSheetName(), 0,
                columnIndex, true, true);
        autofittedColumnWidths.put(cr, autofitWidth);
        for (SpreadsheetTable st : tablesForActiveSheet) {
            if (!(st instanceof SpreadsheetFilterTable)) {
                continue;
            }
            SpreadsheetFilterTable ft = (SpreadsheetFilterTable) st;
            PopupButton popupButton = ft.getPopupButton(cr);
            if (popupButton != null) {
                return autofitWidth + FILTER_BUTTON_PIXEL_WIDTH
                        + FILTER_BUTTON_PIXEL_PADDING;
            }
        }
        return autofitWidth;
    }

    /**
     * Called when number of rows has moved. Spreadsheet needs to update its
     * internal state.
     *
     * Note: If n is negative it would mean the rows has moved up. Positive
     * value indicates that new rows are moved below.
     *
     * @param first
     *            the first row that has changed, 0-based
     * @param last
     *            the last row that has changed, 0-based
     * @param n
     *            the amount of lines that rows has been moved
     */
    private void rowsMoved(int first, int last, int n) {
        // Merged regions
        if (n < 0) {
            // Remove merged cells from deleted rows. POI will handle the other
            // updated values.
            for (int row = (first + n); row <= first; ++row) {
                Sheet sheet = getActiveSheet();
                for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                    CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
                    if (mergedRegion.getFirstRow() == row) {
                        removeMergedRegion(i);
                    }
                }
            }
        }

        // PopupButtons
        if (!sheetPopupButtons.isEmpty()) {
            Map<CellReference, PopupButton> updated = new HashMap<CellReference, PopupButton>();
            for (PopupButton pbutton : sheetPopupButtons.values()) {
                CellReference cell = pbutton.getCellReference();
                unRegisterPopupButton(pbutton);
                int row = cell.getRow();
                if (rowWasRemoved(row, first, n)) {
                    // do nothing -> will be removed
                } else if (numberOfRowsAboveWasChanged(row, last, first)) {
                    int newRow = cell.getRow() + n;
                    int col = cell.getCol();
                    CellReference newCell = new CellReference(newRow, col);
                    pbutton.setCellReference(newCell);
                    updated.put(newCell, pbutton);
                } else {
                    updated.put(cell, pbutton);
                }
            }
            sheetPopupButtons = updated;
        }

        // Invalid formula indicators
        int activeSheetIndex = workbook.getActiveSheetIndex();
        HashSet<String> original = invalidFormulas.get(activeSheetIndex);
        if (original != null) {
            HashSet<String> updated = new HashSet<String>();
            for (String key : original) {
                int row = SpreadsheetUtil.getRowFromKey(key) - 1;
                int col = SpreadsheetUtil.getColumnIndexFromKey(key) - 1;
                if (rowWasRemoved(row, first, n)) {
                    // do nothing -> will be removed
                } else if (numberOfRowsAboveWasChanged(row, last, first)) {
                    // the number of the rows above has changed -> update the
                    // row index
                    updated.add(SpreadsheetUtil.toKey(col + 1, row + n + 1));
                } else {
                    updated.add(key);
                }
            }
            original.clear();
            invalidFormulas.put(activeSheetIndex, updated);
        }
    }

    private boolean numberOfRowsAboveWasChanged(int row, int last, int first) {
        return first <= row && row <= last;
    }

    private boolean rowWasRemoved(int row, int first, int n) {
        return n < 0 && first + n < row && row <= first;
    }

    /**
     * @return the common {@link FormulaEvaluator} instance.
     */
    public FormulaEvaluator getFormulaEvaluator() {
        return formulaEvaluator;
    }

    /**
     * POI, as of 4.0.0, now accepts this as an argument to formula evaluation.
     * Some conditional formats can modify the display text of a cell.
     *
     * @return the common {@link ConditionalFormattingEvaluator} instance.
     */
    public ConditionalFormattingEvaluator getConditionalFormattingEvaluator() {
        return conditionalFormattingEvaluator;
    }

    private int getLastNonBlankRow(Sheet sheet) {
        for (int r = sheet.getLastRowNum(); r >= 0; r--) {
            Row row = sheet.getRow(r);
            if (row != null) {
                for (short c = row.getFirstCellNum(); c < row
                        .getLastCellNum(); c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null && cell.getCellType() != CellType.BLANK) {
                        return r;
                    }
                }
            }
        }
        return 0;
    }

    private void updateMergedRegions() {
        int regions = getActiveSheet().getNumMergedRegions();
        if (regions > 0) {
            ArrayList<MergedRegion> _mergedRegions = new ArrayList<MergedRegion>();
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
                    _mergedRegions.add(i, mergedRegion);
                } catch (IndexOutOfBoundsException ioobe) {
                    createMergedRegionIntoSheet(region);
                }
            }
            while (regions < _mergedRegions.size()) {
                _mergedRegions.remove(_mergedRegions.size() - 1);
            }
            setMergedRegions(_mergedRegions);
        } else {
            setMergedRegions(null);
        }
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
                getActiveSheet().removeRow(row);
            }
        }
        float[] _rowH = Arrays.copyOf(getRowH(), getRowH().length);
        for (int i = startRow; i <= endRow; i++) {
            _rowH[i] = sheet.getDefaultRowHeightInPoints();
        }
        setRowH(_rowH);
        updateMergedRegions();
        valueManager.updateDeletedRowsInClientCache(startRow + 1, endRow + 1);

        if (hasSheetOverlays()) {
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
                        + existingRegion + " "
                        + (intersect == CellRangeUtil.OVERLAP ? "overlaps "
                                : "encloses ")
                        + "the given region " + region);
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
        ArrayList<MergedRegion> _mergedRegions = getMergedRegions() != null
                ? new ArrayList<>(getMergedRegions())
                : new ArrayList<MergedRegion>();

        _mergedRegions.add(addMergedRegionIndex - 1, mergedRegion);
        setMergedRegions(_mergedRegions);
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
     * If the removed region affects the currently selected cell, a new
     * {@link SelectionChangeEvent} is fired.
     *
     * @param index
     *            Position of the target merged region in the POI merged region
     *            array, 0-based
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
        ArrayList<MergedRegion> _mergedRegions = new ArrayList<>(
                getMergedRegions());
        MergedRegion mergedRegion = _mergedRegions.remove(index);
        // update the style for the region cells, effects region + 1 row&col
        for (int r = mergedRegion.row1; r <= (mergedRegion.row2 + 1); r++) {
            Row row = sheet.getRow(r - 1);
            if (row != null) {
                for (int c = mergedRegion.col1; c <= (mergedRegion.col2
                        + 1); c++) {
                    Cell cell = row.getCell(c - 1);
                    if (cell != null) {
                        styler.cellStyleUpdated(cell, false);
                        valueManager.markCellForUpdate(cell);
                    } else {
                        styler.clearCellStyle(r, c);
                    }
                }
            }
        }
        setMergedRegions(_mergedRegions);
        styler.loadCustomBorderStylesToState();
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
        SpreadsheetFactory.loadMergedRegions(this);
    }

    /**
     * Reloads all the styles for the currently active sheet.
     */
    public void reloadActiveSheetStyles() {
        styler.reloadActiveSheetCellStyles();
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
        getActiveSheet().setColumnHidden(columnIndex, hidden);
        ArrayList<Integer> _hiddenColumnIndexes = new ArrayList<>(
                getHiddenColumnIndexes());
        int[] _colW = Arrays.copyOf(getColW(), getColW().length);
        if (hidden && !_hiddenColumnIndexes.contains(columnIndex + 1)) {
            _hiddenColumnIndexes.add(columnIndex + 1);
            _colW[columnIndex] = 0;
        } else if (!hidden && _hiddenColumnIndexes.contains(columnIndex + 1)) {
            _hiddenColumnIndexes
                    .remove(_hiddenColumnIndexes.indexOf(columnIndex + 1));
            _colW[columnIndex] = (int) getActiveSheet()
                    .getColumnWidthInPixels(columnIndex);
            getCellValueManager().clearCacheForColumn(columnIndex + 1);
            getCellValueManager().loadCellData(firstRow, columnIndex + 1,
                    lastRow, columnIndex + 1);
        }
        setHiddenColumnIndexes(_hiddenColumnIndexes);
        setColW(_colW);

        if (hasSheetOverlays()) {
            reloadImageSizesFromPOI = true;
            loadOrUpdateOverlays();
        }

        getSpreadsheetStyleFactory().reloadActiveSheetCellStyles();
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
        final Sheet activeSheet = getActiveSheet();
        Row row = activeSheet.getRow(rowIndex);
        if (row == null) {
            row = activeSheet.createRow(rowIndex);
        }
        row.setZeroHeight(hidden);

        // can't assume the state already had room for the row in its
        // arrays, it may have been created above. This avoids
        // ArrayIndexOutOfBoundsException
        SpreadsheetFactory.calculateSheetSizes(this, getActiveSheet());

        if (hasSheetOverlays()) {
            reloadImageSizesFromPOI = true;
            loadOrUpdateOverlays();
        }

        getSpreadsheetStyleFactory().reloadActiveSheetCellStyles();
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
     *            or '.xlsx', the approriate one will be appended.
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
        return rowBufferSize;
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
        this.rowBufferSize = rowBufferInPixels;
        getElement().setProperty("rowBufferSize", rowBufferInPixels);
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
        return columnBufferSize;
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
        columnBufferSize = colBufferInPixels;
        getElement().setProperty("columnBufferSize", columnBufferSize);
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
     */
    public void reloadVisibleCellContents() {
        loadCustomComponents();
        updateRowAndColumnRangeCellData(firstRow, firstColumn, lastRow,
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
    protected void setResource(String key, StreamResource resource) {
        if (resource == null) {
            resources.remove(key);
            getElement().removeAttribute("resource-" + key);
        } else {
            resources.put(key, resource.toString());
            getElement().setProperty("resources",
                    Serializer.serialize(new ArrayList<>(resources.keySet())));
            getElement().setAttribute("resource-" + key, resource);
        }
    }

    protected void setResource(String key, Icon icon) {
        // todo: ver que hacemos con esto
        // super.setResource(key, resource);
    }

    void clearSheetServerSide() {
        workbook = null;
        styler = null;

        valueManager.clearCachedContent();
        selectionManager.clear();
        historyManager.clear();
        invalidFormulas.clear();
        sheetPopupButtons.clear();
        sheetState.clear();
        clearSheetOverlays();
    }

    private void clearSheetOverlays() {
        for (SheetOverlayWrapper image : sheetOverlays) {
            removeOverlayData(image);
        }
        sheetOverlays.clear();
    }

    void setInternalWorkbook(Workbook workbook) {
        this.workbook = workbook;
        formulaEvaluator = workbook.getCreationHelper()
                .createFormulaEvaluator();
        // currently all formula implementations extend BaseFormulaEvaluator
        conditionalFormattingEvaluator = new ConditionalFormattingEvaluator(
                workbook, (BaseFormulaEvaluator) formulaEvaluator);

        styler = createSpreadsheetStyleFactory();

        reloadActiveSheetData();
        if (workbook instanceof HSSFWorkbook) {
            setWorkbookProtected(((HSSFWorkbook) workbook).isWriteProtected());
        } else if (workbook instanceof XSSFWorkbook) {
            setWorkbookProtected(((XSSFWorkbook) workbook).isStructureLocked());
        }
        // clear all tables from memory
        tables.clear();

        setVerticalScrollPositions(new int[getSheetNames().length]);
        setHorizontalScrollPositions(new int[getSheetNames().length]);

        conditionalFormatter = createConditionalFormatter();

        setWorkbookChangeToggle(!isWorkbookChangeToggle());
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

        firstColumn = lastColumn = firstRow = lastRow = -1;
        clearSheetOverlays();
        topLeftCellCommentsLoaded = false;

        setReload(true);

        setSheetIndex(
                getSpreadsheetSheetIndex(workbook.getActiveSheetIndex()) + 1);
        setSheetProtected(getActiveSheet().getProtect());
        setCellKeysToEditorIdMap(null);
        setHyperlinksTooltips(null);
        setComponentIDtoCellKeysMap(null);
        setOverlays(null);
        setMergedRegions(null);
        setCellComments(null);
        setCellCommentAuthors(null);
        setVisibleCellComments(null);
        setInvalidFormulaCells(null);

        for (Component c : customComponents) {
            unRegisterCustomComponent(c);
        }
        customComponents.clear();

        if (attachedPopupButtons != null && !attachedPopupButtons.isEmpty()) {
            for (PopupButton sf : new ArrayList<PopupButton>(
                    attachedPopupButtons)) {
                unRegisterPopupButton(sf);
            }
            attachedPopupButtons.clear();
        }

        // clear all tables, possible tables for new/changed sheet are added
        // after first round trip.
        tablesLoaded = false;

        reloadSheetNames();
        updateMergedRegions();
        styler.reloadActiveSheetColumnRowStyles();
        setDisplayGridlines(getActiveSheet().isDisplayGridlines());
        setDisplayRowColHeadings(getActiveSheet().isDisplayRowColHeadings());

        markAsDirty();
    }

    private void markAsDirty() {
        getElement().setProperty("dirty", System.currentTimeMillis());
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
            HashMap<String, String> cellKeysToEditorIdMap = new HashMap<>(
                    getCellKeysToEditorIdMap());
            if (cellKeysToEditorIdMap != null
                    && cellKeysToEditorIdMap.containsKey(key)
                    && customComponents != null) {
                String componentId = cellKeysToEditorIdMap.get(key);
                for (Component c : customComponents) {
                    if (c.getId().orElse("").equals(componentId)) {
                        // todo: ver que hacemos con esto
                        // if (c.getConnectorId().equals(componentId)) {
                        customComponentFactory.onCustomEditorDisplayed(
                                getCell(row, col), row, col, this,
                                getActiveSheet(), c);
                        return;
                    }
                }
            }
            setCellKeysToEditorIdMap(cellKeysToEditorIdMap);
        }
    }

    private void reloadSheetNames() {
        final ArrayList<String> sheetNamesList = new ArrayList<String>();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (!workbook.isSheetVeryHidden(i) && !workbook.isSheetHidden(i)) {
                sheetNamesList.add(workbook.getSheetName(i));
            }
        }
        setSheetNames(
                sheetNamesList.toArray(new String[sheetNamesList.size()]));
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
    private int getSpreadsheetSheetIndex(int poiSheetIndex) {
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
     * Gets the locked state of the given cell.
     *
     * @param cell
     *            The cell to check
     * @return true if the cell is locked, false otherwise
     */
    public boolean isCellLocked(Cell cell) {
        if (isActiveSheetProtected()) {
            if (cell != null) {
                if (cell.getCellStyle().getIndex() != 0) {
                    return cell.getCellStyle().getLocked();
                } else {
                    return getLockedColumnIndexes()
                            .contains(cell.getColumnIndex() + 1)
                            && getLockedRowIndexes()
                                    .contains(cell.getRowIndex() + 1);
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Gets the RPC proxy for communication to the client side.
     *
     * @return Client RPC proxy instance
     */
    protected SpreadsheetClientRpc getRpcProxy() {
        return clientRpc;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.ui.AbstractComponent#beforeClientResponse(boolean)
     */
    // @Override
    public void beforeClientResponse(boolean initial) {
        // todo: reubicar este código
        // super.beforeClientResponse(initial);
        if (reload) {
            setReload(reload);
            reload = false;
            if (initialSheetSelection == null) {
                if (sheetState
                        .getSelectedCellsOnSheet(getActiveSheet()) == null) {
                    initialSheetSelection = "A1";
                } else {
                    initialSheetSelection = sheetState
                            .getSelectedCellsOnSheet(getActiveSheet());
                }
            }
        } else {
            setReload(reload);
        }
    }

    /**
     * Gets the currently used style factory for this Spreadsheet.
     *
     * @return The current style factory.
     */
    public SpreadsheetStyleFactory getSpreadsheetStyleFactory() {
        return styler;
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
    private void updateRowAndColumnRangeCellData(int r1, int c1, int r2,
            int c2) {
        // FIXME should be optimized, should not go through all links, comments
        // etc. always
        loadHyperLinks();
        loadCellComments();
        loadOrUpdateOverlays();
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
        loadHyperLinks();
        loadCellComments();
        loadOrUpdateOverlays();
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

    void onRowResized(Map<Integer, Float> newRowSizes, int row1, int col1,
            int row2, int col2) {
        SizeChangeCommand command = new SizeChangeCommand(this, Type.ROW);
        command.captureValues(
                newRowSizes.keySet().toArray(new Integer[newRowSizes.size()]));
        historyManager.addCommand(command);
        for (Entry<Integer, Float> entry : newRowSizes.entrySet()) {
            int index = entry.getKey();
            float height = entry.getValue();
            setRowHeight(index - 1, height);
        }

        if (hasSheetOverlays()) {
            reloadImageSizesFromPOI = true;
        }
        loadCells(row1, col1, row2, col2);
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
        if (height == 0.0F) {
            setRowHidden(index, true);
        } else {
            Row row = getActiveSheet().getRow(index);
            ArrayList<Integer> _hiddenRowIndexes = new ArrayList<>(
                    getHiddenRowIndexes());
            if (_hiddenRowIndexes.contains(Integer.valueOf(index + 1))) {
                _hiddenRowIndexes.remove(Integer.valueOf(index + 1));
                if (row != null && row.getZeroHeight()) {
                    row.setZeroHeight(false);
                }
            }
            if (row == null) {
                row = getActiveSheet().createRow(index);
            }
            row.setHeightInPoints(height);
            setHiddenRowIndexes(_hiddenRowIndexes);
            // can't assume the state already had room for the row in its
            // arrays, it may have been created above. This avoids
            // ArrayIndexOutOfBoundsException
            SpreadsheetFactory.calculateSheetSizes(this, getActiveSheet());
        }
    }

    void onColumnResized(Map<Integer, Integer> newColumnSizes, int row1,
            int col1, int row2, int col2) {
        SizeChangeCommand command = new SizeChangeCommand(this, Type.COLUMN);
        command.captureValues(newColumnSizes.keySet()
                .toArray(new Integer[newColumnSizes.size()]));
        historyManager.addCommand(command);
        for (Entry<Integer, Integer> entry : newColumnSizes.entrySet()) {
            int index = entry.getKey();
            int width = entry.getValue();
            setColumnWidth(index - 1, width);
        }

        if (hasSheetOverlays()) {
            reloadImageSizesFromPOI = true;
        }
        loadCells(row1, col1, row2, col2);
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
        if (width == 0) {
            setColumnHidden(index, true);
        } else {
            ArrayList<Integer> _hiddenColumnIndexes = new ArrayList<>(
                    getHiddenColumnIndexes());
            int[] _colW = Arrays.copyOf(getColW(), getColW().length);
            if (_hiddenColumnIndexes.contains(Integer.valueOf(index + 1))) {
                _hiddenColumnIndexes.remove(Integer.valueOf(index + 1));
            }
            if (getActiveSheet().isColumnHidden(index)) {
                getActiveSheet().setColumnHidden(index, false);
            }
            _colW[index] = width;
            setColW(_colW);
            setHiddenColumnIndexes(_hiddenColumnIndexes);
            getActiveSheet().setColumnWidth(index,
                    SpreadsheetUtil.pixel2WidthUnits(width));

            if (getActiveSheet() instanceof XSSFSheet) {
                ((XSSFSheet) getActiveSheet()).getColumnHelper().cleanColumns();
            }

            getCellValueManager().clearCacheForColumn(index + 1);
            getCellValueManager().loadCellData(firstRow, index + 1, lastRow,
                    index + 1);
        }
    }

    void loadHyperLinks() {
        HashMap<String, String> _hyperlinksTooltips = getHyperlinksTooltips() != null
                ? new HashMap<>(getHyperlinksTooltips())
                : null;
        if (_hyperlinksTooltips == null) {
            _hyperlinksTooltips = new HashMap<String, String>();
        } else {
            _hyperlinksTooltips.clear();
        }
        setHyperlinksTooltips(_hyperlinksTooltips);
        // removed && !topLeftCellHyperlinksLoaded as it was always false
        if (getLastFrozenRow() > 0 && getLastFrozenColumn() > 0) {
            loadHyperLinks(1, 1, getLastFrozenRow(), getLastFrozenColumn());
        }
        if (getLastFrozenRow() > 0) {
            loadHyperLinks(1, firstColumn, getLastFrozenRow(), lastColumn);
        }
        if (getLastFrozenColumn() > 0) {
            loadHyperLinks(firstRow, 1, lastRow, getLastFrozenColumn());
        }
        loadHyperLinks(firstRow, firstColumn, lastRow, lastColumn);
    }

    private void loadHyperLinks(int r1, int c1, int r2, int c2) {
        HashMap<String, String> _hyperlinksTooltips = getHyperlinksTooltips();
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
                                    _hyperlinksTooltips.put(
                                            SpreadsheetUtil.toKey(c + 1, r + 1),
                                            tooltip);
                                } else {
                                    _hyperlinksTooltips.put(
                                            SpreadsheetUtil.toKey(c + 1, r + 1),
                                            link.getAddress());
                                }
                            } else {
                                // Check if the cell has HYPERLINK function
                                if (DefaultHyperlinkCellClickHandler
                                        .isHyperlinkFormulaCell(cell)
                                        && hyperlinkCellClickHandler != null) {
                                    _hyperlinksTooltips.put(
                                            SpreadsheetUtil.toKey(c + 1, r + 1),
                                            hyperlinkCellClickHandler
                                                    .getHyperlinkFunctionTarget(
                                                            cell));
                                }
                            }
                        } catch (XmlValueDisconnectedException exc) {
                            LOGGER.trace(exc.getMessage(), exc);
                        }
                    }
                }
            }
        }
        setHyperlinksTooltips(_hyperlinksTooltips);
    }

    private void loadOrUpdateOverlays() {
        // Fixes the issue of overlays being lost when creating or removing
        // frozen rows/columns. More like a kludge, a real solution is yet to be
        // found.
        if (!hasSheetOverlays()) {
            SpreadsheetFactory.loadSheetOverlays(this);
        }

        if (hasSheetOverlays()) {
            // reload images from POI because row / column sizes have changed
            // currently doesn't effect anything because POI doesn't update the
            // image anchor data after resizing
            if (reloadImageSizesFromPOI) {
                clearSheetOverlays();
                SpreadsheetFactory.loadSheetOverlays(this);
                reloadImageSizesFromPOI = false;
            }

            for (final SheetOverlayWrapper overlay : sheetOverlays) {
                if (isOverlayVisible(overlay)) {
                    addOverlayData(overlay);
                    overlay.setVisible(true);
                } else {
                    // was visible but went out of visibility
                    if (overlay.isVisible()) {
                        removeOverlayData(overlay);
                        overlay.setVisible(false);
                    }
                }
            }
        }
    }

    /**
     * Adds necessary data to display the overlay in the current view.
     */
    private void addOverlayData(final SheetOverlayWrapper overlay) {
        if (overlay.getComponent(true) != null) {
            registerCustomComponent(overlay.getComponent(true));
            overlayComponents.add(overlay.getComponent(true));
        }

        if (overlay.getId() != null && overlay.getResource() != null) {
            setResource(overlay.getId(), overlay.getResource());
        }

        if (overlay.getId() != null) {
            HashMap<String, OverlayInfo> _overlays = getOverlays() != null
                    ? new HashMap<>(getOverlays())
                    : new HashMap<>();
            _overlays.put(overlay.getId(), createOverlayInfo(overlay));
            setOverlays(_overlays);

            overlay.setOverlayChangeListener(new OverlayChangeListener() {
                @Override
                public void overlayChanged() {
                    loadOrUpdateOverlays();
                }
            });
        }
    }

    /**
     * Undoes what addOverlayData did.
     */
    private void removeOverlayData(final SheetOverlayWrapper overlay) {
        if (overlay.getId() != null) {
            if (getOverlays() != null) {
                HashMap<String, OverlayInfo> _overlays = getOverlays();
                _overlays.remove(overlay.getId());
                setOverlays(_overlays);
            }
            setResource(overlay.getId(), (StreamResource) null);
        }

        if (overlay.getComponent(false) != null) {
            overlayComponents.remove(overlay.getComponent(false));
            unRegisterCustomComponent(overlay.getComponent(false));
        }
    }

    /**
     * Decides if overlay is visible in the current view.
     */
    private boolean isOverlayVisible(SheetOverlayWrapper overlay) {
        int col1 = overlay.getAnchor().getCol1();
        int col2 = overlay.getAnchor().getCol2();
        int row1 = overlay.getAnchor().getRow1();
        int row2 = overlay.getAnchor().getRow2();

        // type=2, doesn't size with cells
        final boolean isType2 = (col2 == 0 && row2 == 0);

        if (!isType2) {
            // to ensure compatibility with grouping/hidden columns
            if (isColumnRangeHidden(col1, col2)
                    || isRowRangeHidden(row1, row2)) {
                return false;
            }
        }

        int horizontalSplitPosition = getLastFrozenColumn();
        int verticalSplitPosition = getLastFrozenRow();

        // the sheet is divided into four areas by vertical and horizontal split

        boolean visibleInArea1 = horizontalSplitPosition > 0
                && verticalSplitPosition > 0 && overlay.isVisible(1, 1,
                        verticalSplitPosition, horizontalSplitPosition);

        boolean visibleInArea2 = horizontalSplitPosition > 0 && overlay
                .isVisible(firstRow, 1, lastRow, horizontalSplitPosition);

        boolean visibleInArea3 = verticalSplitPosition > 0 && overlay
                .isVisible(1, firstColumn, verticalSplitPosition, lastColumn);

        boolean visibleInArea4 = overlay.isVisible(firstRow, firstColumn,
                lastRow, lastColumn);

        return visibleInArea1 || visibleInArea2 || visibleInArea3
                || visibleInArea4;
    }

    /**
     * Return true if all the rows in the range are hidden (including row2).
     */
    private boolean isRowRangeHidden(int row1, int row2) {
        for (int row = row1; row <= row2; row++) {
            if (!isRowHidden(row)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Return true if all the columns in the range are hidden (including col2).
     */
    private boolean isColumnRangeHidden(int col1, int col2) {
        for (int col = col1; col <= col2; col++) {
            if (!isColumnHidden(col)) {
                return false;
            }
        }

        return true;
    }

    private OverlayInfo createOverlayInfo(SheetOverlayWrapper overlayWrapper) {
        OverlayInfo info = new OverlayInfo(overlayWrapper.getType());

        Sheet sheet = getActiveSheet();

        int col = overlayWrapper.getAnchor().getCol1();
        while (isColumnHidden(col)) {
            col++;
        }

        int row = overlayWrapper.getAnchor().getRow1();
        while (isRowHidden(row)) {
            row++;
        }

        info.col = col + 1; // 1-based
        info.row = row + 1; // 1-based

        info.height = overlayWrapper.getHeight(sheet, getRowH());
        info.width = overlayWrapper.getWidth(sheet, getColW(), getDefColW());

        // FIXME: height and width can be -1, it is never handled anywhere

        // if original start row/column is hidden, use 0 dy/dx
        if (col == overlayWrapper.getAnchor().getCol1()) {
            info.dx = overlayWrapper.getDx1(sheet);
        }

        if (row == overlayWrapper.getAnchor().getRow1()) {
            info.dy = overlayWrapper.getDy1(sheet);
        }

        return info;
    }

    private void loadCellComments() {

        if (firstColumn == -1) {
            // Spreadsheet not loaded. This method will be called again.
            return;
        }

        HashMap<String, String> _cellComments = getCellComments() != null
                ? new HashMap<>(getCellComments())
                : null;
        if (_cellComments == null) {
            _cellComments = new HashMap<>();
        } else {
            _cellComments.clear();
        }
        setCellComments(_cellComments);

        HashMap<String, String> _cellCommentAuthors = getCellCommentAuthors() != null
                ? new HashMap<>(getCellCommentAuthors())
                : null;
        if (_cellCommentAuthors == null) {
            _cellCommentAuthors = new HashMap<String, String>();
        } else {
            _cellCommentAuthors.clear();
        }
        setCellCommentAuthors(_cellCommentAuthors);

        ArrayList<String> _visibleCellComments = getVisibleCellComments() != null
                ? new ArrayList<>(getVisibleCellComments())
                : null;
        if (_visibleCellComments == null) {
            _visibleCellComments = new ArrayList<String>();
        } else {
            _visibleCellComments.clear();
        }
        setVisibleCellComments(_visibleCellComments);

        Set<String> _invalidFormulaCells = getInvalidFormulaCells() != null
                ? new HashSet<>(getInvalidFormulaCells())
                : null;
        if (_invalidFormulaCells == null) {
            _invalidFormulaCells = new HashSet<String>();
        } else {
            _invalidFormulaCells.clear();
        }
        setInvalidFormulaCells(_invalidFormulaCells);

        if (getLastFrozenRow() > 0 && getLastFrozenColumn() > 0
                && !topLeftCellCommentsLoaded) {
            loadCellComments(1, 1, getLastFrozenRow(), getLastFrozenColumn());
        }
        if (getLastFrozenRow() > 0) {
            loadCellComments(1, firstColumn, getLastFrozenRow(), lastColumn);
        }
        if (getLastFrozenColumn() > 0) {
            loadCellComments(firstRow, 1, lastRow, getLastFrozenColumn());
        }
        loadCellComments(firstRow, firstColumn, lastRow, lastColumn);
    }

    private void loadCellComments(int r1, int c1, int r2, int c2) {
        Sheet sheet = getActiveSheet();
        HashMap<String, String> _cellComments = new HashMap<>(
                getCellComments());
        HashMap<String, String> _cellCommentAuthors = new HashMap<>(
                getCellCommentAuthors());
        ArrayList<String> _visibleCellComments = new ArrayList<>(
                getVisibleCellComments());
        Set<String> _invalidFormulaCells = new HashSet<>(
                getInvalidFormulaCells());
        for (int r = r1 - 1; r < r2; r++) {
            Row row = sheet.getRow(r);
            if (row != null && row.getZeroHeight()) {
                continue;
            }
            for (int c = c1 - 1; c < c2; c++) {
                if (sheet.isColumnHidden(c)) {
                    continue;
                }

                int c_one_based = c + 1;
                int row_one_based = r + 1;

                MergedRegion region = mergedRegionContainer
                        .getMergedRegion(c_one_based, row_one_based);
                // do not add comments that are "below" merged regions.
                // client side handles cases where comment "moves" (because
                // shifting etc.) from merged cell into basic or vice versa.
                if (region == null || region.col1 == c_one_based
                        && region.row1 == row_one_based) {
                    Comment comment = sheet
                            .getCellComment(new CellAddress(r, c));
                    String key = SpreadsheetUtil.toKey(c_one_based,
                            row_one_based);
                    if (comment != null) {
                        // by default comments are shown when mouse is over the
                        // red
                        // triangle on the cell's top right corner. the comment
                        // position is calculated so that it is completely
                        // visible.
                        _cellComments.put(key, comment.getString().getString());
                        _cellCommentAuthors.put(key, comment.getAuthor());
                        if (comment.isVisible()) {
                            _visibleCellComments.add(key);
                        }
                    }
                    if (isMarkedAsInvalidFormula(c_one_based, row_one_based)) {
                        _invalidFormulaCells.add(key);
                    }

                } else {
                    c = region.col2 - 1;
                }
            }
        }
        setCellComments(_cellComments);
        setCellCommentAuthors(_cellCommentAuthors);
        setVisibleCellComments(_visibleCellComments);
        setInvalidFormulaCells(_invalidFormulaCells);
    }

    /**
     * Loads the custom components for the currently viewed cells and clears
     * previous components that are not currently visible.
     */
    private void loadCustomComponents() {
        if (customComponentFactory != null) {
            HashMap<String, String> _cellKeysToEditorIdMap = getCellKeysToEditorIdMap() != null
                    ? new HashMap<>(getCellKeysToEditorIdMap())
                    : null;
            if (_cellKeysToEditorIdMap == null) {
                _cellKeysToEditorIdMap = new HashMap<String, String>();
            } else {
                _cellKeysToEditorIdMap.clear();
            }
            setCellKeysToEditorIdMap(_cellKeysToEditorIdMap);
            HashMap<String, String> _componentIDtoCellKeysMap = getComponentIDtoCellKeysMap() != null
                    ? new HashMap<>(getComponentIDtoCellKeysMap())
                    : null;
            if (_componentIDtoCellKeysMap == null) {
                _componentIDtoCellKeysMap = new HashMap<String, String>();
            } else {
                _componentIDtoCellKeysMap.clear();
            }
            setComponentIDtoCellKeysMap(_componentIDtoCellKeysMap);
            if (customComponents == null) {
                customComponents = new HashSet<Component>();
            }
            HashSet<Component> newCustomComponents = new HashSet<Component>();
            Set<Integer> rowsWithComponents = new HashSet<Integer>();
            // iteration indexes 0-based
            int verticalSplitPosition = getLastFrozenRow();
            int horizontalSplitPosition = getLastFrozenColumn();
            if (verticalSplitPosition > 0 && horizontalSplitPosition > 0) {
                // top left pane
                loadRangeComponents(newCustomComponents, rowsWithComponents, 1,
                        1, verticalSplitPosition, horizontalSplitPosition);
            }
            if (verticalSplitPosition > 0) {
                // top right pane
                loadRangeComponents(newCustomComponents, rowsWithComponents, 1,
                        firstColumn, verticalSplitPosition, lastColumn);
            }
            if (horizontalSplitPosition > 0) {
                // bottom left pane
                loadRangeComponents(newCustomComponents, rowsWithComponents,
                        firstRow, 1, lastRow, horizontalSplitPosition);
            }
            loadRangeComponents(newCustomComponents, rowsWithComponents,
                    firstRow, firstColumn, lastRow, lastColumn);
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

            if (!rowsWithComponents.isEmpty()) {
                handleRowSizes(rowsWithComponents);
            }

        } else {
            setCellKeysToEditorIdMap(null);
            setComponentIDtoCellKeysMap(null);
            if (customComponents != null && !customComponents.isEmpty()) {
                for (Component c : customComponents) {
                    unRegisterCustomComponent(c);
                }
                customComponents.clear();
            }
            handleRowSizes(new HashSet<Integer>());
        }
    }

    void loadRangeComponents(HashSet<Component> newCustomComponents,
            Set<Integer> rowsWithComponents, int row1, int col1, int row2,
            int col2) {
        HashMap<String, String> _componentIDtoCellKeysMap = getComponentIDtoCellKeysMap();
        HashMap<String, String> _cellKeysToEditorIdMap = getCellKeysToEditorIdMap();
        for (int r = row1 - 1; r < row2; r++) {
            final Row row = getActiveSheet().getRow(r);
            for (int c = col1 - 1; c < col2; c++) {
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
                        final String key = SpreadsheetUtil.toKey(c + 1, r + 1);
                        if (!customComponents.contains(customComponent)) {
                            registerCustomComponent(customComponent);
                        }
                        _componentIDtoCellKeysMap.put(
                                // todo: revisar
                                customComponent.getId().orElse(""),
                                // customComponent.getConnectorId(),
                                key);
                        newCustomComponents.add(customComponent);
                        rowsWithComponents.add(r);
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
                            _cellKeysToEditorIdMap.put(key,
                                    // todo: revisar
                                    customEditor.getId().orElse("")
                            // customEditor.getConnectorId()
                            );
                            newCustomComponents.add(customEditor);
                            rowsWithComponents.add(r);
                        }
                    }
                }
                if (region != null) {
                    c = region.col2 - 1;
                }
            }
        }
        setCellKeysToEditorIdMap(_cellKeysToEditorIdMap);
        setComponentIDtoCellKeysMap(_componentIDtoCellKeysMap);
    }

    private void handleRowSizes(Set<Integer> rowsWithComponents) {
        // Set larger height for new rows with components
        float[] _rowH = Arrays.copyOf(getRowH(), getRowH().length);
        for (Integer row : rowsWithComponents) {
            if (isRowHidden(row)) {
                continue;
            }
            float currentHeight = _rowH[row];
            if (currentHeight < getMinimumRowHeightForComponents()) {
                _rowH[row] = getMinimumRowHeightForComponents();
            }
        }
        // Reset row height for rows which no longer have components
        if (this.rowsWithComponents != null) {
            Sheet activeSheet = getActiveSheet();
            for (Integer row : this.rowsWithComponents) {
                if (!rowsWithComponents.contains(row)) {
                    if (isRowHidden(row)) {
                        _rowH[row] = 0;
                    } else {
                        Row r = activeSheet.getRow(row);
                        if (r == null) {
                            _rowH[row] = activeSheet
                                    .getDefaultRowHeightInPoints();
                        } else {
                            _rowH[row] = r.getHeightInPoints();
                        }
                    }
                }
            }
        }
        setRowH(_rowH);

        this.rowsWithComponents = rowsWithComponents;
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
    private boolean isCellVisible(int row, int col) {
        int verticalSplitPosition = getLastFrozenRow();
        int horizontalSplitPosition = getLastFrozenColumn();
        return (col >= firstColumn && col <= lastColumn && row >= firstRow
                && row <= lastRow)
                || (col >= 1 && col <= horizontalSplitPosition && row >= 1
                        && row <= verticalSplitPosition)
                || (col >= firstColumn && col <= lastColumn && row >= 1
                        && row <= verticalSplitPosition)
                || (col >= 1 && col <= horizontalSplitPosition
                        && row >= firstRow && row <= lastRow);
    }

    private void registerPopupButton(PopupButton button) {
        if (popupButtonsEnabled) {
            attachedPopupButtons.add(button);
            registerCustomComponent(button);
        }
    }

    private void unRegisterPopupButton(PopupButton button) {
        attachedPopupButtons.remove(button);
        unRegisterCustomComponent(button);
    }

    private void registerCustomComponent(PopupButton component) {
        List<PopupButtonState> popupButtonStates = attachedPopupButtons.stream()
                .map(p -> p.getState()).collect(Collectors.toList());
        getElement().setProperty("popupbuttons",
                Serializer.serialize(popupButtonStates));
    }

    private void registerCustomComponent(Component component) {
        if (!equals(component.getParent())) {
            // todo: se puede eliminar esto? en v8, setparent provoca que se
            // añada el componente en la jerarquía
            // component.setParent(this);
        }
    }

    private void unRegisterCustomComponent(PopupButton component) {
        getElement().removeProperty("popupbuttons");
    }

    private void unRegisterCustomComponent(Component component) {
        // todo: se puede eliminar esto?
        // component.setParent(null);
    }

    /**
     * Set a new component factory for this Spreadsheet. If a {@link Workbook}
     * has been set, all components will be reloaded.
     *
     * @param customComponentFactory
     *            The new component factory to use.
     */
    public void setSpreadsheetComponentFactory(
            SpreadsheetComponentFactory customComponentFactory) {
        this.customComponentFactory = customComponentFactory;
        if (firstRow != -1) {
            loadCustomComponents();
            loadCustomEditorOnSelectedCell();
        } else {
            setCellKeysToEditorIdMap(null);
            if (customComponents != null && !customComponents.isEmpty()) {
                for (Component c : customComponents) {
                    unRegisterCustomComponent(c);
                }
                customComponents.clear();
            }
        }
    }

    /**
     * Gets the current SpreadsheetComponentFactory.
     *
     * @return The currently used component factory.
     */
    public SpreadsheetComponentFactory getSpreadsheetComponentFactory() {
        return customComponentFactory;
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
        setPopup(new CellReference(row, col), popupButton);
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
        removePopupButton(cellReference);
        if (popupButton != null) {
            CellReference absoluteCellReference = SpreadsheetUtil
                    .relativeToAbsolute(this, cellReference);
            popupButton.setCellReference(absoluteCellReference);
            sheetPopupButtons.put(absoluteCellReference, popupButton);
            if (isCellVisible(absoluteCellReference.getRow() + 1,
                    absoluteCellReference.getCol() + 1)) {
                registerPopupButton(popupButton);
                markAsDirty();
            }
        }
    }

    private void removePopupButton(CellReference cellReference) {
        CellReference absoluteCellReference = SpreadsheetUtil
                .relativeToAbsolute(this, cellReference);
        PopupButton oldButton = sheetPopupButtons.get(absoluteCellReference);
        if (oldButton != null) {
            unRegisterPopupButton(oldButton);
            sheetPopupButtons.remove(absoluteCellReference);
            markAsDirty();
        }
    }

    /**
     * Registers and unregister pop-up button components for the currently
     * visible cells.
     */
    private void loadPopupButtons() {
        if (sheetPopupButtons != null) {
            for (PopupButton popupButton : sheetPopupButtons.values()) {
                if (getActiveSheet().getSheetName().equals(
                        popupButton.getCellReference().getSheetName())) {
                    int column = popupButton.getColumn() + 1;
                    int row = popupButton.getRow() + 1;
                    if (isCellVisible(row, column)) {
                        registerPopupButton(popupButton);
                    } else {
                        unRegisterPopupButton(popupButton);
                    }
                }
            }
        }
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
        tables.add(table);
        if (table instanceof SpreadsheetFilterTable) {
            updateAutofittedColumns((SpreadsheetFilterTable) table);
        }
    }

    /**
     * When adding a filter table, re-run autofit for columns that haven't been
     * resized since the last autofit
     *
     * @param table
     *            The SpreadsheetFilterTable that was added
     */
    private void updateAutofittedColumns(SpreadsheetFilterTable table) {
        Sheet filteredSheet = table.getSheet();
        CellRangeAddress fullTableRegion = table.getFullTableRegion();
        int firstColumn = fullTableRegion.getFirstColumn();
        int lastColumn = fullTableRegion.getLastColumn();
        for (int i = firstColumn; i <= lastColumn; i++) {
            CellReference cr = new CellReference(filteredSheet.getSheetName(),
                    0, i, true, true);
            if (!autofittedColumnWidths.containsKey(cr)) {
                continue;
            }
            Integer autofittedWidth = autofittedColumnWidths.get(cr);
            int currentWidth = (int) filteredSheet
                    .getColumnWidthInPixels(cr.getCol());
            // only update columns that haven't changed size since the last
            // autofit
            if (currentWidth == autofittedWidth) {
                autofitColumn(cr.getCol());
            }
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
        tables.remove(table);
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
        unregisterTable(table);
        if (table.isTableSheetCurrentlyActive()) {
            for (PopupButton popupButton : table.getPopupButtons()) {
                removePopupButton(popupButton.getCellReference());
            }
            if (table instanceof SpreadsheetFilterTable) {
                ((SpreadsheetFilterTable) table).clearAllFilters();
            }
            table.clear();
        }
    }

    /**
     * Gets all the tables that have been registered to this Spreadsheet. See
     * {@link #registerTable(SpreadsheetTable)}.
     *
     * @return All tables for this spreadsheet
     */
    public HashSet<SpreadsheetTable> getTables() {
        return tables;
    }

    /**
     * Gets the tables that belong to the currently active sheet (
     * {@link #getActiveSheet()}). See {@link #registerTable(SpreadsheetTable)}.
     *
     * @return All tables for the currently active sheet
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

    /**
     * Reload tables for current sheet
     */
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
     * This is a parent class for a value change events.
     */
    public abstract static class ValueChangeEvent
            extends ComponentEvent<Component> {
        private final Set<CellReference> changedCells;

        public ValueChangeEvent(Component source,
                Set<CellReference> changedCells) {
            super(source, false);
            this.changedCells = changedCells;
        }

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
         *
         * @return A combination of all selected cells, regardless of selection
         *         mode. Doesn't contain duplicates.
         */
        public Set<CellReference> getAllSelectedCells() {
            return Spreadsheet.getAllSelectedCells(selectedCellReference,
                    individualSelectedCells, cellRangeAddresses);

        }
    }

    private static Set<CellReference> getAllSelectedCells(
            CellReference selectedCellReference,
            List<CellReference> individualSelectedCells,
            List<CellRangeAddress> cellRangeAddresses) {
        Set<CellReference> cells = new HashSet<CellReference>();
        for (CellReference r : individualSelectedCells) {
            cells.add(r);
        }
        cells.add(selectedCellReference);

        if (cellRangeAddresses != null) {
            for (CellRangeAddress a : cellRangeAddresses) {

                for (int x = a.getFirstColumn(); x <= a.getLastColumn(); x++) {
                    for (int y = a.getFirstRow(); y <= a.getLastRow(); y++) {
                        cells.add(new CellReference(y, x));
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
     */
    public void addSelectionChangeListener(SelectionChangeListener listener) {
        addListener(SelectionChangeEvent.class, listener::onSelectionChange); // ,
                                                                              // SelectionChangeListener.SELECTION_CHANGE_METHOD);
    }

    /**
     * Adds the given CellValueChangeListener to this Spreadsheet.
     *
     * @param listener
     *            Listener to add.
     */
    public void addCellValueChangeListener(CellValueChangeListener listener) {
        addListener(CellValueChangeEvent.class, listener::onCellValueChange); // ,
                                                                              // CellValueChangeListener.CELL_VALUE_CHANGE_METHOD);
    }

    /**
     * Adds the given FormulaValueChangeListener to this Spreadsheet.
     *
     * @param listener
     *            Listener to add.
     */
    public void addFormulaValueChangeListener(
            FormulaValueChangeListener listener) {
        addListener(FormulaValueChangeEvent.class,
                listener::onFormulaValueChange); // ,
                                                 // FormulaValueChangeListener.FORMULA_VALUE_CHANGE_METHOD);
    }

    /**
     * Removes the given SelectionChangeListener from this Spreadsheet.
     *
     * @param listener
     *            Listener to remove.
     */
    public void removeSelectionChangeListener(
            SelectionChangeListener listener) {
        // todo: el método removeListener no existe en Component
        // removeListener(SelectionChangeEvent.class, listener,
        // SelectionChangeListener.SELECTION_CHANGE_METHOD);
    }

    /**
     * Removes the given CellValueChangeListener from this Spreadsheet.
     *
     * @param listener
     *            Listener to remove.
     */
    public void removeCellValueChangeListener(
            CellValueChangeListener listener) {
        // todo: el método removeListener no existe en Component
        // removeListener(CellValueChangeEvent.class, listener,
        // CellValueChangeListener.CELL_VALUE_CHANGE_METHOD);
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
     */
    public void addProtectedEditListener(ProtectedEditListener listener) {
        addListener(ProtectedEditEvent.class, listener::writeAttempted); // ,
                                                                         // ProtectedEditListener.SELECTION_CHANGE_METHOD);
    }

    /**
     * Removes the given ProtectedEditListener.
     *
     * @param listener
     *            The listener to remove.
     */
    public void removeProtectedEditListener(ProtectedEditListener listener) {
        // todo: el método removeListener no existe en Component
        // removeListener(ProtectedEditEvent.class, listener,
        // ProtectedEditListener.SELECTION_CHANGE_METHOD);
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
            return new HashSet<CellReference>();
        } else {
            return event.getAllSelectedCells();
        }
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
     */
    public void addSheetChangeListener(SheetChangeListener listener) {
        addListener(SheetChangeEvent.class, listener::onSheetChange); // ,
                                                                      // SheetChangeListener.SHEET_CHANGE_METHOD);
    }

    /**
     * Removes the given SheetChangeListener from this Spreadsheet.
     *
     * @param listener
     *            Listener to remove
     */
    public void removeSheetChangeListener(SheetChangeListener listener) {
        // todo: el método removeListener no existe en Component
        // removeListener(SheetChangeEvent.class, listener,
        // SheetChangeListener.SHEET_CHANGE_METHOD);
    }

    private void fireSheetChangeEvent(Sheet previousSheet, Sheet newSheet) {
        int newSheetPOIIndex = workbook.getActiveSheetIndex();

        fireEvent(new SheetChangeEvent(this, newSheet, previousSheet,
                getSpreadsheetSheetIndex(newSheetPOIIndex), newSheetPOIIndex));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.ui.HasComponents#iterator()
     */
    // @Override
    // todo: comprobar si esto es necesario
    public Iterator<Component> iterator() {
        return new IteratorChain<Component>(Arrays.asList(
                customComponents.iterator(), attachedPopupButtons.iterator(),
                overlayComponents.iterator()));
    }

    /**
     * This is called when the client-side connector has been initialized.
     */
    protected void onConnectorInit() {
        reloadCellDataOnNextScroll = true;
        valueManager.clearCachedContent();
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
        return conditionalFormatter;
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
    public CommentAuthorProvider getCommentAuthorProvider() {
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
        this.invalidFormulaErrorMessage = invalidFormulaErrorMessage;
        getElement().setProperty("invalidFormulaErrorMessage",
                invalidFormulaErrorMessage);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.ui.Component.Focusable#getTabIndex()
     */
    @Override
    public int getTabIndex() {
        return this.tabIndex;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.ui.Component.Focusable#setTabIndex(int)
     */
    @Override
    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
        getElement().setProperty("tabIndex", tabIndex);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vaadin.ui.AbstractComponent#focus()
     */
    // todo: no hace falta llamar al padre?
    /*
     * @Override public void focus() { super.focus(); }
     *
     */

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
        SpreadsheetFactory.calculateSheetSizes(this, activeSheet);
        SpreadsheetFactory.loadGrouping(this);
        reloadActiveSheetStyles();
        if (hasSheetOverlays()) {
            reloadImageSizesFromPOI = true;
            loadOrUpdateOverlays();
        }
        updateMarkedCells();
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

        if (getActiveSheet() instanceof HSSFSheet) {
            return;
        }

        XSSFSheet xsheet = (XSSFSheet) getActiveSheet();
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

                    int end = (int) GroupingUtil.findEndOfRowGroup(this, i, row,
                            l);
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

    void markInvalidFormula(int col, int row) {
        int activeSheetIndex = workbook.getActiveSheetIndex();
        if (!invalidFormulas.containsKey(activeSheetIndex)) {
            invalidFormulas.put(activeSheetIndex, new HashSet<>());
        }
        invalidFormulas.get(activeSheetIndex)
                .add(SpreadsheetUtil.toKey(col, row));

    }

    boolean isMarkedAsInvalidFormula(int col, int row) {
        int activeSheetIndex = workbook.getActiveSheetIndex();
        if (invalidFormulas.containsKey(activeSheetIndex)) {
            return invalidFormulas.get(activeSheetIndex)
                    .contains(SpreadsheetUtil.toKey(col, row));
        }
        return false;
    }

    void removeInvalidFormulaMark(int col, int row) {
        int activeSheetIndex = workbook.getActiveSheetIndex();
        if (invalidFormulas.containsKey(activeSheetIndex)) {
            invalidFormulas.get(activeSheetIndex)
                    .remove(SpreadsheetUtil.toKey(col, row));
        }
    }

    public void addSheetOverlay(SheetOverlayWrapper image) {
        sheetOverlays.add(image);
    }

    /**
     * Get the minimum row heigth in points for the rows that contain custom
     * components
     *
     * @return the minimum row heigths in points
     */
    public int getMinimumRowHeightForComponents() {
        return minimumRowHeightForComponents;
    }

    /***
     * Set the minimum row heigth in points for the rows that contain custom
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

    @Override
    public Locale getLocale() {
        return super.getLocale();
    }
}
