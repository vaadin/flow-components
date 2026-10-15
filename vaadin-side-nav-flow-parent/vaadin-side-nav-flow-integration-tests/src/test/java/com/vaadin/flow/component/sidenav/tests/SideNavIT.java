/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.sidenav.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

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

        waitUntil(ExpectedConditions.urlContains("side-nav-test-target-view"));
    }

    @Test
    public void clickChildOfNavigableParent_urlChanged() {
        navigableParent.toggle();
        navigableParent.getItems().get(0).click();

        waitUntil(ExpectedConditions.urlContains("side-nav-test-target-view"));
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
    public void navigateToPage_correctItemIsCurrent() {
        // First navigate away from the page
        navigableParent.click();
        waitUntil(ExpectedConditions
                .presenceOfElementLocated(By.id("navigate-to-main-page")), 1);

        $(NativeButtonElement.class).id("navigate-to-main-page").click();
        waitUntil(driver -> $(SideNavElement.class).exists(), 1);

        Assert.assertTrue(
                $(SideNavItemElement.class).id("current-item").isCurrent());
    }

    private void assertExpandedStateOnServer(String buttonToClick,
            String expectedState) {
        $(NativeButtonElement.class).id(buttonToClick).click();
        final String expandedState = $(DivElement.class)
                .id("expanded-state-printout").getText();
        Assert.assertEquals(expandedState, expectedState);
    }
}
