/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-context-menu/nested-targets")
public class NestedTargetsIT extends AbstractContextMenuIT {

    @Before
    public void init() {
        open();
        verifyClosed();
    }

    @Test
    public void nestedTargets_rightClickParentTargetOutsideChildTarget_onlyParentTargetMenuOpened() {
        rightClickOn("not-in-child-target");
        verifyNumOfOverlays(1);

        Assert.assertArrayEquals(new String[] { "menu on parent target" },
                getMenuItemCaptions());

        getMenuItems().get(0).click();
        verifyClosed();
        Assert.assertEquals("parent", findElement(By.id("messages")).getText());
    }

    @Test
    public void nestedTargets_rightClickChildTarget_onlyChildTargetMenuOpened() {
        rightClickOn("child-target");
        verifyNumOfOverlays(1);

        Assert.assertArrayEquals(new String[] { "menu on child target" },
                getMenuItemCaptions());

        getMenuItems().get(0).click();
        verifyClosed();
        Assert.assertEquals("child", findElement(By.id("messages")).getText());
    }
}
