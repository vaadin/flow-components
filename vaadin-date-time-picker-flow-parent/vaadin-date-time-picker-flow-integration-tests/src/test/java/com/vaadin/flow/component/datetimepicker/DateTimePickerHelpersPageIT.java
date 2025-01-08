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
package com.vaadin.flow.component.datetimepicker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("helper-text-component")
public class DateTimePickerHelpersPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-date-time-picker"));
    }

    @Test
    public void assertHelperText() {
        DateTimePickerElement dtp = $(DateTimePickerElement.class)
                .id("dtp-helper-text");
        Assert.assertEquals("Helper text", dtp.getHelperText());

        $("button").id("button-clear-helper-text").click();
        Assert.assertEquals("", dtp.getHelperText());
    }

    @Test
    public void assertHelperComponent() {
        DateTimePickerElement dtp = $(DateTimePickerElement.class)
                .id("dtp-helper-component");
        Assert.assertEquals("helper-component",
                dtp.getHelperComponent().getAttribute("id"));

        $("button").id("button-clear-helper-component").click();
        Assert.assertNull(dtp.getHelperComponent());
    }
}
