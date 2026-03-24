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
package com.vaadin.flow.component.progressbar.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.shared.HasThemeVariant;

/**
 * @author Vaadin Ltd.
 */
class ProgressBarTest {

    @Test
    void defaultConstructorShouldInitializeAllFieldsToDefault() {

        ProgressBar progressBar = new ProgressBar();

        Assertions.assertEquals(0.0, progressBar.getMin(), 0.0,
                "initial min is wrong");
        Assertions.assertEquals(1.0, progressBar.getMax(), 0.0,
                "initial max is wrong");
        Assertions.assertEquals(0.0, progressBar.getValue(), 0.0,
                "initial value is wrong");
    }

    @Test
    void minMaxConstructorShouldInitializeMinAndMax() {
        double min = 1.8312;
        double max = 3.1415927;

        ProgressBar progressBar = new ProgressBar(min, max);

        Assertions.assertEquals(min, progressBar.getMin(), 0.0,
                "initial min is wrong");
        Assertions.assertEquals(max, progressBar.getMax(), 0.0,
                "initial max is wrong");
        Assertions.assertEquals(min, progressBar.getValue(), 0.0,
                "initial value is wrong");
    }

    @Test
    void fullConstructorShouldInitializeAllFields() {
        double min = 1.8312;
        double max = 3.1415927;
        double value = 2.25;

        ProgressBar progressBar = new ProgressBar(min, max, value);

        Assertions.assertEquals(min, progressBar.getMin(), 0.0,
                "initial min is wrong");
        Assertions.assertEquals(max, progressBar.getMax(), 0.0,
                "initial max is wrong");
        Assertions.assertEquals(value, progressBar.getValue(), 0.0,
                "initial value is wrong");
    }

    @Test
    void constructorShouldThrowIfMinEqualsMax() {
        double min = 42.0;
        double max = 42.0;

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ProgressBar(min, max));
        Assertions.assertTrue(exception.getMessage().contains(String
                .format("min ('%s') must be less than max ('%s')", min, max)));
    }

    @Test
    void constructorShouldThrowIfMinGreaterThanMax() {
        double min = 1.01;
        double max = 1.0;

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ProgressBar(min, max));
        Assertions.assertTrue(exception.getMessage().contains(String
                .format("min ('%s') must be less than max ('%s')", min, max)));
    }

    @Test
    void constructorShouldThrowIfValueLessThanMin() {
        double min = 0.0;
        double max = 1.0;

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ProgressBar(min, max, -0.01));
        Assertions.assertTrue(exception.getMessage().contains(String.format(
                "value must be between min ('%s') and max ('%s')", min, max)));
    }

    @Test
    void constructorShouldThrowIfValueGreaterThanMax() {
        double min = 0.0;
        double max = 1.0;

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ProgressBar(min, max, 1.01));
        Assertions.assertTrue(exception.getMessage().contains(String.format(
                "value must be between min ('%s') and max ('%s')", min, max)));
    }

    @Test
    void setValueShouldUpdateValue() {
        double min = 10;
        double max = 100;
        double value = 25;

        ProgressBar progressBar = new ProgressBar(min, max);
        Assertions.assertEquals(min, progressBar.getValue(), 0.0,
                "initial value is wrong");
        progressBar.setValue(value);

        Assertions.assertEquals(min, progressBar.getMin(), 0.0, "min is wrong");
        Assertions.assertEquals(max, progressBar.getMax(), 0.0, "max is wrong");
        Assertions.assertEquals(value, progressBar.getValue(), 0.0,
                "updated value is wrong");
    }

    @Test
    void setValueShouldUpdateValueToMin() {
        double min = 10;
        double max = 100;
        double value = 42;

        ProgressBar progressBar = new ProgressBar(min, max, value);
        Assertions.assertEquals(value, progressBar.getValue(), 0.0,
                "initial value is wrong");
        progressBar.setValue(min);

        Assertions.assertEquals(min, progressBar.getMin(), 0.0, "min is wrong");
        Assertions.assertEquals(max, progressBar.getMax(), 0.0, "max is wrong");
        Assertions.assertEquals(min, progressBar.getValue(), 0.0,
                "updated value is wrong");
    }

    @Test
    void setValueShouldUpdateValueToMax() {
        double min = 1;
        double max = 99;
        double value = 66;

        ProgressBar progressBar = new ProgressBar(min, max, value);
        Assertions.assertEquals(value, progressBar.getValue(), 0.0,
                "initial value is wrong");
        progressBar.setValue(max);

        Assertions.assertEquals(min, progressBar.getMin(), 0.0, "min is wrong");
        Assertions.assertEquals(max, progressBar.getMax(), 0.0, "max is wrong");
        Assertions.assertEquals(max, progressBar.getValue(), 0.0,
                "updated value is wrong");
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(ProgressBar.class));
    }
}
