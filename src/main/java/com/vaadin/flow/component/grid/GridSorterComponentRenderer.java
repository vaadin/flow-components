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

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.data.provider.ComponentDataGenerator;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;

/**
 * Internal component renderer for sortable headers inside Grid.
 * 
 * @author Vaadin Ltd.
 *
 * @param <SOURCE>
 *            the model type
 */
class GridSorterComponentRenderer<SOURCE>
        extends ComponentRenderer<Component, SOURCE> {

    private final Column<?> column;
    private final Component component;

    /**
     * Creates a new renderer for a specific column, using the defined
     * component.
     * 
     * @param column
     *            The column which header should be rendered
     * @param component
     *            The component to be used by the renderer
     */
    public GridSorterComponentRenderer(Column<?> column, Component component) {
        this.column = column;
        this.component = component;
    }

    @Override
    public Rendering<SOURCE> render(Element container,
            DataKeyMapper<SOURCE> keyMapper, Element contentTemplate) {

        GridSorterComponentRendering rendering = new GridSorterComponentRendering(
                contentTemplate);

        container.getNode()
                .runWhenAttached(ui -> ui.getInternals().getStateTree()
                        .beforeClientResponse(container.getNode(),
                                context -> setupTemplateWhenAttached(
                                        context.getUI(), container, rendering,
                                        keyMapper)));
        return rendering;
    }

    private void setupTemplateWhenAttached(UI ui, Element owner,
            GridSorterComponentRendering rendering,
            DataKeyMapper<SOURCE> keyMapper) {
        String appId = ui.getInternals().getAppId();
        Element templateElement = rendering.getTemplateElement();
        owner.appendChild(templateElement);

        Element container = new Element("div");
        owner.appendVirtualChild(container);
        rendering.setContainer(container);
        String templateInnerHtml;

        if (component != null) {
            container.appendChild(component.getElement());

            templateInnerHtml = String.format(
                    "<flow-component-renderer appid=\"%s\" nodeid=\"%s\"></flow-component-renderer>",
                    appId, component.getElement().getNode().getId());
        } else {
            templateInnerHtml = "";
        }

        /*
         * The renderer must set the base header template back to the column, so
         * if/when the sortable state is changed by the developer, the column
         * knows how to add or remove the grid sorter.
         */
        column.setBaseHeaderTemplate(templateInnerHtml);
        if (column.isSortable()) {
            templateInnerHtml = column.addGridSorter(templateInnerHtml);
        }

        templateElement.setProperty("innerHTML", templateInnerHtml);
    }

    private class GridSorterComponentRendering extends
            ComponentDataGenerator<SOURCE> implements Rendering<SOURCE> {

        private Element templateElement;

        public GridSorterComponentRendering(Element templateElement) {
            super(GridSorterComponentRenderer.this, null);
            this.templateElement = templateElement;
        }

        @Override
        public Element getTemplateElement() {
            return templateElement;
        }

        @Override
        public Optional<DataGenerator<SOURCE>> getDataGenerator() {
            return Optional.of(this);
        }
    }
}
