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
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.component.checkbox.testbench.CheckboxElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.elementsbase.Element;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-checkbox/checkbox-test")
public class CheckboxPageIT extends AbstractComponentIT {

    @Test
    public void testInitialChecked_is_false_initialIndeterminate_is_false() {
        testInitialValue(false, false);
    }

    @Test
    public void testInitialChecked_is_false_initialIndeterminate_is_true() {
        testInitialValue(false, true);
    }

    @Test
    public void testInitialChecked_is_true_initialIndeterminate_is_false() {
        testInitialValue(true, false);
    }

    @Test
    public void testInitialChecked_is_true_initialIndeterminate_is_true() {
        testInitialValue(true, true);
    }

    private void testInitialValue(boolean checkedExpected,
            boolean indeterminateExpected) {
        open();

        int id = checkedExpected ? 1 : 0;
        id += indeterminateExpected ? 2 : 0;

        CheckboxElement cb = $(CheckboxElement.class).id("cb-" + id);
        WebElement valueLabel = findElement(By.id("value-label-" + id));
        WebElement indeterminateLabel = findElement(
                By.id("indeterminate-label-" + id));

        Assert.assertEquals("Wrong checked value", checkedExpected,
                cb.isChecked());
        Assert.assertEquals("Wrong indeterminate value", indeterminateExpected,
                cb.getPropertyBoolean("indeterminate"));

        Assert.assertEquals("Extra checked change event",
                "Value: " + checkedExpected, valueLabel.getText());
        Assert.assertEquals("Extra indeterminate property change event",
                "Indeterminate: " + indeterminateExpected,
                indeterminateLabel.getText());

        // click to remove indeterminate state from the native input
        cb.click();

        // after clicking, checked value should have changed and indeterminate
        // is always false
        Assert.assertEquals("After click wrong checked value", !checkedExpected,
                cb.isChecked());
        Assert.assertEquals("After click wrong indeterminate value", false,
                cb.getPropertyBoolean("indeterminate"));

        Assert.assertEquals("No checked change event",
                "Value: 1 " + !checkedExpected, valueLabel.getText());
        if (indeterminateExpected) {
            Assert.assertEquals("No indeterminate property change event",
                    "Indeterminate: 1 " + false, indeterminateLabel.getText());
        } else {
            Assert.assertEquals("Extra indeterminate property change event",
                    "Indeterminate: " + false, indeterminateLabel.getText());
        }
    }
}
