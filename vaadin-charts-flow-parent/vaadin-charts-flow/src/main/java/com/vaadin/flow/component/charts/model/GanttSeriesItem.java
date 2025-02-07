/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.util.Util;

/**
 * Data for the {@link GanttSeries}. Represents one task in the Gantt chart.
 */
@SuppressWarnings("unused")
public class GanttSeriesItem extends AbstractConfigurationObject {

    private Boolean collapsed;
    private Color color;
    private Number colorIndex;
    private Completed completed;
    private AbstractConfigurationObject custom;
    private List<GanttSeriesItemDependency> dependency;
    private String description;
    private Number end;
    private String id;
    private Number labelrank;
    private Boolean milestone;
    private String name;
    private String parent;
    private Number start;
    private Number y;

    public GanttSeriesItem() {
    }

    public GanttSeriesItem(String name, Instant start, Instant end) {
        setName(name);
        setStart(start);
        setEnd(end);
    }

    public GanttSeriesItem(Number y, Instant start, Instant end) {
        setY(y);
        setStart(start);
        setEnd(end);
    }

    /**
     * @see #setColorIndex(Number)
     */
    public Number getColorIndex() {
        return colorIndex;
    }

    /**
     * A specific color index to use for the point, so its graphic
     * representations are given the class name
     * <code>highcharts-color-{n}</code>. In <a href=
     * "https://www.highcharts.com/docs/chart-design-and-style/style-by-css">styled
     * mode</a> this will change the color of the graphic. In non-styled mode,
     * the color by is set by the <code>fill</code> attribute, so the change in
     * class name won't have a visual effect by default.
     * </p>
     */
    public void setColorIndex(Number colorIndex) {
        this.colorIndex = colorIndex;
    }

    /**
     * @see #setCollapsed(Boolean)
     */
    public Boolean getCollapsed() {
        return collapsed;
    }

    /**
     * Whether the grid node belonging to this point should start as collapsed.
     * Used in axes of type treegrid. Defaults to false.
     *
     * @param collapsed
     */
    public void setCollapsed(Boolean collapsed) {
        this.collapsed = collapsed;
    }

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the individual color for the point. Defaults to null. This might not
     * work for all chart types. In styled mode, the color option doesn't take
     * effect. Instead, use colorIndex.
     *
     * @param color
     *            Color of the point.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setCompleted(Completed)
     */
    public Completed getCompleted() {
        return completed;
    }

    /**
     * Progress indicator of how much of the task is completed.
     *
     * @param completed
     */
    public void setCompleted(Completed completed) {
        this.completed = completed;
    }

    /**
     * Progress indicator of how much of the task is completed.
     *
     * @param completed
     */
    public void setCompleted(Number completed) {
        if (this.completed == null) {
            this.completed = new Completed();
        }

        this.completed.setAmount(completed);
    }

    /**
     * @see #setDependencies(List)
     */
    public List<GanttSeriesItemDependency> getDependencies() {
        return dependency;
    }

    /**
     * Dependencies on another tasks (points) in the Gantt chart.
     * 
     * @see #addDependency(GanttSeriesItemDependency)
     * @see #addDependency(String)
     */
    public void setDependencies(List<GanttSeriesItemDependency> dependencies) {
        this.dependency = dependencies;
    }

    /**
     * Adds a dependency on another task.
     * 
     * @param dependency
     *            The dependency configuration object, allowing to specify
     *            further connecting options between the points.
     */
    public void addDependency(GanttSeriesItemDependency dependency) {
        if (this.dependency == null) {
            this.dependency = new ArrayList<>();
        }
        this.dependency.add(dependency);
    }

    /**
     * Adds a dependency on another task.
     * 
     * @param to
     *            The ID of the point (task) that this point depends on in Gantt
     *            charts
     */
    public void addDependency(String to) {
        addDependency(new GanttSeriesItemDependency(to));
    }

    /**
     * @see #setEnd(Instant)
     */
    public Number getEnd() {
        return end;
    }

    /**
     * The end time of a task.
     * 
     * @param end
     */
    public void setEnd(Instant end) {
        this.end = Util.toHighchartsTS(end);
    }

    /**
     * @see #setMilestone(Boolean)
     */
    public Boolean getMilestone() {
        return milestone;
    }

    /**
     * Whether this point is a milestone. If so, only the start option is
     * handled, while end is ignored. Defaults to false.
     * 
     * @param milestone
     */
    public void setMilestone(Boolean milestone) {
        this.milestone = milestone;
    }

    /**
     * @see #setParent(String)
     */
    public String getParent() {
        return parent;
    }

    /**
     * The ID of the parent point (task) of this point in Gantt charts. Defaults
     * to null
     * 
     * @param parent
     */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * @see #setStart(Instant)
     */
    public Number getStart() {
        return start;
    }

    /**
     * The start time of a task.
     * 
     * @param start
     */
    public void setStart(Instant start) {
        this.start = Util.toHighchartsTS(start);
    }

    /**
     * @see #setY(Number)
     */
    public Number getY() {
        return y;
    }

    /**
     * The Y value of a task.
     * 
     * @param y
     */
    public void setY(Number y) {
        this.y = y;
    }

    /**
     * @see #setDescription(String)
     */
    public String getDescription() {
        return description;
    }

    /**
     * <p>
     * A description of the point to add to the screen reader information about
     * the point.
     * </p>
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see #setId(String)
     */
    public String getId() {
        return id;
    }

    /**
     * An id for the point.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @see #setLabelrank(Number)
     */
    public Number getLabelrank() {
        return labelrank;
    }

    /**
     * <p>
     * The rank for this point's data label in case of collision. If two data
     * labels are about to overlap, only the one with the highest
     * <code>labelrank</code> will be drawn.
     * </p>
     */
    public void setLabelrank(Number labelrank) {
        this.labelrank = labelrank;
    }

    /**
     * @see #setName(String)
     */
    public String getName() {
        return name;
    }

    /**
     * The name of a task. If a treegrid y-axis is used (default in Gantt
     * charts), this will be picked up automatically, and used to calculate the
     * y-value.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * See also {@link #setCustom(AbstractConfigurationObject)}
     */
    public AbstractConfigurationObject getCustom() {
        return custom;
    }

    /**
     * A reserved subspace to store options and values for customized
     * functionality. Here you can add additional data for your own event
     * callbacks and formatter callbacks.
     * 
     * @param custom
     */
    public void setCustom(AbstractConfigurationObject custom) {
        this.custom = custom;
    }
}
