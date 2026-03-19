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
package com.vaadin.flow.component.combobox.validation;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;

class MultiSelectComboBoxBinderValidationTest {
    @Test
    void requiredFieldWithEmptyValue_validationFails() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>("foo",
                "bar", "baz");

        Binder<TestBean> binder = new Binder<>();
        binder.forField(comboBox).asRequired().bind(TestBean::getValues,
                TestBean::setValues);
        binder.validate();

        Assertions.assertTrue(comboBox.isInvalid());
        Assertions.assertFalse(binder.isValid());
    }

    @Test
    void requiredFieldWithNonEmptyValue_validationPasses() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>("foo",
                "bar", "baz");
        comboBox.setValue(Set.of("foo"));

        Binder<TestBean> binder = new Binder<>();
        binder.forField(comboBox).asRequired().bind(TestBean::getValues,
                TestBean::setValues);
        binder.validate();

        Assertions.assertFalse(comboBox.isInvalid());
        Assertions.assertTrue(binder.isValid());
    }

    @Test
    void fieldWithCustomValidatorAndInvalidValue_validationFails() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>("foo",
                "bar", "baz");
        comboBox.setValue(Set.of("foo", "bar", "baz"));

        Binder<TestBean> binder = new Binder<>();
        Validator<Set<String>> noFooValidator = (values,
                context) -> values.contains("foo")
                        ? ValidationResult.error("foo is not allowed")
                        : ValidationResult.ok();
        binder.forField(comboBox).withValidator(noFooValidator)
                .bind(TestBean::getValues, TestBean::setValues);
        binder.validate();

        Assertions.assertTrue(comboBox.isInvalid());
        Assertions.assertFalse(binder.isValid());
    }

    @Test
    void fieldWithCustomValidatorAndValidValue_validationFails() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>("foo",
                "bar", "baz");
        comboBox.setValue(Set.of("bar", "baz"));

        Binder<TestBean> binder = new Binder<>();
        Validator<Set<String>> noFooValidator = (values,
                context) -> values.contains("foo")
                        ? ValidationResult.error("foo is not allowed")
                        : ValidationResult.ok();
        binder.forField(comboBox).withValidator(noFooValidator)
                .bind(TestBean::getValues, TestBean::setValues);
        binder.validate();

        Assertions.assertFalse(comboBox.isInvalid());
        Assertions.assertTrue(binder.isValid());
    }

    private static class TestBean {
        private Set<String> values;

        public Set<String> getValues() {
            return values;
        }

        public void setValues(Set<String> values) {
            this.values = values;
        }
    }
}
