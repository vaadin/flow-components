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
package com.vaadin.flow.component.grid.it;

import java.util.stream.IntStream;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Person;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("context-menu-grid")
public class ContextMenuGridPage extends Div {

    private Label message;
    private Grid<Person> grid;

    public ContextMenuGridPage() {
        message = new Label("-");
        message.setId("message");
        add(message);

        gridWithContextMenu();
        gridInATemplateWithContextMenu();
    }

    private void gridWithContextMenu() {
        grid = new Grid<>();
        grid.addColumn(Person::getName).setHeader("Name");
        grid.addColumn(Person::getBorn).setHeader("Born");
        grid.setItems(IntStream.range(0, 77)
                .mapToObj(i -> new Person("Person " + i, 1900 + i)));

        ContextMenu contextMenu = new ContextMenu(grid);
        contextMenu.addItem("Show name of context menu target item",
                e -> updateMessage());

        NativeButton toggleOpenOnClick = new NativeButton(
                "Toggle open on click",
                e -> contextMenu.setOpenOnClick(!contextMenu.isOpenOnClick()));
        toggleOpenOnClick.setId("toggle-open-on-click");

        NativeButton showName = new NativeButton(
                "Show name of context menu target item", e -> {
                    try {
                        updateMessage();
                    } catch (Exception exception) {
                        message.setText(exception.getClass().getName() + " "
                                + exception.getMessage());
                    }
                });
        showName.setId("show-name");

        add(grid, toggleOpenOnClick, showName);
    }

    private void updateMessage() {
        message.setText(
                grid.getContextMenuTargetItem() == null ? "null target item"
                        : grid.getContextMenuTargetItem().getName());
    }

    private void gridInATemplateWithContextMenu() {
        GridInATemplate template = new GridInATemplate();
        Grid<String> gridInATemplate = template.getGrid();
        gridInATemplate.addColumn(s -> s).setHeader("Item");
        gridInATemplate
                .setItems(IntStream.range(0, 26).mapToObj(i -> "Item " + i));

        ContextMenu contextMenu = new ContextMenu(gridInATemplate);
        contextMenu.addItem("Show name of context menu target item",
                e -> message
                        .setText(gridInATemplate.getContextMenuTargetItem()));

        add(template);
    }
}
