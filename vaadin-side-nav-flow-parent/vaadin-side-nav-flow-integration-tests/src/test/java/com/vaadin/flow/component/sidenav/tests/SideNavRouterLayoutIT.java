/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.sidenav.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.component.sidenav.testbench.SideNavElement;
import com.vaadin.flow.component.sidenav.testbench.SideNavItemElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link SideNavPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-side-nav/side-nav-test-router-layout-target1")
public class SideNavRouterLayoutIT extends AbstractComponentIT {

    private SideNavElement sideNav;

    @Before
    public void init() {
        open();

        sideNav = $(SideNavElement.class).first();
    }

    @Test
    public void pageOpened_target1LinkActive() {
        final SideNavItemElement target1 = sideNav.$(SideNavItemElement.class)
                .id("target-1");

        Assert.assertTrue(target1.isActive());
    }

    @Test
    public void clickTarget2_target2Active() {
        final SideNavItemElement target2 = sideNav.$(SideNavItemElement.class)
                .id("target-2");
        target2.navigate();

        Assert.assertTrue(getDriver().getCurrentUrl()
                .contains("side-nav-test-router-layout-target2"));
        Assert.assertTrue(target2.isActive());
    }

    @Test
    public void clickTarget2_target1NotActive() {
        final SideNavItemElement target1 = sideNav.$(SideNavItemElement.class)
                .id("target-1");
        final SideNavItemElement target2 = sideNav.$(SideNavItemElement.class)
                .id("target-2");
        target2.navigate();

        Assert.assertFalse(target1.isActive());
    }
}
