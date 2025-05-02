/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.router.Route;

/**
 * View for {@link RadioButtonGroup} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-radio-button-group-test-demo")
public class RadioButtonGroupDemoPage extends Div {

    public static class Person {

        private String name;
        private int id;

        public Person(String name) {
            this.name = name;
        }

        public Person(int id, String name) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

    }

    public RadioButtonGroupDemoPage() {
        addBasicFeatures();
        addHelperText();
        addItemRenderer();
        addItemLabelGenerator();
        addItemIconGenerator();
        addDisabled();
        addDisabledItems();
        addReadOnlyGroup();
    }

    private void addHelperText() {
        RadioButtonGroup<String> groupWitHelperText = new RadioButtonGroup<>();
        groupWitHelperText.setId("group-with-helper-text");
        groupWitHelperText.setItems("foo", "bar", "baz");
        groupWitHelperText.setHelperText("helperText");

        RadioButtonGroup<String> groupWitHelperComponent = new RadioButtonGroup<>();
        groupWitHelperComponent.setId("group-with-helper-component");
        groupWitHelperComponent.setItems("foo", "bar", "baz");
        Span helperComponent = new Span("helperComponent");
        helperComponent.setId("helper-component");
        groupWitHelperComponent.setHelperComponent(helperComponent);

        NativeButton clearHelperText = new NativeButton("clear helper text",
                e -> groupWitHelperText.setHelperText(null));
        clearHelperText.setId("clear-helper-text-button");

        NativeButton clearHelperComponent = new NativeButton(
                "clear helper component",
                e -> groupWitHelperComponent.setHelperComponent(null));
        clearHelperComponent.setId("clear-helper-component-button");

        addCard("Helper text", groupWitHelperText, groupWitHelperComponent,
                clearHelperText, clearHelperComponent);
    }

    private void addBasicFeatures() {
        Div message = new Div();

        RadioButtonGroup<String> group = new RadioButtonGroup<>("label",
                event -> message.setText(String.format(
                        "Radio button group value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())),
                "foo", "bar", "baz");

        group.setId("button-group-with-value-change-listener");
        message.setId("button-group-value");

        addCard("Basic radio button group", group, message);
    }

    private void addItemRenderer() {
        Div message = new Div();

        RadioButtonGroup<Person> group = new RadioButtonGroup<>();
        group.setItems(new Person(1, "Joe"), new Person(2, "John"),
                new Person(3, "Bill"));
        group.setRenderer(new ComponentRenderer<>(person -> new Anchor(
                "http://example.com/" + person.getId(), person.getName())));
        group.addValueChangeListener(event -> message.setText(String.format(
                "Radio button group value changed from '%s' to '%s'",
                getName(event.getOldValue()), getName(event.getValue()))));

        group.setId("button-group-renderer");
        message.setId("button-group-renderer-value");

        addCard("Radio button group with renderer", group, message);
    }

    private void addItemLabelGenerator() {
        Div message = new Div();

        RadioButtonGroup<Person> group = new RadioButtonGroup<>();
        group.setItems(new Person("Joe"), new Person("John"),
                new Person("Bill"));
        group.setItemLabelGenerator(Person::getName);
        group.addValueChangeListener(event -> message.setText(String.format(
                "Radio button group value changed from '%s' to '%s'",
                getName(event.getOldValue()), getName(event.getValue()))));

        group.setId("button-group-with-item-generator");
        message.setId("button-group-gen-value");

        addCard("Radio button group with label generator", group, message);
    }

    private void addItemIconGenerator() {
        RadioButtonGroup<Person> group = new RadioButtonGroup<>();
        group.setItems(new Person(1, "Joe"), new Person(2, "John"),
                new Person(3, "Bill"));
        group.setRenderer(new IconRenderer<>(item -> {
            Image image = new Image("https://vaadin.com/images/vaadin-logo.svg",
                    "");
            image.getStyle().set("height", "15px");
            image.getStyle().set("float", "left");
            image.getStyle().set("marginRight", "5px");
            image.getStyle().set("marginTop", "2px");
            return image;
        }, Person::getName));

        group.setId("button-group-icon-generator");

        addCard("Radio button group with icon generator", group);
    }

    private void addDisabled() {

        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar", "baz");
        group.setEnabled(false);

        group.setId("button-group-disabled");

        addCard("Disabled radio button group", group);
    }

    private void addReadOnlyGroup() {
        Div valueInfo = new Div();

        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar", "baz");
        group.setReadOnly(true);

        NativeButton button = new NativeButton("Switch read-only state",
                event -> group.setReadOnly(!group.isReadOnly()));
        group.addValueChangeListener(
                event -> valueInfo.setText(group.getValue()));

        group.setId("button-group-read-only");
        valueInfo.setId("selected-value-info");
        button.setId("switch-read-only");

        addCard("Read-only radio button group", group, button, valueInfo);
    }

    private void addDisabledItems() {

        Div valueInfo = new Div();
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar", "baz");
        group.setItemEnabledProvider(item -> !"bar".equals(item));

        group.addValueChangeListener(
                event -> valueInfo.setText(group.getValue()));

        group.setId("button-group-disabled-items");
        valueInfo.setId("button-group-disabled-items-info");

        addCard("Radio button group with item enabled provider", group,
                valueInfo);
    }

    private String getName(Person person) {
        if (person == null) {
            return null;
        }
        return person.getName();
    }

    private Component addCard(String title, Component... components) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
        return layout;
    }

}
