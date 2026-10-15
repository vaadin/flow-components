/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid.contextmenu;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.it.GridInATemplate;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/context-menu-grid")
public class ContextMenuGridPage extends Div {

    private static final String NO_TARGET_ITEM = "no target item";

    private Span message;

    public ContextMenuGridPage() {
        message = new Span("-");
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
                .mapToObj(i -> new Person("Person " + i, 1900 + i))
                .collect(Collectors.toList()));

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

        NativeButton setMultiSelect = new NativeButton("Multi select grid",
                event -> grid.setSelectionMode(Grid.SelectionMode.MULTI));
        setMultiSelect.setId("set-multi-select");

        add(grid, toggleOpenOnClick, addSubMenu, removeContextMenu,
                setMultiSelect);
        grid.setId("grid-with-context-menu");
    }

    private void gridInATemplateWithContextMenu() {
        GridInATemplate template = new GridInATemplate();
        Grid<String> gridInATemplate = template.getGrid();
        gridInATemplate.addColumn(s -> s).setHeader("Item");
        gridInATemplate.setItems(IntStream.range(0, 26)
                .mapToObj(i -> "Item " + i).collect(Collectors.toList()));

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
