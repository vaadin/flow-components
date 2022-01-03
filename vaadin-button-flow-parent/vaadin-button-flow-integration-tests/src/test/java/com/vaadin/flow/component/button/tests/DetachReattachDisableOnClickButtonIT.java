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
package com.vaadin.flow.component.button.tests;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

@TestPath("vaadin-button/detach-reattach-disable-on-click-button")
public class DetachReattachDisableOnClickButtonIT extends AbstractComponentIT {

    private ButtonElement getDisableOnClickButton() {
        return $(ButtonElement.class).id("disable-on-click");
    }

    private void assertDisableOnClickButtonEnabled(
            ButtonElement disableOnClickButton) {
        Assert.assertTrue("'Disable on click' button should be enabled",
                disableOnClickButton.isEnabled());
    }

    private void assertDisableOnClickButtonDisabled(
            ButtonElement disableOnClickButton) {
        Assert.assertFalse("'Disable on click' button should be disabled",
                disableOnClickButton.isEnabled());
    }

    private void clickDisableOnClickButton(ButtonElement disableOnClickButton) {
        getCommandExecutor().disableWaitForVaadin();

        // Click 'Disable on click' button
        disableOnClickButton.click();

        // Check 'Disable on click' button is disabled
        assertDisableOnClickButtonDisabled(disableOnClickButton);

        waitUntil(ExpectedConditions.elementToBeClickable(
                $(ButtonElement.class).id("disable-on-click")), 2000);

        // Check 'Disable on click' button is enabled again
        assertDisableOnClickButtonEnabled(disableOnClickButton);

        getCommandExecutor().enableWaitForVaadin();
    }

    @Test
    public void testDetachingAndReattachingShouldKeepDisabledOnClick() {
        open();

        ButtonElement disableOnClickButton = getDisableOnClickButton();

        // Check 'Disable on click' button should be enabled
        assertDisableOnClickButtonEnabled(disableOnClickButton);

        // Remove 'Disable on click' button
        ButtonElement removeFromViewButton = $(ButtonElement.class)
                .id("remove-from-view");
        removeFromViewButton.click();

        waitUntil(ExpectedConditions
                .numberOfElementsToBe(By.id("disable-on-click"), 0), 2000);

        // Re-attach 'Disable on click" button
        ButtonElement addToViewButton = $(ButtonElement.class)
                .id("add-to-view");
        addToViewButton.click();

        waitUntil(ExpectedConditions
                .numberOfElementsToBe(By.id("disable-on-click"), 1), 2000);

        disableOnClickButton = getDisableOnClickButton();

        // Check 'Disable on click' button is enabled
        assertDisableOnClickButtonEnabled(disableOnClickButton);

        // Click 'Disable on click' button
        clickDisableOnClickButton(disableOnClickButton);
    }
}
