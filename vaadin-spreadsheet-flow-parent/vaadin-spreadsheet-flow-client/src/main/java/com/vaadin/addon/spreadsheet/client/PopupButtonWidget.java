package com.vaadin.addon.spreadsheet.client;

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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.VOverlay;

public class PopupButtonWidget extends FocusWidget
        implements ClickHandler, HasCloseHandlers<PopupPanel> {

    protected static final String BUTTON_CLASSNAME = "popupbutton";
    protected static final String POPUP_OVERLAY_CLASSNAME = "v-spreadsheet-"
            + BUTTON_CLASSNAME + "-overlay";
    protected static final String POPUP_OVERLAY_LAYOUT_CLASSNAME = "overlay-layout";
    protected static final String BUTTON_ACTIVE_CLASSNAME = "active";

    private final DivElement root = Document.get().createDivElement();

    private PositionCallback callback = new PositionCallback() {

        @Override
        public void setPosition(int offsetWidth, int offsetHeight) {
            Element parentElement = root.getParentElement();
            final int absoluteBottom = parentElement.getAbsoluteBottom();
            final int absoluteRight = parentElement.getAbsoluteRight();
            int left = absoluteRight - offsetWidth;
            if (left < sheet.getAbsoluteLeft()) {
                left = absoluteRight;
            }
            int top = absoluteBottom;
            if ((top + offsetHeight) > sheet.getAbsoluteBottom()) {
                top = parentElement.getAbsoluteTop() - offsetHeight;
            }
            if (top < sheet.getAbsoluteTop()) {
                top = sheet.getAbsoluteTop();
            }
            popup.setPopupPosition(left, top);
        }
    };

    private final VOverlay popup;
    private final PopupButtonHeader popupHeader;
    private final VerticalPanel popupLayout;

    private final List<Widget> popupChildrenWidgets = new ArrayList<Widget>();
    private DivElement sheet;
    private int col;
    private int row;
    private SheetWidget owner;

    public PopupButtonWidget() {
        root.setClassName(BUTTON_CLASSNAME);
        root.setAttribute("role", "button");

        popup = new SpreadsheetOverlay(true, false);
        popup.setStyleName(POPUP_OVERLAY_CLASSNAME);
        popupLayout = new VerticalPanel();
        popupLayout.setStylePrimaryName(POPUP_OVERLAY_LAYOUT_CLASSNAME);
        popupHeader = new PopupButtonHeader();
        popupHeader.setPopup(popup);
        popupLayout.add(popupHeader);
        popup.add(popupLayout);

        setElement(root);

        addClickHandler(this);
    }

    public void setSheetWidget(SheetWidget owner, DivElement sheet) {
        this.sheet = sheet;
        this.owner = owner;
        popup.setOwner(owner);
        popupHeader.setSheet(owner);
    }

    /**
     * 1-based
     *
     * @return the col
     */
    public int getCol() {
        return col;
    }

    /**
     * 1-based
     *
     * @param col
     *            the col to set
     */
    public void setCol(int col) {
        this.col = col;
    }

    /**
     * 1-based
     *
     * @return the row
     */
    public int getRow() {
        return row;
    }

    /**
     * 1-based
     *
     * @param row
     *            the row to set
     */
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * 1-based
     *
     * @param row
     * @param col
     */
    public void setRowCol(int row, int col) {
        Widget owner = popup.getOwner();
        if (owner != null && owner instanceof SheetWidget) {
            ((SheetWidget) owner).updatePopupButtonPosition(this, this.row,
                    this.col, row, col);
        }
        getElement().removeClassName("c" + this.col + "r" + this.row);
        this.col = col;
        this.row = row;
        getElement().addClassName("c" + this.col + "r" + this.row);
    }

    @Override
    public void onClick(ClickEvent event) {
        openPopupOverlay();
        event.stopPropagation();
    }

    protected void openPopupOverlay() {
        Element parentElement = root.getParentElement();
        if (parentElement != null) {
            popup.setPopupPositionAndShow(callback);
        } else {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    popup.setPopupPositionAndShow(callback);
                }
            });
        }
        if (owner != null) {
            popupHeader.setCaption(owner.getCellValue(col, row));
        }
    }

    public void markActive(boolean active) {
        if (active) {
            root.addClassName(BUTTON_ACTIVE_CLASSNAME);
        } else {
            root.removeClassName(BUTTON_ACTIVE_CLASSNAME);
        }
    }

    public void addPopupComponent(Widget widget) {
        popupChildrenWidgets.add(widget);
        popupLayout.add(widget);
    }

    public void removePopupComponent(Widget widget) {
        popupChildrenWidgets.remove(widget);
        popupLayout.remove(widget);
    }

    public void setPopupHeight(String popupHeight) {
        if (popupHeight != null) {
            ((PopupPanel) popup).setHeight(popupHeight);
        } else {
            ((PopupPanel) popup).setHeight("");
        }
    }

    public void setPopupWidth(String popupWidth) {
        if (popupWidth != null) {
            ((PopupPanel) popup).setWidth(popupWidth);
        } else {
            ((PopupPanel) popup).setWidth("");
        }
    }

    /**
     * Override the position callback method for the button's popup.
     *
     * @param positionCallback
     *            not null
     */
    public void setPopupPositionCallback(PositionCallback positionCallback) {
        if (positionCallback != null) {
            callback = positionCallback;
        }
    }

    /**
     * Returns the position callback method used for the button's popup.
     *
     * @return
     */
    public PositionCallback getPositionCallback() {
        return callback;
    }

    public void setPopupHeaderHidden(boolean headerHidden) {
        popupHeader.setHidden(headerHidden);
    }

    public boolean isPopupHeaderHidden() {
        return popupHeader.isHidden();
    }

    @Override
    public void setStyleName(String style, boolean add) {
        super.setStyleName(style, add);
        popup.setStyleName(style, add);
    }

    @Override
    public HandlerRegistration addCloseHandler(
            CloseHandler<PopupPanel> handler) {
        return popup.addCloseHandler(handler);
    }

    public boolean isPopupOpen() {
        return popup.isShowing();
    }

    public void closePopup() {
        popup.hide();
    }

    public void openPopup() {
        if (owner != null && owner.isCellRendered(col, row)) {
            openPopupOverlay();
        }
    }

    @Override
    protected void onDetach() {
        closePopup();
        super.onDetach();
    }
}
