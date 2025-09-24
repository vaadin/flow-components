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
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.MultiSelectComboBoxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-multi-select-combo-box/i18n")
public class MultiSelectComboBoxI18nIT extends AbstractComponentIT {
    private MultiSelectComboBoxElement comboBox;
    private TestBenchElement toggleAttached;
    private TestBenchElement setI18n;
    private TestBenchElement setEmptyI18n;

    @Before
    public void init() {
        open();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();
        toggleAttached = $("button").id("toggle-attached");
        setI18n = $("button").id("set-i18n");
        setEmptyI18n = $("button").id("set-empty-i18n");
    }

    @Test
    public void setI18n_i18nIsUpdated() {
        setI18n.click();

        Assert.assertEquals("Custom cleared",
                comboBox.getPropertyString("i18n", "cleared"));
        Assert.assertEquals("Custom focused",
                comboBox.getPropertyString("i18n", "focused"));
        Assert.assertEquals("Custom selected",
                comboBox.getPropertyString("i18n", "selected"));
        Assert.assertEquals("Custom deselected",
                comboBox.getPropertyString("i18n", "deselected"));
        Assert.assertEquals("{count} Custom total",
                comboBox.getPropertyString("i18n", "total"));
    }

    @Test
    public void setEmptyI18n_defaultI18nIsNotOverridden() {
        setEmptyI18n.click();

        Assert.assertEquals("Selection cleared",
                comboBox.getPropertyString("i18n", "cleared"));
    }

    @Test
    public void setI18n_detach_attach_i18nIsPersisted() {
        setI18n.click();
        toggleAttached.click();
        toggleAttached.click();
        comboBox = $(MultiSelectComboBoxElement.class).waitForFirst();

        Assert.assertEquals("Custom cleared",
                comboBox.getPropertyString("i18n", "cleared"));
    }
}
