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
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.StateNode;
import elemental.json.JsonValue;

/**
 * Utility class for text field mixin web components to disable/enable client
 * side validation.
 *
 * @author Vaadin Ltd
 */
final class RequiredValidationUtil {

    RequiredValidationUtil() {
        // utility class should not be instantiated
    }

    static void attachConnector(Component component) {
        execJS(component, "window.Vaadin.Flow.textConnector = {\n"
                + "        disableClientValidation: function (textComponent){\n"
                + "            if ( typeof textComponent.$checkValidity == 'undefined'){\n"
                + "                textComponent.$checkValidity = textComponent.checkValidity;\n"
                + "                textComponent.checkValidity = function() { return true; };\n"
                + "            }\n  "
                + "            if ( typeof textComponent.$validate == 'undefined'){\n"
                + "                textComponent.$validate = textComponent.validate;\n"
                + "                textComponent.validate = function() { return true; };\n"
                + "            }\n  },\n"
                + "        enableClientValidation: function (textComponent){\n"
                + "            if ( textComponent.$checkValidity ){\n"
                + "                textComponent.checkValidity = textComponent.$checkValidity;\n"
                + "                delete textComponent.$checkValidity;\n"
                + "            }\n  "
                + "            if ( textComponent.$validate ){\n"
                + "                textComponent.validate = textComponent.$validate;\n"
                + "                delete textComponent.$validate;\n"
                + "            }\n  }\n }", null);
    }

    static void updateClientValidation(boolean requiredIndicatorVisible,
                                       Component component) {
        if (requiredIndicatorVisible) {
            disableClientValiation(component, result -> {
                if (component instanceof HasValidation && ((HasValidation) component).isInvalid()) {
                    // By default, the invalid flag is always false when a component is created.
                    // However, if the component is populated and validated in the same HTTP request,
                    // the server side state may have changed before the JavaScript disabling client
                    // side validation was properly executed. This can sometimes lead to a situation
                    // where the client side thinks the value is valid (before client side validation
                    // was disabled) and the server side thinks the value is invalid. This will lead to
                    // strange behavior until the two states are synchronized again. To avoid this, we will
                    // explicitly change the client side value if the server side is invalid.
                    component.getUI().ifPresent(ui -> ui.getPage().executeJs("$0.invalid = true",
                            component.getElement()));
                }
            });
        } else {
            enableClientValiation(component);
        }
    }

    static void disableClientValiation(Component component, SerializableConsumer<JsonValue> resultHandler) {
        execJS(component,
                "window.Vaadin.Flow.textConnector.disableClientValidation($0);", resultHandler);
    }

    static void enableClientValiation(Component component) {
        execJS(component,
                "window.Vaadin.Flow.textConnector.enableClientValidation($0);", null);
    }

    private static void execJS(Component component, String js, SerializableConsumer<JsonValue> resultHandler) {
        StateNode node = component.getElement().getNode();

        node.runWhenAttached(ui -> ui.getInternals().getStateTree()
                .beforeClientResponse(node, context -> {
                            PendingJavaScriptResult javaScriptResult = ui.getPage().executeJs(js, component.getElement());
                            if (resultHandler != null) {
                                javaScriptResult.then(resultHandler);
                            }
                        }
                ));
    }

}
