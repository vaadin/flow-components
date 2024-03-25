/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.fieldhighlighter;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.shared.Registration;

@NpmPackage(value = "@vaadin/field-highlighter", version = "23.4.0")
@JsModule("@vaadin/field-highlighter/src/vaadin-field-highlighter.js")
public class FieldHighlighterInitializer {
    protected static Registration init(Element field) {
        Command initWithJS = () -> field.executeJs(
                "customElements.get('vaadin-field-highlighter').init(this)");
        if (field.getNode().isAttached()) {
            initWithJS.execute();
        }
        return field.addAttachListener(e -> initWithJS.execute());
    }
}
