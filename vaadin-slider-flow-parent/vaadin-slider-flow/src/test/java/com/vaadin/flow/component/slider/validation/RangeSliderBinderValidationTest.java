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
package com.vaadin.flow.component.slider.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.slider.RangeSlider;
import com.vaadin.flow.component.slider.RangeSliderValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;

public class RangeSliderBinderValidationTest {
    private static final String VALIDATION_ERROR_MESSAGE = "End value must be at least 50";

    private RangeSlider rangeSlider;
    private Binder<Bean> binder;

    public static class Bean {
        private RangeSliderValue value = new RangeSliderValue(0, 100);

        public RangeSliderValue getValue() {
            return value;
        }

        public void setValue(RangeSliderValue value) {
            this.value = value;
        }
    }

    @Before
    public void setup() {
        rangeSlider = new RangeSlider();
        binder = new Binder<>(Bean.class);
        binder.forField(rangeSlider)
                .withValidator(value -> value.end() >= 50,
                        VALIDATION_ERROR_MESSAGE)
                .bind(Bean::getValue, Bean::setValue);
    }

    @Test
    public void setValue_validatorPasses_noValidationError() {
        rangeSlider.setValue(new RangeSliderValue(0, 50));

        BindingValidationStatus<?> status = binder.validate()
                .getFieldValidationStatuses().get(0);

        Assert.assertFalse(status.isError());
    }

    @Test
    public void setValue_validatorFails_hasValidationError() {
        rangeSlider.setValue(new RangeSliderValue(0, 49));

        BindingValidationStatus<?> status = binder.validate()
                .getFieldValidationStatuses().get(0);

        Assert.assertTrue(status.isError());
        Assert.assertEquals(VALIDATION_ERROR_MESSAGE,
                status.getMessage().orElse(""));
    }

    @Test
    public void readBean_null_setsEmptyValue() {
        rangeSlider.setValue(new RangeSliderValue(25, 75));
        binder.readBean(null);

        Assert.assertEquals(new RangeSliderValue(rangeSlider.getMin(),
                rangeSlider.getMax()), rangeSlider.getValue());
    }
}
