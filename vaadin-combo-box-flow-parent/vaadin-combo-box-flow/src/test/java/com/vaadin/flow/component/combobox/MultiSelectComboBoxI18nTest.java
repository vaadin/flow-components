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
package com.vaadin.flow.component.combobox;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

class MultiSelectComboBoxI18nTest {

    MultiSelectComboBox<String> comboBox;

    @BeforeEach
    void setup() {
        comboBox = new MultiSelectComboBox<>();
    }

    @Test
    void getI18n_returnsNullByDefault() {
        Assertions.assertNull(comboBox.getI18n());
    }

    @Test
    void setI18n_getI18n_returnsSameInstance() {
        MultiSelectComboBoxI18n i18n = new MultiSelectComboBoxI18n()
                .setCleared("All entries removed");
        comboBox.setI18n(i18n);

        Assertions.assertSame(i18n, comboBox.getI18n());
    }

    @Test
    void setI18nToNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> comboBox.setI18n(null));
    }

    @Test
    void setI18n_allProperties_setsElementProperty() {
        //@formatter:off
        comboBox.setI18n(new MultiSelectComboBoxI18n()
                .setCleared("Cleared")
                .setFocused("Focused")
                .setSelected("Selected")
                .setDeselected("Deselected")
                .setTotal("{count} total")
                .setSelectAll("Select all")
                .setDeselectAll("Deselect all")
                .setSelectFiltered("Select filtered")
                .setDeselectFiltered("Deselect filtered")
                .setRequiredErrorMessage("Required"));
        //@formatter:on

        ObjectNode expected = JacksonUtils.createObjectNode();
        expected.put("cleared", "Cleared");
        expected.put("focused", "Focused");
        expected.put("selected", "Selected");
        expected.put("deselected", "Deselected");
        expected.put("total", "{count} total");
        expected.put("selectAll", "Select all");
        expected.put("deselectAll", "Deselect all");
        expected.put("selectFiltered", "Select filtered");
        expected.put("deselectFiltered", "Deselect filtered");
        Assertions.assertEquals(expected,
                comboBox.getElement().getPropertyRaw("i18n"));
    }

    @Test
    void setTotalWithoutCountPlaceholder_throws() {
        MultiSelectComboBoxI18n i18n = new MultiSelectComboBoxI18n();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> i18n.setTotal("entries selected"));
    }

    @Test
    void setTotalWithCountPlaceholder_doesNotThrow() {
        MultiSelectComboBoxI18n i18n = new MultiSelectComboBoxI18n();
        i18n.setTotal("{count} entries selected");

        Assertions.assertEquals("{count} entries selected", i18n.getTotal());
    }
}
