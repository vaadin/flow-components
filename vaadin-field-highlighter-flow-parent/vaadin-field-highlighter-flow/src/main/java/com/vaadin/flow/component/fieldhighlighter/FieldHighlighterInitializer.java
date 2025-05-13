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
package com.vaadin.flow.component.fieldhighlighter;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.shared.Registration;

@NpmPackage(value = "@vaadin/field-highlighter", version = "24.8.0-alpha18")
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
