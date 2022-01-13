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
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-template-test")
public class GridWithTemplatePage extends Div {

    public GridWithTemplatePage() {
        add(new H2("Grid in a template"));
        createGridInATemplateWithTemplatesInTheCells();
        createGridInATemplateWithTemplatesInTheDetails();
        createGridInATemplateWithTemplatesInTheHeader();
        createGridInTemplateWithColumnProperties();

        getElement().appendChild(new Element("hr"));
        add(new H2("Standalone Grid"));
        createStandaloneGridWithTemplatesInTheCells();
        createStandaloneGridWithTemplatesInTheDetails();
        createStandaloneGridWithTemplatesInTheHeader();
        createStandaloneGridWithColumnProperties();
    }

    private void createGridInATemplateWithTemplatesInTheCells() {
        GridInATemplate gridInATemplate = new GridInATemplate();
        gridInATemplate.setId("injected-template-in-cells");
        Grid<String> grid = gridInATemplate.getGrid();
        setCommonGridFeatures(grid, gridInATemplate.getId().get());

        grid.addColumn(value -> value);
        grid.addColumn(new ComponentRenderer<>(
                value -> getTestTemplate(value, grid.getId().get())));

        add(new H3("Grid with templates in the cells"), gridInATemplate);
    }

    private void createGridInATemplateWithTemplatesInTheDetails() {
        GridInATemplate gridInATemplate = new GridInATemplate();
        gridInATemplate.setId("injected-template-in-details");
        Grid<String> grid = gridInATemplate.getGrid();
        setCommonGridFeatures(grid, gridInATemplate.getId().get());

        grid.addColumn(value -> value);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(
                value -> getTestTemplate(value, grid.getId().get())));

        add(new H3("Grid with templates in the details"), gridInATemplate);
    }

    private void createGridInATemplateWithTemplatesInTheHeader() {
        GridInATemplate gridInATemplate = new GridInATemplate();
        gridInATemplate.setId("injected-template-in-header");
        Grid<String> grid = gridInATemplate.getGrid();
        setCommonGridFeatures(grid, gridInATemplate.getId().get());

        grid.addColumn(value -> value)
                .setHeader(getTestTemplate("header", grid.getId().get()))
                .setFooter(getTestTemplate("footer", grid.getId().get()));

        add(new H3("Grid with templates in the header and footer"),
                gridInATemplate);
    }

    private void createGridInTemplateWithColumnProperties() {
        GridInATemplate gridInATemplate = new GridInATemplate();
        gridInATemplate.setId("injected-columns-with-properties");
        Grid<String> grid = gridInATemplate.getGrid();
        setCommonGridFeatures(grid, gridInATemplate.getId().get());

        grid.addColumn(value -> value).setFlexGrow(2);
        grid.addColumn(TemplateRenderer.of("[[index]]")).setFlexGrow(0)
                .setWidth("20px");
        grid.addColumn(new ComponentRenderer<>(
                value -> getTestTemplate(value, grid.getId().get())))
                .setFrozen(true).setResizable(true);

        add(new H3("Grid with column properties"), gridInATemplate);
    }

    private void createStandaloneGridWithTemplatesInTheCells() {
        Grid<String> grid = new Grid<>();
        setCommonGridFeatures(grid, "standalone-template-in-cells");

        grid.addColumn(value -> value);
        grid.addColumn(new ComponentRenderer<>(
                value -> getTestTemplate(value, grid.getId().get())));

        add(new H3("Grid with templates in the cells"), grid);
    }

    private void createStandaloneGridWithTemplatesInTheDetails() {
        Grid<String> grid = new Grid<>();
        setCommonGridFeatures(grid, "standalone-template-in-details");

        grid.addColumn(value -> value);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(
                value -> getTestTemplate(value, grid.getId().get())));

        add(new H3("Grid with templates in the details"), grid);
    }

    private void createStandaloneGridWithTemplatesInTheHeader() {
        Grid<String> grid = new Grid<>();
        setCommonGridFeatures(grid, "standalone-template-in-header");

        grid.addColumn(value -> value)
                .setHeader(getTestTemplate("header", grid.getId().get()))
                .setFooter(getTestTemplate("footer", grid.getId().get()));

        add(new H3("Grid with templates in the header and footer"), grid);
    }

    private void createStandaloneGridWithColumnProperties() {
        Grid<String> grid = new Grid<>();
        setCommonGridFeatures(grid, "standalone-columns-with-properties");

        grid.addColumn(value -> value).setFlexGrow(2);
        grid.addColumn(TemplateRenderer.of("[[index]]")).setFlexGrow(0)
                .setWidth("20px");
        grid.addColumn(new ComponentRenderer<>(
                value -> getTestTemplate(value, grid.getId().get())))
                .setFrozen(true).setResizable(true);

        add(new H3("Grid with column properties"), grid);
    }

    private void setCommonGridFeatures(Grid<String> grid, String id) {
        grid.setItems("Item 1", "Item 2", "Item 3");
        grid.setHeight("150px");
        grid.setId(id);
    }

    private TestTemplate getTestTemplate(String value, String idPrefix) {
        TestTemplate template = new TestTemplate();
        template.setId(
                idPrefix + "-" + (value.toLowerCase().replace(' ', '-')));
        return template;
    }

}
