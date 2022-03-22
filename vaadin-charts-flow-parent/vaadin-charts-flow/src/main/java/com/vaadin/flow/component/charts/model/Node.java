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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vaadin.flow.component.charts.model.style.Color;

import java.util.Objects;

/**
 * A collection of options for the individual nodes. The nodes in an org chart
 * are auto-generated instances of Highcharts.Point, but options can be applied
 * here and linked by the id.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Node extends AbstractConfigurationObject {
    private String id;
    private Color color;
    private Number colorIndex;
    private Number column;
    private DataLabels dataLabels;
    private String description;
    private String image;
    private NodeLayout layout;
    private Number level;
    private String name;
    private String offset;
    private String title;

    /**
     * Default constructor.
     */
    public Node() {

    }

    /**
     * @param id
     *            see {@link #setId(String)}
     */
    public Node(String id) {
        this.id = id;
    }

    /**
     * @param id
     *            see {@link #setId(String)}
     * @param name
     *            see {@link #setName(String)}
     */
    public Node(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * @param id
     *            see {@link #setId(String)}
     * @param name
     *            see {@link #setName(String)}
     * @param title
     *            see {@link #setTitle(String)}
     */
    public Node(String id, String name, String title) {
        this.id = id;
        this.name = name;
        this.title = title;
    }

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * The color of the auto generated node.
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
     * The color index of the auto generated node, especially for use in styled
     * mode.
     */
    public void setColorIndex(Number colorIndex) {
        this.colorIndex = colorIndex;
    }

    /**
     * @see #setColumn(Number)
     */
    public Number getColumn() {
        return column;
    }

    /**
     * An optional column index of where to place the node. The default
     * behaviour is to place it next to the preceding node. Note that this
     * option name is counter intuitive in inverted charts, like for example an
     * organization chart rendered top down. In this case the "columns" are
     * horizontal.
     */
    public void setColumn(Number column) {
        this.column = column;
    }

    /**
     * @see #setDataLabels(DataLabels)
     */
    public DataLabels getDataLabels() {
        return dataLabels;
    }

    /**
     * Individual data label for each node.
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
     * The job description for the node card, will be inserted by the default
     * dataLabel.nodeFormatter.
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
     * The id of the auto-generated node, referring to the from or to setting of
     * the link.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @see #setImage(String)
     */
    public String getImage() {
        return image;
    }

    /**
     * An image for the node card, will be inserted by the default
     * dataLabel.nodeFormatter.
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * @see #setLayout(NodeLayout)
     */
    public NodeLayout getLayout() {
        return layout;
    }

    /**
     * Layout for the node's children. If hanging, this node's children will
     * hang below their parent, allowing a tighter packing of nodes in the
     * diagram
     */
    public void setLayout(NodeLayout layout) {
        this.layout = layout;
    }

    /**
     * @see #setLevel(Number)
     */
    public Number getLevel() {
        return level;
    }

    /**
     * An optional level index of where to place the node. The default behaviour
     * is to place it next to the preceding node. Alias of nodes.column, but in
     * inverted sankeys and org charts, the levels are laid out as rows.
     */
    public void setLevel(Number level) {
        this.level = level;
    }

    /**
     * @see #setName(String)
     */
    public String getName() {
        return name;
    }

    /**
     * The name to display for the node in data labels and tooltips. Use this
     * when the name is different from the id. Where the id must be unique for
     * each node, this is not necessary for the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see #setOffset(String)
     */
    public String getOffset() {
        return offset;
    }

    /**
     * In a horizontal layout, the vertical offset of a node in terms of weight.
     * Positive values shift the node downwards, negative shift it upwards. In a
     * vertical layout, like organization chart, the offset is horizontal. If a
     * percentage string is given, the node is offset by the percentage of the
     * node size plus nodePadding. Defaults to 0.
     */
    public void setOffset(String offset) {
        this.offset = offset;
    }

    /**
     * @see #setTitle(String)
     */
    public String getTitle() {
        return title;
    }

    /**
     * The job title for the node card, will be inserted by the default
     * dataLabel.nodeFormatter.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass() || id == null)
            return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
