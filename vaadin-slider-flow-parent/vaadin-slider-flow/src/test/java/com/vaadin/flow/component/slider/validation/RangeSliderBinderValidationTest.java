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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.slider.DecimalRangeSlider;
import com.vaadin.flow.component.slider.DecimalRangeSliderValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;

class RangeSliderBinderValidationTest {
    private static final String VALIDATION_ERROR_MESSAGE = "End value must be at least 50";

    private DecimalRangeSlider rangeSlider;
    private Binder<Bean> binder;

    public static class Bean {
        private DecimalRangeSliderValue value = new DecimalRangeSliderValue(0.0,
                100.0);

        public DecimalRangeSliderValue getValue() {
            return value;
        }

        public void setValue(DecimalRangeSliderValue value) {
            this.value = value;
        }
    }

    @BeforeEach
    void setup() {
        rangeSlider = new DecimalRangeSlider();
        binder = new Binder<>(Bean.class);
        binder.forField(rangeSlider)
                .withValidator(value -> value.end() >= 50,
                        VALIDATION_ERROR_MESSAGE)
                .bind(Bean::getValue, Bean::setValue);
    }

    @Test
    void setValue_validatorPasses_noValidationError() {
        rangeSlider.setValue(new DecimalRangeSliderValue(0.0, 50.0));

        BindingValidationStatus<?> status = binder.validate()
                .getFieldValidationStatuses().get(0);

        Assertions.assertFalse(status.isError());
    }

    @Test
    void setValue_validatorFails_hasValidationError() {
        rangeSlider.setValue(new DecimalRangeSliderValue(0.0, 49.0));

        BindingValidationStatus<?> status = binder.validate()
                .getFieldValidationStatuses().get(0);

        Assertions.assertTrue(status.isError());
        Assertions.assertEquals(VALIDATION_ERROR_MESSAGE,
                status.getMessage().orElse(""));
    }

    @Test
    void readBean_null_setsEmptyValue() {
        rangeSlider.setValue(new DecimalRangeSliderValue(25.0, 75.0));
        binder.readBean(null);

        Assertions
                .assertEquals(new DecimalRangeSliderValue(rangeSlider.getMin(),
                        rangeSlider.getMax()), rangeSlider.getValue());
    }
}
