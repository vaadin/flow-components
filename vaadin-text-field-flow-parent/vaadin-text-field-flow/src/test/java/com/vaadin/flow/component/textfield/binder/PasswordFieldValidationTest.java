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
package com.vaadin.flow.component.textfield.binder;

import java.util.Objects;

import com.vaadin.experimental.Feature;
import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.function.SerializablePredicate;

public class PasswordFieldValidationTest
        extends AbstractTextFieldValidationTest<String, PasswordField> {

    @Tag("test-password-field")
    private class TestPasswordField extends PasswordField {
        protected boolean isFeatureFlagEnabled(Feature feature) {
            if (feature.getId() == FeatureFlags.ENFORCE_FIELD_VALIDATION
                    .getId()) {
                return true;
            }

            return super.isFeatureFlagEnabled(feature);
        }
    }

    @Override
    protected void initField() {
        field = new TestPasswordField();
        field.setMaxLength(10);
    }

    @Override
    protected void setValidValue() {
        field.setValue("AAAA");
    }

    @Override
    protected void setComponentInvalidValue() {
        field.setValue("AAAAAAAAAAAAAA");
    }

    @Override
    protected void setBinderInvalidValue() {
        field.setValue("A");
    }

    @Override
    protected SerializablePredicate<? super String> getValidator() {
        return value -> Objects.equals(value, "") || value.length() > 2;
    }
}
