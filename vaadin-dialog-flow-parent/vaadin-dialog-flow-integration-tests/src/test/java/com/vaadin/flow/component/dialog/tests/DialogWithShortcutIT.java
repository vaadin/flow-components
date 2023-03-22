package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.InputTextElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

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
    @Ignore("flaky test see https://github.com/vaadin/flow-components/issues/777")
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
    @Ignore("flaky test see https://github.com/vaadin/flow-components/issues/777")
    public void dialogOpenedWithShortcutNoListenOn_sameShortcutListeningOnUi_bothExecuted() {
        openDialogButton = $(NativeButtonElement.class)
                .id(DialogWithShortcutPage.SHORTCUT_ON_UI);
        pressShortcutKey(uiLevelButton);
        validateLatestShortcutEvent(0, DialogWithShortcutPage.UI_BUTTON);

        openNewDialog();

        pressShortcutKey(getFirstDialogInput());
        // last event is on dialog
        validateLatestShortcutEventOnDialog(2, 0);
        validateShortcutEvent(1, 1, DialogWithShortcutPage.UI_BUTTON);

        closeDialog();
        pressShortcutKey(uiLevelButton);
        validateLatestShortcutEvent(3, DialogWithShortcutPage.UI_BUTTON);
    }

    @Test
    @Ignore("flaky test see https://github.com/vaadin/flow-components/issues/777")
    public void dialogOpenedWithListenOnShortcut_dialogReopened_oldShortcutStillWorks() {
        openDialogButton = $(NativeButtonElement.class)
                .id(DialogWithShortcutPage.REUSABLE_DIALOG);

        pressShortcutKey(uiLevelButton);
        validateLatestShortcutEvent(0, DialogWithShortcutPage.UI_BUTTON);

        openNewDialog();

        pressShortcutKey(getFirstDialogInput());
        validateLatestShortcutEventOnDialog(1, 0);

        pressShortcutKey(uiLevelButton);
        validateLatestShortcutEvent(2, DialogWithShortcutPage.UI_BUTTON);

        closeDialog();

        pressShortcutKey(uiLevelButton);
        validateLatestShortcutEvent(3, DialogWithShortcutPage.UI_BUTTON);

        openNewDialog();

        pressShortcutKey(getFirstDialogInput());
        validateLatestShortcutEventOnDialog(4, 0);
    }

    // vaadin/vaadin-dialog#229
    @Test
    @Ignore("flaky test see https://github.com/vaadin/flow-components/issues/777")
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

        waitUntil(driver -> !eventLog.findElements(By.tagName("div")).isEmpty());

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
        final WebElement latestEvent = eventLog.findElements(By.tagName("div"))
                .get(indexFromTop);
        Assert.assertEquals("Invalid latest event",
                eventCounter + "-" + eventSourceId, latestEvent.getText());
    }
}
