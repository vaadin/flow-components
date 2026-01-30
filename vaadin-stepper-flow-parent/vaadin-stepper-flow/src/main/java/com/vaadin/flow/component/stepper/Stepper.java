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
package com.vaadin.flow.component.stepper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.PropertyDescriptor;
import com.vaadin.flow.component.PropertyDescriptors;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.dom.Element;

/**
 * Stepper is a navigation component that displays progress through a sequence
 * of logical steps. It guides users through a step-by-step process and allows
 * navigation between steps.
 * <p>
 * Use Stepper when you want to break a complex process into smaller, manageable
 * steps and show the user their progress through the process.
 * <p>
 * {@link Step} components can be added to this component with the
 * {@link #add(Step...)} method or the {@link #Stepper(Step...)} constructor.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-stepper")
@JsModule("@vaadin/stepper/src/vaadin-stepper.js")
@NpmPackage(value = "@vaadin/stepper", version = "25.0.0-dev")
public class Stepper extends Component
        implements HasSize, HasStyle, HasThemeVariant<StepperVariant> {

    private static final PropertyDescriptor<String, String> orientationDescriptor = PropertyDescriptors
            .propertyWithDefault("orientation", Orientation.VERTICAL.getValue());

    /**
     * The valid orientations for {@link Stepper} instances.
     */
    public enum Orientation {
        HORIZONTAL("horizontal"), VERTICAL("vertical");

        private final String value;

        Orientation(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Constructs an empty stepper component with vertical orientation.
     */
    public Stepper() {
        super();
    }

    /**
     * Constructs a stepper component with the given steps.
     *
     * @param steps
     *            the steps to add
     */
    public Stepper(Step... steps) {
        this();
        add(steps);
    }

    /**
     * Gets the orientation of the stepper.
     *
     * @return the orientation
     */
    public Orientation getOrientation() {
        String value = get(orientationDescriptor);
        for (Orientation orientation : Orientation.values()) {
            if (orientation.getValue().equals(value)) {
                return orientation;
            }
        }
        return Orientation.VERTICAL;
    }

    /**
     * Sets the orientation of the stepper.
     *
     * @param orientation
     *            the orientation to set
     */
    public void setOrientation(Orientation orientation) {
        Objects.requireNonNull(orientation, "Orientation cannot be null");
        set(orientationDescriptor, orientation.getValue());
    }

    /**
     * Adds the given steps to the component.
     *
     * @param steps
     *            the steps to add, not {@code null}
     */
    public void add(Step... steps) {
        Objects.requireNonNull(steps, "Steps to add cannot be null");
        Arrays.stream(steps).map(step -> Objects.requireNonNull(step,
                "Individual step to add cannot be null"))
                .map(Step::getElement)
                .forEach(getElement()::appendChild);
    }

    /**
     * Removes the given steps from the component.
     *
     * @param steps
     *            the steps to remove, not {@code null}
     */
    public void remove(Step... steps) {
        Objects.requireNonNull(steps, "Steps to remove cannot be null");
        Arrays.stream(steps).map(step -> Objects.requireNonNull(step,
                "Individual step to remove cannot be null"))
                .map(Step::getElement)
                .forEach(getElement()::removeChild);
    }

    /**
     * Removes all steps from the component.
     */
    public void removeAll() {
        getElement().removeAllChildren();
    }

    /**
     * Gets the step at the given index.
     *
     * @param index
     *            the index of the step to get
     * @return the step at the given index
     * @throws IndexOutOfBoundsException
     *             if the index is out of range
     */
    public Step getStepAt(int index) {
        if (index < 0 || index >= getStepCount()) {
            throw new IndexOutOfBoundsException("Index: " + index
                    + ", Size: " + getStepCount());
        }
        Iterator<Step> iterator = getSteps().iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    /**
     * Gets the number of steps in the component.
     *
     * @return the number of steps
     */
    public int getStepCount() {
        return (int) getSteps().count();
    }

    /**
     * Gets the index of the given step.
     *
     * @param step
     *            the step to get the index of
     * @return the index of the step, or -1 if not found
     */
    public int indexOf(Step step) {
        if (step == null) {
            return -1;
        }
        Iterator<Step> iterator = getSteps().iterator();
        int index = 0;
        while (iterator.hasNext()) {
            if (iterator.next().equals(step)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Replaces the step at the given index with a new step.
     *
     * @param index
     *            the index of the step to replace
     * @param newStep
     *            the new step to set, not {@code null}
     * @throws IndexOutOfBoundsException
     *             if the index is out of range
     */
    public void replace(int index, Step newStep) {
        Objects.requireNonNull(newStep, "New step cannot be null");
        if (index < 0 || index >= getStepCount()) {
            throw new IndexOutOfBoundsException("Index: " + index
                    + ", Size: " + getStepCount());
        }

        Step oldStep = getStepAt(index);
        Element parentElement = getElement();
        List<Element> children = new ArrayList<>();
        parentElement.getChildren().forEach(children::add);

        int elementIndex = children.indexOf(oldStep.getElement());
        if (elementIndex >= 0) {
            parentElement.insertChild(elementIndex, newStep.getElement());
            parentElement.removeChild(oldStep.getElement());
        }
    }

    /**
     * Gets all steps in the component as a stream.
     *
     * @return a stream of all steps
     */
    public Stream<Step> getSteps() {
        return getElement().getChildren()
                .filter(element -> element.getComponent()
                        .filter(Step.class::isInstance).isPresent())
                .map(element -> element.getComponent().map(Step.class::cast)
                        .get());
    }

    /**
     * Gets the currently active step.
     *
     * @return the active step, or {@code null} if no step is active
     */
    public Step getActiveStep() {
        return getSteps().filter(Step::isActive).findFirst().orElse(null);
    }

    /**
     * Sets the state of a specific step by index.
     *
     * @param state
     *            the state to set
     * @param stepIndex
     *            the index of the step
     */
    public void setStepState(Step.State state, int stepIndex) {
        Objects.requireNonNull(state, "State cannot be null");
        if (stepIndex >= 0 && stepIndex < getStepCount()) {
            getStepAt(stepIndex).setState(state);
        }
    }

    /**
     * Marks steps as completed up to the specified index (inclusive).
     *
     * @param untilIndex
     *            the index up to which steps should be marked as completed
     */
    public void completeStepsUntil(int untilIndex) {
        if (untilIndex < 0) {
            return;
        }
        int count = getStepCount();
        for (int i = 0; i <= untilIndex && i < count; i++) {
            getStepAt(i).setState(Step.State.COMPLETED);
        }
    }

    /**
     * Resets all steps to inactive state.
     */
    public void reset() {
        getSteps().forEach(step -> step.setState(Step.State.INACTIVE));
    }
}