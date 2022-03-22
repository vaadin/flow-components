package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.vaadin.flow.component.charts.model.style.Color;

/**
 * Data for the {@link NodeSeries}. Represents a link between the from and to
 * nodes.
 */
public class NodeSeriesItem extends AbstractConfigurationObject {

    private String id;

    @JsonIdentityReference
    private Node from;

    @JsonIdentityReference
    private Node to;

    private String className;

    private Color color;

    private Number colorIndex;

    private DataLabels dataLabels;

    private String description;

    private Number labelrank;

    private String name;

    private Boolean outgoing;

    private Boolean selected;

    private Number weight;

    /**
     * Default constructor.
     */
    public NodeSeriesItem() {
    }

    /**
     *
     * @param from
     *            see {@link #setFrom(Node)}
     * @param to
     *            see {@link #setTo(Node)}
     */
    public NodeSeriesItem(Node from, Node to) {
        this.from = from;
        this.to = to;
    }

    /**
     * @see #setFrom(Node)
     */
    public Node getFrom() {
        return from;
    }

    /**
     * @param from
     *            The node that the link runs from.
     */
    public void setFrom(Node from) {
        this.from = from;
    }

    /**
     * @see #setTo(Node)
     */
    public Node getTo() {
        return to;
    }

    /**
     * @param to
     *            The node that the link runs to.
     */
    public void setTo(Node to) {
        this.to = to;
    }

    /**
     * @see #setClassName(String)
     */
    public String getClassName() {
        return className;
    }

    /**
     * <p>
     * An additional, individual class name for the data point's graphic
     * representation.
     * </p>
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * <p>
     * The color for the individual <em>link</em>. By default, the link color is
     * the same as the node it extends from. The <code>series.fillOpacity</code>
     * option also applies to the points, so when setting a specific link color,
     * consider setting the <code>fillOpacity</code> to 1.
     * </p>
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setColorIndex(Number)
     */
    public Number getColorIndex() {
        return colorIndex;
    }

    /**
     * <p>
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
     * @see #setDataLabels(DataLabels)
     */
    public DataLabels getDataLabels() {
        return dataLabels;
    }

    /**
     * <p>
     * Individual data label for each point. The options are the same as the
     * ones for <a href="../highcharts/plotOptions.series.dataLabels" >
     * plotOptions.series.dataLabels</a>.
     * </p>
     */
    public void setDataLabels(DataLabels dataLabels) {
        this.dataLabels = dataLabels;
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
     * <p>
     * An id for the point. This can be used after render time to get a pointer
     * to the point object through <code>chart.get()</code>.
     * </p>
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
     * <p>
     * The name of the point as shown in the legend, tooltip, dataLabels, etc.
     * </p>
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see #setOutgoing(Boolean)
     */
    public Boolean getOutgoing() {
        return outgoing;
    }

    /**
     * <p>
     * Whether the link goes out of the system.
     * </p>
     */
    public void setOutgoing(Boolean outgoing) {
        this.outgoing = outgoing;
    }

    /**
     * @see #setSelected(Boolean)
     */
    public Boolean getSelected() {
        return selected;
    }

    /**
     * <p>
     * Whether the data point is selected initially.
     * </p>
     */
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    /**
     * @see #setWeight(Number)
     */
    public Number getWeight() {
        return weight;
    }

    /**
     * <p>
     * The weight of the link.
     * </p>
     */
    public void setWeight(Number weight) {
        this.weight = weight;
    }

}
