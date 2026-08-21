/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import com.vaadin.flow.component.contextmenu.testbench.ContextMenuElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-context-menu/sub-menu-visibility")
public class SubMenuVisibilityIT extends AbstractContextMenuIT {

    private TestBenchElement target;

    @Before
    public void init() {
        open();
        target = $("p").id("target");
    }

    @Test
    public void hiddenItem_show_subMenuRendered() {
        ContextMenuElement menu = ContextMenuElement.openByRightClick(target);
        Assert.assertFalse("Expected the item to be hidden initially",
                menu.getMenuItems().get(0).isDisplayed());
        clickBody();
        menu.waitUntilClosed();

        $("button").id("show-item").click();

        menu = ContextMenuElement.openByRightClick(target);
        ContextMenuElement subMenu = menu.getMenuItems().get(0).openSubMenu();
        Assert.assertArrayEquals(new String[] { "Sub item 1", "Sub item 2" },
                getMenuItemCaptions(getMenuItems(subMenu)));
    }
}
