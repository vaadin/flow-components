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

import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.html.testbench.DivElement;
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
@TestPath("vaadin-side-nav/side-nav-test")
public class SideNavIT extends AbstractComponentIT {

    private SideNavElement sideNav;

    @Before
    public void init() {
        open();

        sideNav = $(SideNavElement.class).first();
    }

    @Test
    public void pageOpened_allItemsVisible() {
        Assert.assertEquals(7, sideNav.getItems().size());
    }

    @Test
    public void pageOpened_sideNavHasLabel() {
        Assert.assertEquals(sideNav.getLabel(), "Navigation Test");
    }

    @Test
    public void pageOpened_sideNavIsCollapsible() {
        Assert.assertTrue(sideNav.isCollapsible());
    }

    @Test
    public void clickLabelOnlyItem_urlNotChanged() {
        String initialUrl = getDriver().getCurrentUrl();
        sideNav.$(SideNavItemElement.class).id("label-only").navigate();

        Assert.assertEquals(initialUrl, getDriver().getCurrentUrl());
    }

    @Test
    public void clickEmptyPathItem_redirectedToBasePath() {
        String initialUrl = getDriver().getCurrentUrl();
        sideNav.$(SideNavItemElement.class).id("empty-path").navigate();

        Assert.assertNotEquals(initialUrl, getDriver().getCurrentUrl());
        Assert.assertTrue(initialUrl.contains(getDriver().getCurrentUrl()));
    }

    @Test
    public void clickClassTargetItem_urlChanged() {
        sideNav.$(SideNavItemElement.class).id("class-target").navigate();

        Assert.assertTrue(getDriver().getCurrentUrl()
                .contains("side-nav-test-target-view"));
    }

    @Test
    public void clickClassTargetComponentItem_urlChanged() {
        sideNav.$(SideNavItemElement.class).id("class-target-prefix-component")
                .navigate();

        Assert.assertTrue(getDriver().getCurrentUrl()
                .contains("side-nav-test-target-view"));
    }

    @Test
    public void clickPathTargetItem_urlChanged() {
        sideNav.$(SideNavItemElement.class).id("path-target").navigate();

        Assert.assertTrue(getDriver().getCurrentUrl()
                .contains("side-nav-test-target-view"));
    }

    @Test
    public void clickPathTargetIconItem_urlChanged() {
        sideNav.$(SideNavItemElement.class).id("path-target-icon").navigate();

        Assert.assertTrue(getDriver().getCurrentUrl()
                .contains("side-nav-test-target-view"));
    }

    @Test
    public void clickAddItem_itemAdded() {
        $(NativeButtonElement.class).id("add-item").click();
        getCommandExecutor().waitForVaadin();

        Assert.assertEquals(8, sideNav.getItems().size());
        Assert.assertEquals("Added item",
                sideNav.$(SideNavItemElement.class).last().getText());
    }

    @Test
    public void clickRemoveItem_itemRemoved() {
        $(NativeButtonElement.class).id("remove-item").click();
        getCommandExecutor().waitForVaadin();

        Assert.assertEquals(6, sideNav.getItems().size());
    }

    @Test
    public void clickRemoveAllItems_allItemsRemoved() {
        $(NativeButtonElement.class).id("remove-all-items").click();
        getCommandExecutor().waitForVaadin();

        Assert.assertEquals(0, sideNav.getItems().size());
    }

    @Test
    public void clickRemoveAllItems_sideNavLabelStillVisible() {
        $(NativeButtonElement.class).id("remove-all-items").click();
        getCommandExecutor().waitForVaadin();

        Assert.assertEquals(sideNav.getLabel(), "Navigation Test");
    }

    @Test
    public void clickChangeLabel_labelChanged() {
        $(NativeButtonElement.class).id("change-label").click();

        Assert.assertEquals(sideNav.getLabel(), "Label changed");
    }

    @Test
    public void clickMakeNotCollapsible_isNotCollapsible() {
        $(NativeButtonElement.class).id("toggle-collapsible").click();

        Assert.assertFalse(sideNav.isCollapsible());
    }

    @Test
    public void collapseSideNav_expandedStateSynchronized() {
        assertExpandedStateOnServer("true");

        sideNav.clickExpandButton();

        assertExpandedStateOnServer("false");
    }

    private void assertExpandedStateOnServer(String expectedState) {
        $(NativeButtonElement.class).id("print-expanded-state").click();
        final String expandedState = $(DivElement.class)
                .id("expanded-state-printout").getText();
        Assert.assertEquals(expandedState, expectedState);
    }
}
