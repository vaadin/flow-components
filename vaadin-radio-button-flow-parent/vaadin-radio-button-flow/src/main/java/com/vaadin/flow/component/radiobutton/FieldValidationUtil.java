/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.radiobutton;

import com.vaadin.flow.internal.StateNode;

class FieldValidationUtil {
    private FieldValidationUtil() {

    }

    static <T> void disableClientValidation(RadioButtonGroup<T> component) {
        // Since this method should be called for every time when the component
        // is attached to the UI, lets check that it is actually so
        if (!component.getElement().getNode().isAttached()) {
            throw new IllegalStateException(String.format(
                    "Component %s is not attached. Client side validation "
                            + "should be disabled when the component is "
                            + "attached, thus this method needs to be called "
                            + "from the onAttach method of the component.",
                    component.toString()));
        }
        // Wait until the response is being written as the validation state
        // should not change after that
        final StateNode componentNode = component.getElement().getNode();
        componentNode.runWhenAttached(ui -> ui.getInternals().getStateTree()
                .beforeClientResponse(componentNode,
                        executionContext -> overrideClientValidation(
                                component)));
    }

    private static <T> void overrideClientValidation(
            RadioButtonGroup<T> component) {
        StringBuilder expression = new StringBuilder(
                "this.validate = function () {return this.checkValidity();};");

        if (component.isInvalid()) {
            /*
             * By default the invalid flag is set to false. Workaround the case
             * where the client side validation overrides the invalid state
             * before the validation function itself is overridden above.
             */
            expression.append("this.invalid = true;");
        }
        component.getElement().executeJs(expression.toString());
    }
}