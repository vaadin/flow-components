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
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.testbench.CheckboxGroupElement;
import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

@TestPath("vaadin-checkbox/helper")
public class HelperIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    /**
     * Assert that helper component exists after setItems.
     * https://github.com/vaadin/vaadin-checkbox/issues/191
     */
    @Test
    public void assertHelperComponentExists() {
        TestBenchElement checkboxGroup = $("vaadin-checkbox-group").first();

        TestBenchElement helperComponent = checkboxGroup.$("span")
                .attributeContains("slot", "helper").first();
        Assert.assertEquals("Helper text", helperComponent.getText());

    }

    @Test
    public void assertCheckboxGroupHelperGenerator() {
        CheckboxGroupElement group = $(CheckboxGroupElement.class)
                .id("cbg-helper-generator");

        List<CheckboxElement> checkboxes = group.getCheckboxes();
        Assert.assertEquals("A helper", checkboxes.get(0).getHelperText());
        Assert.assertEquals("B helper", checkboxes.get(1).getHelperText());
        Assert.assertEquals("C helper", checkboxes.get(2).getHelperText());

        $("button").id("clear-helper-generator").click();

        Assert.assertEquals("", checkboxes.get(0).getHelperText());
        Assert.assertEquals("", checkboxes.get(1).getHelperText());
        Assert.assertEquals("", checkboxes.get(2).getHelperText());
    }

    @Test
    public void assertCheckboxHelperText() {
        CheckboxElement checkboxHelperText = $(CheckboxElement.class)
                .id("checkbox-helper-text");
        Assert.assertEquals("Helper text", checkboxHelperText.getHelperText());

        $("button").id("empty-helper-text").click();
        Assert.assertEquals("", checkboxHelperText.getHelperText());
    }

    @Test
    public void assertCheckboxHelperComponent() {
        CheckboxElement checkboxHelperComponent = $(CheckboxElement.class)
                .id("checkbox-helper-component");
        Assert.assertEquals("helper-component", checkboxHelperComponent
                .getHelperComponent().getAttribute("id"));

        $("button").id("empty-helper-component").click();
        Assert.assertEquals(null, checkboxHelperComponent.getHelperComponent());
    }

}
