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
    private SideNavItemElement nonNavigableParent;
    private SideNavItemElement navigableParent;
    private SideNavItemElement currentItem;

    @Before
    public void init() {
        open();

        sideNav = $(SideNavElement.class).first();
        nonNavigableParent = sideNav.$(SideNavItemElement.class)
                .id("non-navigable-parent");
        navigableParent = sideNav.$(SideNavItemElement.class)
                .id("navigable-parent");
        currentItem = sideNav.$(SideNavItemElement.class).id("current-item");
    }

    @Test
    public void pageOpened_itemHierarchyRendered() {
        Assert.assertEquals(3, sideNav.getItems().size());
        Assert.assertEquals(3, sideNav.getItems().get(0).getItems().size());
        Assert.assertEquals(2, sideNav.getItems().get(1).getItems().size());
        Assert.assertEquals(0, sideNav.getItems().get(2).getItems().size());
    }

    @Test
    public void clickNonNavigableParent_childNodesDisplayed() {
        Assert.assertFalse(nonNavigableParent.isExpanded());

        nonNavigableParent.click();

        Assert.assertTrue(nonNavigableParent.isExpanded());
    }

    @Test
    public void clickNavigableParent_urlChanged() {
        navigableParent.click();

        Assert.assertTrue(getDriver().getCurrentUrl()
                .contains("side-nav-test-target-view"));
    }

    @Test
    public void clickChildOfNavigableParent_urlChanged() {
        navigableParent.toggle();
        navigableParent.getItems().get(0).click();

        Assert.assertTrue(getDriver().getCurrentUrl()
                .contains("side-nav-test-target-view"));
    }

    @Test
    public void clickExpandItem_itemExpanded() {
        navigableParent.toggle();

        Assert.assertTrue(navigableParent.isExpanded());
    }

    @Test
    public void clickExpandAndCollapse_itemCollapsed() {
        navigableParent.toggle();
        navigableParent.toggle();

        Assert.assertFalse(navigableParent.isExpanded());
    }

    @Test
    public void expandItem_expandedStateSynchronized() {
        assertExpandedStateOnServer("print-item-expanded-state", "false");

        navigableParent.toggle();

        assertExpandedStateOnServer("print-item-expanded-state", "true");
    }

    @Test
    public void collapseSideNav_expandedStateSynchronized() {
        assertExpandedStateOnServer("print-side-nav-expanded-state", "true");

        sideNav.toggle();

        assertExpandedStateOnServer("print-side-nav-expanded-state", "false");
    }

    @Test
    public void pageOpened_itemWithMatchingPathIsCurrent() {
        Assert.assertTrue(currentItem.isCurrent());
    }

    @Test
    public void navigateWithParametersInUrl_itemWithMatchingPathIsCurrent() {
        getDriver().navigate().to(getDriver().getCurrentUrl() + "?key=value");
        waitUntil(driver -> $(SideNavElement.class).exists(), 1);

        Assert.assertTrue(
                $(SideNavItemElement.class).id("current-item").isCurrent());
    }

    @Test
    public void addParametersToCurrentItem_itemWithMatchingPathIsCurrent() {
        $(NativeButtonElement.class).id("add-parameters-to-current-item")
                .click();

        Assert.assertTrue(currentItem.isCurrent());
    }

    @Test
    public void itemWithParameters_removeParametersFromItem_itemWithMatchingPathIsCurrent() {
        $(NativeButtonElement.class).id("add-parameters-to-current-item")
                .click();

        $(NativeButtonElement.class).id("remove-parameters-from-current-item")
                .click();

        Assert.assertTrue(currentItem.isCurrent());
    }

    private void assertExpandedStateOnServer(String buttonToClick,
            String expectedState) {
        $(NativeButtonElement.class).id(buttonToClick).click();
        final String expandedState = $(DivElement.class)
                .id("expanded-state-printout").getText();
        Assert.assertEquals(expandedState, expectedState);
    }
}
