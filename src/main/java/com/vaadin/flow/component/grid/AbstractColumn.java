/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.dom.Element;
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
    protected Element headerTemplate;
    protected Element footerTemplate;
    private Renderer<?> headerRenderer;
    private Renderer<?> footerRenderer;

    private boolean headerRenderingScheduled;
    private boolean footerRenderingScheduled;

    private String rawHeaderTemplate;
    private boolean sortingIndicators;

    /**
     * Base constructor with the destination Grid.
     *
     * @param grid
     *            the grid that is the owner of this column
     */
    public AbstractColumn(Grid<?> grid) {
        this.grid = grid;
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
     * Hides or shows the column. By default columns are visible before
     * explicitly hiding them.
     *
     * @param visible
     *            {@code false} to hide the column, {@code true} to show
     */
    @Override
    public void setVisible(boolean visible) {
        getElement().setProperty("hidden", !visible);
    }

    /**
     * Returns whether this column is visible. Default is {@code true}.
     *
     * @return {@code false} if the column is currently hidden, {@code true}
     *         otherwise
     */
    @Override
    @Synchronize("hidden-changed")
    public boolean isVisible() {
        return !getElement().getProperty("hidden", false);
    }

    protected void setHeaderRenderer(Renderer<?> renderer) {
        headerRenderer = renderer;
        scheduleHeaderRendering();
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

    private Rendering<?> renderHeader() {
        if (headerTemplate != null) {
            headerTemplate.removeFromParent();
            headerTemplate = null;
        }
        if (headerRenderer == null) {
            return null;
        }
        Rendering<?> rendering = headerRenderer.render(getElement(), null);
        headerTemplate = rendering.getTemplateElement();
        headerTemplate.setAttribute("class", "header");

        setBaseHeaderTemplate(headerTemplate.getProperty("innerHTML"));
        if (hasSortingIndicators()) {
            headerTemplate.setProperty("innerHTML",
                    addGridSorter(rawHeaderTemplate));
        }

        return rendering;
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

    protected void setFooterRenderer(Renderer<?> renderer) {
        footerRenderer = renderer;
        scheduleFooterRendering();
    }

    private Rendering<?> renderFooter() {
        if (footerTemplate != null) {
            footerTemplate.removeFromParent();
            footerTemplate = null;
        }
        if (footerRenderer == null) {
            return null;
        }
        Rendering<?> rendering = footerRenderer.render(getElement(), null);
        footerTemplate = rendering.getTemplateElement();
        footerTemplate.setAttribute("class", "footer");
        return rendering;
    }

    protected void setHeaderText(String text) {
        setHeaderRenderer(TemplateRenderer.of(HtmlUtils.escape(text)));
    }

    protected void setFooterText(String text) {
        setFooterRenderer(TemplateRenderer.of(HtmlUtils.escape(text)));
    }

    protected void setHeaderComponent(Component component) {
        /*
         * Uses the special renderer to take care of the vaadin-grid-sorter.
         */
        setHeaderRenderer(new GridSorterComponentRenderer<>(this, component));
    }

    protected void setFooterComponent(Component component) {
        setFooterRenderer(new ComponentRenderer<>(() -> component));
    }

    protected Renderer<?> getHeaderRenderer() {
        return headerRenderer;
    }

    protected Renderer<?> getFooterRenderer() {
        return footerRenderer;
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

    /*
     * The original header template is needed for when sorting is enabled or
     * disabled in a column.
     */
    protected void setBaseHeaderTemplate(String headerTemplate) {
        rawHeaderTemplate = headerTemplate;
    }

    /*
     * Adds the sorting webcomponent markup to an existing template.
     */
    protected String addGridSorter(String templateInnerHtml) {
        String escapedColumnId = HtmlUtils
                .escape(getBottomLevelColumn().getInternalId());
        return String.format(
                "<vaadin-grid-sorter path='%s'>%s</vaadin-grid-sorter>",
                escapedColumnId, templateInnerHtml);
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

}
