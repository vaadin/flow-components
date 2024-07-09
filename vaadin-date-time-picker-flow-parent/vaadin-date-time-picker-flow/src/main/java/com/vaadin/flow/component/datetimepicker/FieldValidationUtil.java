/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.internal.StateNode;

/**
 * Utility class for date time picker component to disable client side
 * validation.
 *
 * @author Vaadin Ltd
 */
final class FieldValidationUtil {

    private FieldValidationUtil() {
        // utility class should not be instantiated
    }

    static void disableClientValidation(DateTimePicker component) {
        // Since this method should be called for every time when the component
        // is attached to the UI, lets check that it is actually so
        if (!component.getElement().getNode().isAttached()) {
            throw new IllegalStateException(String.format(
                    "Component %s is not attached. Client side "
                            + "validation can only be disabled for a component "
                            + "when it has been attached to the UI and because "
                            + "it should be called again once the component is "
                            + "removed/added, you should call this method from "
                            + "the onAttach() method of the component.",
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

    private static void overrideClientValidation(DateTimePicker component) {
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
