/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.util.Optional;

import com.vaadin.flow.component.HasElement;

/**
 * Mixin interface for components that support setting an ARIA role.
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public interface HasAriaRole extends HasElement {

    /**
     * Sets the ARIA role of the component.
     *
     * @param role
     *            the ARIA role, or {@code null} to clear
     */
    default void setAriaRole(String role) {
        if (role == null) {
            getElement().removeAttribute("role");
        } else {
            getElement().setAttribute("role", role);
        }
    }

    /**
     * Gets the ARIA role of the component.
     *
     * @return an {@code Optional} containing the ARIA role, or an empty
     *         {@code Optional} if none is set
     */
    default Optional<String> getAriaRole() {
        return Optional.ofNullable(getElement().getAttribute("role"));
    }
}
