/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.HasElement;

/**
 * Mixin interface for components that have an internal overlay and support
 * setting CSS class names on it for styling.
 *
 * @author Vaadin Ltd
 */
public interface HasOverlayClassName extends HasElement {

    /**
     * A space-delimited list of CSS class names to set on the overlay element.
     *
     * @return the {@code overlayClass} property
     */
    default String getOverlayClassName() {
        return getElement().getProperty("overlayClass", "");
    }

    /**
     * Sets a space-delimited list of CSS class names to set on the overlay
     * element.
     *
     * @param overlayClassName
     *            The overlay class name to set
     */
    default void setOverlayClassName(String overlayClassName) {
        getElement().setProperty("overlayClass",
                overlayClassName == null ? "" : overlayClassName);
    }
}
