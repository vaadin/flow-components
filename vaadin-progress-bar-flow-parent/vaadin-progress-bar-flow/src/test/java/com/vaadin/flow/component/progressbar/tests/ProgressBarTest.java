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
package com.vaadin.flow.component.progressbar.tests;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.progressbar.ProgressBar;

/**
 * @author Vaadin Ltd.
 */
public class ProgressBarTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void defaultConstructorShouldInitializeAllFieldsToDefault() {

        ProgressBar progressBar = new ProgressBar();

        Assert.assertEquals("initial min is wrong", 0.0, progressBar.getMin(),
                0.0);
        Assert.assertEquals("initial max is wrong", 1.0, progressBar.getMax(),
                0.0);
        Assert.assertEquals("initial value is wrong", 0.0,
                progressBar.getValue(), 0.0);
    }

    @Test
    public void minMaxConstructorShouldInitializeMinAndMax() {
        double min = 1.8312;
        double max = 3.1415927;

        ProgressBar progressBar = new ProgressBar(min, max);

        Assert.assertEquals("initial min is wrong", min, progressBar.getMin(),
                0.0);
        Assert.assertEquals("initial max is wrong", max, progressBar.getMax(),
                0.0);
        Assert.assertEquals("initial value is wrong", min,
                progressBar.getValue(), 0.0);
    }

    @Test
    public void fullConstructorShouldInitializeAllFields() {
        double min = 1.8312;
        double max = 3.1415927;
        double value = 2.25;

        ProgressBar progressBar = new ProgressBar(min, max, value);

        Assert.assertEquals("initial min is wrong", min, progressBar.getMin(),
                0.0);
        Assert.assertEquals("initial max is wrong", max, progressBar.getMax(),
                0.0);
        Assert.assertEquals("initial value is wrong", value,
                progressBar.getValue(), 0.0);
    }

    @Test
    public void constructorShouldThrowIfMinEqualsMax() {
        double min = 42.0;
        double max = 42.0;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(String
                .format("min ('%s') must be less than max ('%s')", min, max));

        new ProgressBar(min, max);
    }

    @Test
    public void constructorShouldThrowIfMinGreaterThanMax() {
        double min = 1.01;
        double max = 1.0;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(String
                .format("min ('%s') must be less than max ('%s')", min, max));

        new ProgressBar(min, max);
    }

    @Test
    public void constructorShouldThrowIfValueLessThanMin() {
        double min = 0.0;
        double max = 1.0;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(String.format(
                "value must be between min ('%s') and max ('%s')", min, max));

        new ProgressBar(min, max, -0.01);
    }

    @Test
    public void constructorShouldThrowIfValueGreaterThanMax() {
        double min = 0.0;
        double max = 1.0;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(String.format(
                "value must be between min ('%s') and max ('%s')", min, max));

        new ProgressBar(min, max, 1.01);
    }

    @Test
    public void setValueShouldUpdateValue() {
        double min = 10;
        double max = 100;
        double value = 25;

        ProgressBar progressBar = new ProgressBar(min, max);
        Assert.assertEquals("initial value is wrong", min,
                progressBar.getValue(), 0.0);
        progressBar.setValue(value);

        Assert.assertEquals("min is wrong", min, progressBar.getMin(), 0.0);
        Assert.assertEquals("max is wrong", max, progressBar.getMax(), 0.0);
        Assert.assertEquals("updated value is wrong", value,
                progressBar.getValue(), 0.0);
    }

    @Test
    public void setValueShouldUpdateValueToMin() {
        double min = 10;
        double max = 100;
        double value = 42;

        ProgressBar progressBar = new ProgressBar(min, max, value);
        Assert.assertEquals("initial value is wrong", value,
                progressBar.getValue(), 0.0);
        progressBar.setValue(min);

        Assert.assertEquals("min is wrong", min, progressBar.getMin(), 0.0);
        Assert.assertEquals("max is wrong", max, progressBar.getMax(), 0.0);
        Assert.assertEquals("updated value is wrong", min,
                progressBar.getValue(), 0.0);
    }

    @Test
    public void setValueShouldUpdateValueToMax() {
        double min = 1;
        double max = 99;
        double value = 66;

        ProgressBar progressBar = new ProgressBar(min, max, value);
        Assert.assertEquals("initial value is wrong", value,
                progressBar.getValue(), 0.0);
        progressBar.setValue(max);

        Assert.assertEquals("min is wrong", min, progressBar.getMin(), 0.0);
        Assert.assertEquals("max is wrong", max, progressBar.getMax(), 0.0);
        Assert.assertEquals("updated value is wrong", max,
                progressBar.getValue(), 0.0);
    }

    @Test
    public void setValueShouldThrowIfValueLessThanMin() {
        double min = 10.0;
        double max = 100.0;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(String.format(
                "value must be between min ('%s') and max ('%s')", min, max));

        ProgressBar progressBar = new ProgressBar(min, max);
        progressBar.setValue(9);
    }

    @Test
    public void setValueShouldThrowIfValueGreaterThanMax() {
        double min = 10.0;
        double max = 100.0;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(String.format(
                "value must be between min ('%s') and max ('%s')", min, max));

        ProgressBar progressBar = new ProgressBar(min, max);
        progressBar.setValue(101);
    }
}
