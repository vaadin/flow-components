/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

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
