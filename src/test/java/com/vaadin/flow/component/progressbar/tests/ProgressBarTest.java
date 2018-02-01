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

package com.vaadin.flow.component.progressbar.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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

        assertThat("initial min is wrong", progressBar.getMin(), is(0));
        assertThat("initial max is wrong", progressBar.getMax(), is(10));
        assertThat("initial value is wrong", progressBar.getValue(), is(0));
    }

    @Test
    public void minMaxConstructorShouldInitializeMinAndMax() {
        int min = 1;
        int max = 3;

        ProgressBar progressBar = new ProgressBar(min, max);

        assertThat("initial min is wrong", progressBar.getMin(), is(min));
        assertThat("initial max is wrong", progressBar.getMax(), is(max));
        assertThat("initial value is wrong", progressBar.getValue(), is(min));
    }

    @Test
    public void fullConstructorShouldInitializeAllFields() {
        int min = 1;
        int max = 3;
        int value = 2;

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

        new ProgressBar(2, 1);

        // Nothing to assert here
    }

    @Test
    public void constructorShouldThrowIfValueLessThanMin() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("value must be between min and max");

        new ProgressBar(1, 5, 0);

        // Nothing to assert here
    }

    @Test
    public void constructorShouldThrowIfValueGreaterThanMax() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("value must be between min and max");

        new ProgressBar(0, 1, 2);

        // Nothing to assert here
    }

    @Test
    public void setValueShouldUpdateValue() {
        int min = 10;
        int max = 100;
        int value = 25;

        ProgressBar progressBar = new ProgressBar(min, max);
        assertThat("initial value is wrong", progressBar.getValue(), is(min));
        progressBar.setValue(value);

        assertThat("min is wrong", progressBar.getMin(), is(min));
        assertThat("max is wrong", progressBar.getMax(), is(max));
        assertThat("updated value is wrong", progressBar.getValue(), is(value));
    }

    @Test
    public void setValueShouldUpdateValueToMin() {
        int min = 10;
        int max = 100;
        int value = 42;

        ProgressBar progressBar = new ProgressBar(min, max, value);
        assertThat("initial value is wrong", progressBar.getValue(), is(value));
        progressBar.setValue(min);

        assertThat("min is wrong", progressBar.getMin(), is(min));
        assertThat("max is wrong", progressBar.getMax(), is(max));
        assertThat("updated value is wrong", progressBar.getValue(), is(min));
    }

    @Test
    public void setValueShouldUpdateValueToMax() {
        int min = 1;
        int max = 99;
        int value = 66;

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
