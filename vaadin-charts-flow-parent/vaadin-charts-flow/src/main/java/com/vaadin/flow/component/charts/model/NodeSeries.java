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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A series containing nodes and links to nodes.
 */
@JsonPropertyOrder({ "nodes", "values" })
public class NodeSeries extends AbstractSeries {

    private Set<Node> nodes;
    private List<NodeSeriesItem> data;

    /**
     * Adds a data item. The to and from nodes must also be added using
     * {@link #addNode(Node)}
     *
     * @param nodeSeriesItem
     *            item to be added.
     * @throws IllegalArgumentException
     *             if the nodeSeriesItem is null or if either to or from nodes
     *             null or have null ids.
     */
    public void add(NodeSeriesItem nodeSeriesItem) {
        validateNodeSeriesItem(nodeSeriesItem);
        ensureData().add(nodeSeriesItem);
        addNode(nodeSeriesItem.getFrom());
        addNode(nodeSeriesItem.getTo());
    }

    /**
     * Adds both nodes and links then in the series.
     *
     * @param from
     *            see {@link NodeSeriesItem#setFrom(Node)}
     * @param to
     *            see {@link NodeSeriesItem#setTo(Node)}
     * @return {@link NodeSeriesItem} created.
     */
    public NodeSeriesItem add(Node from, Node to) {
        NodeSeriesItem item = new NodeSeriesItem(from, to);
        add(item);
        return item;
    }

    /**
     * Adds a node to the chart. To link the added node to other nodes use
     * {@link #add(NodeSeriesItem)}.
     *
     * @param node
     *            {@link Node} to be added. Not null.
     * @throws IllegalArgumentException
     *             if the node is null or if its id is null.
     */
    public void addNode(Node node) {
        validateNode(node, "Node");
        ensureNodes().add(node);
    }

    /**
     * Removes the nodeSeriesItem. To remove the to and from nodes, use
     * {@link #remove(Node)}
     *
     * @param nodeSeriesItem
     *            item to be removed.
     */
    public void remove(NodeSeriesItem nodeSeriesItem) {
        if (data != null) {
            data.remove(nodeSeriesItem);
        }
    }

    /**
     * Removes the node. To remove the links, use
     * {@link #remove(NodeSeriesItem)}
     *
     * @param node
     *            item to be removed.
     */
    public void remove(Node node) {
        if (nodes != null) {
            nodes.remove(node);
        }
    }

    /**
     *
     * @return The data in this series.
     */
    public List<NodeSeriesItem> getData() {
        if (data == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(data);
    }

    /**
     *
     * @return The nodes in this Series.
     */
    public Set<Node> getNodes() {
        if (nodes == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(nodes);
    }

    private void validateNodeSeriesItem(NodeSeriesItem item) {
        String description = "NodeSeriesItem";
        validate(item, description);
        validateNode(item.getFrom(), "From node");
        validateNode(item.getTo(), "To node");
    }

    private List<NodeSeriesItem> ensureData() {
        return data = data != null ? data : new ArrayList<>();
    }

    private Set<Node> ensureNodes() {
        return nodes = nodes != null ? nodes : new LinkedHashSet<>();
    }

    private void validateNode(Node node, String description) {
        validate(node, description);
        validate(node.getId(), description + " id");
    }

    private void validate(Object object, String description) {
        if (object == null) {
            String message = description + " may not be null";
            throw new IllegalArgumentException(message);
        }
    }

}
