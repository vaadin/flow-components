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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-item-refresh-page")
public class GridItemRefreshPage extends Div {

    public static final String UPDATED_FIRST_FIELD = "updated";
    public static final int UPDATED_SECOND_FIELD = 12345;

    // update the element values from client side so that it should only be
    // updated in DOM, not in caches
    // updating the previous cell in animation frame so that grid active item
    // handling doesn't reset the changes
    private static final String EDIT_CELL_VALUE = "debugger; this['innerText'] = 'EDITED';window.requestAnimationFrame(() => {this.parentElement.previousElementSibling['textContent']='EDITED';})";
    private static final TemplateRenderer<Bean> UPDATE_RENDERER = TemplateRenderer
            .of("<div id=[[item.thirdField]] onclick=\"" + EDIT_CELL_VALUE
                    + "\">[[item.thirdField]]</div>");

    private static class Bean {
        private String firstField;
        private int secondField;
        private final String thirdField;

        public Bean(String firstField, int secondField) {
            this.firstField = firstField;
            this.secondField = secondField;
            this.thirdField = "div-" + secondField;
        }

        public String getFirstField() {
            return firstField;
        }

        public void setFirstField(String firstField) {
            this.firstField = firstField;
        }

        public int getSecondField() {
            return secondField;
        }

        public void setSecondField(int secondField) {
            this.secondField = secondField;
        }

        @SuppressWarnings("unused")
        public String getThirdField() {
            return thirdField;
        }
    }

    public GridItemRefreshPage() {
        createTemplateGrid();
        createComponentGrid();
    }

    private void createTemplateGrid() {
        Grid<Bean> grid = new Grid<>();
        grid.setHeight("500px");
        grid.addColumn(Bean::getFirstField).setHeader("First Field");
        grid.addColumn(Bean::getSecondField).setHeader("Second Field");
        grid.addColumn(UPDATE_RENDERER.withProperty("thirdField",
                bean -> bean.thirdField)).setHeader("mutation");
        List<Bean> items = createItems(1000);
        grid.setItems(items);
        grid.setId("template-grid");

        Div div = new Div();
        div.setText("Template Grid");
        add(div, grid);
        addButtons(grid, items, "template-");
    }

    private void createComponentGrid() {
        Grid<Bean> grid = new Grid<>();
        grid.setHeight("500px");
        grid.addColumn(new ComponentRenderer<Label, Bean>(
                item -> new Label(item.getFirstField())))
                .setHeader("First Field");
        grid.addColumn(new ComponentRenderer<Label, Bean>(
                item -> new Label(String.valueOf(item.getSecondField()))))
                .setHeader("Second Field");
        List<Bean> items = createItems(1000);
        grid.setItems(items);
        grid.setId("component-grid");

        Div div = new Div();
        div.setText("Component Grid");
        add(div, grid);
        addButtons(grid, items, "component-");
    }

    private void addButtons(Grid<Bean> grid, List<Bean> items,
            String idPrefix) {
        NativeButton refreshFirstBtn = new NativeButton(
                "update and refresh first item", event -> {
                    updateBean(items.get(0));
                    grid.getDataProvider().refreshItem(items.get(0));
                });
        NativeButton refreshMultipleBtn = new NativeButton(
                "update and refresh items 5-10", event -> {
                    items.subList(4, 10).forEach(item -> {
                        updateBean(item);
                        grid.getDataProvider().refreshItem(item);
                    });
                });
        NativeButton refreshProviderBtn = new NativeButton(
                "refresh all through data provider", event -> {
                    items.forEach(this::updateBean);
                    grid.getDataProvider().refreshAll();
                });

        NativeButton refreshFirstCommunicatorBtn = new NativeButton(
                "update and refresh first item through data communicator",
                event -> {
                    updateBean(items.get(0));
                    grid.getDataCommunicator().refresh(items.get(0));
                });
        NativeButton refreshMultipleCommunicatorBtn = new NativeButton(
                "update and refresh items 5-10 through data communicator",
                event -> {
                    items.subList(4, 10).forEach(item -> {
                        updateBean(item);
                        grid.getDataCommunicator().refresh(item);
                    });
                });
        NativeButton resetCommunicatorBtn = new NativeButton(
                "refresh all through data communicator", event -> {
                    items.forEach(this::updateBean);
                    grid.getDataCommunicator().reset();
                });

        refreshFirstBtn.setId(idPrefix + "refresh-first");
        refreshMultipleBtn.setId(idPrefix + "refresh-multiple");
        refreshProviderBtn.setId(idPrefix + "refresh-all");

        refreshFirstCommunicatorBtn
                .setId(idPrefix + "refresh-first-communicator");
        refreshMultipleCommunicatorBtn
                .setId(idPrefix + "refresh-multiple-communicator");
        resetCommunicatorBtn.setId(idPrefix + "reset-communicator");
        add(grid, refreshFirstBtn, refreshMultipleBtn, refreshProviderBtn,
                refreshFirstCommunicatorBtn, refreshMultipleCommunicatorBtn,
                resetCommunicatorBtn);
    }

    private List<Bean> createItems(int numberOfItems) {
        return IntStream.range(0, numberOfItems).mapToObj(
                intValue -> new Bean(String.valueOf(intValue), intValue))
                .collect(Collectors.toList());
    }

    private void updateBean(Bean bean) {
        bean.setFirstField(UPDATED_FIRST_FIELD + " " + bean.getFirstField());
        bean.setSecondField(UPDATED_SECOND_FIELD);
    }
}
