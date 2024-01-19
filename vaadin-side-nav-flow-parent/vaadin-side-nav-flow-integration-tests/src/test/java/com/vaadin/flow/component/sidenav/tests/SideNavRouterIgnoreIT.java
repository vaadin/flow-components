/*
 * Copyright 2023 Vaadin Ltd.
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

import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.sidenav.testbench.SideNavElement;
import com.vaadin.flow.component.sidenav.testbench.SideNavItemElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Integration tests for {@code router-ignore} attribute in {@link SideNavItem}
 * component
 *
 * @author Vaadin Ltd.
 */
@TestPath("vaadin-side-nav/router-ignore")
public class SideNavRouterIgnoreIT extends AbstractComponentIT {

    private SideNavItemElement noPath;
    private SideNavItemElement pathSetAsComponent;
    private SideNavItemElement completePathSetAsString;
    private SideNavItemElement partialPathSetAsString;

    @Before
    public void init() {
        open();

        SideNavElement sideNav = $(SideNavElement.class).first();

        noPath = sideNav.$(SideNavItemElement.class).id("no-path");
        pathSetAsComponent = sideNav.$(SideNavItemElement.class)
                .id("path-set-as-component");
        completePathSetAsString = sideNav.$(SideNavItemElement.class)
                .id("complete-path-set-as-string");
        partialPathSetAsString = sideNav.$(SideNavItemElement.class)
                .id("partial-path-set-as-string");
    }

    @Test
    public void itemWithNoPathSet_routerIgnoreIsNotApplied() {
        assertDoesNotHaveRouterIgnore(noPath);
    }

    @Test
    public void itemWithPathSetAsComponent_routerIgnoreIsNotApplied() {
        assertDoesNotHaveRouterIgnore(pathSetAsComponent);
    }

    @Test
    public void itemWithCompletePathSetAsString_routerIgnoreIsApplied() {
        assertHasRouterIgnore(completePathSetAsString);
    }

    @Test
    public void itemWithPartialPathSetAsString_routerIgnoreIsNotApplied() {
        assertDoesNotHaveRouterIgnore(partialPathSetAsString);
    }

    @Test
    public void setPathsNullAsString_routerIgnoreIsNotApplied() {
        $(NativeButtonElement.class).id("set-paths-null-as-string").click();
        List.of(pathSetAsComponent, completePathSetAsString,
                partialPathSetAsString)
                .forEach(this::assertDoesNotHaveRouterIgnore);
    }

    @Test
    public void setPathsNullAsComponent_routerIgnoreIsNotApplied() {
        $(NativeButtonElement.class).id("set-paths-null-as-component").click();
        List.of(pathSetAsComponent, completePathSetAsString,
                partialPathSetAsString)
                .forEach(this::assertDoesNotHaveRouterIgnore);
    }

    @Test
    public void setPathsAsComponent_routerIgnoreIsNotApplied() {
        $(NativeButtonElement.class).id("set-paths-as-component").click();
        List.of(noPath, completePathSetAsString, partialPathSetAsString)
                .forEach(this::assertDoesNotHaveRouterIgnore);
    }

    @Test
    public void setCompletePathsAsString_routerIgnoreIsApplied() {
        $(NativeButtonElement.class).id("set-complete-paths-as-string").click();
        List.of(noPath, pathSetAsComponent, partialPathSetAsString)
                .forEach(this::assertHasRouterIgnore);
    }

    @Test
    public void setPartialPathsAsString_routerIgnoreNotIsApplied() {
        $(NativeButtonElement.class).id("set-partial-paths-as-string").click();
        List.of(noPath, pathSetAsComponent, completePathSetAsString)
                .forEach(this::assertDoesNotHaveRouterIgnore);
    }

    private void assertHasRouterIgnore(SideNavItemElement item) {
        Assert.assertNotNull(item.getAnchor().getAttribute("router-ignore"));
    }

    private void assertDoesNotHaveRouterIgnore(SideNavItemElement item) {
        Assert.assertNull(item.getAnchor().getAttribute("router-ignore"));
    }
}
