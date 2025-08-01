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
package com.vaadin.flow.component.menubar.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.menubar.testbench.MenuBarElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-menu-bar/close")
public class MenuBarCloseIT extends AbstractComponentIT {

    private MenuBarElement menuBar;

    @Before
    public void init() {
        open();
        menuBar = $(MenuBarElement.class).first();
    }

    @Test
    public void clickingCloseButton_closesSubmenu() {
        menuBar.getButtons().get(0).click();

        Assert.assertEquals(Boolean.TRUE.toString(),
                menuBar.getSubMenu().getDomProperty("opened"));

        clickElementWithJs("close-button");

        Assert.assertEquals(Boolean.FALSE.toString(),
                menuBar.getSubMenu().getDomProperty("opened"));
    }
}
