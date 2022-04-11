package com.vaadin.spreadsheet.flowport.gwtexporter.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import jsinterop.annotations.JsType;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootPanel;
import com.vaadin.addon.spreadsheet.client.PopupButtonConnector;
import com.vaadin.addon.spreadsheet.client.PopupButtonState;
import com.vaadin.addon.spreadsheet.client.PopupButtonWidget;
import com.vaadin.addon.spreadsheet.client.SheetWidget;
import com.vaadin.addon.spreadsheet.client.SpreadsheetClientRpc;
import com.vaadin.addon.spreadsheet.client.SpreadsheetConnector;
import com.vaadin.addon.spreadsheet.client.SpreadsheetHandler;
import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;
import com.vaadin.addon.spreadsheet.client.SpreadsheetWidget;
import com.vaadin.addon.spreadsheet.shared.SpreadsheetState;
import com.vaadin.client.ApplicationConfiguration;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ConnectorMap;
import com.vaadin.client.Profiler;
import com.vaadin.client.ValueMap;
import com.vaadin.client.WidgetSet;
import com.vaadin.client.communication.MessageHandler;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.metadata.ConnectorBundleLoader;
import com.vaadin.client.metadata.TypeDataStore;
import com.vaadin.shared.annotations.NoLayout;
import com.vaadin.shared.communication.URLReference;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.ErrorLevel;

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
    Map<String, PopupButtonWidget> popupButtonWidgets = new HashMap<>();
    Map<String, PopupButtonConnector> popupButtonConnectors = new HashMap<>();
    Map<String, PopupButtonState> popupButtonStates = new HashMap<>();
    protected ApplicationConnection applicationConnection;
    private String originalStyles;

    native void consoleLog(String message) /*-{
      console.log("spreadsheetapi", message );
  }-*/;

    native void debugger() /*-{
      debugger;
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

    /*
     * Takes in a JSON String and evals it.
     * @param JSON String that you trust
     * @return JavaScriptObject that you can cast to an Overlay Type
     */
    public static <T extends JavaScriptObject> T parseJson(String jsonStr)
    {
        return JsonUtils.safeEval(jsonStr);
    }

    private void init(Element element) {
        // Only support eager connectors for now
        ConnectorBundleLoader.get()
                .loadBundle(ConnectorBundleLoader.EAGER_BUNDLE_NAME, null);

        applicationConnection = new ApplicationConnection();
        spreadsheetConnector = new SpreadsheetConnector();
        spreadsheetConnector.doInit("1", new ApplicationConnection());
        spreadsheetWidget = spreadsheetConnector.getWidget();

        //initState(spreadsheetConnector.getState());
        /*
        ApplicationConfiguration conf = ApplicationConfiguration.getConfigFromJson("1", json0, element);
        // must be initialized after conf
        Profiler.initialize();
        applicationConnection.init(new WidgetSet(), conf);

        RootPanel.getForElement(element).add(applicationConnection.getUIConnector().getWidget());
        consoleLog("widget appended !");

        Scheduler.get().scheduleDeferred(() -> {
            String[] jsons = {json1, json2};
            for (String json : jsons) {
                applicationConnection.getMessageHandler().handleMessage(MessageHandler.parseWrappedJson(json));
            }
            //spreadsheetConnector = (SpreadsheetConnector) ConnectorMap.get(applicationConnection).getConnector("0");
            //spreadsheetWidget = spreadsheetConnector.getWidget();
            //spreadsheetWidget.setHeight("100%");
        });
*/

        // esto es para evitar el bundle
        TypeDataStore.get().setClass(spreadsheetConnector.getClass().getName(), SpreadsheetConnector.class);

        RootPanel.getForElement(element).add(spreadsheetWidget);
    }

    public void disconnected() {
        if (spreadsheetConnector != null) {
            spreadsheetConnector.onUnregister();
        }
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
                "namedRanges",

            "height",
                "width",
                "description",
                "descriptionContentMode",
                "caption",
                "styles",
                "id",
                "primaryStyleName",
                "errorMessage",
                "captionAsHtml",
                "tabIndex",
                "enabled"
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

                if ("height".equals(propertyName)) w.setHeight(s.height);
                if ("width".equals(propertyName)) w.setWidth(s.width);
                if ("id".equals(propertyName)) w.setId(s.id);
            }
        }
    }

    public void layout() {
        spreadsheetConnector.getLayoutManager().layoutNow();
        spreadsheetWidget.relayoutSheet();
        spreadsheetConnector.postLayout();
    }

    public void relayout() {
        Scheduler.get().scheduleDeferred(() -> {
            //spreadsheetWidget.getSheetWidget().ensureCustomStyleTagsAreInTheRightShadowRoot();
            spreadsheetWidget.relayoutSheet();
        });
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





    public void setRowBufferSize(int rowBufferSize) {
        getState().rowBufferSize = rowBufferSize;
    }

    public void setColumnBufferSize(int columnBufferSize) {
        getState().columnBufferSize = columnBufferSize;
    }

    public void setRows(int rows) {
        getState().rows = rows;
    }

    public void setCols(int cols) {
        getState().cols = cols;
    }

    public void setColGroupingData(String colGroupingData) {
        getState().colGroupingData = Parser.parseListOfGroupingData(colGroupingData);
    }

    public void setRowGroupingData(String rowGroupingData) {
        getState().rowGroupingData = Parser.parseListOfGroupingData(rowGroupingData);
    }

    public void setColGroupingMax(int colGroupingMax) {
        getState().colGroupingMax = colGroupingMax;
    }

    public void setRowGroupingMax(int rowGroupingMax) {
        getState().rowGroupingMax = rowGroupingMax;
    }

    public void setColGroupingInversed(boolean colGroupingInversed) {
        getState().colGroupingInversed = colGroupingInversed;
    }

    public void setRowGroupingInversed(boolean rowGroupingInversed) {
        getState().rowGroupingInversed = rowGroupingInversed;
    }

    public void setDefRowH(float defRowH) {
        getState().defRowH = defRowH;
    }

    public void setDefColW(int defColW) {
        getState().defColW = defColW;
    }

    public void setRowH(String rowH) {
        getState().rowH = Parser.parseArrayFloat(rowH);
    }

    public void setColW(String colW) {
        getState().colW = Parser.parseArrayInt(colW);
    }

    public void setReload(boolean reload) {
        getState().reload = true;
    }

    public void setSheetIndex(int sheetIndex) {
        getState().sheetIndex = sheetIndex;
    }

    public void setSheetNames(String sheetNames) {
        getState().sheetNames = Parser.parseArrayOfStrings(sheetNames);
    }

    public void setCellStyleToCSSStyle(String cellStyleToCSSStyle) {
        getState().cellStyleToCSSStyle = Parser.parseMapIntegerString(cellStyleToCSSStyle);
    }

    public void setRowIndexToStyleIndex(String rowIndexToStyleIndex) {
        getState().rowIndexToStyleIndex = Parser.parseMapIntegerInteger(rowIndexToStyleIndex);
    }

    public void setColumnIndexToStyleIndex(String columnIndexToStyleIndex) {
        getState().columnIndexToStyleIndex = Parser.parseMapIntegerInteger(columnIndexToStyleIndex);
    }

    public void setLockedColumnIndexes(String lockedColumnIndexes) {
        getState().lockedColumnIndexes = Parser.parseSetInteger(lockedColumnIndexes);
    }

    public void setLockedRowIndexes(String lockedRowIndexes) {
        getState().lockedRowIndexes = Parser.parseSetInteger(lockedRowIndexes);
    }

    public void setShiftedCellBorderStyles(String shiftedCellBorderStyles) {
        getState().shiftedCellBorderStyles = Parser.parseArraylistString(shiftedCellBorderStyles);
    }

    public void setConditionalFormattingStyles(String conditionalFormattingStyles) {
        getState().conditionalFormattingStyles = Parser.parseMapIntegerString(conditionalFormattingStyles);
    }

    public void setHiddenColumnIndexes(String hiddenColumnIndexes) {
        getState().hiddenColumnIndexes = Parser.parseArraylistInteger(hiddenColumnIndexes);
    }

    public void setHiddenRowIndexes(String hiddenRowIndexes) {
        getState().hiddenRowIndexes = Parser.parseArraylistInteger(hiddenRowIndexes);
    }

    public void setVerticalScrollPositions(String verticalScrollPositions) {
        getState().verticalScrollPositions = Parser.parseArrayInt(verticalScrollPositions);
    }

    public void setHorizontalScrollPositions(String horizontalScrollPositions) {
        getState().horizontalScrollPositions = Parser.parseArrayInt(horizontalScrollPositions);
    }

    public void setSheetProtected(boolean sheetProtected) {
        getState().sheetProtected = sheetProtected;
    }

    public void setWorkbookProtected(boolean workbookProtected) {
        getState().workbookProtected = workbookProtected;
    }

    public void setCellKeysToEditorIdMap(String cellKeysToEditorIdMap) {
        getState().cellKeysToEditorIdMap = Parser.parseMapStringString(cellKeysToEditorIdMap);
    }

    public void setComponentIDtoCellKeysMap(String componentIDtoCellKeysMap) {
        getState().componentIDtoCellKeysMap = Parser.parseMapStringString(componentIDtoCellKeysMap);
    }

    public void setHyperlinksTooltips(String hyperlinksTooltips) {
        getState().hyperlinksTooltips = Parser.parseMapStringString(hyperlinksTooltips);
    }

    public void setCellComments(String cellComments) {
        getState().cellComments = Parser.parseMapStringString(cellComments);
    }

    public void setCellCommentAuthors(String cellCommentAuthors) {
        getState().cellCommentAuthors = Parser.parseMapStringString(cellCommentAuthors);
    }

    public void setVisibleCellComments(String visibleCellComments) {
        getState().visibleCellComments = Parser.parseArraylistString(visibleCellComments);
    }

    public void setInvalidFormulaCells(String invalidFormulaCells) {
        getState().invalidFormulaCells = Parser.parseSetString(invalidFormulaCells);
    }

    public void setHasActions(boolean hasActions) {
        getState().hasActions = hasActions;
    }

    public void setOverlays(String overlays) {
        getState().overlays = Parser.parseMapStringOverlayInfo(overlays);
    }

    public void setMergedRegions(String mergedRegions) {
        getState().mergedRegions = Parser.parseArrayMergedRegion(mergedRegions);
    }

    public void setDisplayGridlines(boolean displayGridlines) {
        getState().displayGridlines = displayGridlines;
    }

    public void setDisplayRowColHeadings(boolean displayRowColHeadings) {
        getState().displayRowColHeadings = displayRowColHeadings;
    }

    public void setVerticalSplitPosition(int verticalSplitPosition) {
        getState().verticalSplitPosition = verticalSplitPosition;
    }

    public void setHorizontalSplitPosition(int horizontalSplitPosition) {
        getState().horizontalSplitPosition = horizontalSplitPosition;
    }

    public void setInfoLabelValue(String infoLabelValue) {
        getState().infoLabelValue = infoLabelValue;
    }

    public void setWorkbookChangeToggle(boolean workbookChangeToggle) {
        getState().workbookChangeToggle = workbookChangeToggle;
    }

    public void setInvalidFormulaErrorMessage(String invalidFormulaErrorMessage) {
        getState().invalidFormulaErrorMessage = invalidFormulaErrorMessage;
    }

    public void setLockFormatColumns(boolean lockFormatColumns) {
        getState().lockFormatColumns = lockFormatColumns;
    }

    public void setLockFormatRows(boolean lockFormatRows) {
        getState().lockFormatRows = lockFormatRows;
    }

    public void setNamedRanges(String namedRanges) {
        getState().namedRanges = Parser.parseArraylistString(namedRanges);
    }

    public void setHeight(String height) {
        getState().height = height;
    }

    public void setWidth(String width) {
        getState().width = width;
    }

    public void setId(String id) {
        getState().id = id;
    }

    public void setClass(String classNames) {
        if (originalStyles  == null) {
            originalStyles = spreadsheetWidget.getStyleName();
        }
        List<String> fixedStyles = Arrays.asList(originalStyles != null?originalStyles.split(" "):new String[0]);
        List<String> oldStyles = Arrays.asList(spreadsheetWidget.getStyleName() != null?spreadsheetWidget.getStyleName().split(" "):new String[0]);
        List<String> newStyles = Arrays.asList(classNames != null?classNames.split(" "):new String[0]);
        for (String style : oldStyles) if (!"".equals(style)) {
            if (!fixedStyles.contains(style) && !newStyles.contains(style)) spreadsheetWidget.removeStyleName(style);
        }
        for (String style : newStyles) if (!"".equals(style)) {
            if (!fixedStyles.contains(style) && !oldStyles.contains(style)) spreadsheetWidget.addStyleName(style);
        }
    }

    public void setPopups(String raw) {
        Map<String, PopupButtonState> l = Parser.parseListOfPopupButtons(raw);
        l.keySet().forEach(k -> {
            if (popupButtonWidgets.containsKey(k)) {
                consoleLog("popup already exists");
            } else {
                consoleLog("adding popup widget");
                PopupButtonWidget w;
                popupButtonWidgets.put(k, w = new PopupButtonWidget());
                PopupButtonConnector c;
                popupButtonConnectors.put(k, c = new PopupButtonConnector());
                PopupButtonState s;
                popupButtonStates.put(k, s = l.get(k));
                w.setCol(s.col);
                w.setRow(s.row);
                w.setPopupHeaderHidden(s.headerHidden);
                w.setSheetWidget(spreadsheetWidget.getSheetWidget(), DivElement.as(spreadsheetWidget.getSheetWidget().getElement()));
                w.setPopupWidth(s.popupWidth);
                w.setPopupHeight(s.popupHeight);
                spreadsheetWidget.addPopupButton(w);
            }
        });
    }

    public void setResources(Element element, String resources) {
        ArrayList<String> l = Parser.parseArraylistString(resources);
        l.forEach(k -> spreadsheetConnector.getConnection().setResource(k, element.getAttribute("resource-" + k)));
    }

    public void notifyStateChanges(String[] propNames, boolean initial) {
        JsonObject stateJson = Json.createObject();
        for (String propName : propNames) stateJson.put(propName, "");
        StateChangeEvent event = new StateChangeEvent(spreadsheetConnector, stateJson, initial);
        delegateToWidget(spreadsheetConnector, event);
        spreadsheetConnector.onStateChanged(event);
    }

    /*
    CLIENT RPC METHODS
     */

    public void updateBottomRightCellValues(String cellData) {
        getClientRpcInstance().updateBottomRightCellValues(Parser.parseArraylistOfCellData(cellData));
    }

    public void updateTopLeftCellValues(String cellData) {
        getClientRpcInstance().updateTopLeftCellValues(Parser.parseArraylistOfCellData(cellData));
    }

    public void updateTopRightCellValues(String cellData) {
        getClientRpcInstance().updateTopRightCellValues(Parser.parseArraylistOfCellData(cellData));
    }

    public void updateBottomLeftCellValues(String cellData) {
        getClientRpcInstance().updateBottomLeftCellValues(Parser.parseArraylistOfCellData(cellData));
    }

    public void updateFormulaBar(String possibleName, int col, int row) {
        getClientRpcInstance().updateFormulaBar(possibleName, col, row);
    }

    public void invalidCellAddress() {
        getClientRpcInstance().invalidCellAddress();
    }

    public void showSelectedCell(String name, int col, int row, String cellValue, boolean function, boolean locked, boolean initialSelection) {
        getClientRpcInstance().showSelectedCell(name, col, row, cellValue, function, locked, initialSelection);
    }

    public void showActions(String actionDetails) {
        getClientRpcInstance().showActions(Parser.parseArraylistSpreadsheetActionDetails(actionDetails));
    }

    public void setSelectedCellAndRange(String name, int col, int row, int c1, int c2, int r1, int r2, boolean scroll) {
        getClientRpcInstance().setSelectedCellAndRange(name, col, row, c1, c2, r1, r2, scroll);
    }

    public void cellsUpdated(String updatedCellData) {
        getClientRpcInstance().cellsUpdated(Parser.parseArraylistOfCellData(updatedCellData));
    }

    public void refreshCellStyles() {
        Scheduler.get().scheduleDeferred(() -> {
            getClientRpcInstance().refreshCellStyles();
        });
    }

    public void editCellComment(int col, int row) {
        // On a new comment, server creates the comment setting the author but
        // properties are updated after actions, thus, executing this
        // asynchronously fixes it (#790)
        Scheduler.get().scheduleDeferred(() -> {
            getClientRpcInstance().editCellComment(col, row);
        });
    }

    /*
    SERVER RPC METHOD CALLBACKS
     */
    public void setGroupingCollapsedCallback(Consumer<String> callback) {
        getServerRpcInstance().setGroupingCollapsedCallback(callback);
    }

    public void setLevelHeaderClickedCallback(Consumer<String> callback) {
        getServerRpcInstance().setLevelHeaderClickedCallback(callback);
    }

    public void setOnSheetScrollCallback(Consumer<String> callback) {
        getServerRpcInstance().setOnSheetScrollCallback(callback);
    }

    public void setSheetAddressChangedCallback(Consumer<String> callback) {
        getServerRpcInstance().setSheetAddressChangedCallback(callback);
    }

    public void setCellSelectedCallback(Consumer<String> callback) {
        getServerRpcInstance().setCellSelectedCallback(callback);
    }

    public void setCellRangeSelectedCallback(Consumer<String> callback) {
        getServerRpcInstance().setCellRangeSelectedCallback(callback);
    }

    public void setCellAddedToSelectionAndSelectedCallback(Consumer<String> callback) {
        getServerRpcInstance().setCellAddedToSelectionAndSelectedCallback(callback);
    }

    public void setCellsAddedToRangeSelectionCallback(Consumer<String> callback) {
        getServerRpcInstance().setCellsAddedToRangeSelectionCallback(callback);
    }

    public void setRowSelectedCallback(Consumer<String> callback) {
        getServerRpcInstance().setRowSelectedCallback(callback);
    }

    public void setRowAddedToRangeSelectionCallback(Consumer<String> callback) {
        getServerRpcInstance().setRowAddedToRangeSelectionCallback(callback);
    }

    public void setColumnSelectedCallback(Consumer<String> callback) {
        getServerRpcInstance().setColumnSelectedCallback(callback);
    }

    public void setColumnAddedToSelectionCallback(Consumer<String> callback) {
        getServerRpcInstance().setColumnAddedToSelectionCallback(callback);
    }

    public void setSelectionIncreasePaintedCallback(Consumer<String> callback) {
        getServerRpcInstance().setSelectionIncreasePaintedCallback(callback);
    }


    public void setSelectionDecreasePaintedCallback(Consumer<String> callback) {
        getServerRpcInstance().setSelectionDecreasePaintedCallback(callback);
    }

    public void setCellValueEditedCallback(Consumer<String> callback) {
        getServerRpcInstance().setCellValueEditedCallback(callback);
    }

    public void setSheetSelectedCallback(Consumer<String> callback) {
        getServerRpcInstance().setSheetSelectedCallback(callback);
    }

    public void setSheetRenamedCallback(Consumer<String> callback) {
        getServerRpcInstance().setSheetRenamedCallback(callback);
    }

    public void setSheetCreatedCallback(Consumer<String> callback) {
        getServerRpcInstance().setSheetCreatedCallback(callback);
    }

    public void setCellRangePaintedCallback(Consumer<String> callback) {
        getServerRpcInstance().setCellRangePaintedCallback(callback);
    }

    public void setDeleteSelectedCellsCallback(Consumer<String> callback) {
        getServerRpcInstance().setDeleteSelectedCellsCallback(callback);
    }

    public void setLinkCellClickedCallback(Consumer<String> callback) {
        getServerRpcInstance().setLinkCellClickedCallback(callback);
    }

    public void setRowsResizedCallback(Consumer<String> callback) {
        getServerRpcInstance().setRowsResizedCallback(callback);
    }

    public void setColumnResizedCallback(Consumer<String> callback) {
        getServerRpcInstance().setColumnResizedCallback(callback);
    }

    public void setOnRowAutofitCallback(Consumer<Integer> callback) {
        getServerRpcInstance().setOnRowAutofitCallback(callback);
    }

    public void setOnColumnAutofitCallback(Consumer<Integer> callback) {
        getServerRpcInstance().setOnColumnAutofitCallback(callback);
    }

    public void setOnUndoCallback(Runnable callback) {
        getServerRpcInstance().setOnUndoCallback(callback);
    }

    public void setOnRedoCallback(Runnable callback) {
        getServerRpcInstance().setOnRedoCallback(callback);
    }

    public void setSetCellStyleWidthRatiosCallback(Consumer<String> callback) {
        getServerRpcInstance().setSetCellStyleWidthRatiosCallback(callback);
    }

    public void setProtectedCellWriteAttemptedCallback(Runnable callback) {
        getServerRpcInstance().setProtectedCellWriteAttemptedCallback(callback);
    }

    public void setOnPasteCallback(Consumer<String> callback) {
        getServerRpcInstance().setOnPasteCallback(callback);
    }

    public void setClearSelectedCellsOnCutCallback(Runnable callback) {
        getServerRpcInstance().setClearSelectedCellsOnCutCallback(callback);
    }

    public void setUpdateCellCommentCallback(Consumer<String> callback) {
        getServerRpcInstance().setUpdateCellCommentCallback(callback);
    }

    public void setOnConnectorInitCallback(Runnable callback) {
        getServerRpcInstance().setOnConnectorInitCallback(callback);
    }

    public void setContextMenuOpenOnSelectionCallback(Consumer<String> callback) {
        getServerRpcInstance().setContextMenuOpenOnSelectionCallback(callback);
    }

    public void setActionOnCurrentSelectionCallback(Consumer<String> callback) {
        getServerRpcInstance().setActionOnCurrentSelectionCallback(callback);
    }

    public void setRowHeaderContextMenuOpenCallback(Consumer<Integer> callback) {
        getServerRpcInstance().setRowHeaderContextMenuOpenCallback(callback);
    }

    public void setActionOnRowHeaderCallback(Consumer<String> callback) {
        getServerRpcInstance().setActionOnRowHeaderCallback(callback);
    }

    public void setColumnHeaderContextMenuOpenCallback(Consumer<Integer> callback) {
        getServerRpcInstance().setColumnHeaderContextMenuOpenCallback(callback);
    }

    public void setActionOnColumnHeaderCallback(Consumer<String> callback) {
        getServerRpcInstance().setActionOnColumnHeaderCallback(callback);
    }








    /*
{"v-uiId":0,"uidl":"{\"Vaadin-Security-Key\":\"b484bdff-0ede-463f-92ae-5614fa3243ab\",\"syncId\": 0, \"resynchronize\": true, \"clientId\": 0, \"changes\" : [[\"change\",{\"pid\":\"0\"},[\"0\",{\"id\":\"0\"}]]], \"state\":{\"0\":{\"localeServiceState\":{\"localeData\":[{\"name\":\"es_ES\",\"monthNames\":[\"enero\",\"febrero\",\"marzo\",\"abril\",\"mayo\",\"junio\",\"julio\",\"agosto\",\"septiembre\",\"octubre\",\"noviembre\",\"diciembre\"],\"shortMonthNames\":[\"ene\",\"feb\",\"mar\",\"abr\",\"may\",\"jun\",\"jul\",\"ago\",\"sep\",\"oct\",\"nov\",\"dic\"],\"shortDayNames\":[\"dom\",\"lun\",\"mar\",\"mié\",\"jue\",\"vie\",\"sáb\"],\"dayNames\":[\"domingo\",\"lunes\",\"martes\",\"miércoles\",\"jueves\",\"viernes\",\"sábado\"],\"firstDayOfWeek\":1,\"dateFormat\":\"d/MM/yy\",\"twelveHourClock\":false,\"hourMinuteDelimiter\":\":\",\"am\":null,\"pm\":null}]},\"theme\":\"demo\",\"height\":\"100.0%\",\"width\":\"100.0%\"},\"1\":{\"rows\":200,\"cols\":52,\"colGroupingData\":[],\"rowGroupingData\":[],\"defRowH\":15,\"defColW\":56,\"rowH\":[15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15],\"colW\":[56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56],\"reload\":true,\"sheetNames\":[\"Hoja 1\"],\"cellStyleToCSSStyle\":[[0],[\"font-family:Calibri,swiss,Helvetica,arial;font-size:11pt;background-color:rgba(255,255,255,1.0);color:rgba(0, 0, 0, 1.0);\"]],\"rowIndexToStyleIndex\":[],\"columnIndexToStyleIndex\":[[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52],[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]],\"lockedColumnIndexes\":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52],\"lockedRowIndexes\":[],\"shiftedCellBorderStyles\":[],\"conditionalFormattingStyles\":[],\"hiddenColumnIndexes\":[],\"hiddenRowIndexes\":[],\"verticalScrollPositions\":[0],\"horizontalScrollPositions\":[0],\"hasActions\":true,\"workbookChangeToggle\":true,\"namedRanges\":[],\"height\":\"100.0%\",\"width\":\"100.0%\"}}, \"types\":{\"0\":\"0\",\"1\":\"1\"}, \"hierarchy\":{\"0\":[\"1\"]}, \"rpc\" : [], \"meta\" : {\"repaintAll\":true}, \"resources\" : {}, \"typeMappings\" : { \"com.vaadin.ui.AbstractComponent\" : 2 , \"com.vaadin.server.AbstractClientConnector\" : 3 , \"com.vaadin.addon.spreadsheet.Spreadsheet\" : 1 , \"com.vaadin.ui.AbstractSingleComponentContainer\" : 4 , \"com.vaadin.addon.spreadsheet.test.demoapps.SpreadsheetOnlyUI\" : 0 , \"com.vaadin.ui.UI\" : 5 }, \"typeInheritanceMap\" : { \"2\" : 3 , \"1\" : 2 , \"4\" : 2 , \"0\" : 5 , \"5\" : 4 }, \"timings\":[51, 51]}"}


for(;;);[{"syncId": 1, "clientId": 1, "changes" : [], "state":{}, "types":{}, "hierarchy":{}, "rpc" : [], "meta" : {}, "resources" : {}, "timings":[392, 341]}]


for(;;);[{"syncId": 2, "clientId": 2, "changes" : [], "state":{"1":{"reload":false,"hyperlinksTooltips":[],"cellComments":[],"cellCommentAuthors":[],"visibleCellComments":[],"invalidFormulaCells":[]}}, "types":{"1":"1"}, "hierarchy":{}, "rpc" : [["1","com.vaadin.addon.spreadsheet.client.SpreadsheetClientRpc","updateBottomRightCellValues",[[{"row":1,"col":1,"value":"Hola!","formulaValue":null,"originalValue":"Hola!","cellStyle":"cs0","locked":false,"needsMeasure":false,"isPercentage":false}]]],["1","com.vaadin.addon.spreadsheet.client.SpreadsheetClientRpc","showSelectedCell",[null,1,1,"Hola!",false,false,true]]], "meta" : {}, "resources" : {}, "timings":[394, 2]}]


  */
    public void setInitialState() {
        /*
        {\"rows\":200,\"cols\":52,\"colGroupingData\":[],\"rowGroupingData\":[],\"defRowH\":15,\"defColW\":56,\"rowH\":[15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15],\"colW\":[56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56,56],\"reload\":true,\"sheetNames\":[\"Hoja 1\"],\"cellStyleToCSSStyle\":[[0],[\"font-family:Calibri,swiss,Helvetica,arial;font-size:11pt;background-color:rgba(255,255,255,1.0);color:rgba(0, 0, 0, 1.0);\"]],\"rowIndexToStyleIndex\":[],\"columnIndexToStyleIndex\":[[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52],[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]],\"lockedColumnIndexes\":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52],\"lockedRowIndexes\":[],\"shiftedCellBorderStyles\":[],\"conditionalFormattingStyles\":[],\"hiddenColumnIndexes\":[],\"hiddenRowIndexes\":[],\"verticalScrollPositions\":[0],\"horizontalScrollPositions\":[0],\"hasActions\":true,\"workbookChangeToggle\":true,\"namedRanges\":[],\"height\":\"100.0%\",\"width\":\"100.0%\"}
         */
        setCols(52);
        setRows(200);
        setColGroupingData("");
        setRowGroupingData("");
        setDefRowH(15);
        setDefColW(56);
        setRowH("15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15"
        );
        setColW("70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70");
        setReload(true);
        setSheetNames("Hoja 1");
        setCellStyleToCSSStyle("0@\"font-family:Calibri,swiss,Helvetica,arial;font-size:11pt;background-color:rgba(255,255,255,1.0);color:rgba(0, 0, 0, 1.0);\"");
        setRowIndexToStyleIndex("");
        setColumnIndexToStyleIndex("");
        setLockedColumnIndexes("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52");
        setLockedRowIndexes("");
        setShiftedCellBorderStyles("");
        setConditionalFormattingStyles("");
        setHiddenColumnIndexes("");
        setHiddenRowIndexes("");
        setVerticalScrollPositions("0");
        setHorizontalScrollPositions("0");
        setHasActions(true);
        setWorkbookChangeToggle(true);
        setNamedRanges("");
        setHeight("100.0%");
        setWidth("100.0%");

        StateChangeEvent event = new StateChangeEvent(spreadsheetConnector, Json.createObject(), true);
        delegateToWidget(spreadsheetConnector, event);

        spreadsheetConnector.onStateChanged(event);

    }

    public void load() {
        spreadsheetWidget.load();
    }

    public void relayoutSheet() {
        spreadsheetWidget.relayoutSheet();
    }

    public void updateCellsAndRefreshCellStyles() {


    }

    public void updateBottomRightCellValuesAndShowSelectedCell() {
        /*
        for(;;);[{"syncId": 2, "clientId": 2, "changes" : [], "state":{"1":{"reload":false,"hyperlinksTooltips":[],"cellComments":[],"cellCommentAuthors":[],"visibleCellComments":[],"invalidFormulaCells":[]}}, "types":{"1":"1"}, "hierarchy":{}, "rpc" : [["1","com.vaadin.addon.spreadsheet.client.SpreadsheetClientRpc","updateBottomRightCellValues",[[{"row":1,"col":1,"value":"Hola!","formulaValue":null,"originalValue":"Hola!","cellStyle":"cs0","locked":false,"needsMeasure":false,"isPercentage":false}]]],["1","com.vaadin.addon.spreadsheet.client.SpreadsheetClientRpc","showSelectedCell",[null,1,1,"Hola!",false,false,true]]], "meta" : {}, "resources" : {}, "timings":[394, 2]}]
         */
        setReload(false);
        setHyperlinksTooltips("");
        setCellComments("");
        setCellCommentAuthors("");
        setInvalidFormulaCells("");

        updateBottomRightCellValues("1#1#\"Hola!\"#null#\"Hola!\"#\"cs0\"#false#false#false");
        showSelectedCell(null, 1, 1, "Hola!", false, false, true);
    }


}
