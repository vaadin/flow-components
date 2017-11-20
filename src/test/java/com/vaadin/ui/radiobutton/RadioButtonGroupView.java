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
import com.vaadin.ui.html.Div;

@Route("vaadin-radio-button")
@HtmlImport("bower_components/vaadin-valo-theme/vaadin-radio-button.html")
public class RadioButtonGroupView extends DemoView {

    @Override
    protected void initView() {
        addBasicFeatures();
        addDisabled();
    }

    private void addBasicFeatures() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Basic text area
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar", "baz");
        group.addValueChangeListener(event -> message.setText(
                String.format("Text area value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())));
        // end-source-example

        group.setId("button-group-with-value-change-listener");
        message.setId("button-group-value");

        addCard("Basic text area", group, message);
    }

    private void addDisabled() {

        // begin-source-example
        // source-example-heading: Basic text area
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("foo", "bar", "baz");
        group.setDisabled(true);
        // end-source-example

        group.setId("button-group-disabled");

        addCard("Basic text area", group);
    }

}
