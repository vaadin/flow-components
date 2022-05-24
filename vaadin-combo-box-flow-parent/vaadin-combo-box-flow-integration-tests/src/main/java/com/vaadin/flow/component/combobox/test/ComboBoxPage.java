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
package com.vaadin.flow.component.combobox.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.bean.SimpleBean;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link ComboBox}.
 */
@Route("vaadin-combo-box/combo-box-test")
public class ComboBoxPage extends Div {

    /**
     * Data class for a ComboBox.
     *
     */
    public enum Title {
        MR, MRS;
    }

    private Label selectedTitle = new Label();

    /**
     * Creates a new instance.
     */
    public ComboBoxPage() {
        createExternalSetValue();
        createExternalDisableTest();
        createWithRequestsSpyDataProvider();
        createWithUpdateProvider();
        createWithValueChangeListener();
        createWithUpdatableValue();
        createWithUpdateOnValueChange();
        createWithPresetValue();
        createWithButtonRenderer();
        setLabelGeneratorAfterValue();
    }

    private void createExternalSetValue() {
        ComboBox<String> comboBox = new ComboBox<>();

        comboBox.setItems("foo", "bar");
        comboBox.setId("external-selected-item");
        comboBox.setValue("foo");

        NativeButton changeSelectedItem = new NativeButton(
                "Changed selected item", evt -> comboBox.setValue(
                        "bar".equals(comboBox.getValue()) ? "foo" : "bar"));
        changeSelectedItem.setId("toggle-selected-item");

        add(comboBox, changeSelectedItem);
    }

    private void createExternalDisableTest() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setId("client-test");
        Label message = new Label("Nothing clicked yet...");
        message.setId("get-value");
        comboBox.setItems("foo", "bar", "paa");
        NativeButton valueSet = new NativeButton("Set Value");
        valueSet.setId("set-value");
        valueSet.addClickListener(event -> {
            comboBox.setValue("bar");
            message.setText(comboBox.getValue());
        });

        NativeButton disableCB = new NativeButton("Set Disabled");
        disableCB.setId("disable-combo-box");
        disableCB.addClickListener(event -> {
            comboBox.setEnabled(false);
            message.setText(comboBox.getValue());
        });
        add(comboBox, valueSet, disableCB, message);
    }

    private void createWithUpdateProvider() {
        ComboBox<String> comboBox = new ComboBox<>();

        comboBox.setItems("foo", "bar");
        comboBox.setId("combo");

        NativeButton setProvider = new NativeButton("Update data provider",
                event -> comboBox.setDataProvider(
                        DataProvider.ofItems("baz", "foobar")));
        setProvider.setId("update-provider");

        NativeButton setItemCaptionGenerator = new NativeButton(
                "Update caption generator",
                event -> comboBox.setItemLabelGenerator(
                        item -> String.valueOf(item.length())));
        setItemCaptionGenerator.setId("update-caption-gen");

        NativeButton setValue = new NativeButton("Update value",
                event -> comboBox.setValue("baz"));
        setValue.setId("update-value");

        add(comboBox, setProvider, setItemCaptionGenerator, setValue);
    }

    private void createWithRequestsSpyDataProvider() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setId("combobox-list-size-request-count");

        AtomicInteger sizeRequestCounter = new AtomicInteger(0);
        Span sizeRequestCountSpan = new Span("0");
        sizeRequestCountSpan.setId("list-size-request-count-span");

        ListDataProvider<String> dataProvider = new SpyListDataProvider<>(
                Arrays.asList("foo", "bar", "baz"),
                ignore -> sizeRequestCountSpan.setText(
                        String.valueOf(sizeRequestCounter.incrementAndGet())));
        comboBox.setDataProvider(dataProvider);

        NativeButton resetDataProvider = new NativeButton("Set data provider",
                event -> comboBox.setItems("new item"));
        resetDataProvider.setId("size-request-count-update-provider");

        add(comboBox, resetDataProvider, sizeRequestCountSpan);
    }

    private void createWithValueChangeListener() {
        ComboBox<Title> titles = new ComboBox<>();

        titles.setItems(Stream.of(Title.values()));

        titles.setId("titles");
        selectedTitle.setId("selected-titles");
        titles.addValueChangeListener(event -> handleSelection(titles));
        add(titles, selectedTitle);
    }

    private void createWithPresetValue() {
        ComboBox<Title> titles = new ComboBox<>();

        titles.setItems(Stream.of(Title.values()));
        titles.setValue(Title.MRS);

        titles.setId("titles-with-preset-value");
        add(titles);
    }

    private void createWithButtonRenderer() {
        ComboBox<String> comboBox = new ComboBox<>();
        Label message = new Label("Nothing clicked yet...");
        message.setId("button-renderer-message");

        comboBox.setRenderer(new NativeButtonRenderer<>(item -> item,
                item -> message.setText("Button clicked: " + item)));
        comboBox.setItems("foo", "bar");
        comboBox.setId("button-renderer");

        add(comboBox, message);
    }

    private void createWithUpdatableValue() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setItems("Item 1", "Item 2", "Item 3");
        Label message = new Label();
        NativeButton button = new NativeButton("Update value",
                evt -> combo.setValue("Item 2"));

        combo.addValueChangeListener(event -> message.setText("Value: "
                + event.getValue() + " isFromClient: " + event.isFromClient()));

        combo.setId("updatable-combo");
        message.setId("updatable-combo-message");
        button.setId("updatable-combo-button");
        add(combo, message, button);
    }

    private void createWithUpdateOnValueChange() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setItems("1", "2", "3");
        combo.setValue("2");

        combo.addValueChangeListener(e -> {
            String value = e.getValue();
            combo.setValue(value);
        });

        combo.setId("update-on-change-combo");
        add(combo);
    }

    private void setLabelGeneratorAfterValue() {
        ComboBox<SimpleBean> combo = new ComboBox<>();
        SimpleBean foo = new SimpleBean("foo");
        combo.setItems(foo);
        combo.setId("label-generator-after-value");

        combo.setValue(foo);
        combo.setItemLabelGenerator(SimpleBean::getName);

        add(combo);
    }

    private void handleSelection(ComboBox<Title> titles) {
        selectedTitle.setText(Optional.ofNullable(titles.getValue())
                .map(Enum::name).orElse(""));
    }

    @Override
    public void add(Component... components) {
        Div div = new Div(components);
        div.getStyle().set("padding", "10px").set("borderBottom",
                "1px solid lightgray");
        super.add(div);
    }

    private static class SpyListDataProvider<T> extends ListDataProvider<T> {

        private SerializableConsumer<Void> sizeRequestListener;

        public SpyListDataProvider(Collection<T> items,
                SerializableConsumer<Void> sizeRequestListener) {
            super(items);
            this.sizeRequestListener = sizeRequestListener;
        }

        @Override
        public int size(Query<T, SerializablePredicate<T>> query) {
            sizeRequestListener.accept(null);
            return super.size(query);
        }
    }
}
