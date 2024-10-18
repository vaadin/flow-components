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
package com.vaadin.flow.component.combobox.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("helper-text")
public class HelperTextPageIT extends AbstractComboBoxIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void assertHelperText() {
        ComboBoxElement comboHelperText = $(ComboBoxElement.class)
                .id("combobox-helper-text");
        Assert.assertEquals("Helper text", comboHelperText.getHelperText());

        clickButton("empty-helper-text");
        Assert.assertEquals("", comboHelperText.getHelperText());
    }

    @Test
    public void assertHelperComponent() {
        ComboBoxElement comboHelperComponent = $(ComboBoxElement.class)
                .id("combobox-helper-component");
        Assert.assertEquals("helper-component",
                comboHelperComponent.getHelperComponent().getAttribute("id"));

        clickButton("empty-helper-component");
        Assert.assertEquals(null, comboHelperComponent.getHelperComponent());
    }

}
