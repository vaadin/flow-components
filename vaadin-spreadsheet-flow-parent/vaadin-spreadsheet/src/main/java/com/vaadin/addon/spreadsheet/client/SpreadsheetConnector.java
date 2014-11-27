package com.vaadin.addon.spreadsheet.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.client.SpreadsheetWidget.SheetContextMenuHandler;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.Util;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.client.ui.Action;
import com.vaadin.client.ui.ActionOwner;
import com.vaadin.client.ui.PostLayoutListener;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.Connect.LoadStyle;

@Connect(value = Spreadsheet.class, loadStyle = LoadStyle.DEFERRED)
public class SpreadsheetConnector extends AbstractHasComponentsConnector
        implements PostLayoutListener {

    SpreadsheetClientRpc clientRPC = new SpreadsheetClientRpc() {

        @Override
        public void showCellValue(String value, int col, int row,
                boolean formula, boolean locked) {
            getWidget().showCellValue(value, col, row, formula, locked);
        }

        @Override
        public void showSelectedCell(int col, int row, String value,
                boolean formula, boolean locked) {
            getWidget().setCellSelection(col, row, value, formula, locked);
        }

        @Override
        public void invalidCellAddress() {
            getWidget().invalidCellAddress();
        }

        @Override
        public void showSelectedCellRange(int firstColumn, int lastColumn,
                int firstRow, int lastRow, String value, boolean formula,
                boolean locked) {
            getWidget().setCellRangeSelection(firstColumn, firstRow,
                    firstColumn, lastColumn, firstRow, lastRow, value, formula,
                    locked);
        }

        @Override
        public void showActions(
                final ArrayList<SpreadsheetActionDetails> actionDetails) {
            int left;
            int top;
            if (latestCellContextMenuEvent != null) {
                left = Util.getTouchOrMouseClientX(latestCellContextMenuEvent);
                top = Util.getTouchOrMouseClientY(latestCellContextMenuEvent);
            } else {
                left = Util
                        .getTouchOrMouseClientX(latestHeaderContextMenuEvent);
                top = Util.getTouchOrMouseClientY(latestHeaderContextMenuEvent);
            }
            top += Window.getScrollTop();
            left += Window.getScrollLeft();
            getConnection().getContextMenu().showAt(new ActionOwner() {

                @Override
                public String getPaintableId() {
                    return SpreadsheetConnector.this.getConnectorId();
                }

                @Override
                public ApplicationConnection getClient() {
                    return SpreadsheetConnector.this.getConnection();
                }

                @Override
                public Action[] getActions() {
                    List<Action> actions = new ArrayList<Action>();
                    SpreadsheetWidget widget = getWidget();
                    SpreadsheetServerRpc rpcProxy = getRpcProxy(SpreadsheetServerRpc.class);
                    for (SpreadsheetActionDetails actionDetail : actionDetails) {
                        SpreadsheetAction spreadsheetAction = new SpreadsheetAction(
                                this, rpcProxy, actionDetail.key,
                                actionDetail.type, widget);
                        spreadsheetAction.setCaption(actionDetail.caption);
                        spreadsheetAction
                                .setIconUrl(getResourceUrl(actionDetail.key));
                        actions.add(spreadsheetAction);
                    }
                    return actions.toArray(new Action[actions.size()]);
                }
            }, left, top);
        }

        @Override
        public void setSelectedCellAndRange(int col, int row, int c1, int c2,
                int r1, int r2, String value, boolean formula,
                boolean cellLocked) {
            getWidget().setCellRangeSelection(col, row, c1, c2, r1, r2, value,
                    formula, cellLocked);
        }

        @Override
        public void updateBottomRightCellValues(ArrayList<CellData> cellData) {
            getWidget().updateBottomRightCellValues(cellData);
        }

        @Override
        public void updateTopLeftCellValues(ArrayList<CellData> cellData) {
            getWidget().updateTopLeftCellValues(cellData);
        }

        @Override
        public void updateTopRightCellValues(ArrayList<CellData> cellData) {
            getWidget().updateTopRightCellValues(cellData);
        }

        @Override
        public void updateBottomLeftCellValues(ArrayList<CellData> cellData) {
            getWidget().updateBottomLeftCellValues(cellData);
        }

        @Override
        public void cellsUpdated(ArrayList<CellData> updatedCellData) {
            getWidget().cellValuesUpdated(updatedCellData);
        }

    };

    private final ElementResizeListener elementResizeListener = new ElementResizeListener() {

        @Override
        public void onElementResize(ElementResizeEvent e) {
            getWidget().widgetSizeChanged();
        }
    };

    private SpreadsheetCustomEditorFactory customEditorFactory;

    private NativeEvent latestCellContextMenuEvent;

    private NativeEvent latestHeaderContextMenuEvent;

    private List<String> visibleCellCommentKeys = new ArrayList<String>();

    private Set<String> sheetImageKeys;

    @Override
    protected void init() {
        super.init();
        registerRpc(SpreadsheetClientRpc.class, clientRPC);
        getWidget().setSpreadsheetHandler(
                getRpcProxy(SpreadsheetServerRpc.class));
        getWidget().setSheetContextMenuHandler(new SheetContextMenuHandler() {

            @Override
            public void cellContextMenu(NativeEvent event, int column, int row) {
                if (getState().hasActions) {
                    latestCellContextMenuEvent = event;
                    latestHeaderContextMenuEvent = null;
                    getRpcProxy(SpreadsheetServerRpc.class)
                            .contextMenuOpenOnSelection(column, row);
                }
            }

            @Override
            public void rowHeaderContextMenu(NativeEvent nativeEvent,
                    int rowIndex) {
                if (getState().hasActions) {
                    latestHeaderContextMenuEvent = nativeEvent;
                    latestCellContextMenuEvent = null;
                    getRpcProxy(SpreadsheetServerRpc.class)
                            .rowHeaderContextMenuOpen(rowIndex);
                }
            }

            @Override
            public void columnHeaderContextMenu(NativeEvent nativeEvent,
                    int columnIndex) {
                if (getState().hasActions) {
                    latestHeaderContextMenuEvent = nativeEvent;
                    latestCellContextMenuEvent = null;
                    getRpcProxy(SpreadsheetServerRpc.class)
                            .columnHeaderContextMenuOpen(columnIndex);
                }
            }
        });
        getLayoutManager().addElementResizeListener(getWidget().getElement(),
                elementResizeListener);
        getRpcProxy(SpreadsheetServerRpc.class).onConnectorInit();
    }

    @Override
    public void onUnregister() {
        super.onUnregister();
        getWidget().clearSpreadsheet(true);
        getLayoutManager().removeElementResizeListener(
                getWidget().getElement(), elementResizeListener);
        if (sheetImageKeys != null) {
            sheetImageKeys.clear();
        }
        visibleCellCommentKeys.clear();
    }

    @Override
    protected Widget createWidget() {
        return GWT.create(SpreadsheetWidget.class);
    }

    @Override
    public SpreadsheetWidget getWidget() {
        return (SpreadsheetWidget) super.getWidget();
    }

    @Override
    public SpreadsheetState getState() {
        return (SpreadsheetState) super.getState();
    }

    @Override
    public void onStateChanged(final StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        final SpreadsheetWidget widget = getWidget();
        SpreadsheetState state = getState();
        // in case the component client side is just created, but server side
        // has been existing (like when component has been invisible
        if (!state.reload && stateChangeEvent.isInitialStateChange()) {
            loadInitialStateDataToWidget(stateChangeEvent);
            // this is deferred because the first layout of the spreadsheet is
            // done as deferred
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    loadStateChangeDataToWidget(stateChangeEvent);
                }
            });
        } else if (state.reload) {
            loadInitialStateDataToWidget(stateChangeEvent);
        } else {
            if (stateChangeEvent.hasPropertyChanged("sheetNames")
                    || stateChangeEvent.hasPropertyChanged("sheetIndex")) {
                widget.sheetUpdated(state.sheetNames, state.sheetIndex);
            }
            if (stateChangeEvent.hasPropertyChanged("hiddenColumnIndexes")
                    || stateChangeEvent.hasPropertyChanged("hiddenRowIndexes")
                    || stateChangeEvent.hasPropertyChanged("colW")
                    || stateChangeEvent.hasPropertyChanged("rowH")
                    || stateChangeEvent.hasPropertyChanged("rows")
                    || stateChangeEvent.hasPropertyChanged("cols")
                    || stateChangeEvent
                            .hasPropertyChanged("verticalSplitPosition")
                    || stateChangeEvent
                            .hasPropertyChanged("horizontalSplitPosition")) {
                widget.relayoutSheet();
                getWidget().updateMergedRegions(getState().mergedRegions);
            } else if (stateChangeEvent.hasPropertyChanged("mergedRegions")) {
                getWidget().updateMergedRegions(getState().mergedRegions);
            }
            if (stateChangeEvent.hasPropertyChanged("sheetProtected")) {
                widget.setSheetProtected(state.sheetProtected);
            }
            loadStateChangeDataToWidget(stateChangeEvent);
        }
    }

    private void loadInitialStateDataToWidget(StateChangeEvent stateChangeEvent) {
        SpreadsheetState state = getState();
        SpreadsheetWidget widget = getWidget();
        setupCustomEditors();
        widget.sheetUpdated(state.sheetNames, state.sheetIndex);
        widget.setSheetProtected(state.sheetProtected);
        widget.load();
        getWidget().updateMergedRegions(getState().mergedRegions);
    }

    private void loadStateChangeDataToWidget(StateChangeEvent stateChangeEvent) {
        final SpreadsheetWidget widget = getWidget();
        SpreadsheetState state = getState();
        if (stateChangeEvent.hasPropertyChanged("componentIDtoCellKeysMap")) {
            HashMap<String, String> cellKeysToComponentIdMap = state.componentIDtoCellKeysMap;
            if (cellKeysToComponentIdMap != null
                    && !cellKeysToComponentIdMap.isEmpty()) {
                List<ComponentConnector> childComponents = getChildComponents();
                HashMap<String, Widget> customWidgetMap = new HashMap<String, Widget>();
                for (ComponentConnector cc : childComponents) {
                    String connectorId = cc.getConnectorId();
                    if (cellKeysToComponentIdMap.containsKey(connectorId)) {
                        customWidgetMap.put(
                                cellKeysToComponentIdMap.get(connectorId),
                                cc.getWidget());
                    }
                }
                widget.showCellCustomComponents(customWidgetMap);
            }
        }
        if (stateChangeEvent.hasPropertyChanged("cellKeysToEditorIdMap")) {
            setupCustomEditors();
        }
        if (stateChangeEvent.hasPropertyChanged("cellComments")) {
            widget.setCellComments(state.cellComments);
        }
        if (stateChangeEvent.hasPropertyChanged("visibleCellComments")) {
            setupVisibleCellComments();
        }

        if (stateChangeEvent.hasPropertyChanged("resourceKeyToImage")) {
            Map<String, ImageInfo> resourceKeyToImage = getState().resourceKeyToImage;
            // remove old:
            if (sheetImageKeys != null) {
                for (String key : sheetImageKeys) {
                    if (resourceKeyToImage != null
                            && !resourceKeyToImage.containsKey(key)) {
                        widget.removeImage(key);
                    }
                }
            }
            if (resourceKeyToImage != null) {
                for (Entry<String, ImageInfo> entry : resourceKeyToImage
                        .entrySet()) {
                    String key = entry.getKey();
                    ImageInfo imageInfo = entry.getValue();
                    if (sheetImageKeys != null && sheetImageKeys.contains(key)) {
                        widget.updateImage(key, imageInfo);
                    } else {
                        widget.addImage(key, getResourceUrl(key), imageInfo);
                    }
                }
            }
            sheetImageKeys = resourceKeyToImage == null ? null
                    : resourceKeyToImage.keySet();
        }
    }

    private void setupCustomEditors() {
        if (getState().cellKeysToEditorIdMap == null) {
            getWidget().setCustomEditorFactory(null);
        } else if (getWidget().getCustomEditorFactory() == null) {
            if (customEditorFactory == null) {
                customEditorFactory = new SpreadsheetCustomEditorFactory() {

                    @Override
                    public boolean hasCustomEditor(String key) {
                        return getState().cellKeysToEditorIdMap
                                .containsKey(key);
                    }

                    @Override
                    public Widget getCustomEditor(String key) {
                        String editorId = getState().cellKeysToEditorIdMap
                                .get(key);
                        List<ComponentConnector> childComponents = getChildComponents();
                        for (ComponentConnector cc : childComponents) {
                            if (editorId.equals(cc.getConnectorId())) {
                                return cc.getWidget();
                            }
                        }
                        return null;
                    }

                };
            }
            getWidget().setCustomEditorFactory(customEditorFactory);
        } else {
            getWidget().loadSelectedCellEditor();
        }
    }

    private void setupVisibleCellComments() {
        List<String> visibleCellComments = getState().visibleCellComments;
        SpreadsheetWidget widget = getWidget();
        // remove old
        for (String key : visibleCellCommentKeys) {
            if (!visibleCellComments.contains(key)) {
                widget.removeVisibleCellComment(key);
            }
        }
        if (visibleCellComments != null) {
            // add new
            for (String key : visibleCellComments) {
                if (!visibleCellCommentKeys.contains(key)) {
                    widget.addVisibleCellComment(key);
                }
            }
        }
        visibleCellCommentKeys.clear();
        if (visibleCellComments != null) {
            visibleCellCommentKeys.addAll(visibleCellComments);
        }
    }

    @Override
    public void updateCaption(ComponentConnector connector) {

    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
        // remove old popup buttons
        List<ComponentConnector> oldChildren = event.getOldChildren();
        for (ComponentConnector child : oldChildren) {
            if (child instanceof PopupButtonConnector
                    && child.getParent() != this) {
                getWidget().removePopupButton(
                        ((PopupButtonConnector) child).getWidget());
            }
        }

        for (ComponentConnector child : getChildComponents()) {
            if (child instanceof PopupButtonConnector
                    && !oldChildren.contains(child)) {
                getWidget().addPopupButton(
                        ((PopupButtonConnector) child).getWidget());
            }
        }
    }

    @Override
    public void postLayout() {
        getWidget().refreshOverlayPositions();
    }
}