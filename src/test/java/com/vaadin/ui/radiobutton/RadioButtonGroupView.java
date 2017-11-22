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
package com.vaadin.ui.radiobutton;

import com.vaadin.flow.demo.DemoView;
import com.vaadin.router.Route;
import com.vaadin.ui.common.HtmlImport;
import com.vaadin.ui.html.Anchor;
import com.vaadin.ui.html.Div;

@Route("vaadin-radio-button")
@HtmlImport("bower_components/vaadin-valo-theme/vaadin-radio-button.html")
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
        addDisabled();
        addDisabledItems();
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

    private void addDisabled() {

        // begin-source-example
        // source-example-heading: Disabled radio button group
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar", "baz");
        group.setDisabled(true);
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

}
