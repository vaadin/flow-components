/*
 * Copyright 2000-2017 Vaadin Ltd.
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
 *
 */

package com.vaadin.ui.progressbar.tests;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.ui.progressbar.ProgressBar;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author Vaadin Ltd.
 */
public class ProgressBarTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void defaultConstructorShouldInitializeAllFieldsToDefault() {

        ProgressBar progressBar = new ProgressBar();

        assertThat("initial min is wrong", progressBar.getMin(), is(0.0));
        assertThat("initial max is wrong", progressBar.getMax(), is(1.0));
        assertThat("initial value is wrong", progressBar.getValue(), is(0.0));
    }

    @Test
    public void minMaxConstructorShouldInitializeMinAndMax() {
        double min = 1.8312;
        double max = 3.1415927;

        ProgressBar progressBar = new ProgressBar(min, max);

        assertThat("initial min is wrong", progressBar.getMin(), is(min));
        assertThat("initial max is wrong", progressBar.getMax(), is(max));
        assertThat("initial value is wrong", progressBar.getValue(), is(min));
    }

    @Test
    public void fullConstructorShouldInitializeAllFields() {
        double min = 1.8312;
        double max = 3.1415927;
        double value = 2.56;

        ProgressBar progressBar = new ProgressBar(min, max, value);

        assertThat("initial min is wrong", progressBar.getMin(), is(min));
        assertThat("initial max is wrong", progressBar.getMax(), is(max));
        assertThat("initial value is wrong", progressBar.getValue(), is(value));
    }

    @Test
    public void constructorShouldThrowIfMinEqualsMax() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("min must be less than max");

        new ProgressBar(42, 42);

        // Nothing to assert here
    }

    @Test
    public void constructorShouldThrowIfMinGreaterThanMax() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("min must be less than max");

        new ProgressBar(1.01, 1.0);

        // Nothing to assert here
    }

    @Test
    public void constructorShouldThrowIfValueLessThanMin() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("value must be between min and max");

        new ProgressBar(0.0, 1.0, -0.01);

        // Nothing to assert here
    }

    @Test
    public void constructorShouldThrowIfValueGreaterThanMax() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("value must be between min and max");

        new ProgressBar(0.0, 1.0, 1.01);

        // Nothing to assert here
    }

    @Test
    public void setValueShouldUpdateValue() {
        double min = 10;
        double max = 100;
        double value = 25;

        ProgressBar progressBar = new ProgressBar(min, max);
        assertThat("initial value is wrong", progressBar.getValue(), is(min));
        progressBar.setValue(value);

        assertThat("min is wrong", progressBar.getMin(), is(min));
        assertThat("max is wrong", progressBar.getMax(), is(max));
        assertThat("updated value is wrong", progressBar.getValue(), is(value));
    }

    @Test
    public void setValueShouldUpdateValueToMin() {
        double min = 10;
        double max = 100;
        double value = 42;

        ProgressBar progressBar = new ProgressBar(min, max, value);
        assertThat("initial value is wrong", progressBar.getValue(), is(value));
        progressBar.setValue(min);

        assertThat("min is wrong", progressBar.getMin(), is(min));
        assertThat("max is wrong", progressBar.getMax(), is(max));
        assertThat("updated value is wrong", progressBar.getValue(), is(min));
    }

    @Test
    public void setValueShouldUpdateValueToMax() {
        double min = 1;
        double max = 99;
        double value = 66;

        ProgressBar progressBar = new ProgressBar(min, max, value);
        assertThat("initial value is wrong", progressBar.getValue(), is(value));
        progressBar.setValue(max);

        assertThat("min is wrong", progressBar.getMin(), is(min));
        assertThat("max is wrong", progressBar.getMax(), is(max));
        assertThat("updated value is wrong", progressBar.getValue(), is(max));
    }

    @Test
    public void setValueShouldThrowIfValueLessThanMin() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("value must be between min and max");

        ProgressBar progressBar = new ProgressBar(10, 100);
        progressBar.setValue(9);

        // Nothing to assert here
    }

    @Test
    public void setValueShouldThrowIfValueGreaterThanMax() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("value must be between min and max");

        ProgressBar progressBar = new ProgressBar(10, 100);
        progressBar.setValue(101);

        // Nothing to assert here
    }
}
