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

class MultiSelectComboBoxI18nTest {

    MultiSelectComboBox<String> comboBox;

    @BeforeEach
    void setup() {
        comboBox = new MultiSelectComboBox<>();
    }

    @Test
    void setI18n() {
        MultiSelectComboBoxI18n i18n = new MultiSelectComboBoxI18n()
                .setCleared("All entries removed");
        comboBox.setI18n(i18n);

        Assertions.assertEquals(i18n, comboBox.getI18n());
    }

    @Test
    void setI18nToNull_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> comboBox.setI18n(null));
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
