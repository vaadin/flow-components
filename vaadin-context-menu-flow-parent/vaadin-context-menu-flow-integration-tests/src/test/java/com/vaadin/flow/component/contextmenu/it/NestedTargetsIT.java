/*
 * Copyright 2000-2019 Vaadin Ltd.
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
