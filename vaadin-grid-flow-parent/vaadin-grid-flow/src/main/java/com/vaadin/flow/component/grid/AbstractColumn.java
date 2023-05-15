/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.internal.HtmlUtils;

/**
 * Base class with common implementation for different types of columns used
 * inside a {@link Grid}.
 *
 * @author Vaadin Ltd.
 * @param <T>
 *            the subclass type
 */
abstract class AbstractColumn<T extends AbstractColumn<T>> extends Component
        implements ColumnBase<T> {

    protected final Grid<?> grid;
    private boolean headerRenderingScheduled;
    private boolean footerRenderingScheduled;
    private boolean sortingIndicators;
    private String headerText;
    private Component headerComponent;
    private String footerText;
    private Component footerComponent;

    /**
     * Base constructor with the destination Grid.
     *
     * @param grid
     *            the grid that is the owner of this column
     */
    public AbstractColumn(Grid<?> grid) {
        this.grid = grid;

        // Needed to update node ids when refreshing with @PreserveOnRefresh.
        addAttachListener(e -> {
            scheduleHeaderRendering();
            scheduleFooterRendering();
        });
    }

    /**
     * Gets the owner of this column.
     *
     * @return the grid which owns this column
     */
    public Grid<?> getGrid() {
        return grid;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that column related data is sent to the client side even if the
     * column is invisible. Use {@link Grid#removeColumn(Column)} to remove
     * column (or don't add the column all) and avoid sending extra data.
     * </p>
     *
     * @see Grid#removeColumn(Column)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

    private void scheduleHeaderRendering() {
        if (headerRenderingScheduled) {
            return;
        }
        headerRenderingScheduled = true;
        getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(this, context -> {
                    if (!headerRenderingScheduled) {
                        return;
                    }
                    renderHeader();
                    headerRenderingScheduled = false;
                }));
    }

    private void renderHeader() {
        Serializable headerContent = headerComponent != null
                ? headerComponent.getElement()
                : headerText;
        boolean showSorter = hasSortingIndicators();
        String sorterPath = showSorter
                ? HtmlUtils.escape(getBottomLevelColumn().getInternalId())
                : null;

        grid.getElement().executeJs(
                "this.$connector.setHeaderRenderer($0, { content: $1, showSorter: $2, sorterPath: $3 })",
                this.getElement(), headerContent, showSorter, sorterPath);
    }

    private void scheduleFooterRendering() {
        if (footerRenderingScheduled) {
            return;
        }
        footerRenderingScheduled = true;
        getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(this, context -> {
                    if (!footerRenderingScheduled) {
                        return;
                    }
                    renderFooter();
                    footerRenderingScheduled = false;
                }));
    }

    private void renderFooter() {
        Serializable footerContent = footerComponent != null
                ? footerComponent.getElement()
                : footerText;

        grid.getElement().executeJs(
                "this.$connector.setFooterRenderer($0, { content: $1 })",
                this.getElement(), footerContent);
    }

    /**
     * Returns the header text of the column.
     *
     * @return the header text
     */
    public String getHeaderText() {
        return headerText;
    }

    protected void setHeaderText(String text) {
        setHeaderContent(text, null);
    }

    /**
     * Returns the footer text of the column.
     *
     * @return the footer text
     */
    public String getFooterText() {
        return footerText;
    }

    protected void setFooterText(String text) {
        setFooterContent(text, null);
    }

    /**
     * Returns the header component of the column.
     *
     * @return the header component
     */
    public Component getHeaderComponent() {
        return headerComponent;
    }

    protected void setHeaderComponent(Component component) {
        setHeaderContent(null, component);
    }

    /**
     * Returns the footer component of the column.
     *
     * @return the footer component
     */
    public Component getFooterComponent() {
        return footerComponent;
    }

    protected void setFooterComponent(Component component) {
        setFooterContent(null, component);
    }

    /**
     * Sets the content of the column header to either a text or a component,
     * and triggers a re-render of the header. If both are null, then the
     * content of the header is cleared.
     *
     * @param text
     *            the new text content, can be null
     * @param component
     *            the new component content, can be null
     */
    void setHeaderContent(String text, Component component) {
        headerText = text;
        headerComponent = replaceChildComponent(headerComponent, component);
        scheduleHeaderRendering();
    }

    /**
     * Sets the content of the column footer to either a text or a component,
     * and triggers a re-render of the footer. If both are null, then the
     * content of the footer is cleared.
     *
     * @param text
     *            the new text content, can be null
     * @param component
     *            the new component content, can be null
     */
    void setFooterContent(String text, Component component) {
        footerText = text;
        footerComponent = replaceChildComponent(footerComponent, component);
        scheduleFooterRendering();
    }

    /**
     * Removes the old component as a virtual child, adds the new component as a
     * virtual child. Both components can be null, in which case the operation
     * does nothing. Additionally, the old component will only be removed if it
     * is still a child of this component, and the new component will be removed
     * from its current parent before adding it to this component.
     *
     * @param oldComponent
     *            the component to remove as a virtual child, can be null
     * @param newComponent
     *            the component to add as a virtual child, can be null
     * @return the newly added component, or null if there was no component to
     *         add
     */
    private Component replaceChildComponent(Component oldComponent,
            Component newComponent) {
        if (oldComponent != null && oldComponent.getParent().isPresent()
                && oldComponent.getParent().get() == this) {
            getElement().removeVirtualChild(oldComponent.getElement());
        }
        if (newComponent != null) {
            if (newComponent.getElement().getParent() != null) {
                newComponent.getElement().getParent()
                        .removeVirtualChild(newComponent.getElement());
            }
            getElement().appendVirtualChild(newComponent.getElement());
        }
        return newComponent;
    }

    /**
     * Moves the current header content, either a text or a component, to a
     * different column or column group. Also clears the content of this column.
     *
     * @param otherColumn
     *            the column or group to move the content to
     */
    protected void moveHeaderContent(AbstractColumn<?> otherColumn) {
        otherColumn.setHeaderContent(headerText, headerComponent);
        setHeaderContent(null, null);
    }

    /**
     * Moves the current footer content, either a text or a component, to a
     * different column or column group. Also clears the content of this column.
     *
     * @param otherColumn
     *            the column or group to move the content to
     */
    protected void moveFooterContent(AbstractColumn<?> otherColumn) {
        otherColumn.setFooterContent(footerText, footerComponent);
        setFooterContent(null, null);
    }

    /**
     * Updates this component to either have sorting indicators according to the
     * sortable state of the underlying column, or removes the sorting
     * indicators.
     *
     * @param sortable
     *            {@code true} to have sorting indicators if the column is
     *            sortable, {@code false} to not have sorting indicators
     */
    protected void updateSortingIndicators(boolean sortable) {
        if (sortable) {
            setSortingIndicators(getBottomLevelColumn().isSortable());
        } else {
            setSortingIndicators(false);
        }
    }

    /**
     * Sets this component to show sorting indicators or not.
     *
     * @param sortingIndicators
     *            {@code true} to show sorting indicators, {@code false} to
     *            remove them
     */
    protected void setSortingIndicators(boolean sortingIndicators) {
        if (this.sortingIndicators == sortingIndicators) {
            return;
        }
        this.sortingIndicators = sortingIndicators;
        scheduleHeaderRendering();
    }

    protected boolean hasSortingIndicators() {
        return sortingIndicators;
    }

    /**
     * Gets the {@code <vaadin-grid-column>} component that is a child of this
     * component, or this component in case this is a bottom level
     * {@code <vaadin-grid-column>} component. This method should be called only
     * on components which have only one such bottom-level column (not on
     * ColumnGroups with multiple children).
     *
     * @return the bottom column component
     */
    protected abstract Column<?> getBottomLevelColumn();

    /**
     * Gets recursively the child components of this component that are
     * instances of Column.
     *
     * @return the Column children of this component
     */
    protected List<Column<?>> getBottomChildColumns() {
        List<Column<?>> columnChildren = getChildren()
                .filter(child -> child instanceof Column<?>)
                .map(child -> (Column<?>) child).collect(Collectors.toList());

        columnChildren.addAll(
                getChildren().filter(child -> child instanceof ColumnGroup)
                        .flatMap(child -> ((ColumnGroup) child)
                                .getBottomChildColumns().stream())
                        .toList());
        return columnChildren;
    }

    protected void setRowHeader(boolean isRowHeader) {
        getElement().getNode()
    }
}
