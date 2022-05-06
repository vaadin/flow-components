/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.dom.Element;

/**
 * Server-side component for the {@code <vaadin-grid-column-group>} element.
 *
 * @author Vaadin Ltd.
 */
@JsModule("@vaadin/grid/src/vaadin-grid-column-group.js")
@Tag("vaadin-grid-column-group")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
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

        getElement().setAttribute("suppress-template-warning", true);
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
