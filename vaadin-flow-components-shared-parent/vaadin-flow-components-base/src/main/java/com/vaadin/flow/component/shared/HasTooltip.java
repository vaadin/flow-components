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
 * Mixin interface for components that support a tooltip.
 *
 * @author Vaadin Ltd
 */
public interface HasTooltip extends HasElement {

    /**
     * Sets a tooltip text for the component.
     *
     * @param text
     *            The tooltip text
     *
     * @return the tooltip handle
     */
    default Tooltip setTooltipText(String text) {
        var tooltip = Tooltip.getForElement(getElement());
        if (tooltip == null) {
            tooltip = Tooltip.forHasTooltip(this);
        }
        tooltip.setText(text);
        return tooltip;
    }

    /**
     * Gets the tooltip handle of the component.
     *
     * @return the tooltip handle
     */
    default Tooltip getTooltip() {
        var tooltip = Tooltip.getForElement(getElement());
        if (tooltip == null) {
            tooltip = setTooltipText(null);
        }
        return tooltip;
    }

}
