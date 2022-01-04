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
package com.vaadin.flow.component.grid.contextmenu;

import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.it.GridInATemplate;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/context-menu-grid")
public class ContextMenuGridPage extends Div {

    private static final String NO_TARGET_ITEM = "no target item";

    private Label message;

    public ContextMenuGridPage() {
        message = new Label("-");
        message.setId("message");
        add(message);

        gridWithContextMenu();
        gridInATemplateWithContextMenu();
    }

    private void gridWithContextMenu() {
        final Grid<Person> grid = new Grid<>();
        grid.addColumn(Person::getFirstName).setHeader("Name").setId("Name-Id");
        grid.addColumn(Person::getAge).setHeader("Born").setId("Born-Id");
        grid.setItems(IntStream.range(0, 77)
                .mapToObj(i -> new Person("Person " + i, 1900 + i)));

        GridContextMenu<Person> contextMenu = grid.addContextMenu();
        addItems(contextMenu);
        contextMenu.addComponentAtIndex(1, new Hr());

        contextMenu.addGridContextMenuOpenedListener(event -> {
            String name = event.getItem().map(Person::getFirstName)
                    .orElse(NO_TARGET_ITEM);
            String columnId = event.getColumnId().orElse("No column");
            message.setText("pre-open: name=" + name + ", colId=" + columnId);
        });

        NativeButton toggleOpenOnClick = new NativeButton(
                "Toggle open on click",
                e -> contextMenu.setOpenOnClick(!contextMenu.isOpenOnClick()));
        toggleOpenOnClick.setId("toggle-open-on-click");

        NativeButton addSubMenu = new NativeButton("Add sub-menu", e -> {
            GridMenuItem<Person> parent = contextMenu.addItem("parent");
            GridSubMenu<Person> subMenu = parent.getSubMenu();
            addItems(subMenu);
            subMenu.addComponentAtIndex(1, new H1("bar"));
        });
        addSubMenu.setId("add-sub-menu");

        NativeButton removeContextMenu = new NativeButton("Remove context menu",
                event -> contextMenu.setTarget(null));
        removeContextMenu.setId("remove-context-menu");

        add(grid, toggleOpenOnClick, addSubMenu, removeContextMenu);
        grid.setId("grid-with-context-menu");
    }

    private void gridInATemplateWithContextMenu() {
        GridInATemplate template = new GridInATemplate();
        Grid<String> gridInATemplate = template.getGrid();
        gridInATemplate.addColumn(s -> s).setHeader("Item");
        gridInATemplate
                .setItems(IntStream.range(0, 26).mapToObj(i -> "Item " + i));

        GridContextMenu<String> contextMenu = gridInATemplate.addContextMenu();
        contextMenu.addItem("Show name of context menu target item",
                e -> message.setText(e.getItem().orElse(NO_TARGET_ITEM)));

        add(template);
    }

    private void addItems(HasGridMenuItems<Person> menu) {
        menu.addItem("Show name of context menu target item", event -> {
            String name = event.getItem().map(Person::getFirstName)
                    .orElse(NO_TARGET_ITEM);
            message.setText(name);
        });
        menu.addItem("Show connected grid id", e -> {
            String id = e.getGrid().getId().get();
            message.setText("Grid id: " + id);
        });

        Anchor link = new Anchor("foo", "Link");
        menu.addItem(link, event -> {
            message.setText("Link is clicked");
        });
    }
}
