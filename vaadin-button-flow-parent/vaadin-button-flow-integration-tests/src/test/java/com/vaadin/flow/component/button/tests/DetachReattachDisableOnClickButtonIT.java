/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.button.tests;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

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
