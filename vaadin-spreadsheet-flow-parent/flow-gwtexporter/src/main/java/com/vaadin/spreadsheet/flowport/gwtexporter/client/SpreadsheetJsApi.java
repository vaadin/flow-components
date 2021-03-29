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

    public void updateFromState() {
        StateChangeEvent event = new StateChangeEvent(spreadsheetConnector, Json.createObject(), true);
        spreadsheetConnector.onStateChanged(event);
    }

    public void setRowBufferSize(int rowBufferSize, boolean notify) {
        consoleLog("setRowBufferSize(" + rowBufferSize + ")");
        getState().rowBufferSize = rowBufferSize;
        if (notify) notifyStateChange("rowBufferSize");
    }

    public void setColumnBufferSize(int columnBufferSize, boolean notify) {
        consoleLog("setColumnBufferSize(" + columnBufferSize + ")");
        getState().columnBufferSize = columnBufferSize;
        if (notify) notifyStateChange("columnBufferSize");
    }

    public void setRows(int rows, boolean notify) {
        consoleLog("setRows(" + rows + ")");
        getState().rows = rows;
        if (notify) notifyStateChange("rows");
    }

    public void setCols(int cols, boolean notify) {
        consoleLog("setCols(" + cols + ")");
        getState().cols = cols;
        if (notify) notifyStateChange("cols");
    }

    public void setColGroupingData(String colGroupingData, boolean notify) {
        consoleLog("setColGroupingData(" + colGroupingData + ")");
        getState().colGroupingData = Parser.parseListOfGroupingData(colGroupingData);
        if (notify) notifyStateChange("colGroupingData");
    }

    public void setRowGroupingData(String rowGroupingData, boolean notify) {
        consoleLog("setRowGroupingData(" + rowGroupingData + ")");
        getState().rowGroupingData = Parser.parseListOfGroupingData(rowGroupingData);
        if (notify) notifyStateChange("rowGroupingData");
    }

    public void setColGroupingMax(int colGroupingMax, boolean notify) {
        consoleLog("setColGroupingMax(" + colGroupingMax + ")");
        getState().colGroupingMax = colGroupingMax;
        if (notify) notifyStateChange("colGroupingMax");
    }

    public void setRowGroupingMax(int rowGroupingMax, boolean notify) {
        consoleLog("setRowGroupingMax(" + rowGroupingMax + ")");
        getState().rowGroupingMax = rowGroupingMax;
        if (notify) notifyStateChange("rowGroupingMax");
    }

    public void setColGroupingInversed(boolean colGroupingInversed, boolean notify) {
        consoleLog("setColGroupingInversed(" + colGroupingInversed + ")");
        getState().colGroupingInversed = colGroupingInversed;
        if (notify) notifyStateChange("colGroupingInversed");
    }

    public void setRowGroupingInversed(boolean rowGroupingInversed, boolean notify) {
        consoleLog("setRowGroupingInversed(" + rowGroupingInversed + ")");
        getState().rowGroupingInversed = rowGroupingInversed;
        if (notify) notifyStateChange("rowGroupingInversed");
    }

    public void setDefRowH(float defRowH, boolean notify) {
        consoleLog("setDefRowH(" + defRowH + ")");
        getState().defRowH = defRowH;
        if (notify) notifyStateChange("defRowH");
    }

    public void setDefColW(int defColW, boolean notify) {
        consoleLog("setDefColW(" + defColW + ")");
        getState().defColW = defColW;
        if (notify) notifyStateChange("defColW");
    }

    public void setRowH(float[] rowH, boolean notify) {
        consoleLog("setRowH(" + rowH + ")");
        getState().rowH = rowH;
        if (notify) notifyStateChange("rowH");
    }

    public void setColW(int[] colW, boolean notify) {
        consoleLog("setColW(" + colW + ")");
        getState().colW = colW;
        if (notify) notifyStateChange("colW");
    }

    public void setReload(boolean reload, boolean notify) {
        consoleLog("setReload(" + reload + ")");
        getState().reload = reload;
        if (notify) notifyStateChange("reload");
    }

    public void setSheetIndex(int sheetIndex, boolean notify) {
        consoleLog("setSheetIndex(" + sheetIndex + ")");
        getState().sheetIndex = sheetIndex;
        if (notify) notifyStateChange("sheetIndex");
    }

    public void setSheetNames(String sheetNames, boolean notify) {
        consoleLog("setSheetNames(" + sheetNames + ")");
        getState().sheetNames = Parser.parseArrayOfStrings(sheetNames);
        if (notify) notifyStateChange("sheetNames");
    }

    public void setCellStyleToCSSStyle(String cellStyleToCSSStyle, boolean notify) {
        consoleLog("setCellStyleToCSSStyle(" + cellStyleToCSSStyle + ")");
        getState().cellStyleToCSSStyle = Parser.parseMapIntegerString(cellStyleToCSSStyle);
        if (notify) notifyStateChange("cellStyleToCSSStyle");
    }

    public void setRowIndexToStyleIndex(String rowIndexToStyleIndex, boolean notify) {
        consoleLog("setRowIndexToStyleIndex(" + rowIndexToStyleIndex + ")");
        getState().rowIndexToStyleIndex = Parser.parseMapIntegerInteger(rowIndexToStyleIndex);
        if (notify) notifyStateChange("rowIndexToStyleIndex");
    }

    public void setColumnIndexToStyleIndex(String columnIndexToStyleIndex, boolean notify) {
        consoleLog("setColumnIndexToStyleIndex(" + columnIndexToStyleIndex + ")");
        getState().columnIndexToStyleIndex = Parser.parseMapIntegerInteger(columnIndexToStyleIndex);
        if (notify) notifyStateChange("columnIndexToStyleIndex");
    }

    public void setLockedColumnIndexes(String lockedColumnIndexes, boolean notify) {
        consoleLog("setLockedColumnIndexes(" + lockedColumnIndexes + ")");
        getState().lockedColumnIndexes = Parser.parseSetInteger(lockedColumnIndexes);
        if (notify) notifyStateChange("lockedColumnIndexes");
    }

    public void setLockedRowIndexes(String lockedRowIndexes, boolean notify) {
        consoleLog("setLockedRowIndexes(" + lockedRowIndexes + ")");
        getState().lockedRowIndexes = Parser.parseSetInteger(lockedRowIndexes);
        if (notify) notifyStateChange("lockedRowIndexes");
    }

    public void setShiftedCellBorderStyles(String shiftedCellBorderStyles, boolean notify) {
        consoleLog("setShiftedCellBorderStyles(" + shiftedCellBorderStyles + ")");
        getState().shiftedCellBorderStyles = Parser.parseArraylistString(shiftedCellBorderStyles);
        if (notify) notifyStateChange("shiftedCellBorderStyles");
    }

    public void setConditionalFormattingStyles(String conditionalFormattingStyles, boolean notify) {
        consoleLog("setConditionalFormattingStyles(" + conditionalFormattingStyles + ")");
        getState().conditionalFormattingStyles = Parser.parseMapIntegerString(conditionalFormattingStyles);
        if (notify) notifyStateChange("conditionalFormattingStyles");
    }

    public void setHiddenColumnIndexes(String hiddenColumnIndexes, boolean notify) {
        consoleLog("setHiddenColumnIndexes(" + hiddenColumnIndexes + ")");
        getState().hiddenColumnIndexes = Parser.parseArraylistInteger(hiddenColumnIndexes);
        if (notify) notifyStateChange("hiddenColumnIndexes");
    }

    public void setHiddenRowIndexes(String hiddenRowIndexes, boolean notify) {
        consoleLog("setHiddenRowIndexes(" + hiddenRowIndexes + ")");
        getState().hiddenRowIndexes = Parser.parseArraylistInteger(hiddenRowIndexes);
        if (notify) notifyStateChange("hiddenRowIndexes");
    }

    public void setVerticalScrollPositions(String verticalScrollPositions, boolean notify) {
        consoleLog("setVerticalScrollPositions(" + verticalScrollPositions + ")");
        getState().verticalScrollPositions = Parser.parseArrayInt(verticalScrollPositions);
        if (notify) notifyStateChange("verticalScrollPositions");
    }

    public void setHorizontalScrollPositions(String horizontalScrollPositions, boolean notify) {
        consoleLog("setHorizontalScrollPositions(" + horizontalScrollPositions + ")");
        getState().horizontalScrollPositions = Parser.parseArrayInt(horizontalScrollPositions);
        if (notify) notifyStateChange("horizontalScrollPositions");
    }

    public void setSheetProtected(boolean sheetProtected, boolean notify) {
        consoleLog("setSheetProtected(" + sheetProtected + ")");
        getState().sheetProtected = sheetProtected;
        if (notify) notifyStateChange("sheetProtected");
    }

    public void setWorkbookProtected(boolean workbookProtected, boolean notify) {
        consoleLog("setWorkbookProtected(" + workbookProtected + ")");
        getState().workbookProtected = workbookProtected;
        if (notify) notifyStateChange("workbookProtected");
    }

    public void setCellKeysToEditorIdMap(String cellKeysToEditorIdMap, boolean notify) {
        consoleLog("setCellKeysToEditorIdMap(" + cellKeysToEditorIdMap + ")");
        getState().cellKeysToEditorIdMap = Parser.parseMapStringString(cellKeysToEditorIdMap);
        if (notify) notifyStateChange("cellKeysToEditorIdMap");
    }

    public void setComponentIDtoCellKeysMap(String componentIDtoCellKeysMap, boolean notify) {
        consoleLog("setComponentIDtoCellKeysMap(" + componentIDtoCellKeysMap + ")");
        getState().componentIDtoCellKeysMap = Parser.parseMapStringString(componentIDtoCellKeysMap);
        if (notify) notifyStateChange("componentIDtoCellKeysMap");
    }

    public void setHyperlinksTooltips(String hyperlinksTooltips, boolean notify) {
        consoleLog("setHyperlinksTooltips(" + hyperlinksTooltips + ")");
        getState().hyperlinksTooltips = Parser.parseMapStringString(hyperlinksTooltips);
        if (notify) notifyStateChange("hyperlinksTooltips");
    }

    public void setCellComments(String cellComments, boolean notify) {
        consoleLog("setCellComments(" + cellComments + ")");
        getState().cellComments = Parser.parseMapStringString(cellComments);
        if (notify) notifyStateChange("cellComments");
    }

    public void setCellCommentAuthors(String cellCommentAuthors, boolean notify) {
        consoleLog("setCellCommentAuthors(" + cellCommentAuthors + ")");
        getState().cellCommentAuthors = Parser.parseMapStringString(cellCommentAuthors);
        if (notify) notifyStateChange("cellCommentAuthors");
    }

    public void setVisibleCellComments(String visibleCellComments, boolean notify) {
        consoleLog("setVisibleCellComments(" + visibleCellComments + ")");
        getState().visibleCellComments = Parser.parseArraylistString(visibleCellComments);
        if (notify) notifyStateChange("visibleCellComments");
    }

    public void setInvalidFormulaCells(String invalidFormulaCells, boolean notify) {
        consoleLog("setInvalidFormulaCells(" + invalidFormulaCells + ")");
        getState().invalidFormulaCells = Parser.parseSetString(invalidFormulaCells);
        if (notify) notifyStateChange("invalidFormulaCells");
    }

    public void setHasActions(boolean hasActions, boolean notify) {
        consoleLog("setHasActions(" + hasActions + ")");
        getState().hasActions = hasActions;
        if (notify) notifyStateChange("hasActions");
    }

    public void setOverlays(String overlays, boolean notify) {
        consoleLog("setOverlays(" + overlays + ")");
        getState().overlays = Parser.parseMapStringOverlayInfo(overlays);
        if (notify) notifyStateChange("overlays");
    }

    public void setMergedRegions(String mergedRegions, boolean notify) {
        consoleLog("setMergedRegions(" + mergedRegions + ")");
        getState().mergedRegions = Parser.parseArrayMergedRegion(mergedRegions);
        if (notify) notifyStateChange("mergedRegions");
    }

    public void setDisplayGridlines(boolean displayGridlines, boolean notify) {
        consoleLog("setDisplayGridlines(" + displayGridlines + ")");
        getState().displayGridlines = displayGridlines;
        if (notify) notifyStateChange("displayGridlines");
    }

    public void setDisplayRowColHeadings(boolean displayRowColHeadings, boolean notify) {
        consoleLog("setDisplayRowColHeadings(" + displayRowColHeadings + ")");
        getState().displayRowColHeadings = displayRowColHeadings;
        if (notify) notifyStateChange("displayRowColHeadings");
    }

    public void setVerticalSplitPosition(int verticalSplitPosition, boolean notify) {
        consoleLog("setVerticalSplitPosition(" + verticalSplitPosition + ")");
        getState().verticalSplitPosition = verticalSplitPosition;
        if (notify) notifyStateChange("verticalSplitPosition");
    }

    public void setHorizontalSplitPosition(int horizontalSplitPosition, boolean notify) {
        consoleLog("setHorizontalSplitPosition(" + horizontalSplitPosition + ")");
        getState().horizontalSplitPosition = horizontalSplitPosition;
        if (notify) notifyStateChange("horizontalSplitPosition");
    }

    public void setInfoLabelValue(String infoLabelValue, boolean notify) {
        consoleLog("setInfoLabelValue(" + infoLabelValue + ")");
        getState().infoLabelValue = infoLabelValue;
        if (notify) notifyStateChange("infoLabelValue");
    }

    public void setWorkbookChangeToggle(boolean workbookChangeToggle, boolean notify) {
        consoleLog("setWorkbookChangeToggle(" + workbookChangeToggle + ")");
        getState().workbookChangeToggle = workbookChangeToggle;
        if (notify) notifyStateChange("workbookChangeToggle");
    }

    public void setInvalidFormulaErrorMessage(String invalidFormulaErrorMessage, boolean notify) {
        consoleLog("setInvalidFormulaErrorMessage(" + invalidFormulaErrorMessage + ")");
        getState().invalidFormulaErrorMessage = invalidFormulaErrorMessage;
        if (notify) notifyStateChange("invalidFormulaErrorMessage");
    }

    public void setLockFormatColumns(boolean lockFormatColumns, boolean notify) {
        consoleLog("setLockFormatColumns(" + lockFormatColumns + ")");
        getState().lockFormatColumns = lockFormatColumns;
        if (notify) notifyStateChange("lockFormatColumns");
    }

    public void setLockFormatRows(boolean lockFormatRows, boolean notify) {
        consoleLog("setLockFormatRows(" + lockFormatRows + ")");
        getState().lockFormatRows = lockFormatRows;
        if (notify) notifyStateChange("lockFormatRows");
    }

    public void setNamedRanges(String namedRanges, boolean notify) {
        consoleLog("setNamedRanges(" + namedRanges + ")");
        getState().namedRanges = Parser.parseArraylistString(namedRanges);
        if (notify) notifyStateChange("namedRanges");
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
        getServerRpcInstance().setGroupingCollapsedCallback(callback);
    }

    public void setLevelHeaderClickedCallback(SerializedCallback callback) {
        getServerRpcInstance().setLevelHeaderClickedCallback(callback);
    }

    public void setOnSheetScrollCallback(SerializedCallback callback) {
        getServerRpcInstance().setOnSheetScrollCallback(callback);
    }

    public void setSheetAddressChangedCallback(StringCallback callback) {
        getServerRpcInstance().setSheetAddressChangedCallback(callback);
    }

    public void setCellSelectedCallback(SerializedCallback callback) {
        getServerRpcInstance().setCellSelectedCallback(callback);
    }

    public void setCellRangeSelectedCallback(SerializedCallback callback) {
        getServerRpcInstance().setCellRangeSelectedCallback(callback);
    }

    public void setCellAddedToSelectionAndSelectedCallback(SerializedCallback callback) {
        getServerRpcInstance().setCellAddedToSelectionAndSelectedCallback(callback);
    }

    public void setCellsAddedToRangeSelectionCallback(SerializedCallback callback) {
        getServerRpcInstance().setCellsAddedToRangeSelectionCallback(callback);
    }

    public void setRowSelectedCallback(SerializedCallback callback) {
        getServerRpcInstance().setRowSelectedCallback(callback);
    }

    public void setRowAddedToRangeSelectionCallback(SerializedCallback callback) {
        getServerRpcInstance().setRowAddedToRangeSelectionCallback(callback);
    }

    public void setColumnSelectedCallback(SerializedCallback callback) {
        getServerRpcInstance().setColumnSelectedCallback(callback);
    }

    public void setColumnAddedToSelectionCallback(SerializedCallback callback) {
        getServerRpcInstance().setColumnAddedToSelectionCallback(callback);
    }

    public void setSelectionIncreasePaintedCallback(SerializedCallback callback) {
        getServerRpcInstance().setSelectionIncreasePaintedCallback(callback);
    }


    public void setSelectionDecreasePaintedCallback(SerializedCallback callback) {
        getServerRpcInstance().setSelectionDecreasePaintedCallback(callback);
    }

    public void setCellValueEditedCallback(SerializedCallback callback) {
        getServerRpcInstance().setCellValueEditedCallback(callback);
    }

    public void setSheetSelectedCallback(SerializedCallback callback) {
        getServerRpcInstance().setSheetSelectedCallback(callback);
    }

    public void setSheetRenamedCallback(SerializedCallback callback) {
        getServerRpcInstance().setSheetRenamedCallback(callback);
    }

    public void setSheetCreatedCallback(SerializedCallback callback) {
        getServerRpcInstance().setSheetCreatedCallback(callback);
    }

    public void setCellRangePaintedCallback(SerializedCallback callback) {
        getServerRpcInstance().setCellRangePaintedCallback(callback);
    }

    public void setDeleteSelectedCellsCallback(SerializedCallback callback) {
        getServerRpcInstance().setDeleteSelectedCellsCallback(callback);
    }

    public void setLinkCellClickedCallback(SerializedCallback callback) {
        getServerRpcInstance().setLinkCellClickedCallback(callback);
    }

    public void setRowsResizedCallback(SerializedCallback callback) {
        getServerRpcInstance().setRowsResizedCallback(callback);
    }

    public void setColumnResizedCallback(SerializedCallback callback) {
        getServerRpcInstance().setColumnResizedCallback(callback);
    }

    public void setOnRowAutofitCallback(IntCallback callback) {
        getServerRpcInstance().setOnRowAutofitCallback(callback);
    }

    public void setOnColumnAutofitCallback(IntCallback callback) {
        getServerRpcInstance().setOnColumnAutofitCallback(callback);
    }

    public void setOnUndoCallback(VoidCallback callback) {
        getServerRpcInstance().setOnUndoCallback(callback);
    }

    public void setOnRedoCallback(VoidCallback callback) {
        getServerRpcInstance().setOnRedoCallback(callback);
    }

    public void setSetCellStyleWidthRatiosCallback(SerializedCallback callback) {
        getServerRpcInstance().setSetCellStyleWidthRatiosCallback(callback);
    }

    public void setProtectedCellWriteAttemptedCallback(VoidCallback callback) {
        getServerRpcInstance().setProtectedCellWriteAttemptedCallback(callback);
    }

    public void setOnPasteCallback(StringCallback callback) {
        getServerRpcInstance().setOnPasteCallback(callback);
    }

    public void setClearSelectedCellsOnCutCallback(VoidCallback callback) {
        getServerRpcInstance().setClearSelectedCellsOnCutCallback(callback);
    }

    public void setUpdateCellCommentCallback(SerializedCallback callback) {
        getServerRpcInstance().setUpdateCellCommentCallback(callback);
    }

    public void setOnConnectorInitCallback(VoidCallback callback) {
        getServerRpcInstance().setOnConnectorInitCallback(callback);
    }

    public void setContextMenuOpenOnSelectionCallback(SerializedCallback callback) {
        getServerRpcInstance().setContextMenuOpenOnSelectionCallback(callback);
    }

    public void setActionOnCurrentSelectionCallback(StringCallback callback) {
        getServerRpcInstance().setActionOnCurrentSelectionCallback(callback);
    }

    public void setRowHeaderContextMenuOpenCallback(IntCallback callback) {
        getServerRpcInstance().setRowHeaderContextMenuOpenCallback(callback);
    }

    public void setActionOnRowHeaderCallback(StringCallback callback) {
        getServerRpcInstance().setActionOnRowHeaderCallback(callback);
    }

    public void setColumnHeaderContextMenuOpenCallback(IntCallback callback) {
        getServerRpcInstance().setColumnHeaderContextMenuOpenCallback(callback);
    }

    public void setActionOnColumnHeaderCallback(StringCallback callback) {
        getServerRpcInstance().setActionOnColumnHeaderCallback(callback);
    }



}
