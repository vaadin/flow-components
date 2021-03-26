package com.vaadin.spreadsheet.flowport.gwtexporter.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import jsinterop.annotations.JsType;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootPanel;
import com.vaadin.addon.spreadsheet.client.PopupButtonConnector;
import com.vaadin.addon.spreadsheet.client.PopupButtonWidget;
import com.vaadin.addon.spreadsheet.client.SpreadsheetClientRpc;
import com.vaadin.addon.spreadsheet.client.SpreadsheetConnector;
import com.vaadin.addon.spreadsheet.client.SpreadsheetHandler;
import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;
import com.vaadin.addon.spreadsheet.client.SpreadsheetWidget;
import com.vaadin.addon.spreadsheet.shared.SpreadsheetState;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.metadata.ConnectorBundleLoader;
import com.vaadin.client.metadata.TypeDataStore;
import com.vaadin.shared.annotations.NoLayout;
import com.vaadin.shared.communication.URLReference;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.ErrorLevel;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.IntCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SerializedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.StringCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.VoidCallback;

import elemental.json.Json;
import elemental.json.JsonObject;

/**
 *
 * this is the public api which we will export to js
 *
 */
@JsType
public class SpreadsheetJsApi {

    public SpreadsheetWidget spreadsheetWidget;
    protected SpreadsheetConnector spreadsheetConnector;
    protected PopupButtonConnector popupButtonConnector;
    protected PopupButtonWidget popupButtonWidget;
    protected ApplicationConnection applicationConnection;

    native void consoleLog(String message) /*-{
      console.log("spreadsheetapi", message );
  }-*/;

    /**
     * receives the element where the widget mut be embedded into, and publishes
     * the methods which can be used from js
     *
     * @param element
     */
    public SpreadsheetJsApi(Element element) {
        if (element != null) {
           init(element);
        }
    }

    private void init(Element element) {
        // Only support eager connectors for now
        ConnectorBundleLoader.get()
                .loadBundle(ConnectorBundleLoader.EAGER_BUNDLE_NAME, null);

        spreadsheetConnector = new SpreadsheetConnector();
        applicationConnection = new ApplicationConnection();
        spreadsheetConnector.doInit("1", applicationConnection);
        spreadsheetWidget = spreadsheetConnector.getWidget();
        spreadsheetWidget.setHeight("100%");

        TypeDataStore.get().setClass(spreadsheetConnector.getClass().getName(), SpreadsheetConnector.class);

        initState(spreadsheetConnector.getState());

        delegateToWidget(spreadsheetConnector, new StateChangeEvent(spreadsheetConnector, Json.createObject(), true));


        consoleLog("" + spreadsheetConnector.getState());

        popupButtonConnector = new PopupButtonConnector();
        popupButtonWidget = popupButtonConnector.getWidget();


        RootPanel.getForElement(element).add(spreadsheetWidget);
        consoleLog("widget appended !");
        spreadsheetWidget.load();
        consoleLog("widget loaded !");
        Scheduler.get().scheduleDeferred(() -> {
            //spreadsheetWidget.getSheetWidget().ensureCustomStyleTagsAreInTheRightShadowRoot();
            consoleLog("deferred relayout!");
            spreadsheetWidget.relayoutSheet();
        });
        /*
        Map<Integer, Integer> newSizes = new HashMap<>();
        newSizes.put(1, 70);
        spreadsheetWidget.onColumnsResized(newSizes);

         */

        //spreadsheetConnector.getLayoutManager().layoutNow();
        //spreadsheetWidget.relayoutSheet();
    }

    private void delegateToWidget(SpreadsheetConnector connector, StateChangeEvent sce) {
        for (String propertyName : new String[] {
        "rowBufferSize",
        "columnBufferSize",
        "rows",
        "cols",
        "colGroupingData",
        "rowGroupingData",
        "colGroupingMax",
        "rowGroupingMax",
        "colGroupingInversed",
        "rowGroupingInversed",
        "defRowH",
        "defColW",
        "rowH",
        "colW",
        "cellStyleToCSSStyle",
        "rowIndexToStyleIndex",
        "columnIndexToStyleIndex",
        "lockedColumnIndexes",
        "lockedRowIndexes",
        "shiftedCellBorderStyles",
        "conditionalFormattingStyles",
        "hiddenColumnIndexes",
        "hiddenRowIndexes",
        "verticalScrollPositions",
        "horizontalScrollPositions",
        "workbookProtected",
        "hyperlinksTooltips",
        "displayGridlines",
        "displayRowColHeadings",
        "verticalSplitPosition",
        "horizontalSplitPosition",
        "infoLabelValue",
        "invalidFormulaErrorMessage",
        "lockFormatColumns",
        "lockFormatRows",
                "namedRanges"
        }) {
            if (sce.isInitialStateChange() || sce.hasPropertyChanged(propertyName)) {
                SpreadsheetWidget w = connector.getWidget();
                SpreadsheetState s = getState();
                if ("rowBufferSize".equals(propertyName)) w.setRowBufferSize(s.rowBufferSize);
                if ("columnBufferSize".equals(propertyName)) w.setColumnBufferSize(s.columnBufferSize);
                if ("rows".equals(propertyName)) w.setRows(s.rows);
                if ("cols".equals(propertyName)) w.setCols(s.cols);
                if ("colGroupingData".equals(propertyName)) w.setColGroupingData(s.colGroupingData);
                if ("rowGroupingData".equals(propertyName)) w.setRowGroupingData(s.rowGroupingData);
                if ("colGroupingMax".equals(propertyName)) w.setColGroupingMax(s.colGroupingMax);
                if ("rowGroupingMax".equals(propertyName)) w.setRowGroupingMax(s.rowGroupingMax);
                if ("colGroupingInversed".equals(propertyName)) w.setColGroupingInversed(s.colGroupingInversed);
                if ("rowGroupingInversed".equals(propertyName)) w.setRowGroupingInversed(s.rowGroupingInversed);
                if ("defRowH".equals(propertyName)) w.setDefRowH(s.defRowH);
                if ("defColW".equals(propertyName)) w.setDefColW(s.defColW);
                if ("rowH".equals(propertyName)) w.setRowH(s.rowH);
                if ("colW".equals(propertyName)) w.setColW(s.colW);
                if ("cellStyleToCSSStyle".equals(propertyName)) w.setCellStyleToCSSStyle(s.cellStyleToCSSStyle);
                if ("rowIndexToStyleIndex".equals(propertyName)) w.setRowIndexToStyleIndex(s.rowIndexToStyleIndex);
                if ("columnIndexToStyleIndex".equals(propertyName)) w.setColumnIndexToStyleIndex(s.columnIndexToStyleIndex);
                if ("lockedColumnIndexes".equals(propertyName)) w.setLockedColumnIndexes(s.lockedColumnIndexes);
                if ("lockedRowIndexes".equals(propertyName)) w.setLockedRowIndexes(s.lockedRowIndexes);
                if ("shiftedCellBorderStyles".equals(propertyName)) w.setShiftedCellBorderStyles(s.shiftedCellBorderStyles);
                if ("conditionalFormattingStyles".equals(propertyName)) w.setConditionalFormattingStyles(s.conditionalFormattingStyles);
                if ("hiddenColumnIndexes".equals(propertyName)) w.setHiddenColumnIndexes(s.hiddenColumnIndexes);
                if ("hiddenRowIndexes".equals(propertyName)) w.setHiddenRowIndexes(s.hiddenRowIndexes);
                if ("verticalScrollPositions".equals(propertyName)) w.setVerticalScrollPositions(s.verticalScrollPositions);
                if ("horizontalScrollPositions".equals(propertyName)) w.setHorizontalScrollPositions(s.horizontalScrollPositions);
                if ("workbookProtected".equals(propertyName)) w.setWorkbookProtected(s.workbookProtected);
                if ("hyperlinksTooltips".equals(propertyName)) w.setHyperlinksTooltips(s.hyperlinksTooltips);
                if ("displayGridlines".equals(propertyName)) w.setDisplayGridlines(s.displayGridlines);
                if ("displayRowColHeadings".equals(propertyName)) w.setDisplayRowColHeadings(s.displayRowColHeadings);
                if ("verticalSplitPosition".equals(propertyName)) w.setVerticalSplitPosition(s.verticalSplitPosition);
                if ("horizontalSplitPosition".equals(propertyName)) w.setHorizontalSplitPosition(s.horizontalSplitPosition);
                if ("infoLabelValue".equals(propertyName)) w.setInfoLabelValue(s.infoLabelValue);
                if ("invalidFormulaErrorMessage".equals(propertyName)) w.setInvalidFormulaErrorMessage(s.invalidFormulaErrorMessage);
                if ("lockFormatColumns".equals(propertyName)) w.setLockFormatColumns(s.lockFormatColumns);
                if ("lockFormatRows".equals(propertyName)) w.setLockFormatRows(s.lockFormatRows);
                if ("namedRanges".equals(propertyName)) w.setNamedRanges(s.namedRanges);
            }
        }
    }

    private void initState(SpreadsheetState state) {
        state.rows = 50;
        state.cols = 30;
        state.colGroupingData = new ArrayList<>();
        state.rowGroupingData = new ArrayList<>();
        state.defRowH = 15;
        state.defColW = 70;
        state.rowH = new float[] {15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15};
        state.colW = new int[] {70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70};
        state.reload = true;
        state.sheetNames = new String[] {"Sheet1"};
        state.cellStyleToCSSStyle = new HashMap<>();
        state.cellStyleToCSSStyle.put(0, "font-family:Calibri,swiss,Helvetica,arial;font-size:11pt;background-color:rgba(255,255,255,1.0);color:rgba(0, 0, 0, 1.0);");
        state.cellStyleToCSSStyle.put(1, "background-color:rgba(255, 255, 0, 1.0);justify-content:flex-end;");
        state.rowIndexToStyleIndex = new HashMap<>();
        state.columnIndexToStyleIndex = new HashMap<>();
        for (int pos = 1; pos <= state.cols; pos++) state.columnIndexToStyleIndex.put(pos, 0);
        state.lockedColumnIndexes = new HashSet<>();
        for (int pos = 1; pos <= state.cols; pos++) state.lockedColumnIndexes.add(pos);
        state.lockedRowIndexes = new HashSet<>();
        state.shiftedCellBorderStyles = new ArrayList<>();
        state.conditionalFormattingStyles = new HashMap<>();
        state.hiddenColumnIndexes = new ArrayList<>();
        state.hiddenRowIndexes = new ArrayList<>();
        state.verticalScrollPositions = new int[] {0};
        state.horizontalScrollPositions = new int[] {0};
        state.hyperlinksTooltips = new HashMap<>();
        state.hasActions = true;
        state.workbookChangeToggle = true;
        state.width = "100%";
        state.height = "100%";
    }

    private SpreadsheetServerRpcImpl getServerRpcInstance() {
        return (SpreadsheetServerRpcImpl) spreadsheetConnector.getProtectedRpcProxy(SpreadsheetServerRpc.class);
    }

    private SpreadsheetClientRpc getClientRpcInstance() {
        String rpcInterfaceId = SpreadsheetClientRpc.class.getName().replaceAll("\\$", ".");
        return (SpreadsheetClientRpc) spreadsheetConnector.getRpcImplementations(rpcInterfaceId).iterator().next();
    }

    /*
    SHARED STATE
     */
    
    protected SpreadsheetState getState() {
        return spreadsheetConnector.getState();
    }

    /**
     * The <i>tabulator index</i> of the field.
     */
    @NoLayout
    public int tabIndex = 0;

    public String height = "";
    public String width = "";
    @NoLayout
    public String description = "";
    @NoLayout
    public ContentMode descriptionContentMode = ContentMode.PREFORMATTED;
    // Note: for the caption, there is a difference between null and an empty
    // string!
    public String caption = null;
    public List<String> styles = null;
    public String id = null;
    public String primaryStyleName = null;

    /** HTML formatted error message for the component. */
    public String errorMessage = null;

    /**
     * Level of error.
     *
     * @since 8.2
     */
    public ErrorLevel errorLevel = null;

    public boolean captionAsHtml = false;

    /**
     * The automatically managed resources used by the connector.q
     *
     * @see com.vaadin.server.AbstractClientConnector#setResource(String,
     *      com.vaadin.server.Resource)
     * @see com.vaadin.client.ui.AbstractConnector#getResourceUrl(String)
     */
    public Map<String, URLReference> resources = new HashMap<>();

    public boolean enabled = true;

    /**
     * A set of event identifiers with registered listeners.
     */
    @NoLayout
    public Set<String> registeredEventListeners;

    public void setRowBufferSize(int rowBufferSize) {
        consoleLog("setRowBufferSize(" + rowBufferSize + ")");
        getState().rowBufferSize = rowBufferSize;
        notifyStateChange("rowBufferSize");
    }

    public void setColumnBufferSize(int columnBufferSize) {
        consoleLog("setColumnBufferSize(" + columnBufferSize + ")");
        getState().columnBufferSize = columnBufferSize;
        notifyStateChange("columnBufferSize");
    }

    public void setRows(int rows) {
        consoleLog("setRows(" + rows + ")");
        getState().rows = rows;
        notifyStateChange("rows");
    }

    public void setCols(int cols) {
        consoleLog("setCols(" + cols + ")");
        getState().cols = cols;
        notifyStateChange("cols");
    }

    public void setColGroupingData(String colGroupingData) {
        consoleLog("setColGroupingData(" + colGroupingData + ")");
        getState().colGroupingData = Parser.parseListOfGroupingData(colGroupingData);
        notifyStateChange("colGroupingData");
    }

    public void setRowGroupingData(String rowGroupingData) {
        consoleLog("setRowGroupingData(" + rowGroupingData + ")");
        getState().rowGroupingData = Parser.parseListOfGroupingData(rowGroupingData);
        notifyStateChange("rowGroupingData");
    }

    public void setColGroupingMax(int colGroupingMax) {
        consoleLog("setColGroupingMax(" + colGroupingMax + ")");
        getState().colGroupingMax = colGroupingMax;
        notifyStateChange("colGroupingMax");
    }

    public void setRowGroupingMax(int rowGroupingMax) {
        consoleLog("setRowGroupingMax(" + rowGroupingMax + ")");
        getState().rowGroupingMax = rowGroupingMax;
        notifyStateChange("rowGroupingMax");
    }

    public void setColGroupingInversed(boolean colGroupingInversed) {
        consoleLog("setColGroupingInversed(" + colGroupingInversed + ")");
        getState().colGroupingInversed = colGroupingInversed;
        notifyStateChange("colGroupingInversed");
    }

    public void setRowGroupingInversed(boolean rowGroupingInversed) {
        consoleLog("setRowGroupingInversed(" + rowGroupingInversed + ")");
        getState().rowGroupingInversed = rowGroupingInversed;
        notifyStateChange("rowGroupingInversed");
    }

    public void setDefRowH(float defRowH) {
        consoleLog("setDefRowH(" + defRowH + ")");
        getState().defRowH = defRowH;
        notifyStateChange("defRowH");
    }

    public void setDefColW(int defColW) {
        consoleLog("setDefColW(" + defColW + ")");
        getState().defColW = defColW;
        notifyStateChange("defColW");
    }

    public void setRowH(float[] rowH) {
        consoleLog("setRowH(" + rowH + ")");
        getState().rowH = rowH;
        notifyStateChange("rowH");
    }

    public void setColW(int[] colW) {
        consoleLog("setColW(" + colW + ")");
        getState().colW = colW;
        notifyStateChange("colW");
    }

    public void setReload(boolean reload) {
        consoleLog("setReload(" + reload + ")");
        getState().reload = reload;
        notifyStateChange("reload");
    }

    public void setSheetIndex(int sheetIndex) {
        consoleLog("setSheetIndex(" + sheetIndex + ")");
        getState().sheetIndex = sheetIndex;
        notifyStateChange("sheetIndex");
    }

    public void setSheetNames(String sheetNames) {
        consoleLog("setSheetNames(" + sheetNames + ")");
        getState().sheetNames = Parser.parseArrayOfStrings(sheetNames);
        notifyStateChange("sheetNames");
    }

    public void setCellStyleToCSSStyle(String cellStyleToCSSStyle) {
        consoleLog("setCellStyleToCSSStyle(" + cellStyleToCSSStyle + ")");
        getState().cellStyleToCSSStyle = Parser.parseMapIntegerString(cellStyleToCSSStyle);
        notifyStateChange("cellStyleToCSSStyle");
    }

    public void setRowIndexToStyleIndex(String rowIndexToStyleIndex) {
        consoleLog("setRowIndexToStyleIndex(" + rowIndexToStyleIndex + ")");
        getState().rowIndexToStyleIndex = Parser.parseMapIntegerInteger(rowIndexToStyleIndex);
        notifyStateChange("rowIndexToStyleIndex");
    }

    public void setColumnIndexToStyleIndex(String columnIndexToStyleIndex) {
        consoleLog("setColumnIndexToStyleIndex(" + columnIndexToStyleIndex + ")");
        getState().columnIndexToStyleIndex = Parser.parseMapIntegerInteger(columnIndexToStyleIndex);
        notifyStateChange("columnIndexToStyleIndex");
    }

    public void setLockedColumnIndexes(String lockedColumnIndexes) {
        consoleLog("setLockedColumnIndexes(" + lockedColumnIndexes + ")");
        getState().lockedColumnIndexes = Parser.parseSetInteger(lockedColumnIndexes);
        notifyStateChange("lockedColumnIndexes");
    }

    public void setLockedRowIndexes(String lockedRowIndexes) {
        consoleLog("setLockedRowIndexes(" + lockedRowIndexes + ")");
        getState().lockedRowIndexes = Parser.parseSetInteger(lockedRowIndexes);
        notifyStateChange("lockedRowIndexes");
    }

    public void setShiftedCellBorderStyles(String shiftedCellBorderStyles) {
        consoleLog("setShiftedCellBorderStyles(" + shiftedCellBorderStyles + ")");
        getState().shiftedCellBorderStyles = Parser.parseArraylistString(shiftedCellBorderStyles);
        notifyStateChange("shiftedCellBorderStyles");
    }

    public void setConditionalFormattingStyles(String conditionalFormattingStyles) {
        consoleLog("setConditionalFormattingStyles(" + conditionalFormattingStyles + ")");
        getState().conditionalFormattingStyles = Parser.parseMapIntegerString(conditionalFormattingStyles);
        notifyStateChange("conditionalFormattingStyles");
    }

    public void setHiddenColumnIndexes(String hiddenColumnIndexes) {
        consoleLog("setHiddenColumnIndexes(" + hiddenColumnIndexes + ")");
        getState().hiddenColumnIndexes = Parser.parseArraylistInteger(hiddenColumnIndexes);
        notifyStateChange("hiddenColumnIndexes");
    }

    public void setHiddenRowIndexes(String hiddenRowIndexes) {
        consoleLog("setHiddenRowIndexes(" + hiddenRowIndexes + ")");
        getState().hiddenRowIndexes = Parser.parseArraylistInteger(hiddenRowIndexes);
        notifyStateChange("hiddenRowIndexes");
    }

    public void setVerticalScrollPositions(String verticalScrollPositions) {
        consoleLog("setVerticalScrollPositions(" + verticalScrollPositions + ")");
        getState().verticalScrollPositions = Parser.parseArrayInt(verticalScrollPositions);
        notifyStateChange("verticalScrollPositions");
    }

    public void setHorizontalScrollPositions(String horizontalScrollPositions) {
        consoleLog("setHorizontalScrollPositions(" + horizontalScrollPositions + ")");
        getState().horizontalScrollPositions = Parser.parseArrayInt(horizontalScrollPositions);
        notifyStateChange("horizontalScrollPositions");
    }

    public void setSheetProtected(boolean sheetProtected) {
        consoleLog("setSheetProtected(" + sheetProtected + ")");
        getState().sheetProtected = sheetProtected;
        notifyStateChange("sheetProtected");
    }

    public void setWorkbookProtected(boolean workbookProtected) {
        consoleLog("setWorkbookProtected(" + workbookProtected + ")");
        getState().workbookProtected = workbookProtected;
        notifyStateChange("workbookProtected");
    }

    public void setCellKeysToEditorIdMap(String cellKeysToEditorIdMap) {
        consoleLog("setCellKeysToEditorIdMap(" + cellKeysToEditorIdMap + ")");
        getState().cellKeysToEditorIdMap = Parser.parseMapStringString(cellKeysToEditorIdMap);
        notifyStateChange("cellKeysToEditorIdMap");
    }

    public void setComponentIDtoCellKeysMap(String componentIDtoCellKeysMap) {
        consoleLog("setComponentIDtoCellKeysMap(" + componentIDtoCellKeysMap + ")");
        getState().componentIDtoCellKeysMap = Parser.parseMapStringString(componentIDtoCellKeysMap);
        notifyStateChange("componentIDtoCellKeysMap");
    }

    public void setHyperlinksTooltips(String hyperlinksTooltips) {
        consoleLog("setHyperlinksTooltips(" + hyperlinksTooltips + ")");
        getState().hyperlinksTooltips = Parser.parseMapStringString(hyperlinksTooltips);
        notifyStateChange("hyperlinksTooltips");
    }

    public void setCellComments(String cellComments) {
        consoleLog("setCellComments(" + cellComments + ")");
        getState().cellComments = Parser.parseMapStringString(cellComments);
        notifyStateChange("cellComments");
    }

    public void setCellCommentAuthors(String cellCommentAuthors) {
        consoleLog("setCellCommentAuthors(" + cellCommentAuthors + ")");
        getState().cellCommentAuthors = Parser.parseMapStringString(cellCommentAuthors);
        notifyStateChange("cellCommentAuthors");
    }

    public void setVisibleCellComments(String visibleCellComments) {
        consoleLog("setVisibleCellComments(" + visibleCellComments + ")");
        getState().visibleCellComments = Parser.parseArraylistString(visibleCellComments);
        notifyStateChange("visibleCellComments");
    }

    public void setInvalidFormulaCells(String invalidFormulaCells) {
        consoleLog("setInvalidFormulaCells(" + invalidFormulaCells + ")");
        getState().invalidFormulaCells = Parser.parseSetString(invalidFormulaCells);
        notifyStateChange("invalidFormulaCells");
    }

    public void setHasActions(boolean hasActions) {
        consoleLog("setHasActions(" + hasActions + ")");
        getState().hasActions = hasActions;
        notifyStateChange("hasActions");
    }

    public void setOverlays(String overlays) {
        consoleLog("setOverlays(" + overlays + ")");
        getState().overlays = Parser.parseMapStringOverlayInfo(overlays);
        notifyStateChange("overlays");
    }

    public void setMergedRegions(String mergedRegions) {
        consoleLog("setMergedRegions(" + mergedRegions + ")");
        getState().mergedRegions = Parser.parseArrayMergedRegion(mergedRegions);
        notifyStateChange("mergedRegions");
    }

    public void setDisplayGridlines(boolean displayGridlines) {
        consoleLog("setDisplayGridlines(" + displayGridlines + ")");
        getState().displayGridlines = displayGridlines;
        notifyStateChange("displayGridlines");
    }

    public void setDisplayRowColHeadings(boolean displayRowColHeadings) {
        consoleLog("setDisplayRowColHeadings(" + displayRowColHeadings + ")");
        getState().displayRowColHeadings = displayRowColHeadings;
        notifyStateChange("displayRowColHeadings");
    }

    public void setVerticalSplitPosition(int verticalSplitPosition) {
        consoleLog("setVerticalSplitPosition(" + verticalSplitPosition + ")");
        getState().verticalSplitPosition = verticalSplitPosition;
        notifyStateChange("verticalSplitPosition");
    }

    public void setHorizontalSplitPosition(int horizontalSplitPosition) {
        consoleLog("setHorizontalSplitPosition(" + horizontalSplitPosition + ")");
        getState().horizontalSplitPosition = horizontalSplitPosition;
        notifyStateChange("horizontalSplitPosition");
    }

    public void setInfoLabelValue(String infoLabelValue) {
        consoleLog("setInfoLabelValue(" + infoLabelValue + ")");
        getState().infoLabelValue = infoLabelValue;
        notifyStateChange("infoLabelValue");
    }

    public void setWorkbookChangeToggle(boolean workbookChangeToggle) {
        consoleLog("setWorkbookChangeToggle(" + workbookChangeToggle + ")");
        getState().workbookChangeToggle = workbookChangeToggle;
        notifyStateChange("workbookChangeToggle");
    }

    public void setInvalidFormulaErrorMessage(String invalidFormulaErrorMessage) {
        consoleLog("setInvalidFormulaErrorMessage(" + invalidFormulaErrorMessage + ")");
        getState().invalidFormulaErrorMessage = invalidFormulaErrorMessage;
        notifyStateChange("invalidFormulaErrorMessage");
    }

    public void setLockFormatColumns(boolean lockFormatColumns) {
        consoleLog("setLockFormatColumns(" + lockFormatColumns + ")");
        getState().lockFormatColumns = lockFormatColumns;
        notifyStateChange("lockFormatColumns");
    }

    public void setLockFormatRows(boolean lockFormatRows) {
        consoleLog("setLockFormatRows(" + lockFormatRows + ")");
        getState().lockFormatRows = lockFormatRows;
        notifyStateChange("lockFormatRows");
    }

    public void setNamedRanges(String namedRanges) {
        consoleLog("setNamedRanges(" + namedRanges + ")");
        getState().namedRanges = Parser.parseArraylistString(namedRanges);
        notifyStateChange("namedRanges");
    }

    public void notifyStateChange(String propName) {
        JsonObject stateJson = Json.createObject();
        stateJson.put(propName, "");
        boolean initialStateChange = false;
        spreadsheetConnector.onStateChanged(new StateChangeEvent(spreadsheetConnector, stateJson, initialStateChange));
    }


    /*
    CLIENT RPC METHODS
     */

    public void updateBottomRightCellValues(String cellData) {
        consoleLog("updateBottomRightCellValues(" + cellData + ")");
        getClientRpcInstance().updateBottomRightCellValues(Parser.parseArraylistOfCellData(cellData));
    }

    public void updateTopLeftCellValues(String cellData) {
        consoleLog("updateTopLeftCellValues(" + cellData + ")");
        getClientRpcInstance().updateTopLeftCellValues(Parser.parseArraylistOfCellData(cellData));
    }

    public void updateTopRightCellValues(String cellData) {
        consoleLog("updateTopRightCellValues(" + cellData + ")");
        getClientRpcInstance().updateTopRightCellValues(Parser.parseArraylistOfCellData(cellData));
    }

    public void updateBottomLeftCellValues(String cellData) {
        consoleLog("updateBottomLeftCellValues(" + cellData + ")");
        getClientRpcInstance().updateBottomLeftCellValues(Parser.parseArraylistOfCellData(cellData));
    }

    public void updateFormulaBar(String possibleName, int col, int row) {
        consoleLog("updateFormulaBar(" + possibleName + "," + col + "," + row + ")");
        getClientRpcInstance().updateFormulaBar(possibleName, col, row);
    }

    public void invalidCellAddress() {
        consoleLog("invalidCellAddress()");
        getClientRpcInstance().invalidCellAddress();
    }

    public void showSelectedCell(String name, int col, int row, String cellValue, boolean function, boolean locked, boolean initialSelection) {
        consoleLog("showSelectedCell(" + name + "," + col + "," + row + "," + cellValue + "," + function + "," + locked + "," + initialSelection + ")");
        getClientRpcInstance().showSelectedCell(name, col, row, cellValue, function, locked, initialSelection);
    }

    public void showActions(String actionDetails) {
        consoleLog("showActions(" + actionDetails + ")");
        getClientRpcInstance().showActions(Parser.parseArraylistSpreadsheetActionDetails(actionDetails));
    }

    public void setSelectedCellAndRange(String name, int col, int row, int c1, int c2, int r1, int r2, boolean scroll) {
        consoleLog("setSelectedCellAndRange(" + name + "," + col + "," + row + "," + c1 + "," + c2 + "," + r1 + "," + r2 + "," + scroll + ")");
        getClientRpcInstance().setSelectedCellAndRange(name, col, row, c1, c2, r1, r2, scroll);
    }

    public void cellsUpdated(String updatedCellData) {
        consoleLog("cellsUpdated(" + updatedCellData + ")");
        getClientRpcInstance().cellsUpdated(Parser.parseArraylistOfCellData(updatedCellData));
    }

    public void refreshCellStyles() {
        consoleLog("refreshCellStyles()");
        getClientRpcInstance().refreshCellStyles();
    }

    public void editCellComment(int col, int row) {
        consoleLog("editCellComment(" + col + "," + row + ")");
        getClientRpcInstance().editCellComment(col, row);
    }

    /*
    SERVER RPC METHOD CALLBACKS
     */
    public void setGroupingCollapsedCallback(SerializedCallback callback) {
        consoleLog("setGroupingCollapsedCallback(" + callback + ")");
        getServerRpcInstance().setGroupingCollapsedCallback(callback);
    }

    public void setLevelHeaderClickedCallback(SerializedCallback callback) {
        consoleLog("setLevelHeaderClickedCallback(" + callback + ")");
        getServerRpcInstance().setLevelHeaderClickedCallback(callback);
    }

    public void setOnSheetScrollCallback(SerializedCallback callback) {
        consoleLog("setOnSheetScrollCallback(" + callback + ")");
        getServerRpcInstance().setOnSheetScrollCallback(callback);
    }

    public void setSheetAddressChangedCallback(StringCallback callback) {
        consoleLog("setSheetAddressChangedCallback(" + callback + ")");
        getServerRpcInstance().setSheetAddressChangedCallback(callback);
    }

    public void setCellSelectedCallback(SerializedCallback callback) {
        consoleLog("setCellSelectedCallback(" + callback + ")");
        getServerRpcInstance().setCellSelectedCallback(callback);
    }

    public void setCellRangeSelectedCallback(SerializedCallback callback) {
        consoleLog("setCellRangeSelectedCallback(" + callback + ")");
        getServerRpcInstance().setCellRangeSelectedCallback(callback);
    }

    public void setCellAddedToSelectionAndSelectedCallback(SerializedCallback callback) {
        consoleLog("setCellAddedToSelectionAndSelectedCallback(" + callback + ")");
        getServerRpcInstance().setCellAddedToSelectionAndSelectedCallback(callback);
    }

    public void setCellsAddedToRangeSelectionCallback(SerializedCallback callback) {
        consoleLog("setCellsAddedToRangeSelectionCallback(" + callback + ")");
        getServerRpcInstance().setCellsAddedToRangeSelectionCallback(callback);
    }

    public void setRowSelectedCallback(SerializedCallback callback) {
        consoleLog("setRowSelectedCallback(" + callback + ")");
        getServerRpcInstance().setRowSelectedCallback(callback);
    }

    public void setRowAddedToRangeSelectionCallback(SerializedCallback callback) {
        consoleLog("setRowAddedToRangeSelectionCallback(" + callback + ")");
        getServerRpcInstance().setRowAddedToRangeSelectionCallback(callback);
    }

    public void setColumnSelectedCallback(SerializedCallback callback) {
        consoleLog("setColumnSelectedCallback(" + callback + ")");
        getServerRpcInstance().setColumnSelectedCallback(callback);
    }

    public void setColumnAddedToSelectionCallback(SerializedCallback callback) {
        consoleLog("setColumnAddedToSelectionCallback(" + callback + ")");
        getServerRpcInstance().setColumnAddedToSelectionCallback(callback);
    }

    public void setSelectionIncreasePaintedCallback(SerializedCallback callback) {
        consoleLog("setSelectionIncreasePaintedCallback(" + callback + ")");
        getServerRpcInstance().setSelectionIncreasePaintedCallback(callback);
    }


    public void setSelectionDecreasePaintedCallback(SerializedCallback callback) {
        consoleLog("setSelectionDecreasePaintedCallback(" + callback + ")");
        getServerRpcInstance().setSelectionDecreasePaintedCallback(callback);
    }

    public void setCellValueEditedCallback(SerializedCallback callback) {
        consoleLog("setCellValueEditedCallback(" + callback + ")");
        getServerRpcInstance().setCellValueEditedCallback(callback);
    }

    public void setSheetSelectedCallback(SerializedCallback callback) {
        consoleLog("setSheetSelectedCallback(" + callback + ")");
        getServerRpcInstance().setSheetSelectedCallback(callback);
    }

    public void setSheetRenamedCallback(SerializedCallback callback) {
        consoleLog("setSheetRenamedCallback(" + callback + ")");
        getServerRpcInstance().setSheetRenamedCallback(callback);
    }

    public void setSheetCreatedCallback(SerializedCallback callback) {
        consoleLog("setSheetCreatedCallback(" + callback + ")");
        getServerRpcInstance().setSheetCreatedCallback(callback);
    }

    public void setCellRangePaintedCallback(SerializedCallback callback) {
        consoleLog("setCellRangePaintedCallback(" + callback + ")");
        getServerRpcInstance().setCellRangePaintedCallback(callback);
    }

    public void setDeleteSelectedCellsCallback(SerializedCallback callback) {
        consoleLog("setDeleteSelectedCellsCallback(" + callback + ")");
        getServerRpcInstance().setDeleteSelectedCellsCallback(callback);
    }

    public void setLinkCellClickedCallback(SerializedCallback callback) {
        consoleLog("setLinkCellClickedCallback(" + callback + ")");
        getServerRpcInstance().setLinkCellClickedCallback(callback);
    }

    public void setRowsResizedCallback(SerializedCallback callback) {
        consoleLog("setRowsResizedCallback(" + callback + ")");
        getServerRpcInstance().setRowsResizedCallback(callback);
    }

    public void setColumnResizedCallback(SerializedCallback callback) {
        consoleLog("setColumnResizedCallback(" + callback + ")");
        getServerRpcInstance().setColumnResizedCallback(callback);
    }

    public void setOnRowAutofitCallback(IntCallback callback) {
        consoleLog("setOnRowAutofitCallback(" + callback + ")");
        getServerRpcInstance().setOnRowAutofitCallback(callback);
    }

    public void setOnColumnAutofitCallback(IntCallback callback) {
        consoleLog("setOnColumnAutofitCallback(" + callback + ")");
        getServerRpcInstance().setOnColumnAutofitCallback(callback);
    }

    public void setOnUndoCallback(VoidCallback callback) {
        consoleLog("setOnUndoCallback(" + callback + ")");
        getServerRpcInstance().setOnUndoCallback(callback);
    }

    public void setOnRedoCallback(VoidCallback callback) {
        consoleLog("setOnRedoCallback(" + callback + ")");
        getServerRpcInstance().setOnRedoCallback(callback);
    }

    public void setSetCellStyleWidthRatiosCallback(SerializedCallback callback) {
        consoleLog("setSetCellStyleWidthRatiosCallback(" + callback + ")");
        getServerRpcInstance().setSetCellStyleWidthRatiosCallback(callback);
    }

    public void setProtectedCellWriteAttemptedCallback(VoidCallback callback) {
        consoleLog("setProtectedCellWriteAttemptedCallback(" + callback + ")");
        getServerRpcInstance().setProtectedCellWriteAttemptedCallback(callback);
    }

    public void setOnPasteCallback(StringCallback callback) {
        consoleLog("setOnPasteCallback(" + callback + ")");
        getServerRpcInstance().setOnPasteCallback(callback);
    }

    public void setClearSelectedCellsOnCutCallback(VoidCallback callback) {
        consoleLog("setClearSelectedCellsOnCutCallback(" + callback + ")");
        getServerRpcInstance().setClearSelectedCellsOnCutCallback(callback);
    }

    public void setUpdateCellCommentCallback(SerializedCallback callback) {
        consoleLog("setUpdateCellCommentCallback(" + callback + ")");
        getServerRpcInstance().setUpdateCellCommentCallback(callback);
    }

    public void setOnConnectorInitCallback(VoidCallback callback) {
        consoleLog("setOnConnectorInitCallback(" + callback + ")");
        getServerRpcInstance().setOnConnectorInitCallback(callback);
    }

    public void setContextMenuOpenOnSelectionCallback(SerializedCallback callback) {
        consoleLog("setContextMenuOpenOnSelectionCallback(" + callback + ")");
        getServerRpcInstance().setContextMenuOpenOnSelectionCallback(callback);
    }

    public void setActionOnCurrentSelectionCallback(StringCallback callback) {
        consoleLog("setActionOnCurrentSelectionCallback(" + callback + ")");
        getServerRpcInstance().setActionOnCurrentSelectionCallback(callback);
    }

    public void setRowHeaderContextMenuOpenCallback(IntCallback callback) {
        consoleLog("setRowHeaderContextMenuOpenCallback(" + callback + ")");
        getServerRpcInstance().setRowHeaderContextMenuOpenCallback(callback);
    }

    public void setActionOnRowHeaderCallback(StringCallback callback) {
        consoleLog("setActionOnRowHeaderCallback(" + callback + ")");
        getServerRpcInstance().setActionOnRowHeaderCallback(callback);
    }

    public void setColumnHeaderContextMenuOpenCallback(IntCallback callback) {
        consoleLog("setColumnHeaderContextMenuOpenCallback(" + callback + ")");
        getServerRpcInstance().setColumnHeaderContextMenuOpenCallback(callback);
    }

    public void setActionOnColumnHeaderCallback(StringCallback callback) {
        consoleLog("setActionOnColumnHeaderCallback(" + callback + ")");
        getServerRpcInstance().setActionOnColumnHeaderCallback(callback);
    }



}
