/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import java.util.List;

import com.vaadin.flow.component.charts.model.style.Color;

@SuppressWarnings("unused")
public class AxisGrid extends AbstractConfigurationObject {

    private Color borderColor;
    private Number borderWidth;
    private Number cellHeight;
    private List<XAxis> columns;
    private Boolean enabled;

    /**
     * @see #setBorderColor(Color)
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Set border color for the label grid lines. Defaults to undefined.
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @see #setBorderWidth(Number)
     */
    public Number getBorderWidth() {
        return borderWidth;
    }

    /**
     * Set border width of the label grid lines. Defaults to 1.
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * @see #setCellHeight(Number)
     */
    public Number getCellHeight() {
        return cellHeight;
    }

    /**
     * Set cell height for grid axis labels. By default this is calculated from
     * font size. This option only applies to horizontal axes. Defaults to
     * undefined.
     */
    public void setCellHeight(Number cellHeight) {
        this.cellHeight = cellHeight;
    }

    /**
     * @see #setColumns(List)
     */
    public List<XAxis> getColumns() {
        return columns;
    }

    /**
     * Set specific options for each column (or row for horizontal axes) in the
     * grid. Each extra column/row is its own axis, and the axis options can be
     * set here. Defaults to undefined.
     */
    public void setColumns(List<XAxis> columns) {
        this.columns = columns;
    }

    /**
     * Add a new column to the grid. See {@link #setColumns(List)}
     * 
     * @param column
     *            A column to be added
     */
    public void addColumn(XAxis column) {
        if (columns == null) {
            columns = new java.util.ArrayList<>();
        }
        columns.add(column);
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable grid on the axis labels. Defaults to true for Gantt charts.
     * Defaults to true.
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
