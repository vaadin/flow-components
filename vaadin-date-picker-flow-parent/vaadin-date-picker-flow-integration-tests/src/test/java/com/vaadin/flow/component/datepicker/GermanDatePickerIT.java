/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.datepicker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.tests.ComponentDemoTest;
import com.vaadin.testbench.TestBenchElement;

import static org.junit.Assert.assertTrue;

@TestPath("vaadin-date-picker/german-picker-format")
public class GermanDatePickerIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-date-picker"));
    }

    @Test
    public void selectDateOnFinnishDatePicker() {
        DatePickerElement picker = $(DatePickerElement.class)
                .id("german-picker");
        String value = picker.getInputValue();

        Assert.assertEquals("14.02.22, Montag", value);
    }
}
