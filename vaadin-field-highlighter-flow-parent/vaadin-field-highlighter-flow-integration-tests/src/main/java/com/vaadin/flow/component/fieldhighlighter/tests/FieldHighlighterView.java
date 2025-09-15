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
package com.vaadin.flow.component.fieldhighlighter.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.fieldhighlighter.FieldHighlighterInitializer;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-field-highlighter")
public class FieldHighlighterView extends Div {

    static class FieldHighlighter extends FieldHighlighterInitializer {
        public void initialize(Element field) {
            init(field);
        }
    }

    public FieldHighlighterView() {
        TextField tf = new TextField("TF with field highlighter");
        Button button = new Button("call init");
        button.addClickListener(
                event -> new FieldHighlighter().initialize(tf.getElement()));

        tf.setId("tf-with-highlighter");
        button.setId("call-init");

        add(tf, button);
    }
}
