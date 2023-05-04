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
package com.vaadin.flow.component.grid.it;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.WeakHashMap;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.bean.Item;
import com.vaadin.flow.data.bean.ItemGenerator;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-it-demo/using-renderers")
public class GridViewUsingRenderersPage extends LegacyTestView {

    public GridViewUsingRenderersPage() {
        createBasicRenderers();
    }

    private void createBasicRenderers() {
        Grid<Item> grid = new Grid<>();
        grid.setItems(getShoppingCart());

        Grid.Column<Item> nameColumn = grid.addColumn(Item::getName)
                .setHeader("Name");

        Binder<Item> binder = new Binder<>(Item.class);
        Editor<Item> editor = grid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);

        TextField nameField = new TextField();
        binder.forField(nameField).bind("name");
        nameColumn.setEditorComponent(nameField);
        nameColumn.setRenderer(LitRenderer.<Item> of("<b>${item.name}</b>")
                .withProperty("name", Item::getName));

        // NumberRenderer to render numbers in general
        Grid.Column<Item> priceColumn = grid
                .addColumn(new NumberRenderer<>(Item::getPrice, "$ %(,.2f",
                        Locale.US, "$ 0.00"))
                .setHeader("Price");
        priceColumn.setSortable(true);

        NumberField field = new NumberField();
        binder.forField(field)
                .withValidator(price -> price >= 0, "Price cannot be negative")
                .bind("price");
        priceColumn.setEditorComponent(field);

        // LocalDateTimeRenderer for date and time
        grid.addColumn(new LocalDateTimeRenderer<>(Item::getPurchaseDate,
                () -> DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT,
                        FormatStyle.MEDIUM)))
                .setHeader("Purchase date and time").setFlexGrow(2);

        // LocalDateRenderer for dates
        grid.addColumn(new LocalDateRenderer<>(Item::getEstimatedDeliveryDate,
                () -> DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
                .setHeader("Estimated delivery date");

        // Icons
        grid.addColumn(new IconRenderer<>(
                item -> item.getPrice() > 50 ? new Span("$$$") : new Span("$"),
                item -> ""));

        // NativeButtonRenderer for an easy clickable button,
        // without creating a component
        grid.addColumn(new NativeButtonRenderer<>("Remove", item -> {
            ListDataProvider<Item> dataProvider = (ListDataProvider<Item>) grid
                    .getDataProvider();
            dataProvider.getItems().remove(item);
            dataProvider.refreshAll();
        })).setWidth("100px").setFlexGrow(0);

        Collection<Button> editButtons = Collections
                .newSetFromMap(new WeakHashMap<>());

        Grid.Column<Item> editorColumn = grid.addComponentColumn(item -> {
            Button edit = new Button("Edit", clickEvent -> {
                editor.editItem(item);
                field.focus();
            });
            edit.setEnabled(!editor.isOpen());
            editButtons.add(edit);
            return edit;
        });

        editor.addOpenListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));
        editor.addCloseListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));

        Button save = new Button("Save", e -> editor.save());

        Button cancel = new Button("Cancel", e -> editor.cancel());

        // Add a keypress listener that listens for an escape key up event.
        // Note! some browsers return key as Escape and some as Esc
        grid.getElement().addEventListener("keyup", event -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");

        Div buttons = new Div(save, cancel);
        editorColumn.setEditorComponent(buttons);

        grid.setId("grid-basic-renderers");

        LitRenderer<Item> litRenderer = LitRenderer
                .<Item> of(
                        """
                                <span style="color: ${item.price > 50 ? 'red' : 'blue'}">${new Intl.NumberFormat('en-fi',{ style: 'currency', currency: 'USD' }).format(item.price)}</span>
                                """)
                .withProperty("price", Item::getPrice);
        NativeButton swapRenderersButton = new NativeButton(
                "Swap price column renderer",
                clickEvent -> priceColumn.setRenderer(litRenderer));
        swapRenderersButton.setId("btn-swap-renderers");

        addCard("Using renderers", "Using basic renderers", grid,
                swapRenderersButton);
    }

    private static List<Item> getShoppingCart() {
        return new ItemGenerator().generateItems(100);
    }
}
