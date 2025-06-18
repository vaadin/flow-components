/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
