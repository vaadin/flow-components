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
package com.vaadin.flow.component.checkbox.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.checkbox.testbench.SwitchElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-switch")
public class SwitchIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void clickSwitch_togglesCheckedAndUpdatesValue() {
        SwitchElement field = $(SwitchElement.class).id("default-switch");
        Assert.assertFalse(field.isChecked());

        field.click();

        Assert.assertTrue(field.isChecked());
        Assert.assertEquals("true", findElement(By.id("value")).getText());
    }
}
