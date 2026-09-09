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
package com.vaadin.flow.component.splitlayout;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The internationalization properties for {@link SplitLayout}. This can be used
 * to customize and translate the accessible labels used in the split layout
 * component.
 *
 * @see SplitLayout#setI18n(SplitLayoutI18n)
 *
 * @author Vaadin Ltd.
 * @since 25.3
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SplitLayoutI18n implements Serializable {
    private String separator;

    /**
     * Gets the accessible label for the splitter that separates the two content
     * areas.
     *
     * @return the accessible label for the splitter
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Sets the accessible label for the splitter that separates the two content
     * areas.
     * <p>
     * This label is used as the {@code aria-label} of the splitter, announced
     * by screen readers when the splitter is focused.
     *
     * @param separator
     *            the accessible label for the splitter
     * @return this instance for method chaining
     */
    public SplitLayoutI18n setSeparator(String separator) {
        this.separator = separator;
        return this;
    }
}
