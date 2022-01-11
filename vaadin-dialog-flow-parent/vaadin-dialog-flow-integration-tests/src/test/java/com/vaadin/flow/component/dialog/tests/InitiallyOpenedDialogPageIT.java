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
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-dialog/initial-dialog-open")
public class InitiallyOpenedDialogPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void openDialogDuringPageLoad() {
        waitForElementPresent(By.tagName(DialogTestPageIT.DIALOG_OVERLAY_TAG));
        WebElement overlay = findElement(
                By.tagName(DialogTestPageIT.DIALOG_OVERLAY_TAG));
        Assert.assertTrue(isElementPresent(By.id("nested-component")));
    }
}
