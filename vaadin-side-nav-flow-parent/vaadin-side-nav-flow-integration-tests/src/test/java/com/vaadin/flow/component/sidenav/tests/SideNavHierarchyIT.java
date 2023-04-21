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
 * Integration tests for the {@link SideNavHierarchyPage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-side-nav/side-nav-hierarchy-test")
public class SideNavHierarchyIT extends AbstractComponentIT {

    private SideNavElement sideNav;
    private SideNavItemElement nonNavigableParent;
    private SideNavItemElement navigableParent;

    @Before
    public void init() {
        open();

        sideNav = $(SideNavElement.class).first();
        nonNavigableParent = sideNav.$(SideNavItemElement.class)
                .id("non-navigable-parent");
        navigableParent = sideNav.$(SideNavItemElement.class)
                .id("navigable-parent");
    }

    @Test
    public void pageOpened_twoParentNodesVisible() {
        Assert.assertEquals(2, sideNav.getItems().size());
    }

    @Test
    public void pageOpened_navigableParentHasLabel() {
        Assert.assertEquals("Navigable parent", navigableParent.getLabel());
    }

    @Test
    public void clickNonNavigableParent_childNodesDisplayed() {
        Assert.assertFalse(nonNavigableParent.isExpanded());

        nonNavigableParent.navigate();

        Assert.assertTrue(nonNavigableParent.isExpanded());
    }

    @Test
    public void clickNavigableParent_urlChanged() {
        navigableParent.navigate();

        Assert.assertTrue(getDriver().getCurrentUrl()
                .contains("side-nav-test-target-view"));
    }

    @Test
    public void clickChildOfNonNavigableParent_urlChanged() {
        nonNavigableParent.clickExpandButton();
        nonNavigableParent.getItems().get(1).navigate();

        Assert.assertTrue(getDriver().getCurrentUrl()
                .contains("side-nav-test-target-view"));
    }

    @Test
    public void clickChildOfNavigableParent_urlChanged() {
        navigableParent.clickExpandButton();
        navigableParent.getItems().get(0).navigate();

        Assert.assertTrue(getDriver().getCurrentUrl()
                .contains("side-nav-test-target-view"));
    }

    @Test
    public void pageOpened_sideNavHasLabel() {
        Assert.assertEquals(sideNav.getLabel(), "Navigation");
    }

    @Test
    public void pageOpened_sideNavIsNotCollapsible() {
        Assert.assertFalse(sideNav.isCollapsible());
    }

    @Test
    public void pageOpened_navigableParentHasTwoChildren() {
        Assert.assertEquals(navigableParent.getItems().size(), 2);
    }

    @Test
    public void clickAddItem_subItemAdded() {
        $(NativeButtonElement.class).id("add-sub-item").click();
        getCommandExecutor().waitForVaadin();

        Assert.assertEquals(3, navigableParent.getItems().size());
        Assert.assertEquals("Added item",
                navigableParent.$(SideNavItemElement.class).last().getText());
    }

    @Test
    public void clickRemoveItem_subItemRemoved() {
        $(NativeButtonElement.class).id("remove-sub-item").click();
        getCommandExecutor().waitForVaadin();

        Assert.assertEquals(1, navigableParent.getItems().size());
    }

    @Test
    public void clickRemoveAllItems_allSubItemsRemoved() {
        $(NativeButtonElement.class).id("remove-all-sub-items").click();
        getCommandExecutor().waitForVaadin();

        Assert.assertEquals(0, navigableParent.getItems().size());
    }

    @Test
    public void clickRemoveAllItems_labelStillVisible() {
        $(NativeButtonElement.class).id("remove-all-sub-items").click();
        getCommandExecutor().waitForVaadin();

        Assert.assertEquals(navigableParent.getLabel(), "Navigable parent");
    }

    @Test
    public void clickExpandItem_itemExpanded() {
        $(NativeButtonElement.class).id("expand-item").click();

        Assert.assertTrue(navigableParent.isExpanded());
    }

    @Test
    public void clickExpandAndCollapse_itemCollapsed() {
        $(NativeButtonElement.class).id("expand-item").click();
        $(NativeButtonElement.class).id("collapse-item").click();

        Assert.assertFalse(navigableParent.isExpanded());
    }

    @Test
    public void clickChangePath_itemPathChanged() {
        $(NativeButtonElement.class).id("set-path").click();
        getCommandExecutor().waitForVaadin();

        navigableParent.navigate();

        Assert.assertTrue(
                getDriver().getCurrentUrl().contains("side-nav-test"));
    }

    @Test
    public void clickChangeLabel_itemLabelChanged() {
        $(NativeButtonElement.class).id("change-label").click();
        getCommandExecutor().waitForVaadin();

        Assert.assertEquals(navigableParent.getLabel(), "Changed label");
    }
}
