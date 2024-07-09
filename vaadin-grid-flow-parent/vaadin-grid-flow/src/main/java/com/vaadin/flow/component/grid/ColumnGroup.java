/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.dom.Element;

/**
 * Server-side component for the {@code <vaadin-grid-column-group>} element.
 *
 * @author Vaadin Ltd.
 */
@JsModule("@vaadin/vaadin-grid/src/vaadin-grid-column-group.js")
@HtmlImport("frontend://bower_components/vaadin-grid/src/vaadin-grid-column-group.html")
@Tag("vaadin-grid-column-group")
class ColumnGroup extends AbstractColumn<ColumnGroup> {

    /**
     * Constructs a new column group with the given header and grouping the
     * given columns.
     *
     * @param grid
     *            the owner of this column group
     * @param columns
     *            the columns to group
     */
    public ColumnGroup(Grid<?> grid, AbstractColumn<?>... columns) {
        this(grid, Arrays.asList(columns));
    }

    /**
     * Constructs a new column group with the given header and grouping the
     * given columns.
     *
     * @param grid
     *            the owner of this column group
     * @param columns
     *            the columns to group
     */
    public ColumnGroup(Grid<?> grid, Collection<AbstractColumn<?>> columns) {
        super(grid);
        columns.forEach(
                column -> getElement().appendChild(column.getElement()));
    }

    /**
     * Gets the child columns of this column group.
     *
     * @return the child columns of this column group
     */
    public List<AbstractColumn<?>> getChildColumns() {
        return getElement().getChildren()
                .filter(element -> element.getComponent().isPresent() && element
                        .getComponent().get() instanceof AbstractColumn)
                .map(element -> (AbstractColumn<?>) element.getComponent()
                        .get())
                .collect(Collectors.toList());
    }

    /**
     * Gets the underlying {@code <vaadin-grid-column-group>} element.
     * <p>
     * <strong>It is highly discouraged to directly use the API exposed by the
     * returned element.</strong>
     *
     * @return the root element of this component
     */
    @Override
    public Element getElement() {
        return super.getElement();
    }

    @Override
    protected Column<?> getBottomLevelColumn() {
        return getBottomChildColumns().get(0);
    }
}
