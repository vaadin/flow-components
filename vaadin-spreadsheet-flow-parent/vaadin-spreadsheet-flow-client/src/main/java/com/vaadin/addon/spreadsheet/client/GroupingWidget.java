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
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Class that represents a single column grouping indicator.
 *
 * @author Thomas Mattsson / Vaadin LTD
 *
 */
public abstract class GroupingWidget extends FlowPanel {

    protected static final int SINGLE_ITEM_SIZE_HEIGHT = 18;
    protected static final int SINGLE_ITEM_SIZE_WIDTH = 15;
    public static final String EXPAND_CHAR = "+";
    public static final String CONTRACT_SIGN = "&#x2212;";

    public interface GroupingHandler {
        void setGroupingCollapsed(boolean cols, int colIndex,
                boolean collapsed);

        void levelHeaderClicked(boolean cols, int level);
    }

    private DivElement btn = Document.get().createDivElement();

    private boolean collapsed = false;
    private boolean inversed = false;

    private int index;

    protected GroupingHandler handler;

    protected int top = -1;
    protected int left = -1;
    protected double marginLeft = -1;
    protected double marginTop = -1;
    protected double width = -1;
    protected double height = -1;

    /**
     * @param index
     *            Unique index for the group, 0-based. This index is used on
     *            server side when collapsing/expanding the group.
     * @param handler
     *            The gateway to the server side
     */
    public GroupingWidget(final int index, final GroupingHandler handler) {

        this.index = index;
        this.handler = handler;

        setStyleName("grouping");
        addStyleName("minus");

        btn.setInnerHTML(CONTRACT_SIGN);
        btn.setClassName("expand");
        getElement().appendChild(btn);

        Event.sinkEvents(getElement(), Event.ONCLICK | Event.ONCONTEXTMENU);
    }

    public void setWidthPX(double w) {
        if (isCollapsed()) {
            setSize(0);
            if (!isInversed()) {
                setMargin(w);
            }
        } else {
            setSize(w);
        }
    }

    protected abstract void setSize(double size);

    protected abstract void setMargin(double size);

    public void setCollapsed(boolean collapsed) {

        if (this.collapsed == collapsed) {
            return;
        }

        if (collapsed) {
            btn.setInnerHTML(EXPAND_CHAR);
            removeStyleName("minus");
            addStyleName("plus");
        } else {
            btn.setInnerHTML(CONTRACT_SIGN);
            removeStyleName("plus");
            addStyleName("minus");
        }
        this.collapsed = collapsed;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    @Override
    public void onBrowserEvent(Event event) {

        event.preventDefault();
        event.stopPropagation();

        if (event.getButton() == NativeEvent.BUTTON_LEFT) {
            handler.setGroupingCollapsed(isCols(), index, !collapsed);
            setCollapsed(!collapsed);
        }
    }

    protected abstract boolean isCols();

    /**
     * Where this marker should be positioned.
     *
     * @param offset
     *            The number of pixels from 0 (top or left) this group should be
     *            positioned.
     * @param level
     *            The level of the group, 0-based.
     */
    public abstract void setPos(int offset, int level);

    /**
     * @return The total height of a panel with the given amount of groups
     */
    public static int getTotalHeight(int maxGrouping) {
        return 3 + maxGrouping * SINGLE_ITEM_SIZE_HEIGHT;
    }

    /**
     * @return The total width of a panel with the given amount of groups
     */
    public static int getTotalWidth(int maxGrouping) {
        return 1 + maxGrouping * SINGLE_ITEM_SIZE_WIDTH;
    }

    public void setIndex(int i) {
        index = i;
    }

    public int getIndex() {
        return index;
    }

    public boolean isInversed() {
        return inversed;
    }

    public void setInversed(boolean inversed) {
        this.inversed = inversed;
        if (inversed) {
            addStyleName("inversed");
        } else {
            removeStyleName("inversed");
        }
    }

    protected abstract GroupingWidget cloneWidget();

    protected void copyfields(GroupingWidget newWidget) {
        newWidget.collapsed = collapsed;
        newWidget.index = index;
        newWidget.inversed = inversed;

        newWidget.btn.setInnerText(btn.getInnerText());

        Style style = newWidget.getElement().getStyle();

        newWidget.setStyleName(getStyleName());

        if (marginLeft > -1) {
            style.setMarginLeft(marginLeft, Unit.PX);
        }
        if (marginTop > -1) {
            style.setMarginTop(marginTop, Unit.PX);
        }
        if (height > -1) {
            style.setHeight(height, Unit.PX);
        }
        if (width > -1) {
            style.setWidth(width, Unit.PX);
        }
        if (top > -1) {
            style.setTop(top, Unit.PX);
        }
        if (left > -1) {
            style.setLeft(left, Unit.PX);
        }
    }

}
