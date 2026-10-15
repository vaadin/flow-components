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

import com.vaadin.flow.component.sidenav.testbench.SideNavElement;
import com.vaadin.flow.component.sidenav.testbench.SideNavItemElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link SideNavInTemplatePage}.
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-side-nav/side-nav-in-template")
public class SideNavInTemplateIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void sideNavItem_getLabel_returnsSingleNodeTextContent() {
        SideNavElement sideNav = $("side-nav-in-template").first()
                .$(SideNavElement.class).first();
        SideNavItemElement item = sideNav.getItems().get(0);

        Assert.assertEquals("Home", item.getLabel());
    }

    @Test
    public void sideNavItem_getLabel_returnsMergedNodesTextContent() {
        SideNavElement sideNav = $("side-nav-in-template").first()
                .$(SideNavElement.class).first();
        SideNavItemElement item = sideNav.getItems().get(1);

        Assert.assertEquals("About this project", item.getLabel());
    }
}
