
package com.vaadin.flow.component.contextmenu.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-context-menu/auto-attached-context-menu")
public class AutoAttachedContextMenuIT extends AbstractContextMenuIT {

    public static final String TARGET_ID = "target-for-not-attached-context-menu";
    private final String MENU_ID = "not-attached-context-menu";

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("label"));
        checkLogsForErrors();
    }

    @Test
    public void contextMenuNotAttachedToThePage_openAndClose_contextMenuIsAttachedAndRemoved() {
        waitForElementNotPresent(By.id(MENU_ID));

        rightClickOn(TARGET_ID);
        waitForElementPresent(By.id(MENU_ID));
        verifyOpened();

        clickBody();
        waitForElementNotPresent(By.id(MENU_ID));
        verifyClosed();
    }

    @Test
    public void autoAttachedContextMenu_openMultipleTimes() {
        open();

        rightClickOn(TARGET_ID);
        verifyOpened();
        clickBody();
        verifyClosed();
        rightClickOn(TARGET_ID);

        verifyOpened();
        Assert.assertEquals("Auto-attached context menu",
                getOverlay().getAttribute("innerText"));

        checkLogsForErrors();
    }
}
