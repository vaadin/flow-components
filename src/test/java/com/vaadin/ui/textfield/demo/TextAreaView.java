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
package com.vaadin.ui.textfield.demo;

import com.vaadin.flow.demo.DemoView;
import com.vaadin.router.Route;
import com.vaadin.ui.common.HtmlImport;
import com.vaadin.ui.html.Div;
import com.vaadin.ui.textfield.GeneratedVaadinTextArea;
import com.vaadin.ui.textfield.TextArea;

/**
 * View for {@link GeneratedVaadinTextArea} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-text-area")
@HtmlImport("bower_components/vaadin-valo-theme/vaadin-text-field.html")
public class TextAreaView extends DemoView {

    @Override
    public void initView() {
        addBasicFeatures();
        addMaxHeightFeature();
        addMinHeightFeature();
    }

    private void addMaxHeightFeature() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Text area with max-height
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area growing stops at 125px");
        textArea.getStyle().set("maxHeight", "125px");
        // end-source-example

        textArea.setId("text-area-with-max-height");

        addCard("Text area with max-height", textArea, message);
    }

    private void addMinHeightFeature() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Text area with min-height
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area won't shrink under 125px");
        textArea.getStyle().set("minHeight", "125px");
        // end-source-example

        textArea.setId("text-area-with-min-height");

        addCard("Text area with min-height", textArea, message);
    }

    private void addBasicFeatures() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Basic text area
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area label");
        textArea.setPlaceholder("placeholder text");
        textArea.addValueChangeListener(event -> message.setText(
                String.format("Text area value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())));
        // end-source-example

        textArea.setId("text-area-with-value-change-listener");
        message.setId("text-area-value");

        addCard("Basic text area", textArea, message);
    }
}
