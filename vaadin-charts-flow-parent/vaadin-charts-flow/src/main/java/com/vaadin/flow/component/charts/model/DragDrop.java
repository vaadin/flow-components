/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

@SuppressWarnings("unused")
public class DragDrop extends AbstractConfigurationObject {

    private Boolean draggableEnd;
    private Boolean draggableStart;
    private Boolean draggableX;
    private Boolean draggableX1;
    private Boolean draggableX2;
    private Boolean draggableY;
    private DragHandle dragHandle;
    private Number dragMaxX;
    private Number dragMaxY;
    private Number dragMinX;
    private Number dragMinY;
    private Number dragPrecisionX;
    private Number dragPrecisionY;
    private Number dragSensitivity;
    private String groupBy;
    private GuideBox guideBox;
    private Boolean liveRedraw;

    /**
     * @see #setDraggableEnd(Boolean)
     */
    public Boolean getDraggableEnd() {
        return draggableEnd;
    }

    /**
     * Supported only in Gantt chart: Allow end value to be dragged
     * individually. Defaults to true.
     *
     * @param draggableEnd
     */
    public void setDraggableEnd(Boolean draggableEnd) {
        this.draggableEnd = draggableEnd;
    }

    /**
     * @see #setDraggableStart(Boolean)
     */
    public Boolean getDraggableStart() {
        return draggableStart;
    }

    /**
     * Supported only in Gantt chart: Allow start value to be dragged
     * individually. Defaults to true.
     *
     * @param draggableStart
     */
    public void setDraggableStart(Boolean draggableStart) {
        this.draggableStart = draggableStart;
    }

    /**
     * @see #setDraggableX(Boolean)
     */
    public Boolean getDraggableX() {
        return draggableX;
    }

    /**
     * Enable dragging in the X dimension.
     *
     * @param draggableX
     */
    public void setDraggableX(Boolean draggableX) {
        this.draggableX = draggableX;
    }

    /**
     * @see #setDraggableX1(Boolean)
     */
    public Boolean getDraggableX1() {
        return draggableX1;
    }

    /**
     * Allow X1 value to be dragged individually. Defaults to true.
     *
     * @param draggableX1
     */
    public void setDraggableX1(Boolean draggableX1) {
        this.draggableX1 = draggableX1;
    }

    /**
     * @see #setDraggableX2(Boolean)
     */
    public Boolean getDraggableX2() {
        return draggableX2;
    }

    /**
     * Allow X2 value to be dragged individually. Defaults to true.
     *
     * @param draggableX2
     */
    public void setDraggableX2(Boolean draggableX2) {
        this.draggableX2 = draggableX2;
    }

    /**
     * @see #setDraggableY(Boolean)
     */
    public Boolean getDraggableY() {
        return draggableY;
    }

    /**
     * Enable dragging in the Y dimension. Note that this is not supported for
     * TreeGrid axes (the default axis type in Gantt charts).
     *
     * @param draggableY
     */
    public void setDraggableY(Boolean draggableY) {
        this.draggableY = draggableY;
    }

    /**
     * @see #setDragHandle(DragHandle)
     */
    public DragHandle getDragHandle() {
        if (dragHandle == null) {
            dragHandle = new DragHandle();
        }
        return dragHandle;
    }

    /**
     * Options for the drag handles.
     *
     * @param dragHandle
     */
    public void setDragHandle(DragHandle dragHandle) {
        this.dragHandle = dragHandle;
    }

    /**
     * @see #setDragMaxX(Number)
     */
    public Number getDragMaxX() {
        return dragMaxX;
    }

    /**
     * Set the maximum X value the points can be moved to.
     *
     * @param dragMaxX
     */
    public void setDragMaxX(Number dragMaxX) {
        this.dragMaxX = dragMaxX;
    }

    /**
     * @see #setDragMaxY(Number)
     */
    public Number getDragMaxY() {
        return dragMaxY;
    }

    /**
     * Set the maximum Y value the points can be moved to.
     *
     * @param dragMaxY
     */
    public void setDragMaxY(Number dragMaxY) {
        this.dragMaxY = dragMaxY;
    }

    /**
     * @see #setDragMinX(Number)
     */
    public Number getDragMinX() {
        return dragMinX;
    }

    /**
     * Set the minimum X value the points can be moved to.
     *
     * @param dragMinX
     */
    public void setDragMinX(Number dragMinX) {
        this.dragMinX = dragMinX;
    }

    /**
     * @see #setDragMinY(Number)
     */
    public Number getDragMinY() {
        return dragMinY;
    }

    /**
     * Set the minimum Y value the points can be moved to.
     * 
     * @param dragMinY
     */
    public void setDragMinY(Number dragMinY) {
        this.dragMinY = dragMinY;
    }

    /**
     * @see #setDragPrecisionX(Number)
     */
    public Number getDragPrecisionX() {
        return dragPrecisionX;
    }

    /**
     * The X precision value to drag to for this series. Set to 0 to disable. By
     * default this is disabled, except for category axes, where the default is
     * 1. Defaults to 0.
     * 
     * @param dragPrecisionX
     */
    public void setDragPrecisionX(Number dragPrecisionX) {
        this.dragPrecisionX = dragPrecisionX;
    }

    /**
     * @see #setDragPrecisionY(Number)
     */
    public Number getDragPrecisionY() {
        return dragPrecisionY;
    }

    /**
     * The Y precision value to drag to for this series. Set to 0 to disable. By
     * default this is disabled, except for category axes, where the default is
     * 1. Defaults to 0.
     * 
     * @param dragPrecisionY
     */
    public void setDragPrecisionY(Number dragPrecisionY) {
        this.dragPrecisionY = dragPrecisionY;
    }

    /**
     * @see #setDragSensitivity(Number)
     */
    public Number getDragSensitivity() {
        return dragSensitivity;
    }

    /**
     * The amount of pixels to drag the pointer before it counts as a drag
     * operation. This prevents drag/drop to fire when just clicking or
     * selecting points. Defaults to 2.
     * 
     * @param dragSensitivity
     */
    public void setDragSensitivity(Number dragSensitivity) {
        this.dragSensitivity = dragSensitivity;
    }

    /**
     * @see #setGroupBy(String)
     */
    public String getGroupBy() {
        return groupBy;
    }

    /**
     * Group the points by a property. Points with the same property value will
     * be grouped together when moving. Defaults to undefined.
     * 
     * @param groupBy
     */
    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    /**
     * @see #setGuideBox(GuideBox)
     */
    public GuideBox getGuideBox() {
        if (guideBox == null) {
            guideBox = new GuideBox();
        }
        return guideBox;
    }

    /**
     * Style options for the guide box. The guide box has one state by default,
     * the default state. Guide box is visible only when liveRedraw is false.
     * 
     * @see #setLiveRedraw(Boolean)
     * 
     * @param guideBox
     */
    public void setGuideBox(GuideBox guideBox) {
        this.guideBox = guideBox;
    }

    /**
     * @see #setLiveRedraw(Boolean)
     */
    public Boolean getLiveRedraw() {
        return liveRedraw;
    }

    /**
     * Update points as they are dragged. If false, a guide box is drawn to
     * illustrate the new point size. Defaults to true.
     * 
     * @param liveRedraw
     */
    public void setLiveRedraw(Boolean liveRedraw) {
        this.liveRedraw = liveRedraw;
    }
}
