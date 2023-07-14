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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;

import java.util.List;

/**
 * Test view for {@code router-ignore} attribute in {@link SideNavItem}
 * component
 */
@Route("vaadin-side-nav/router-ignore")
public class SideNavRouterIgnorePage extends Div {

    public SideNavRouterIgnorePage() {
        SideNav sideNav = new SideNav();
        sideNav.setLabel("Router Ignore Test");

        SideNavItem noPath = new SideNavItem("No path");
        noPath.setId("no-path");

        SideNavItem pathSetAsComponent = new SideNavItem(
                "Path set as component", SideNavPage.class);
        pathSetAsComponent.setId("path-set-as-component");

        SideNavItem completePathSetAsString = new SideNavItem(
                "Complete path set as string",
                "https://vaadin.com/docs/latest/");
        completePathSetAsString.setId("complete-path-set-as-string");

        SideNavItem partialPathSetAsString = new SideNavItem(
                "Partial path set as string",
                "vaadin-side-nav/side-nav-test-target-view");
        partialPathSetAsString.setId("partial-path-set-as-string");

        sideNav.addItem(noPath, pathSetAsComponent, completePathSetAsString,
                partialPathSetAsString);

        NativeButton setPathsNullAsString = new NativeButton(
                "Set paths null as string",
                click -> List
                        .of(pathSetAsComponent, completePathSetAsString,
                                partialPathSetAsString)
                        .forEach(item -> item.setPath((String) null)));
        setPathsNullAsString.setId("set-paths-null-as-string");

        NativeButton setPathsNullAsComponent = new NativeButton(
                "Set paths null as component",
                click -> List
                        .of(pathSetAsComponent, completePathSetAsString,
                                partialPathSetAsString)
                        .forEach(
                                item -> item.setPath((Class<Component>) null)));
        setPathsNullAsComponent.setId("set-paths-null-as-component");

        NativeButton setPathsAsComponent = new NativeButton(
                "Set paths as component",
                click -> List
                        .of(noPath, completePathSetAsString,
                                partialPathSetAsString)
                        .forEach(item -> item.setPath(SideNavPage.class)));
        setPathsAsComponent.setId("set-paths-as-component");

        NativeButton setCompletePathsAsString = new NativeButton(
                "Set complete paths as string",
                click -> List
                        .of(noPath, pathSetAsComponent, partialPathSetAsString)
                        .forEach(item -> item
                                .setPath("https://vaadin.com/docs/latest/")));
        setCompletePathsAsString.setId("set-complete-paths-as-string");

        NativeButton setPartialPathsAsString = new NativeButton(
                "Set partial paths as string",
                click -> List
                        .of(noPath, pathSetAsComponent, completePathSetAsString)
                        .forEach(item -> item.setPath(
                                "vaadin-side-nav/side-nav-test-target-view")));
        setPartialPathsAsString.setId("set-partial-paths-as-string");

        add(sideNav, setPathsNullAsString, setPathsNullAsComponent,
                setPathsAsComponent, setCompletePathsAsString,
                setPartialPathsAsString);
    }
}
