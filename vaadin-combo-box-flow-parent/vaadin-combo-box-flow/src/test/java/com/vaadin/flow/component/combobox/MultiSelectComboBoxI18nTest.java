/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MultiSelectComboBoxI18nTest {

    MultiSelectComboBox<String> comboBox;

    @Before
    public void setup() {
        comboBox = new MultiSelectComboBox<>();
    }

    @Test
    public void setI18n() {
        MultiSelectComboBoxI18n i18n = new MultiSelectComboBoxI18n()
                .setCleared("All entries removed");
        comboBox.setI18n(i18n);

        Assert.assertEquals(i18n, comboBox.getI18n());
    }

    @Test(expected = NullPointerException.class)
    public void setI18nToNull_throws() {
        comboBox.setI18n(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setTotalWithoutCountPlaceholder_throws() {
        MultiSelectComboBoxI18n i18n = new MultiSelectComboBoxI18n();
        i18n.setTotal("entries selected");
    }

    @Test()
    public void setTotalWithCountPlaceholder_doesNotThrow() {
        MultiSelectComboBoxI18n i18n = new MultiSelectComboBoxI18n();
        i18n.setTotal("{count} entries selected");

        Assert.assertEquals("{count} entries selected", i18n.getTotal());
    }
}
