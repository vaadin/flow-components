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
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.HasElement;

/**
 * Mixin interface for fields with {@code autocorrect} attribute.
 */
public interface HasAutocorrect extends HasElement {

    /**
     * Name of {@code autocorrect} attribute.
     */
    String AUTOCORRECT_ATTRIBUTE = "autocorrect";

    /**
     * Enable or disable {@code autocorrect} for the field.
     * <p>
     * If not set, devices may apply their own defaults.
     * <p>
     * <em>Note:</em> <a href=
     * "https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input#attr-autocorrect">This
     * only supported by Safari</a>.
     *
     * @param autocorrect
     *            true to enable {@code autocorrect}, false to disable
     */
    default void setAutocorrect(boolean autocorrect) {
        if (autocorrect) {
            getElement().setAttribute(AUTOCORRECT_ATTRIBUTE, "on");
        } else {
            getElement().setAttribute(AUTOCORRECT_ATTRIBUTE, "off");
        }
    }

    /**
     * Checks if the field has {@code autocorrect} enabled.
     *
     * @return true if the field has {@code autocorrect} enabled
     */
    default boolean isAutocorrect() {
        String autocorrect = getElement().getAttribute(AUTOCORRECT_ATTRIBUTE);
        if (autocorrect == null || "".equals(autocorrect)) {
            return false;
        } else {
            return "on".equals(autocorrect);
        }
    }
}
