/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;

/**
 * A utility class that allows preventing the web component from setting itself
 * to valid as a result of client-side validation.
 *
 * @author Vaadin Ltd
 */
public final class ClientValidationUtil {

    private ClientValidationUtil() {
        // utility class should not be instantiated
    }

    public static <C extends Component & HasValidation> void preventWebComponentFromModifyingInvalidState(
            C component) {
        StringBuilder expression = new StringBuilder(
                "this._shouldSetInvalid = function (invalid) { return false };");

        if (component.isInvalid()) {
            /*
             * By default the invalid flag is set to false. Workaround the case
             * where the client side validation overrides the invalid state
             * before the `_shouldSetInvalid` method is overridden above.
             */
            expression.append("this.invalid = true;");
        }

        component.getElement().executeJs(expression.toString());
    }
}
