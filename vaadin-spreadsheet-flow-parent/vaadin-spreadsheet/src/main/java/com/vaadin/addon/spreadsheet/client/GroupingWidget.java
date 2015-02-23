package com.vaadin.addon.spreadsheet.client;

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

    private static final int SINGLE_ITEM_SIZE = 17;

    public interface GroupingHandler {
        void setGroupingCollapsed(boolean cols, int colIndex, boolean collapsed);

        void levelHeaderClicked(boolean cols, int level);
    }

    public static class GroupingData {
        public int startIndex;
        public int endIndex;
        public int level;
        /** index unique for this group, for collapse/expand */
        public int uniqueIndex;
        public boolean collapsed;

        public GroupingData() {
        }

        public GroupingData(long start, long end, short level, long unique,
                boolean coll) {
            this((int) start, (int) end, (int) level, (int) unique, coll);
        }

        public GroupingData(int start, int end, int level, int unique,
                boolean coll) {
            startIndex = start;
            endIndex = end;
            this.level = level;
            uniqueIndex = unique;
            collapsed = coll;
        }

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

        btn.setInnerText("-");
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
            btn.setInnerText("+");
        } else {
            btn.setInnerText("-");
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
    public static int getTotalSize(int maxGrouping) {
        return 6 + maxGrouping * SINGLE_ITEM_SIZE;
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

        Style style = newWidget.getElement().getStyle();

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
