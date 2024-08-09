/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;

public class MultiSelectComboBoxBinderValidationTest {
    @Test
    public void requiredFieldWithEmptyValue_validationFails() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>("foo",
                "bar", "baz");

        Binder<TestBean> binder = new Binder<>();
        binder.forField(comboBox).asRequired().bind(TestBean::getValues,
                TestBean::setValues);
        binder.validate();

        Assert.assertTrue(comboBox.isInvalid());
        Assert.assertFalse(binder.isValid());
    }

    @Test
    public void requiredFieldWithNonEmptyValue_validationPasses() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>("foo",
                "bar", "baz");
        comboBox.setValue(Set.of("foo"));

        Binder<TestBean> binder = new Binder<>();
        binder.forField(comboBox).asRequired().bind(TestBean::getValues,
                TestBean::setValues);
        binder.validate();

        Assert.assertFalse(comboBox.isInvalid());
        Assert.assertTrue(binder.isValid());
    }

    @Test
    public void fieldWithCustomValidatorAndInvalidValue_validationFails() {
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

        Assert.assertTrue(comboBox.isInvalid());
        Assert.assertFalse(binder.isValid());
    }

    @Test
    public void fieldWithCustomValidatorAndValidValue_validationFails() {
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

        Assert.assertFalse(comboBox.isInvalid());
        Assert.assertTrue(binder.isValid());
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
