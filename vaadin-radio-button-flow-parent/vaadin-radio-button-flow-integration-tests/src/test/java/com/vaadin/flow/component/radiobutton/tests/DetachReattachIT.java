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
package com.vaadin.flow.component.radiobutton.tests;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-radio-button/detach-reattach")
public class DetachReattachIT extends AbstractComponentIT {
    @Test
    public void attachWithValue_detach_attachWithAnotherValue() {
        open();
        WebElement valueBlock = findElement(By.id("valueBlock"));

        clickButton("valueA");
        clickButton("addGroup");
        clickButton("getValueTemplate");
        Assert.assertEquals(valueBlock.getText(), "A");

        clickButton("removeGroup");
        clickButton("valueB");
        clickButton("addGroup");
        clickButton("getValueTemplate");
        Assert.assertEquals(valueBlock.getText(), "B");
    }

    @Test
    public void selectValue_detachRadioButtonGroup_reattach_valuesChecked() {
        open();
        WebElement valueBlock = findElement(By.id("valueBlock"));

        clickButton("setValue");
        clickButton("getValue");
        String value = valueBlock.getText();

        clickButton("detach");
        clickButton("attach");
        clickButton("getValue");
        Assert.assertEquals("Radio button should remain checked on reattach",
                value, valueBlock.getText());
    }

    private void clickButton(String id) {
        $("button").id(id).click();
    }
}
