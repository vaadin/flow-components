/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import java.util.Locale;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.bean.Item;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/renderer-swap")
public class GridRendererSwapPage extends Div {

    public GridRendererSwapPage() {
        List<Item> items = List.of(createItem("Item 1", 73.1),
                createItem("Item 2", 24.05));

        Grid<Item> grid = new Grid<>();
        grid.setItems(items);

        Binder<Item> binder = new Binder<>(Item.class);
        Editor<Item> editor = grid.getEditor();
        editor.setBinder(binder);

        grid.addColumn(Item::getName).setHeader("Name");

        NumberField priceField = new NumberField();
        binder.forField(priceField).bind("price");
        Grid.Column<Item> priceColumn = grid.addColumn(
                new NumberRenderer<>(Item::getPrice, "$ %(,.2f", Locale.US))
                .setHeader("Price");
        priceColumn.setEditorComponent(priceField);

        NativeButton swapRenderer = new NativeButton("Swap price renderer",
                event -> priceColumn.setRenderer(LitRenderer.<Item> of(
                        "<span style=\"color: ${item.price > 50 ? 'red' : 'blue'}\">${item.price}</span>")
                        .withProperty("price", Item::getPrice)));
        swapRenderer.setId("swap-renderer");

        NativeButton editFirst = new NativeButton("Edit first item",
                event -> editor.editItem(items.get(0)));
        editFirst.setId("edit-first");

        add(grid, swapRenderer, editFirst);
    }

    private static Item createItem(String name, double price) {
        Item item = new Item();
        item.setName(name);
        item.setPrice(price);
        return item;
    }
}
