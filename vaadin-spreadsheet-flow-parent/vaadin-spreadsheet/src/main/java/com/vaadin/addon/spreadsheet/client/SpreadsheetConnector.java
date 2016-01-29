package com.vaadin.addon.spreadsheet.client;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.client.SpreadsheetWidget.SheetContextMenuHandler;
import com.vaadin.addon.spreadsheet.shared.SpreadsheetState;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.client.ui.Action;
import com.vaadin.client.ui.ActionOwner;
import com.vaadin.client.ui.PostLayoutListener;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.Connect.LoadStyle;

@SuppressWarnings("serial")
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
                boolean formula, boolean locked, boolean initialSelection) {
            getWidget().selectCell(col, row, value, formula, locked,
                    initialSelection);
        }

        @Override
        public void invalidCellAddress() {
            getWidget().invalidCellAddress();
        }

        @Override
        public void showSelectedCellRange(int firstColumn, int lastColumn,
                int firstRow, int lastRow, String value, boolean formula,
                boolean locked) {
            getWidget()
                    .selectCellRange(firstColumn, firstRow, firstColumn,
                            lastColumn, firstRow, lastRow, value, formula,
                            locked, true);
        }

        @Override
        public void showActions(
                final ArrayList<SpreadsheetActionDetails> actionDetails) {
            int left;
            int top;
            if (latestCellContextMenuEvent != null) {
                left = SpreadsheetWidget
                        .getTouchOrMouseClientX(latestCellContextMenuEvent);
                top = SpreadsheetWidget
                        .getTouchOrMouseClientY(latestCellContextMenuEvent);
            } else {
                left = SpreadsheetWidget
                        .getTouchOrMouseClientX(latestHeaderContextMenuEvent);
                top = SpreadsheetWidget
                        .getTouchOrMouseClientY(latestHeaderContextMenuEvent);
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
                boolean cellLocked, boolean scroll) {
            getWidget().selectCellRange(col, row, c1, c2, r1, r2, value,
                    formula, cellLocked, scroll);
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

        @Override
        public void refreshCellStyles() {
            getWidget().refreshCellStyles();
        }

        @Override
        public void editCellComment(int col, int row) {
            getWidget().editCellComment(col, row);
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

    private Set<String> currentOverlays = new HashSet<String>();

    @Override
    protected void init() {
        super.init();
        getWidget().setId(getConnectorId());
        registerRpc(SpreadsheetClientRpc.class, clientRPC);
        getWidget().setCommsTrigger(new CommsTrigger() {

            @Override
            public void sendUpdates() {
                getConnection().sendPendingVariableChanges();
            }
        });

        getWidget().setSpreadsheetHandler(
                getRpcProxy(SpreadsheetServerRpc.class));
        getWidget().setSheetContextMenuHandler(new SheetContextMenuHandler() {

            @Override
            public void cellContextMenu(NativeEvent event, int column, int row) {
                if (getState().hasActions) {
                    latestCellContextMenuEvent = event;
                    latestHeaderContextMenuEvent = null;
                    getRpcProxy(SpreadsheetServerRpc.class)
                            .contextMenuOpenOnSelection(row, column);
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
        // Prevent context menu on context menu
        getConnection().getContextMenu().addDomHandler(
                new ContextMenuHandler() {

                    @Override
                    public void onContextMenu(ContextMenuEvent event) {
                        event.preventDefault();
                        event.stopPropagation();
                    }
                }, ContextMenuEvent.getType());
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
        if (currentOverlays != null) {
            currentOverlays.clear();
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
                widget.sheetUpdated(state.sheetNames, state.sheetIndex,
                        stateChangeEvent
                                .hasPropertyChanged("workbookChangeToggle"));
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
        widget.sheetUpdated(state.sheetNames, state.sheetIndex,
                stateChangeEvent.hasPropertyChanged("workbookChangeToggle"));
        widget.setSheetProtected(state.sheetProtected);
        widget.load();
        widget.updateMergedRegions(state.mergedRegions);

    }

    private void loadStateChangeDataToWidget(StateChangeEvent stateChangeEvent) {
        final SpreadsheetWidget widget = getWidget();
        SpreadsheetState state = getState();
        if (stateChangeEvent.hasPropertyChanged("componentIDtoCellKeysMap")) {
            HashMap<String, String> cellKeysToComponentIdMap = state.componentIDtoCellKeysMap;
            HashMap<String, Widget> customWidgetMap = new HashMap<String, Widget>();
            if (cellKeysToComponentIdMap != null
                    && !cellKeysToComponentIdMap.isEmpty()) {
                List<ComponentConnector> childComponents = getChildComponents();
                for (ComponentConnector cc : childComponents) {
                    String connectorId = cc.getConnectorId();
                    if (cellKeysToComponentIdMap.containsKey(connectorId)) {
                        customWidgetMap.put(
                                cellKeysToComponentIdMap.get(connectorId),
                                cc.getWidget());
                    }
                }
            }
            widget.showCellCustomComponents(customWidgetMap);
        }
        if (stateChangeEvent.hasPropertyChanged("cellKeysToEditorIdMap")) {
            setupCustomEditors();
        }
        if (stateChangeEvent.hasPropertyChanged("cellComments")
                || stateChangeEvent.hasPropertyChanged("cellCommentAuthors")) {
            widget.setCellComments(state.cellComments, state.cellCommentAuthors);
        }
        if (stateChangeEvent.hasPropertyChanged("visibleCellComments")) {
            setupVisibleCellComments();
        }
        if (stateChangeEvent.hasPropertyChanged("invalidFormulaCells")) {
            widget.setInvalidFormulaCells(state.invalidFormulaCells);
        }

        if (stateChangeEvent.hasPropertyChanged("overlays")) {
            overlaysChange();
        }

        widget.getSheetWidget().updateSheetPanePositions();
    }

    private void overlaysChange() {
        Map<String, OverlayInfo> overlayInfos = getState().overlays == null ? Collections
                .<String, OverlayInfo> emptyMap() : getState().overlays;

        removeOldOverlays(overlayInfos.keySet());

        addOrUpdateOverlays(overlayInfos);

        currentOverlays = overlayInfos.keySet();
    }

    private void addOrUpdateOverlays(Map<String, OverlayInfo> overlayInfos) {
        for (String key : overlayInfos.keySet()) {
            if (currentOverlays.contains(key)) {
                getWidget().updateOverlay(key, overlayInfos.get(key));
            } else {
                addOverlay(key, overlayInfos.get(key));
            }
        }
    }

    private void addOverlay(String id, OverlayInfo overlayInfo) {
        switch (overlayInfo.type) {
        case IMAGE:
            getWidget().addOverlay(id, new Image(getResourceUrl(id)), overlayInfo);
            break;
        case COMPONENT:
            for (ComponentConnector c : getChildComponents()) {
                if (c.getConnectorId().equals(id)) {
                    getWidget().addOverlay(id, c.getWidget(), overlayInfo);
                }
            }
            break;
        }
    }

    private void removeOldOverlays(Set<String> newOverlayKeys) {
        for (String key : currentOverlays) {
            if (!newOverlayKeys.contains(key)) {
                getWidget().removeOverlay(key);
            }
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

    public interface CommsTrigger {
        void sendUpdates();
    }
}
