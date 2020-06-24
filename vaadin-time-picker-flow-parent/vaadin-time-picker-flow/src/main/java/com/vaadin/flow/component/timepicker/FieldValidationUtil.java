/*
 * Copyright 2000-2019 Vaadin Ltd.
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
package com.vaadin.flow.component.timepicker;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.page.PendingJavaScriptResult;

/**
 * Utility class for time picker web component to disable client
 * side validation.
 *
 * @author Vaadin Ltd
 */
final class FieldValidationUtil {

    private FieldValidationUtil() {
        // utility class should not be instantiated
    }

    static void disableClientValidation(Component component) {
        // if the component was already attached override the validate()
        overrideClientValidation(component);

        component.addAttachListener(e -> overrideClientValidation(component));
    }

    private static void overrideClientValidation(Component component) {
        PendingJavaScriptResult javaScriptResult =
                component.getElement()
                        .executeJs("$0.validate = function () {return this.checkValidity();}",
                                component.getElement());

        javaScriptResult.then(result -> {
            if (component instanceof HasValidation && ((HasValidation) component).isInvalid()) {
                // By default, the invalid flag is always false when a component is created.
                // However, if the component is populated and validated in the same HTTP request,
                // the server side state may have changed before the JavaScript disabling client
                // side validation was properly executed. This can sometimes lead to a situation
                // side validation was properly executed. This can sometimes lead to a situation
                // where the client side thinks the value is valid (before client side validation
                // was disabled) and the server side thinks the value is invalid. This will lead to
                // strange behavior until the two states are synchronized again. To avoid this, we will
                // explicitly change the client side value if the server side is invalid.
                component.getElement().executeJs("$0.invalid = true",
                        component.getElement());
            }
        });
    }
}