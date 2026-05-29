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
package com.vaadin.flow.component.ai.form;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.ai.form.FormTestFields.MultiSelectField;
import com.vaadin.flow.component.ai.form.FormTestFields.TestField;
import com.vaadin.flow.data.binder.HasItems;

/**
 * Tests for {@link FormFieldType#classify(com.vaadin.flow.component.HasValue)}:
 * classification is contract-based, so {@link FormFieldType#MULTI_SELECT} is
 * gated on the {@link com.vaadin.flow.data.selection.MultiSelect} marker
 * interface alone.
 */
class FormFieldTypeTest {

    @Test
    void classifyTestField_returnsString() {
        Assertions.assertEquals(FormFieldType.STRING,
                FormFieldType.classify(new TestField()));
    }

    @Test
    void classifyMultiSelectField_returnsMultiSelect() {
        Assertions.assertEquals(FormFieldType.MULTI_SELECT,
                FormFieldType.classify(new MultiSelectField<String>()));
    }

    @Test
    void classifyFieldImplementingBothMultiSelectAndHasItems_returnsMultiSelect() {
        // Real-world shape (MultiSelectComboBox): implements both
        // MultiSelect and HasItems. MultiSelect wins; reordering the
        // doClassify checks would silently misclassify these as
        // SINGLE_SELECT and drop the array-shape from the schema.
        Assertions.assertEquals(FormFieldType.MULTI_SELECT,
                FormFieldType.classify(new MultiSelectWithItemsField()));
    }

    @Tag("multi-select-with-items-field")
    private static class MultiSelectWithItemsField extends
            com.vaadin.flow.component.AbstractField<MultiSelectWithItemsField, java.util.Set<String>>
            implements
            com.vaadin.flow.data.selection.MultiSelect<MultiSelectWithItemsField, String>,
            HasItems<String> {

        MultiSelectWithItemsField() {
            super(java.util.Set.of());
        }

        @Override
        public void setItems(java.util.Collection<String> items) {
        }

        @SafeVarargs
        @SuppressWarnings("varargs")
        @Override
        public final void setItems(String... items) {
        }

        @Override
        public void updateSelection(java.util.Set<String> added,
                java.util.Set<String> removed) {
        }

        @Override
        public java.util.Set<String> getSelectedItems() {
            return getValue();
        }

        @Override
        public com.vaadin.flow.shared.Registration addSelectionListener(
                com.vaadin.flow.data.selection.MultiSelectionListener<MultiSelectWithItemsField, String> listener) {
            return () -> {
            };
        }

        @Override
        protected void setPresentationValue(java.util.Set<String> value) {
        }
    }
}
