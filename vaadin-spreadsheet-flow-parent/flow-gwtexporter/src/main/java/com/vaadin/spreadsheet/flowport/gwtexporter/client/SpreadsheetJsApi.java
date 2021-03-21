package com.vaadin.spreadsheet.flowport.gwtexporter.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jsinterop.annotations.JsType;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dev.util.collect.Lists;
import com.google.gwt.dev.util.collect.Maps;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.addon.spreadsheet.client.CellData;
import com.vaadin.addon.spreadsheet.client.MergedRegion;
import com.vaadin.addon.spreadsheet.client.OverlayInfo;
import com.vaadin.addon.spreadsheet.client.PopupButtonConnector;
import com.vaadin.addon.spreadsheet.client.PopupButtonWidget;
import com.vaadin.addon.spreadsheet.client.SpreadsheetActionDetails;
import com.vaadin.addon.spreadsheet.client.SpreadsheetClientRpc;
import com.vaadin.addon.spreadsheet.client.SpreadsheetConnector;
import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;
import com.vaadin.addon.spreadsheet.client.SpreadsheetWidget;
import com.vaadin.addon.spreadsheet.shared.GroupingData;
import com.vaadin.addon.spreadsheet.shared.SpreadsheetState;
import com.vaadin.client.ApplicationConfiguration;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.FastStringSet;
import com.vaadin.client.Profiler;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.WidgetSet;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.metadata.ConnectorBundleLoader;
import com.vaadin.client.metadata.NoDataException;
import com.vaadin.client.metadata.Property;
import com.vaadin.client.metadata.Type;
import com.vaadin.client.metadata.TypeData;
import com.vaadin.client.metadata.TypeDataStore;
import com.vaadin.client.ui.AbstractConnector;
import com.vaadin.shared.annotations.DelegateToWidget;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ActionOnColumnHeaderCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ActionOnCurrentSelectionCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ActionOnRowHeaderCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.CellAddedToSelectionAndSelectedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.CellRangePaintedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.CellRangeSelectedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.CellSelectedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.CellValueEditedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.CellsAddedToRangeSelectionCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ClearSelectedCellsOnCutCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ColumnAddedToSelectionCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ColumnHeaderContextMenuOpenCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ColumnResizedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ColumnSelectedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ContextMenuOpenOnSelectionCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.DeleteSelectedCellsCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.GroupingCollapsedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.LevelHeaderClickedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.LinkCellClickedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnColumnAutofitCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnConnectorInitCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnPasteCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnRedoCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnRowAutofitCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnSheetScrollCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.OnUndoCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.ProtectedCellWriteAttemptedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.RowAddedToRangeSelectionCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.RowHeaderContextMenuOpenCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.RowSelectedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.RowsResizedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SelectionDecreasePaintedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SelectionIncreasePaintedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SetCellStyleWidthRatiosCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SheetAddressChangedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SheetCreatedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SheetRenamedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.SheetSelectedCallback;
import com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks.UpdateCellCommentCallback;

import elemental.json.Json;
import elemental.json.JsonObject;

/**
 *
 * this is the public api which we will export to js
 *
 */
@JsType
public class SpreadsheetJsApi implements SpreadsheetClientRpc {

    protected SpreadsheetWidget spreadsheetWidget;
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

    public void setRowBufferSize(int rowBufferSize) {
        getState().rowBufferSize = rowBufferSize;
        notifyStateChange("rowBufferSize");
    }

    public void setColumnBufferSize(int columnBufferSize) {
        getState().columnBufferSize = columnBufferSize;
        notifyStateChange("columnBufferSize");
    }

    public void setRows(int rows) {
        getState().rows = rows;
        notifyStateChange("rows");
    }

    public void setCols(int cols) {
        getState().cols = cols;
        notifyStateChange("cols");
    }

    public void setColGroupingData(List<GroupingData> colGroupingData) {
        getState().colGroupingData = colGroupingData;
        notifyStateChange("colGroupingData");
    }

    public void setRowGroupingData(List<GroupingData> rowGroupingData) {
        getState().rowGroupingData = rowGroupingData;
        notifyStateChange("rowGroupingData");
    }

    public void setColGroupingMax(int colGroupingMax) {
        getState().colGroupingMax = colGroupingMax;
        notifyStateChange("colGroupingMax");
    }

    public void setRowGroupingMax(int rowGroupingMax) {
        getState().rowGroupingMax = rowGroupingMax;
        notifyStateChange("rowGroupingMax");
    }

    public void setColGroupingInversed(boolean colGroupingInversed) {
        getState().colGroupingInversed = colGroupingInversed;
        notifyStateChange("colGroupingInversed");
    }

    public void setRowGroupingInversed(boolean rowGroupingInversed) {
        getState().rowGroupingInversed = rowGroupingInversed;
        notifyStateChange("rowGroupingInversed");
    }

    public void setDefRowH(float defRowH) {
        getState().defRowH = defRowH;
        notifyStateChange("defRowH");
    }

    public void setDefColW(int defColW) {
        getState().defColW = defColW;
        notifyStateChange("defColW");
    }

    public void setRowH(float[] rowH) {
        getState().rowH = rowH;
        notifyStateChange("rowH");
    }

    public void setColW(int[] colW) {
        getState().colW = colW;
        notifyStateChange("colW");
    }

    public void setReload(boolean reload) {
        getState().reload = reload;
        notifyStateChange("reload");
    }

    public void setSheetIndex(int sheetIndex) {
        getState().sheetIndex = sheetIndex;
        notifyStateChange("sheetIndex");
    }

    public void setSheetNames(String[] sheetNames) {
        getState().sheetNames = sheetNames;
        notifyStateChange("sheetNames");
    }

    public void setCellStyleToCSSStyle(HashMap<Integer, String> cellStyleToCSSStyle) {
        getState().cellStyleToCSSStyle = cellStyleToCSSStyle;
        notifyStateChange("cellStyleToCSSStyle");
    }

    public void setRowIndexToStyleIndex(HashMap<Integer, Integer> rowIndexToStyleIndex) {
        getState().rowIndexToStyleIndex = rowIndexToStyleIndex;
        notifyStateChange("rowIndexToStyleIndex");
    }

    public void setColumnIndexToStyleIndex(HashMap<Integer, Integer> columnIndexToStyleIndex) {
        getState().columnIndexToStyleIndex = columnIndexToStyleIndex;
        notifyStateChange("columnIndexToStyleIndex");
    }

    public void setLockedColumnIndexes(Set<Integer> lockedColumnIndexes) {
        getState().lockedColumnIndexes = lockedColumnIndexes;
        notifyStateChange("lockedColumnIndexes");
    }

    public void setLockedRowIndexes(Set<Integer> lockedRowIndexes) {
        getState().lockedRowIndexes = lockedRowIndexes;
        notifyStateChange("lockedRowIndexes");
    }

    public void setShiftedCellBorderStyles(ArrayList<String> shiftedCellBorderStyles) {
        getState().shiftedCellBorderStyles = shiftedCellBorderStyles;
        notifyStateChange("shiftedCellBorderStyles");
    }

    public void setConditionalFormattingStyles(HashMap<Integer, String> conditionalFormattingStyles) {
        getState().conditionalFormattingStyles = conditionalFormattingStyles;
        notifyStateChange("conditionalFormattingStyles");
    }

    public void setHiddenColumnIndexes(ArrayList<Integer> hiddenColumnIndexes) {
        getState().hiddenColumnIndexes = hiddenColumnIndexes;
        notifyStateChange("hiddenColumnIndexes");
    }

    public void setHiddenRowIndexes(ArrayList<Integer> hiddenRowIndexes) {
        getState().hiddenRowIndexes = hiddenRowIndexes;
        notifyStateChange("hiddenRowIndexes");
    }

    public void setVerticalScrollPositions(int[] verticalScrollPositions) {
        getState().verticalScrollPositions = verticalScrollPositions;
        notifyStateChange("verticalScrollPositions");
    }

    public void setHorizontalScrollPositions(int[] horizontalScrollPositions) {
        getState().horizontalScrollPositions = horizontalScrollPositions;
        notifyStateChange("horizontalScrollPositions");
    }

    public void setSheetProtected(boolean sheetProtected) {
        getState().sheetProtected = sheetProtected;
        notifyStateChange("sheetProtected");
    }

    public void setWorkbookProtected(boolean workbookProtected) {
        getState().workbookProtected = workbookProtected;
        notifyStateChange("workbookProtected");
    }

    public void setCellKeysToEditorIdMap(HashMap<String, String> cellKeysToEditorIdMap) {
        getState().cellKeysToEditorIdMap = cellKeysToEditorIdMap;
        notifyStateChange("cellKeysToEditorIdMap");
    }

    public void setComponentIDtoCellKeysMap(HashMap<String, String> componentIDtoCellKeysMap) {
        getState().componentIDtoCellKeysMap = componentIDtoCellKeysMap;
        notifyStateChange("componentIDtoCellKeysMap");
    }

    public void setHyperlinksTooltips(HashMap<String, String> hyperlinksTooltips) {
        getState().hyperlinksTooltips = hyperlinksTooltips;
        notifyStateChange("hyperlinksTooltips");
    }

    public void setCellComments(HashMap<String, String> cellComments) {
        getState().cellComments = cellComments;
        notifyStateChange("cellComments");
    }

    public void setCellCommentAuthors(HashMap<String, String> cellCommentAuthors) {
        getState().cellCommentAuthors = cellCommentAuthors;
        notifyStateChange("cellCommentAuthors");
    }

    public void setVisibleCellComments(ArrayList<String> visibleCellComments) {
        getState().visibleCellComments = visibleCellComments;
        notifyStateChange("visibleCellComments");
    }

    public void setInvalidFormulaCells(Set<String> invalidFormulaCells) {
        getState().invalidFormulaCells = invalidFormulaCells;
        notifyStateChange("invalidFormulaCells");
    }

    public void setHasActions(boolean hasActions) {
        getState().hasActions = hasActions;
        notifyStateChange("hasActions");
    }

    public void setOverlays(HashMap<String, OverlayInfo> overlays) {
        getState().overlays = overlays;
        notifyStateChange("overlays");
    }

    public void setMergedRegions(ArrayList<MergedRegion> mergedRegions) {
        getState().mergedRegions = mergedRegions;
        notifyStateChange("mergedRegions");
    }

    public void setDisplayGridlines(boolean displayGridlines) {
        getState().displayGridlines = displayGridlines;
        notifyStateChange("displayGridlines");
    }

    public void setDisplayRowColHeadings(boolean displayRowColHeadings) {
        getState().displayRowColHeadings = displayRowColHeadings;
        notifyStateChange("displayRowColHeadings");
    }

    public void setVerticalSplitPosition(int verticalSplitPosition) {
        getState().verticalSplitPosition = verticalSplitPosition;
        notifyStateChange("verticalSplitPosition");
    }

    public void setHorizontalSplitPosition(int horizontalSplitPosition) {
        getState().horizontalSplitPosition = horizontalSplitPosition;
        notifyStateChange("horizontalSplitPosition");
    }

    public void setInfoLabelValue(String infoLabelValue) {
        getState().infoLabelValue = infoLabelValue;
        notifyStateChange("infoLabelValue");
    }

    public void setWorkbookChangeToggle(boolean workbookChangeToggle) {
        getState().workbookChangeToggle = workbookChangeToggle;
        notifyStateChange("workbookChangeToggle");
    }

    public void setInvalidFormulaErrorMessage(String invalidFormulaErrorMessage) {
        getState().invalidFormulaErrorMessage = invalidFormulaErrorMessage;
        notifyStateChange("invalidFormulaErrorMessage");
    }

    public void setLockFormatColumns(boolean lockFormatColumns) {
        getState().lockFormatColumns = lockFormatColumns;
        notifyStateChange("lockFormatColumns");
    }

    public void setLockFormatRows(boolean lockFormatRows) {
        getState().lockFormatRows = lockFormatRows;
        notifyStateChange("lockFormatRows");
    }

    public void setNamedRanges(List<String> namedRanges) {
        getState().namedRanges = namedRanges;
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

    @Override
    public void updateBottomRightCellValues(ArrayList<CellData> cellData) {
        getClientRpcInstance().updateBottomRightCellValues(cellData);
    }

    @Override
    public void updateTopLeftCellValues(ArrayList<CellData> cellData) {
        getClientRpcInstance().updateTopLeftCellValues(cellData);
    }

    @Override
    public void updateTopRightCellValues(ArrayList<CellData> cellData) {
        getClientRpcInstance().updateTopRightCellValues(cellData);
    }

    @Override
    public void updateBottomLeftCellValues(ArrayList<CellData> cellData) {
        getClientRpcInstance().updateBottomLeftCellValues(cellData);
    }

    @Override
    public void updateFormulaBar(String possibleName, int col, int row) {
        getClientRpcInstance().updateFormulaBar(possibleName, col, row);
    }

    @Override
    public void invalidCellAddress() {
        getClientRpcInstance().invalidCellAddress();
    }

    @Override
    public void showSelectedCell(String name, int col, int row, String cellValue, boolean function, boolean locked, boolean initialSelection) {
        getClientRpcInstance().showSelectedCell(name, col, row, cellValue, function, locked, initialSelection);
    }

    @Override
    public void showActions(ArrayList<SpreadsheetActionDetails> actionDetails) {
        getClientRpcInstance().showActions(actionDetails);
    }

    @Override
    public void setSelectedCellAndRange(String name, int col, int row, int c1, int c2, int r1, int r2, boolean scroll) {
        getClientRpcInstance().setSelectedCellAndRange(name, col, row, c1, c2, r1, r2, scroll);
    }

    @Override
    public void cellsUpdated(ArrayList<CellData> updatedCellData) {
        getClientRpcInstance().cellsUpdated(updatedCellData);
    }

    @Override
    public void refreshCellStyles() {
        getClientRpcInstance().refreshCellStyles();
    }

    @Override
    public void editCellComment(int col, int row) {
        getClientRpcInstance().editCellComment(col, row);
    }

    /*
    SERVER RPC METHOD CALLBACKS
     */
    public void setGroupingCollapsedCallback(GroupingCollapsedCallback callback) {
        getServerRpcInstance().setGroupingCollapsedCallback(callback);
    }

    public void setLevelHeaderClickedCallback(LevelHeaderClickedCallback callback) {
        getServerRpcInstance().setLevelHeaderClickedCallback(callback);
    }

    public void setOnSheetScrollCallback(OnSheetScrollCallback callback) {
        getServerRpcInstance().setOnSheetScrollCallback(callback);
    }

    public void setSheetAddressChangedCallback(SheetAddressChangedCallback callback) {
        getServerRpcInstance().setSheetAddressChangedCallback(callback);
    }

    public void setCellSelectedCallback(CellSelectedCallback callback) {
        getServerRpcInstance().setCellSelectedCallback(callback);
    }

    public void setCellRangeSelectedCallback(CellRangeSelectedCallback callback) {
        getServerRpcInstance().setCellRangeSelectedCallback(callback);
    }

    public void setCellAddedToSelectionAndSelectedCallback(CellAddedToSelectionAndSelectedCallback callback) {
        getServerRpcInstance().setCellAddedToSelectionAndSelectedCallback(callback);
    }

    public void setCellsAddedToRangeSelectionCallback(CellsAddedToRangeSelectionCallback callback) {
        getServerRpcInstance().setCellsAddedToRangeSelectionCallback(callback);
    }

    public void setRowSelectedCallback(RowSelectedCallback callback) {
        getServerRpcInstance().setRowSelectedCallback(callback);
    }

    public void setRowAddedToRangeSelectionCallback(RowAddedToRangeSelectionCallback callback) {
        getServerRpcInstance().setRowAddedToRangeSelectionCallback(callback);
    }

    public void setColumnSelectedCallback(ColumnSelectedCallback callback) {
        getServerRpcInstance().setColumnSelectedCallback(callback);
    }

    public void setColumnAddedToSelectionCallback(ColumnAddedToSelectionCallback callback) {
        getServerRpcInstance().setColumnAddedToSelectionCallback(callback);
    }

    public void setSelectionIncreasePaintedCallback(SelectionIncreasePaintedCallback callback) {
        getServerRpcInstance().setSelectionIncreasePaintedCallback(callback);
    }


    public void setSelectionDecreasePaintedCallback(SelectionDecreasePaintedCallback callback) {
        getServerRpcInstance().setSelectionDecreasePaintedCallback(callback);
    }

    public void setCellValueEditedCallback(CellValueEditedCallback callback) {
        getServerRpcInstance().setCellValueEditedCallback(callback);
    }

    public void setSheetSelectedCallback(SheetSelectedCallback callback) {
        getServerRpcInstance().setSheetSelectedCallback(callback);
    }

    public void setSheetRenamedCallback(SheetRenamedCallback callback) {
        getServerRpcInstance().setSheetRenamedCallback(callback);
    }

    public void setSheetCreatedCallback(SheetCreatedCallback callback) {
        getServerRpcInstance().setSheetCreatedCallback(callback);
    }

    public void setCellRangePaintedCallback(CellRangePaintedCallback callback) {
        getServerRpcInstance().setCellRangePaintedCallback(callback);
    }

    public void setDeleteSelectedCellsCallback(DeleteSelectedCellsCallback callback) {
        getServerRpcInstance().setDeleteSelectedCellsCallback(callback);
    }

    public void setLinkCellClickedCallback(LinkCellClickedCallback callback) {
        getServerRpcInstance().setLinkCellClickedCallback(callback);
    }

    public void setRowsResizedCallback(RowsResizedCallback callback) {
        getServerRpcInstance().setRowsResizedCallback(callback);
    }

    public void setColumnResizedCallback(ColumnResizedCallback callback) {
        getServerRpcInstance().setColumnResizedCallback(callback);
    }

    public void setOnRowAutofitCallback(OnRowAutofitCallback callback) {
        getServerRpcInstance().setOnRowAutofitCallback(callback);
    }

    public void setOnColumnAutofitCallback(OnColumnAutofitCallback callback) {
        getServerRpcInstance().setOnColumnAutofitCallback(callback);
    }

    public void setOnUndoCallback(OnUndoCallback callback) {
        getServerRpcInstance().setOnUndoCallback(callback);
    }

    public void setOnRedoCallback(OnRedoCallback callback) {
        getServerRpcInstance().setOnRedoCallback(callback);
    }

    public void setSetCellStyleWidthRatiosCallback(SetCellStyleWidthRatiosCallback callback) {
        getServerRpcInstance().setSetCellStyleWidthRatiosCallback(callback);
    }

    public void setProtectedCellWriteAttemptedCallback(ProtectedCellWriteAttemptedCallback callback) {
        getServerRpcInstance().setProtectedCellWriteAttemptedCallback(callback);
    }

    public void setOnPasteCallback(OnPasteCallback callback) {
        getServerRpcInstance().setOnPasteCallback(callback);
    }

    public void setClearSelectedCellsOnCutCallback(ClearSelectedCellsOnCutCallback callback) {
        getServerRpcInstance().setClearSelectedCellsOnCutCallback(callback);
    }

    public void setUpdateCellCommentCallback(UpdateCellCommentCallback callback) {
        getServerRpcInstance().setUpdateCellCommentCallback(callback);
    }

    public void setOnConnectorInitCallback(OnConnectorInitCallback callback) {
        getServerRpcInstance().setOnConnectorInitCallback(callback);
    }

    public void setContextMenuOpenOnSelectionCallback(ContextMenuOpenOnSelectionCallback callback) {
        getServerRpcInstance().setContextMenuOpenOnSelectionCallback(callback);
    }

    public void setActionOnCurrentSelectionCallback(ActionOnCurrentSelectionCallback callback) {
        getServerRpcInstance().setActionOnCurrentSelectionCallback(callback);
    }

    public void setRowHeaderContextMenuOpenCallback(RowHeaderContextMenuOpenCallback callback) {
        getServerRpcInstance().setRowHeaderContextMenuOpenCallback(callback);
    }

    public void setActionOnRowHeaderCallback(ActionOnRowHeaderCallback callback) {
        getServerRpcInstance().setActionOnRowHeaderCallback(callback);
    }

    public void setColumnHeaderContextMenuOpenCallback(ColumnHeaderContextMenuOpenCallback callback) {
        getServerRpcInstance().setColumnHeaderContextMenuOpenCallback(callback);
    }

    public void setActionOnColumnHeaderCallback(ActionOnColumnHeaderCallback callback) {
        getServerRpcInstance().setActionOnColumnHeaderCallback(callback);
    }



}
