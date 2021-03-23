package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.flow.component.dialog.testbench.DialogElement;
import com.vaadin.flow.component.html.testbench.DivElement;
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
        validateLatestShortcutEventOnDialog(2, 0);
        validateShortcutEvent(1, 1, DialogWithShortcutPage.UI_BUTTON);

        closeDialog();
        pressShortcutKey(uiLevelButton);
        validateLatestShortcutEvent(3, DialogWithShortcutPage.UI_BUTTON);
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

    private void openNewDialog() {
        openDialogButton.click();
    }

    private void closeDialog() {
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();
    }

    private TestBenchElement getFirstDialogInput() {
        return getDialogInput(0);
    }

    private TestBenchElement getDialogInput(int dialogIndex) {
        return $(DialogElement.class)
                .id(DialogWithShortcutPage.DIALOG_ID + dialogIndex).$("input")
                .first();
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
