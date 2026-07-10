/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.tests.MockUIRule;

/**
 * Tests for the URL-scheme validation performed by {@link SideNavItem}. A
 * {@link MockUIRule} provides the current {@code VaadinService} so that the
 * safe URL schemes configured by the deployment configuration are applied.
 */
public class SideNavItemUrlSchemeTest {

    @Rule
    public MockUIRule mockUIRule = new MockUIRule();

    @Before
    public void enableUrlSchemeValidation() {
        mockUIRule.enableUrlSchemeValidation();
    }

    @Test
    public void setPathWithUnsafeScheme_throws() {
        SideNavItem sideNavItem = new SideNavItem("Item", "path");
        Assert.assertThrows(IllegalArgumentException.class,
                () -> sideNavItem.setPath("javascript:alert(1)"));
    }

    @Test
    public void constructor_labelPath_unsafeScheme_throws() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new SideNavItem("Docs", "javascript:alert(1)"));
    }

    @Test
    public void constructor_labelPathPrefixComponent_unsafeScheme_throws() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> new SideNavItem("Docs", "javascript:alert(1)",
                        new Div()));
    }
}
