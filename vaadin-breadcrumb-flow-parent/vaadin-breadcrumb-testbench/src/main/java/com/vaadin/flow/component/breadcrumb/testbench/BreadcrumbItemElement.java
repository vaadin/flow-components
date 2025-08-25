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
package com.vaadin.flow.component.breadcrumb.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * TestBench element for the {@code <vaadin-breadcrumb-item>} component.
 *
 * @author Vaadin Ltd.
 */
@Element("vaadin-breadcrumb-item")
public class BreadcrumbItemElement extends TestBenchElement {

    /**
     * Gets the href of this breadcrumb item.
     *
     * @return the href, or {@code null} if not set
     */
    public String getHref() {
        return getPropertyString("href");
    }

    /**
     * Gets the target of this breadcrumb item.
     *
     * @return the target, or {@code null} if not set
     */
    public String getTarget() {
        return getPropertyString("target");
    }

    /**
     * Checks if this breadcrumb item is marked as the current page.
     *
     * @return {@code true} if this is the current page, {@code false} otherwise
     */
    public boolean isCurrent() {
        return hasAttribute("current");
    }

    /**
     * Checks if this breadcrumb item is the last item in the breadcrumb.
     *
     * @return {@code true} if this is the last item, {@code false} otherwise
     */
    public boolean isLast() {
        return hasAttribute("last");
    }

    /**
     * Checks if this breadcrumb item is disabled.
     *
     * @return {@code true} if disabled, {@code false} otherwise
     */
    public boolean isDisabled() {
        return hasAttribute("disabled");
    }

    /**
     * Checks if this breadcrumb item should be ignored by client-side routers.
     *
     * @return {@code true} if router should ignore this item, {@code false}
     *         otherwise
     */
    public boolean isRouterIgnore() {
        return getPropertyBoolean("routerIgnore");
    }

    /**
     * Gets the tooltip text of this breadcrumb item.
     *
     * @return the tooltip text, or {@code null} if not set
     */
    public String getTooltipText() {
        return getAttribute("title");
    }

    /**
     * Clicks on the link part of this breadcrumb item. This will navigate to
     * the href if one is set and the item is not disabled.
     */
    public void clickLink() {
        // Click on the link part in the shadow DOM
        getCommandExecutor().executeScript(
                "arguments[0].shadowRoot.querySelector('[part=\"link\"]').click();",
                this);
    }

    /**
     * Checks if this breadcrumb item has a separator.
     *
     * @return {@code true} if a separator is visible, {@code false} otherwise
     */
    public boolean hasSeparator() {
        return (Boolean) getCommandExecutor().executeScript(
                "return !!arguments[0].shadowRoot.querySelector('[part=\"separator\"]');",
                this);
    }
}
