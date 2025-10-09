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
package com.vaadin.flow.component.stepper.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * TestBench element for the {@code <vaadin-step>} component.
 *
 * @author Vaadin Ltd.
 */
@Element("vaadin-step")
public class StepElement extends TestBenchElement {

    /**
     * Gets the href of this step.
     *
     * @return the href, or {@code null} if not set
     */
    public String getHref() {
        return getPropertyString("href");
    }

    /**
     * Gets the target of this step.
     *
     * @return the target, or {@code null} if not set
     */
    public String getTarget() {
        return getPropertyString("target");
    }

    /**
     * Gets the label of this step.
     *
     * @return the label text
     */
    public String getLabel() {
        return getPropertyString("label");
    }

    /**
     * Gets the description of this step.
     *
     * @return the description text
     */
    public String getDescription() {
        return getPropertyString("description");
    }

    /**
     * Gets the state of this step.
     *
     * @return the state ("active", "completed", "error", or "inactive")
     */
    public String getState() {
        return getPropertyString("state");
    }

    /**
     * Checks if this step is marked as the current page.
     *
     * @return {@code true} if this is the current page, {@code false}
     *         otherwise
     */
    public boolean isCurrent() {
        return hasAttribute("current");
    }

    /**
     * Checks if this step is disabled.
     *
     * @return {@code true} if disabled, {@code false} otherwise
     */
    public boolean isDisabled() {
        return hasAttribute("disabled");
    }

    /**
     * Checks if this step should be ignored by client-side routers.
     *
     * @return {@code true} if router should ignore this step, {@code false}
     *         otherwise
     */
    public boolean isRouterIgnore() {
        return getPropertyBoolean("routerIgnore");
    }

    /**
     * Checks if this step is in active state.
     *
     * @return {@code true} if the step is active, {@code false} otherwise
     */
    public boolean isActive() {
        return "active".equals(getState());
    }

    /**
     * Checks if this step is in completed state.
     *
     * @return {@code true} if the step is completed, {@code false} otherwise
     */
    public boolean isCompleted() {
        return "completed".equals(getState());
    }

    /**
     * Checks if this step is in error state.
     *
     * @return {@code true} if the step is in error state, {@code false}
     *         otherwise
     */
    public boolean isError() {
        return "error".equals(getState());
    }

    /**
     * Checks if this step is in inactive state.
     *
     * @return {@code true} if the step is inactive, {@code false} otherwise
     */
    public boolean isInactive() {
        return "inactive".equals(getState());
    }

    /**
     * Gets the tooltip text of this step.
     *
     * @return the tooltip text, or {@code null} if not set
     */
    public String getTooltipText() {
        return getAttribute("title");
    }

    /**
     * Clicks on the indicator part of this step. This will navigate to the
     * href if one is set and the step is not disabled.
     */
    public void clickIndicator() {
        // Click on the indicator part in the shadow DOM
        getCommandExecutor().executeScript(
                "arguments[0].shadowRoot.querySelector('[part=\"indicator\"]').click();",
                this);
    }

    /**
     * Checks if this step has a connector line to the next step.
     *
     * @return {@code true} if a connector is visible, {@code false} otherwise
     */
    public boolean hasConnector() {
        return (Boolean) getCommandExecutor().executeScript(
                "return !!arguments[0].shadowRoot.querySelector('[part=\"connector\"]');",
                this);
    }

    /**
     * Gets the text content of the step indicator (usually a number or icon).
     *
     * @return the indicator text content
     */
    public String getIndicatorText() {
        return (String) getCommandExecutor().executeScript(
                "const indicator = arguments[0].shadowRoot.querySelector('[part=\"indicator\"]');"
                        + "return indicator ? indicator.textContent.trim() : '';",
                this);
    }

    /**
     * Checks if the step indicator shows a checkmark (completed state).
     *
     * @return {@code true} if checkmark is visible, {@code false} otherwise
     */
    public boolean hasCheckmark() {
        return (Boolean) getCommandExecutor().executeScript(
                "const indicator = arguments[0].shadowRoot.querySelector('[part=\"indicator\"]');"
                        + "return indicator && indicator.textContent.includes('✓');",
                this);
    }

    /**
     * Checks if the step indicator shows an error mark (error state).
     *
     * @return {@code true} if error mark is visible, {@code false} otherwise
     */
    public boolean hasErrorMark() {
        return (Boolean) getCommandExecutor().executeScript(
                "const indicator = arguments[0].shadowRoot.querySelector('[part=\"indicator\"]');"
                        + "return indicator && indicator.textContent.includes('!');",
                this);
    }
}