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
package com.vaadin.flow.component.slider;

import java.util.Optional;

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Slider is an input field that allows the user to select a numeric value
 * within a range by dragging a handle along a track or using arrow keys for
 * precise input.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-slider")
@NpmPackage(value = "@vaadin/slider", version = "25.1.0-alpha9")
@JsModule("@vaadin/slider/src/vaadin-slider.js")
public class Slider extends SliderBase<Slider, Double> implements HasAriaLabel {

    private static final double DEFAULT_MIN = 0.0;
    private static final double DEFAULT_MAX = 100.0;

    /**
     * Constructs a {@code Slider} with min 0 and max 100. The initial value is
     * 0.
     * <p>
     * The step defaults to 1.
     */
    public Slider() {
        this(DEFAULT_MIN, DEFAULT_MAX);
    }

    /**
     * Constructs a {@code Slider} with the given min and max. The initial value
     * is set to the minimum value.
     * <p>
     * The step defaults to 1.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     */
    public Slider(double min, double max) {
        super(min, max, Double.class, (v) -> v, (v) -> v);
    }

    /**
     * Constructs a {@code Slider} with the given label, min 0, and max 100. The
     * initial value is 0.
     * <p>
     * The step defaults to 1.
     *
     * @param label
     *            the text to set as the label
     */
    public Slider(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs a {@code Slider} with the given label, min and max. The
     * initial value is set to the minimum value.
     * <p>
     * The step defaults to 1.
     *
     * @param label
     *            the text to set as the label
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     */
    public Slider(String label, double min, double max) {
        this(min, max);
        setLabel(label);
    }

    /**
     * Sets an accessible name for the range input element of the slider.
     *
     * @param ariaLabel
     *            the accessible name to set, or {@code null} to remove it
     */
    @Override
    public void setAriaLabel(String ariaLabel) {
        getElement().setProperty("accessibleName", ariaLabel);
    }

    /**
     * Gets the accessible name for the range input element of the slider.
     *
     * @return an optional accessible name, or an empty optional if no
     *         accessible name has been set
     */
    @Override
    public Optional<String> getAriaLabel() {
        return Optional.ofNullable(getElement().getProperty("accessibleName"));
    }

    /**
     * Sets the id of an element to be used as the accessible name for the range
     * input element of the slider.
     *
     * @param ariaLabelledBy
     *            the id of the element to be used as the label, or {@code null}
     *            to remove it
     */
    @Override
    public void setAriaLabelledBy(String ariaLabelledBy) {
        getElement().setProperty("accessibleNameRef", ariaLabelledBy);
    }

    /**
     * Gets the id of the element used as the accessible name for the range
     * input element of the slider.
     *
     * @return an optional id of the element used as the label, or an empty
     *         optional if no id has been set
     */
    @Override
    public Optional<String> getAriaLabelledBy() {
        return Optional
                .ofNullable(getElement().getProperty("accessibleNameRef"));
    }

    /**
     * Gets the minimum value of the slider.
     *
     * @return the minimum value
     */
    public double getMin() {
        return super.getMinDouble();
    }

    /**
     * Sets the minimum value of the slider.
     *
     * @param min
     *            the minimum value
     */
    public void setMin(double min) {
        setMinDouble(min);
    }

    /**
     * Gets the maximum value of the slider.
     *
     * @return the maximum value
     */
    public double getMax() {
        return super.getMaxDouble();
    }

    /**
     * Sets the maximum value of the slider.
     *
     * @param max
     *            the maximum value
     */
    public void setMax(double max) {
        setMaxDouble(max);
    }

    /**
     * Gets the step value of the slider.
     * <p>
     * Valid slider values are calculated relative to the minimum value:
     * {@code min}, {@code min + step}, {@code min + 2*step}, etc.
     *
     * @return the step value
     */
    public double getStep() {
        return super.getStepDouble();
    }

    /**
     * Sets the step value of the slider.
     * <p>
     * Valid slider values are calculated relative to the minimum value:
     * {@code min}, {@code min + step}, {@code min + 2*step}, etc.
     *
     * @param step
     *            the step value
     */
    public void setStep(double step) {
        setStepDouble(step);
    }

    /**
     * Clears the slider value, setting it to the minimum value.
     *
     * @see #getMin()
     */
    @Override
    public void clear() {
        setValue(getMin());
    }

    @Override
    protected boolean hasValidValue() {
        Double value = getElement().getProperty("value", 0.0);
        return value != null && isValueWithinMinMax(value)
                && isValueAlignedWithStep(value);
    }

    @Override
    protected boolean isValueWithinMinMax(Double value) {
        double min = getMinDouble();
        double max = getMaxDouble();
        if (min > max) {
            return false;
        }

        return value.equals(SliderUtil.clampToMinMax(value, min, max));
    }

    @Override
    protected boolean isValueAlignedWithStep(Double value) {
        double min = getMinDouble();
        double max = getMaxDouble();
        if (min > max) {
            return false;
        }

        return value.equals(SliderUtil.snapToStep(value, min, max, getStep()));
    }
}
