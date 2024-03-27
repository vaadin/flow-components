/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
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

        // Needed to update node ids used by <flow-component-renderer> when
        // refreshing with @PreserveOnRefresh.
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

    /**
     * Only intended for internal use.
     *
     * @param renderer
     *            the new footer renderer
     * @deprecated since 23.3, internal usage of renderers for grid headers and
     *             footers will be removed in 24
     */
    @Deprecated
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

        if (hasSortingIndicators()) {
            headerTemplate.setProperty("innerHTML",
                    addGridSorter(headerTemplate.getProperty("innerHTML")));
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

    /**
     * Only intended for internal use.
     *
     * @param renderer
     *            the new footer renderer
     * @deprecated since 23.3, internal usage of renderers for grid headers and
     *             footers will be removed in 24
     */
    @Deprecated
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

    /**
     * Returns the header text of the column.
     *
     * @return the header text
     */
    public String getHeaderText() {
        return headerText;
    }

    protected void setHeaderText(String text) {
        headerText = text;
        headerComponent = null;
        setHeaderRenderer(TemplateRenderer
                .of(text != null ? HtmlUtils.escape(text) : ""));
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
        footerText = text;
        footerComponent = null;
        setFooterRenderer(TemplateRenderer
                .of(text != null ? HtmlUtils.escape(text) : ""));
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
        headerText = null;
        headerComponent = component;
        setHeaderRenderer(new ComponentRenderer<>(() -> component));
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
        footerText = null;
        footerComponent = component;
        setFooterRenderer(new ComponentRenderer<>(() -> component));
    }

    /**
     * Only intended for internal use.
     *
     * @return the header renderer
     * @deprecated since 23.3, internal usage of renderers for grid headers and
     *             footers will be removed in 24
     */
    @Deprecated
    protected Renderer<?> getHeaderRenderer() {
        return headerRenderer;
    }

    /**
     * Only intended for internal use.
     *
     * @return the footer renderer
     * @deprecated since 23.3, internal usage of renderers for grid headers and
     *             footers will be removed in 24
     */
    @Deprecated
    protected Renderer<?> getFooterRenderer() {
        return footerRenderer;
    }

    protected void moveHeaderContent(AbstractColumn<?> otherColumn) {
        if (headerComponent != null) {
            otherColumn.setHeaderComponent(headerComponent);
        } else {
            otherColumn.setHeaderText(headerText);
        }
    }

    protected void moveFooterContent(AbstractColumn<?> otherColumn) {
        if (footerComponent != null) {
            otherColumn.setFooterComponent(footerComponent);
        } else {
            otherColumn.setFooterText(footerText);
        }
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
     * Adds the sorting webcomponent markup to an existing template.
     */
    protected String addGridSorter(String templateInnerHtml) {
        String escapedColumnId = HtmlUtils
                .escape(getBottomLevelColumn().getInternalId());

        String textContent = headerComponent != null
                ? headerComponent.getElement().getTextRecursively()
                : headerText;
        if (textContent != null) {
            textContent = HtmlUtils.escape(textContent);
        }
        String sortBy = textContent == null || textContent.isBlank() ? ""
                : "aria-label='Sort by " + textContent + "'";

        return String.format(
                "<vaadin-grid-sorter path='%s' %s>%s</vaadin-grid-sorter>",
                escapedColumnId, sortBy, templateInnerHtml);
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
                        .collect(Collectors.toList()));
        return columnChildren;
    }

}
