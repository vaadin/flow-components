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
package com.vaadin.flow.component.combobox.test;

import java.util.Optional;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link ComboBox}.
 */
@Route("combo-box-test")
public class ComboBoxPage extends Div {

    /**
     * Data class for a Grid.
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
        createWithUpdateProvider();
        createWithValueChangeListener();
        createWithPresetValue();
        createWithButtonRenderer();
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
}
