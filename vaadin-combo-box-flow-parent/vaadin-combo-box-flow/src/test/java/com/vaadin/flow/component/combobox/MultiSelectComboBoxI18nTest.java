/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
