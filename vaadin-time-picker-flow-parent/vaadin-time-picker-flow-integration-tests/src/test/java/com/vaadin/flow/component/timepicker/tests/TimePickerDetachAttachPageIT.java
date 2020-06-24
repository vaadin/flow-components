/*
 * Copyright 2000-2019 Vaadin Ltd.
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
package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * Integration tests for attaching / detaching time picker.
 */
@TestPath("time-picker-detach-attach")
public class TimePickerDetachAttachPageIT extends AbstractComponentIT {

    @Test
    public void clientSideValidationIsOverriddenOnAttach() {
        open();

        assertTimePickerIsValidOnTab();

        // Detaching and attaching time picker
        WebElement toggleAttach = findElement(By.id("toggle-attached"));
        toggleAttach.click();
        toggleAttach.click();

        assertTimePickerIsValidOnTab();
    }

    private void assertTimePickerIsValidOnTab() {
        WebElement timePicker = findElement(By.id("time-picker"));
        timePicker.sendKeys(Keys.TAB);
        Assert.assertFalse("Time picker should be valid after Tab", Boolean.parseBoolean(timePicker.getAttribute("invalid")));
    }
}
