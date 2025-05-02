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
package com.vaadin.flow.component.checkbox.tests;

import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.router.Route;

/**
 * View for {@link CheckboxGroup} integration tests.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-checkbox-group-test-demo")
public class CheckboxGroupDemoPage extends Div {

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

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return String.valueOf(name);
        }
    }

    public CheckboxGroupDemoPage() {
        addBasicFeatures();
        addItemLabelGenerator();
        addDisabled();
        addDisabledItems();
        addReadOnlyGroup();
        addHelperCheckboxGroup();
        addItemIconRenderer();
    }

    private void addBasicFeatures() {
        Div message = new Div();

        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setItems("foo", "bar", "baz");
        group.addValueChangeListener(event -> message.setText(String.format(
                "Checkbox group value changed from '%s' to '%s'",
                toString(event.getOldValue()), toString(event.getValue()))));

        group.setId("checkbox-group-with-value-change-listener");
        message.setId("checkbox-group-value");

        addCard("Basic checkbox group", group, message);
    }

    private void addItemLabelGenerator() {
        Div message = new Div();

        CheckboxGroup<Person> group = new CheckboxGroup<>();
        group.setItems(new Person("Joe"), new Person("John"),
                new Person("Bill"));
        group.setItemLabelGenerator(Person::getName);
        group.addValueChangeListener(event -> message.setText(String.format(
                "Checkbox group value changed from '%s' to '%s'",
                getNames(event.getOldValue()), getNames(event.getValue()))));

        group.setId("checkbox-group-with-item-generator");
        message.setId("checkbox-group-gen-value");

        addCard("Checkbox group with label generator", group, message);
    }

    private void addDisabled() {

        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setItems("foo", "bar", "baz");
        group.setEnabled(false);

        group.setId("checkbox-group-disabled");

        addCard("Disabled checkbox group", group);
    }

    private void addDisabledItems() {

        Div valueInfo = new Div();
        // provider
        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setItems("foo", "bar", "baz");
        group.setItemEnabledProvider(item -> !"bar".equals(item));

        group.addValueChangeListener(
                event -> valueInfo.setText(toString(group.getValue())));

        group.setId("checkbox-group-disabled-items");
        valueInfo.setId("checkbox-group-disabled-items-info");

        addCard("Checkbox group with item enabled provider", group, valueInfo);
    }

    private void addReadOnlyGroup() {
        Div valueInfo = new Div();

        CheckboxGroup<String> group = new CheckboxGroup<>();
        group.setItems("foo", "bar", "baz");
        group.setReadOnly(true);

        NativeButton button = new NativeButton("Switch read-only state",
                event -> group.setReadOnly(!group.isReadOnly()));
        group.addValueChangeListener(
                event -> valueInfo.setText(toString(group.getValue())));

        group.setId("checkbox-group-read-only");
        valueInfo.setId("selected-value-info");
        button.setId("switch-read-only");

        addCard("Read-only checkbox group", group, button, valueInfo);
    }

    private void addHelperCheckboxGroup() {
        // component
        CheckboxGroup<String> groupHelperText = new CheckboxGroup<>();
        groupHelperText.setItems("foo", "bar", "baz");
        groupHelperText.setHelperText("Helper text");

        NativeButton clearHelper = new NativeButton("Clear helper text", e -> {
            groupHelperText.setHelperText(null);
        });

        CheckboxGroup<String> groupHelperComponent = new CheckboxGroup<>();
        groupHelperComponent.setItems("foo", "bar", "baz");
        Span span = new Span("Helper text");
        span.setId("helper-component");
        groupHelperComponent.setHelperComponent(span);

        NativeButton clearHelperComponent = new NativeButton(
                "Clear helper text", e -> {
                    groupHelperComponent.setHelperComponent(null);
                });

        groupHelperText.setId("checkbox-helper-text");
        groupHelperComponent.setId("checkbox-helper-component");
        span.setId("component-helper");
        clearHelper.setId("button-clear-helper");
        clearHelperComponent.setId("button-clear-component");

        addCard("CheckboxGroup with helper text and helper component",
                groupHelperText, clearHelper, groupHelperComponent,
                clearHelperComponent);
    }

    private void addItemIconRenderer() {
        CheckboxGroup<Person> group = new CheckboxGroup<>();
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

        group.setId("checkbox-group-icon-renderer");

        addCard("Checkbox group with icon renderer", group);
    }

    private String toString(Set<String> value) {
        return value.stream().sorted().collect(Collectors.toList()).toString();
    }

    private String getNames(Set<Person> persons) {
        return persons.stream().map(Person::getName).sorted()
                .collect(Collectors.toList()).toString();
    }

    private void addCard(String title, Component... components) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }

}
