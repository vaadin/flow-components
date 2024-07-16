/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-combo-box/combo-box-in-lit-template-page")
public class ComboBoxInLitTemplateIT extends AbstractComboBoxIT {

    private ComboBoxElement comboBox;

    @Before
    public void init() {
        open();
        Assume.assumeFalse(isBower());
    }

    /**
     * See: https://github.com/vaadin/flow-components/issues/2059
     */
    @Test
    public void comboBoxInitialValue_ShouldBeSetCorrectly_WhenSetValueUsed() {
        comboBox = $("combo-box-lit-wrapper").first().$(ComboBoxElement.class)
                .id("combo");
        String labelValue = "1";
        Assert.assertEquals(labelValue, comboBox.getSelectedText());
    }

    @Test
    public void comboBox_retainValue_WhenOpenClosed() {
        comboBox = $("combo-box-lit-wrapper").first().$(ComboBoxElement.class)
                .id("combo");
        String labelValue = "1";
        comboBox.openPopup();
        comboBox.closePopup();

        Assert.assertEquals(labelValue, comboBox.getSelectedText());
    }

    private boolean isBower() {
        return (Boolean) $("html").first().getCommandExecutor()
                .executeScript("return !!window.Vaadin.Lumo");
    }
}
