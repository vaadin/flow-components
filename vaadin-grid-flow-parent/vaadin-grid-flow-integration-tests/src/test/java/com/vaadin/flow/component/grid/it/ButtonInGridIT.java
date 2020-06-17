/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-button-inside-grid")
public class ButtonInGridIT extends AbstractComponentIT {

    @Test
    @Ignore
    /**
     * The test is disabled due #4268. The test is for grid#122 (which is
     * actually an issue in the flow-component-renderer inside flow-data
     * module).
     *
     * At the moment the bug is not fixed. So the test fails. Should be enabled
     * back once the gird#122 is fixed.
     */
    public void pressButtonUsingKeyboard() {
        open();

        new Actions(getDriver()).sendKeys(Keys.TAB, Keys.TAB, Keys.SPACE)
                .build().perform();

        WebElement info = findElement(By.id("info"));
        Assert.assertEquals("foo", info.getText());
    }
}
