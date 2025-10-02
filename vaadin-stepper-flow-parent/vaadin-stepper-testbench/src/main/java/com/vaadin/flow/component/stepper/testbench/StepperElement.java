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

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * TestBench element for the {@code <vaadin-stepper>} component.
 *
 * @author Vaadin Ltd.
 */
@Element("vaadin-stepper")
public class StepperElement extends TestBenchElement {

    /**
     * Gets all steps in this stepper.
     *
     * @return a list of all steps
     */
    public List<StepElement> getSteps() {
        return $(StepElement.class).all();
    }

    /**
     * Gets the step at the given index.
     *
     * @param index
     *            the index of the step to get
     * @return the step at the given index
     */
    public StepElement getStep(int index) {
        return $(StepElement.class).get(index);
    }

    /**
     * Gets the number of steps.
     *
     * @return the number of steps
     */
    public int getStepCount() {
        return getSteps().size();
    }

    /**
     * Clicks on the step at the given index.
     *
     * @param index
     *            the index of the step to click
     */
    public void clickStep(int index) {
        getStep(index).click();
    }

    /**
     * Gets the label of the step at the given index.
     *
     * @param index
     *            the index of the step
     * @return the label of the step
     */
    public String getStepLabel(int index) {
        return getStep(index).getLabel();
    }

    /**
     * Gets the description of the step at the given index.
     *
     * @param index
     *            the index of the step
     * @return the description of the step
     */
    public String getStepDescription(int index) {
        return getStep(index).getDescription();
    }

    /**
     * Gets the state of the step at the given index.
     *
     * @param index
     *            the index of the step
     * @return the state of the step
     */
    public String getStepState(int index) {
        return getStep(index).getState();
    }

    /**
     * Gets the orientation of the stepper.
     *
     * @return the orientation ("horizontal" or "vertical")
     */
    public String getOrientation() {
        return getPropertyString("orientation");
    }

    /**
     * Checks if the stepper has the given theme variant.
     *
     * @param variant
     *            the theme variant to check
     * @return {@code true} if the stepper has the variant, {@code false}
     *         otherwise
     */
    public boolean hasThemeVariant(String variant) {
        String theme = getAttribute("theme");
        return theme != null && theme.contains(variant);
    }

    /**
     * Gets the currently active step.
     *
     * @return the active step, or {@code null} if no step is active
     */
    public StepElement getActiveStep() {
        return getSteps().stream().filter(StepElement::isActive).findFirst()
                .orElse(null);
    }

    /**
     * Gets all completed steps.
     *
     * @return a list of completed steps
     */
    public List<StepElement> getCompletedSteps() {
        return getSteps().stream().filter(StepElement::isCompleted).toList();
    }

    /**
     * Gets all steps in error state.
     *
     * @return a list of steps in error state
     */
    public List<StepElement> getErrorSteps() {
        return getSteps().stream().filter(StepElement::isError).toList();
    }
}