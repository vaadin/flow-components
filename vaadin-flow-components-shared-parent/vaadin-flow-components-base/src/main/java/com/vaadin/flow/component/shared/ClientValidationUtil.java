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
package com.vaadin.flow.component.shared;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValidation;

/**
 * A utility class that allows preventing the web component from setting itself
 * to valid as a result of client-side validation.
 *
 * @author Vaadin Ltd
 * @deprecated since 24.8, use
 *             {@code getElement().setProperty("manualValidation", true)}
 *             instead.
 */
@Deprecated
public final class ClientValidationUtil {

    private ClientValidationUtil() {
        // utility class should not be instantiated
    }

    /**
     * @deprecated since 24.8, use
     *             {@code getElement().setProperty("manualValidation", true)}
     *             instead.
     */
    @Deprecated
    public static <C extends Component & HasValidation> void preventWebComponentFromModifyingInvalidState(
            C component) {
        StringBuilder expression = new StringBuilder(
                "this._shouldSetInvalid = function (invalid) { return false };");

        /*
         * Workaround the case where the client side validation overrides the
         * invalid state before the `_shouldSetInvalid` method is overridden
         * above.
         */
        expression.append("this.invalid = ").append(component.isInvalid())
                .append(";");

        component.getElement().executeJs(expression.toString());
    }
}
