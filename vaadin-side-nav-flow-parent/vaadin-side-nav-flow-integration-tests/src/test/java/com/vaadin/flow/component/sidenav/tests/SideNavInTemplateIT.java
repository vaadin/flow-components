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
