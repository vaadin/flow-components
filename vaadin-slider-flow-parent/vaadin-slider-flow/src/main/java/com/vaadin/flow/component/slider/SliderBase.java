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

import java.util.HashSet;
import java.util.Set;

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.function.SerializableRunnable;

/**
 * Abstract base class for slider components.
 *
 * @param <TComponent>
 *            the component type
 * @param <TValue>
 *            the value type
 *
 * @author Vaadin Ltd
 */
public abstract class SliderBase<TComponent extends SliderBase<TComponent, TValue>, TValue extends Number>
        extends AbstractSinglePropertyField<TComponent, TValue>
        implements HasLabel, HasHelper, HasValidationProperties, HasSize,
        Focusable<Slider>, KeyNotifier {
    private Set<String> pendingBeforeClientResponseActions = new HashSet<>();

    /**
     * Constructs a slider with the given min, max, and initial value.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param value
     *            the initial value
     */
    SliderBase(double min, double max, double step, TValue value) {
        super("value", null, false);

        getElement().setProperty("manualValidation", true);

        setMinDouble(min);
        setMaxDouble(max);
        setStepDouble(step);
        setValue(value);

        // workaround for // https://github.com/vaadin/flow/issues/3496
        setInvalid(false);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag(attachEvent.getUI());
    }

    private void checkFeatureFlag(UI ui) {
        FeatureFlags featureFlags = FeatureFlags
                .get(ui.getSession().getService().getContext());
        boolean enabled = featureFlags
                .isEnabled(SliderFeatureFlagProvider.SLIDER_COMPONENT);

        if (!enabled) {
            throw new ExperimentalFeatureException();
        }
    }

    /**
     * Sets the minimum value of the slider.
     *
     * @param min
     *            the minimum value
     * @throws IllegalArgumentException
     *             if the min is greater than the max value
     */
    void setMinDouble(double min) {
        if (min > getMaxDouble()) {
            throw new IllegalArgumentException(
                    "The min value cannot be greater than the max value");
        }

        getElement().setProperty("min", min);
    }

    /**
     * Gets the minimum value of the slider.
     *
     * @return the minimum value
     */
    double getMinDouble() {
        return getElement().getProperty("min", 0);
    }

    /**
     * Sets the maximum value of the slider.
     *
     * @param max
     *            the maximum value
     * @throws IllegalArgumentException
     *             if the max is less than the min value
     */
    void setMaxDouble(double max) {
        if (max < getMinDouble()) {
            throw new IllegalArgumentException(
                    "The max value cannot be less than the min value");
        }

        getElement().setProperty("max", max);
    }

    /**
     * Gets the maximum value of the slider.
     *
     * @return the maximum value
     */
    double getMaxDouble() {
        return getElement().getProperty("max", 100);
    }

    /**
     * Sets the step value of the slider. The step is the amount the value
     * changes when the user moves the handle.
     *
     * @param step
     *            the step value
     * @throws IllegalArgumentException
     *             if the step is less than or equal to zero
     */
    void setStepDouble(double step) {
        if (step <= 0) {
            throw new IllegalArgumentException(
                    "The step value must be a positive number");
        }

        getElement().setProperty("step", step);
    }

    /**
     * Gets the step value of the slider.
     *
     * @return the step value
     */
    double getStepDouble() {
        return getElement().getProperty("step", 1);
    }

    /**
     * Schedules the given action to be executed before the client response,
     * identified by the given key. If an action with the same key is already
     * scheduled, it will not be added again.
     *
     * @param key
     *            the unique key identifying the action
     * @param action
     *            the action to be executed
     */
    void scheduleBeforeClientResponse(String key, SerializableRunnable action) {
        if (pendingBeforeClientResponseActions.contains(key)) {
            return;
        }

        getElement().getNode().runWhenAttached(ui -> {
            ui.beforeClientResponse(this, context -> {
                pendingBeforeClientResponseActions.remove(key);
                action.run();
            });
        });

        pendingBeforeClientResponseActions.add(key);
    }
}
