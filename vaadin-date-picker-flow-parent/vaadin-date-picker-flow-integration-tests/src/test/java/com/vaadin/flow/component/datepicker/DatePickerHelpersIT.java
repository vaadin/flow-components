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
package com.vaadin.flow.component.datepicker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("helpers-view")
public class DatePickerHelpersIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void assertHelperText() {
        DatePickerElement dataPickerHelperText = $(DatePickerElement.class)
                .id("data-picker-helper-text");
        Assert.assertEquals("Helper text",
                dataPickerHelperText.getHelperText());

        $("button").id("button-clear-text").click();
        Assert.assertEquals("", dataPickerHelperText.getHelperText());
    }

    @Test
    public void assertHelperComponent() {
        DatePickerElement dataPickerHelperComponent = $(DatePickerElement.class)
                .id("data-picker-helper-component");
        Assert.assertEquals("helper-component", dataPickerHelperComponent
                .getHelperComponent().getDomAttribute("id"));

        $("button").id("button-clear-component").click();
        Assert.assertNull(dataPickerHelperComponent.getHelperComponent());
    }
}
