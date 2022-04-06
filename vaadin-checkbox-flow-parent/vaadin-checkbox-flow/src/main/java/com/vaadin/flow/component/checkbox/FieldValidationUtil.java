/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.checkbox;

class FieldValidationUtil {
    private FieldValidationUtil() {
    }

    static <T> void disableClientValidation(CheckboxGroup<T> component) {
        // Since this method should be called for every time when the component
        // is attached to the UI, lets check that it is actually so
        if (!component.isAttached()) {
            throw new IllegalStateException(String.format(
                    "Component %s is not attached. Client side validation "
                            + "should be disabled when the component is "
                            + "attached, thus this method needs to be called "
                            + "from the onAttach method of the component.",
                    component));
        }
        // Wait until the response is being written as the validation state
        // should not change after that
        component.getUI().ifPresent(ui -> ui.beforeClientResponse(component,
                executionContext -> overrideClientValidation(component)));
    }

    private static <T> void overrideClientValidation(
            CheckboxGroup<T> component) {
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