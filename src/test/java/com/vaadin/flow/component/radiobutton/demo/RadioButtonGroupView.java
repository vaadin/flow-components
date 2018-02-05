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
package com.vaadin.flow.component.radiobutton.demo;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.renderer.IconRenderer;
import com.vaadin.flow.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-radio-button")
public class RadioButtonGroupView extends DemoView {

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

    @Override
    protected void initView() {
        addBasicFeatures();
        addItemRenderer();
        addItemLabelGenerator();
        addItemIconGenerator();
        addDisabled();
        addDisabledItems();
        addComponentAfterItems();
        insertComponentsBetweenItems();
        prependAndInsertComponents();
        dynamicComponents();
    }

    private void addBasicFeatures() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Basic radio button group
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar", "baz");
        group.addValueChangeListener(event -> message.setText(String.format(
                "Radio button group value changed from '%s' to '%s'",
                event.getOldValue(), event.getValue())));
        // end-source-example

        group.setId("button-group-with-value-change-listener");
        message.setId("button-group-value");

        addCard("Basic radio button group", group, message);
    }

    private void addItemRenderer() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Radio button group with renderer
        RadioButtonGroup<Person> group = new RadioButtonGroup<>();
        group.setItems(new Person(1, "Joe"), new Person(2, "John"),
                new Person(3, "Bill"));
        group.setItemRenderer(person -> new Anchor(
                "http://example.com/" + person.getId(), person.getName()));
        group.addValueChangeListener(event -> message.setText(String.format(
                "Radio button group value changed from '%s' to '%s'",
                getName(event.getOldValue()), getName(event.getValue()))));
        // end-source-example

        group.setId("button-group-renderer");
        message.setId("button-group-renderer-value");

        addCard("Radio button group with renderer", group, message);
    }

    private void addItemLabelGenerator() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Radio button group with label generator
        RadioButtonGroup<Person> group = new RadioButtonGroup<>();
        group.setItems(new Person("Joe"), new Person("John"),
                new Person("Bill"));
        group.setItemRenderer(new TextRenderer<>(Person::getName));
        group.addValueChangeListener(event -> message.setText(String.format(
                "Radio button group value changed from '%s' to '%s'",
                getName(event.getOldValue()), getName(event.getValue()))));
        // end-source-example

        group.setId("button-group-with-item-generator");
        message.setId("button-group-gen-value");

        addCard("Radio button group with label generator", group, message);
    }

    private void addItemIconGenerator() {
        // begin-source-example
        // source-example-heading: Radio button group with icon generator
        RadioButtonGroup<Person> group = new RadioButtonGroup<>();
        group.setItems(new Person(1, "Joe"), new Person(2, "John"),
                new Person(3, "Bill"));
        group.setItemRenderer(new IconRenderer<>(item -> {
            Image image = new Image("https://vaadin.com/images/vaadin-logo.svg",
                    "");
            image.getStyle().set("height", "15px");
            image.getStyle().set("float", "left");
            image.getStyle().set("marginRight", "5px");
            image.getStyle().set("marginTop", "2px");
            return image;
        }, Person::getName));
        // end-source-example

        group.setId("button-group-icon-generator");

        addCard("Radio button group with icon generator", group);
    }

    private void addDisabled() {

        // begin-source-example
        // source-example-heading: Disabled radio button group
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar", "baz");
        group.setEnabled(false);
        // end-source-example

        group.setId("button-group-disabled");

        addCard("Disabled radio button group", group);
    }

    private void addDisabledItems() {

        // begin-source-example
        // source-example-heading: Radio button group with item enabled provider
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar", "baz");
        group.setItemEnabledProvider(item -> !"bar".equals(item));
        // end-source-example

        group.setId("button-group-disabled-items");

        addCard("Radio button group with item enabled provider", group);
    }

    private String getName(Person person) {
        if (person == null) {
            return null;
        }
        return person.getName();
    }

    private void addComponentAfterItems() {
        // begin-source-example
        // source-example-heading: Add component to group
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar", "baz");
        group.add(new Label("My Custom text"));

        group.getElement().getStyle().set("display", "flex");
        group.getElement().getStyle().set("flexDirection", "column");
        // end-source-example

        group.setId("button-group-with-appended-text");

        addCard("Add component to group", group);
    }

    private void insertComponentsBetweenItems() {
        // begin-source-example
        // source-example-heading: Insert component after item in group
        RadioButtonGroup<String> group = new RadioButtonGroup<>();

        // Note that setting items clear any components
        group.add(new Label("Foo group"), getFullSizeHr());

        group.setItems("foo", "bar", "baz");
        group.addComponents("foo", new Label("Not foo selections"),
                getFullSizeHr());

        group.getElement().getStyle().set("display", "flex");
        group.getElement().getStyle().set("flexDirection", "column");
        // end-source-example

        group.setId("button-group-with-inserted-component");

        addCard("Insert component after item in group", group);
    }

    private void prependAndInsertComponents() {
        // begin-source-example
        // source-example-heading: Insert components before item in group
        RadioButtonGroup<String> group = new RadioButtonGroup<>();

        group.setItems("foo", "foo-bar", "bar", "bar-foo", "baz", "baz-baz");

        group.prependComponents("foo", new Label("Foo group"), getFullSizeHr());
        group.prependComponents("bar", new Label("Bar group"), getFullSizeHr());
        group.prependComponents("baz", new Label("Baz group"), getFullSizeHr());

        group.getElement().getStyle().set("display", "flex");
        group.getElement().getStyle().set("flexDirection", "column");
        // end-source-example

        group.setId("button-group-with-prepended-component");

        addCard("Insert components before item in group", group);
    }

    private void dynamicComponents() {
        // begin-source-example
        // source-example-heading: Move component in group on selection
        RadioButtonGroup<String> group = new RadioButtonGroup<>();

        group.setItems("foo", "foo-bar", "bar", "bar-foo", "baz", "baz-baz");

        Label below = new Label("= After Selected =");

        group.addValueChangeListener(event -> {
            if (below.getParent().isPresent()) {
                group.remove(below);
            }
            group.addComponents(event.getValue(), below);
        });

        group.getElement().getStyle().set("display", "flex");
        group.getElement().getStyle().set("flexDirection", "column");
        // end-source-example

        group.setId("button-group-with-dynamic-component");

        addCard("Move component in group on selection", group);
    }

    private Hr getFullSizeHr() {
        Hr hr = new Hr();
        hr.setSizeFull();
        return hr;
    }

}
