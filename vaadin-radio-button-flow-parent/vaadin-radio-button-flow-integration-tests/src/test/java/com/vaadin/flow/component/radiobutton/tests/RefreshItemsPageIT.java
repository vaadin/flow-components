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
package com.vaadin.flow.component.radiobutton.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.radiobutton.testbench.RadioButtonGroupElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-radio-button/refresh-items")
public class RefreshItemsPageIT extends AbstractComponentIT {

    @Test
    public void resetComponentOnItemRefresh() {
        open();

        findElement(By.id("reset")).click();

        List<TestBenchElement> radioButtons = $("vaadin-radio-button").all();
        Assert.assertEquals(2, radioButtons.size());

        Assert.assertEquals("bar", radioButtons.get(0).getText());
        Assert.assertEquals("baz", radioButtons.get(1).getText());
    }

    @Test
    public void resetComponentExpectLabel() {
        open();

        findElement(By.id("reset")).click();

        RadioButtonGroupElement group = $(RadioButtonGroupElement.class)
                .id("group");
        String label = group.findElement(By.cssSelector("label[slot='label']"))
                .getText();
        Assert.assertEquals("Label", label);
    }
}
