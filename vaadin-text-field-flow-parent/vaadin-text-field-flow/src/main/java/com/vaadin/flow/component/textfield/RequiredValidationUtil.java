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
import com.vaadin.flow.internal.StateNode;

/**
 * Utility class for text field mixin web components to disable/enable client
 * side validation.
 *
 * @author Vaadin Ltd
 *
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
                + "            }\n  }\n }");
    }

    static void updateClientValidation(boolean requiredIndicatorVisible,
            Component component) {
        if (requiredIndicatorVisible) {
            disableClientValiation(component);
        } else {
            enableClientValiation(component);
        }
    }

    static void disableClientValiation(Component component) {
        execJS(component,
                "window.Vaadin.Flow.textConnector.disableClientValidation($0);");
    }

    static void enableClientValiation(Component component) {
        execJS(component,
                "window.Vaadin.Flow.textConnector.enableClientValidation($0);");
    }

    private static void execJS(Component component, String js) {
        StateNode node = component.getElement().getNode();

        node.runWhenAttached(ui -> ui.getInternals().getStateTree()
                .beforeClientResponse(node, context -> ui.getPage()
                        .executeJavaScript(js, component.getElement())));

    }

}
