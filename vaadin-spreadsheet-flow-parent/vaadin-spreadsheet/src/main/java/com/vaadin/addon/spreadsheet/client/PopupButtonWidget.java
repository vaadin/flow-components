package com.vaadin.addon.spreadsheet.client;

import java.util.ArrayList;
import java.util.List;

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

public class PopupButtonWidget extends FocusWidget implements ClickHandler,
        HasCloseHandlers<PopupPanel> {

    protected static final String FILTER_BUTTON_CLASSNAME = "popupbutton";
    protected static final String FILTER_ARROW_CLASSNAME = FILTER_BUTTON_CLASSNAME
            + "-arrow";
    protected static final String FILTER_OVERLAY_CLASSNAME = "v-spreadsheet-"
            + FILTER_BUTTON_CLASSNAME + "-overlay";
    protected static final String FILTER_OVERLAY_LAYOUT_CLASSNAME = "overlay-layout";
    protected static final String FILTER_ACTIVE_CLASSNAME = "active";

    private final DivElement root = Document.get().createDivElement();
    private final DivElement arrow = Document.get().createDivElement();

    private PositionCallback callback = new PositionCallback() {

        @Override
        public void setPosition(int offsetWidth, int offsetHeight) {
            Element parentElement = root.getParentElement();
            final int absoluteBottom = parentElement.getAbsoluteBottom();
            final int absoluteRight = parentElement.getAbsoluteRight();
            int left = absoluteRight;
            if ((absoluteRight + offsetWidth) > sheet.getAbsoluteRight()) {
                left = parentElement.getAbsoluteLeft() - offsetWidth;
            }
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

    private final List<Widget> filters = new ArrayList<Widget>();
    private DivElement sheet;
    private int col;
    private int row;
    private SheetWidget owner;

    public PopupButtonWidget() {
        root.setClassName(FILTER_BUTTON_CLASSNAME);
        root.setAttribute("role", "button");
        arrow.setClassName(FILTER_ARROW_CLASSNAME);
        root.appendChild(arrow);

        popup = new VOverlay(true, false, true);
        popup.setStyleName(FILTER_OVERLAY_CLASSNAME);
        popupLayout = new VerticalPanel();
        popupLayout.setStylePrimaryName(FILTER_OVERLAY_LAYOUT_CLASSNAME);
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
     * @return the col
     */
    public int getCol() {
        return col;
    }

    /**
     * @param col
     *            the col to set
     */
    public void setCol(int col) {
        this.col = col;
    }

    /**
     * @return the row
     */
    public int getRow() {
        return row;
    }

    /**
     * @param row
     *            the row to set
     */
    public void setRow(int row) {
        this.row = row;
    }

    public void setRowCol(int row, int col) {
        Widget owner = popup.getOwner();
        if (owner != null && owner instanceof SheetWidget) {
            ((SheetWidget) owner).updatePopupButtonPosition(this, this.row,
                    this.col, row, col);
        }
        this.col = col;
        this.row = row;
    }

    @Override
    public void onClick(ClickEvent event) {
        Element parentElement = root.getParentElement();
        if (parentElement != null) {
            popup.setPopupPositionAndShow(callback);
        } else {
            popup.showRelativeTo(this);
        }
        if (owner != null) {
            popupHeader.setCaption(owner.getCellValue(col, row));
        }
    }

    public void markActive(boolean active) {
        if (active) {
            root.addClassName(FILTER_ACTIVE_CLASSNAME);
        } else {
            root.removeClassName(FILTER_ACTIVE_CLASSNAME);
        }
    }

    public void addPopupComponent(Widget filter) {
        filters.add(filter);
        popupLayout.add(filter);
    }

    public void removePopupComponent(Widget filter) {
        filters.remove(filter);
        popupLayout.remove(filter);
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
     * Override the position callback method for the filter's popup.
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
     * Returns the position callback method used for the filter's popup.
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
    public HandlerRegistration addCloseHandler(CloseHandler<PopupPanel> handler) {
        return popup.addCloseHandler(handler);
    }

}
