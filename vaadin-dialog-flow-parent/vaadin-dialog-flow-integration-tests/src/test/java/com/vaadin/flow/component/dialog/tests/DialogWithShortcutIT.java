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
package com.vaadin.flow.component.dialog.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.InputTextElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-dialog/shortcuts")
public class DialogWithShortcutIT extends AbstractComponentIT {

    private TestBenchElement eventLog;
    private TestBenchElement openDialogButton;
    private NativeButtonElement uiLevelButton;

    @Before
    public void init() {
        open();
        eventLog = $(DivElement.class).id(DialogWithShortcutPage.EVENT_LOG);
        uiLevelButton = $(NativeButtonElement.class)
                .id(DialogWithShortcutPage.UI_BUTTON);
    }

    // #7799
    @Test
    public void dialogOpenedWithListenOnShortcut_sameShortcutListeningOnUi_focusDecidesWhichIsExecuted() {
        openDialogButton = $(NativeButtonElement.class)
                .id(DialogWithShortcutPage.LISTEN_ON_DIALOG);
        pressShortcutKey(uiLevelButton);
        validateLatestShortcutEvent(0, DialogWithShortcutPage.UI_BUTTON);

        openNewDialog();
        pressShortcutKey(getFirstDialogInput());
        validateLatestShortcutEventOnDialog(1, 0);

        pressShortcutKey(getFirstDialogInput());
        validateLatestShortcutEventOnDialog(2, 0);

        closeDialog();
        pressShortcutKey(uiLevelButton);
        validateLatestShortcutEvent(3, DialogWithShortcutPage.UI_BUTTON);
    }

    @Test
    public void dialogOpenedWithShortcutNoListenOn_sameShortcutListeningOnUi_bothExecuted() {
        openDialogButton = $(NativeButtonElement.class)
                .id(DialogWithShortcutPage.SHORTCUT_ON_UI);
        pressShortcutKey(uiLevelButton);
        validateLatestShortcutEvent(0, DialogWithShortcutPage.UI_BUTTON);

        openNewDialog();

        pressShortcutKey(getFirstDialogInput());
        // last event is on dialog
        validateLatestShortcutEventOnDialog(1, 0);
        validateShortcutEvent(1, 0, DialogWithShortcutPage.UI_BUTTON);

        closeDialog();
        pressShortcutKey(uiLevelButton);
        validateLatestShortcutEvent(2, DialogWithShortcutPage.UI_BUTTON);
    }

    @Test
    public void dialogOpenedWithListenOnShortcut_dialogReopened_oldShortcutStillWorks() {
        openDialogButton = $(NativeButtonElement.class)
                .id(DialogWithShortcutPage.REUSABLE_DIALOG);

        pressShortcutKey(uiLevelButton);
        validateLatestShortcutEvent(0, DialogWithShortcutPage.UI_BUTTON);

        openNewDialog();

        pressShortcutKey(getFirstDialogInput());
        validateLatestShortcutEventOnDialog(1, 0);

        closeDialog();

        pressShortcutKey(uiLevelButton);
        validateLatestShortcutEvent(2, DialogWithShortcutPage.UI_BUTTON);

        openNewDialog();

        pressShortcutKey(getFirstDialogInput());
        validateLatestShortcutEventOnDialog(3, 0);
    }

    // vaadin/vaadin-dialog#229
    @Test
    public void twoModelessDialogsOpenedWithSameShortcutKeyOnListenOn_dialogWithFocusExecuted() {
        openDialogButton = $(NativeButtonElement.class)
                .id(DialogWithShortcutPage.MODELESS_SHORTCUT_LISTEN_ON_DIALOG);

        openNewDialog();
        openNewDialog();

        pressShortcutKey(getFirstDialogInput());
        validateLatestShortcutEventOnDialog(0, 0);

        pressShortcutKey(getDialogInput(1));
        validateLatestShortcutEventOnDialog(1, 1);

        pressShortcutKey(getFirstDialogInput());
        validateLatestShortcutEventOnDialog(2, 0);

        pressShortcutKey(uiLevelButton);
        validateLatestShortcutEvent(3, DialogWithShortcutPage.UI_BUTTON);

        pressShortcutKey(getDialogInput(1));
        validateLatestShortcutEventOnDialog(4, 1);
    }

    // vaadin/vaadin-flow#10362
    @Test
    public void dialogWithShortcut_containsInput_shortcutDoesNotBlockInput() {
        openDialogButton = $(NativeButtonElement.class)
                .id(DialogWithShortcutPage.LISTEN_ON_DIALOG);

        openNewDialog();
        final InputTextElement dialogInput = getFirstDialogInput();
        dialogInput.focus();
        dialogInput.sendKeys("foo" + DialogWithShortcutPage.SHORTCUT + "bar");

        waitUntil(
                driver -> !eventLog.findElements(By.tagName("div")).isEmpty());

        validateLatestShortcutEventOnDialog(0, 0);

        // by default the shortcut does preventDefault
        Assert.assertEquals("Shortcut did not update input value", "foobar",
                getFirstDialogInput().getValue());
    }

    // vaadin/vaadin-flow#10362
    @Test
    public void dialogWithShortcutAndAllowBrowserDefault_containsInput_shortcutDoesNotBlockInput() {
        openDialogButton = $(NativeButtonElement.class)
                .id(DialogWithShortcutPage.LISTEN_ON_DIALOG_ALLOW_DEFAULT);

        openNewDialog();
        final InputTextElement dialogInput = getFirstDialogInput();
        dialogInput.focus();
        dialogInput.sendKeys("foo" + DialogWithShortcutPage.SHORTCUT + "bar");

        validateLatestShortcutEventOnDialog(0, 0);

        // shortcut key is registered
        Assert.assertEquals("Shortcut did not update input value",
                "foo" + DialogWithShortcutPage.SHORTCUT + "bar",
                getFirstDialogInput().getValue());
    }

    private void openNewDialog() {
        openDialogButton.click();
    }

    private void closeDialog() {
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();
    }

    private InputTextElement getFirstDialogInput() {
        return getDialogInput(0);
    }

    private InputTextElement getDialogInput(int dialogIndex) {
        return $(DialogElement.class)
                .id(DialogWithShortcutPage.DIALOG_ID + dialogIndex)
                .$(InputTextElement.class).first();
    }

    private void pressShortcutKey(TestBenchElement elementToFocus) {
        elementToFocus.focus();
        elementToFocus.sendKeys("x");
    }

    private void validateLatestShortcutEventOnDialog(int eventCounter,
            int dialogId) {
        validateShortcutEvent(0, eventCounter,
                DialogWithShortcutPage.DIALOG_ID + dialogId + "-button");
    }

    private void validateLatestShortcutEvent(int eventCounter,
            String eventSourceId) {
        validateShortcutEvent(0, eventCounter, eventSourceId);
    }

    private void validateShortcutEvent(int indexFromTop, int eventCounter,
            String eventSourceId) {
        waitForElementPresent(By.cssSelector("#%s div:nth-child(%d)".formatted(
                DialogWithShortcutPage.EVENT_LOG, indexFromTop + 1)));
        final WebElement latestEvent = eventLog.findElements(By.tagName("div"))
                .get(indexFromTop);
        Assert.assertEquals("Invalid latest event",
                eventCounter + "-" + eventSourceId, latestEvent.getText());
    }
}
