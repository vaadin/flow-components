/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.badge.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.badge.testbench.BadgeElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-badge")
public class BadgeIT extends AbstractComponentIT {

    private BadgeElement badge;

    @Before
    public void init() {
        open();
        badge = $(BadgeElement.class).id("badge-primary");
    }

    @Test
    public void rendersBadgeComponent() {
        boolean hasShadowRoot = (Boolean) executeScript(
                "return arguments[0].shadowRoot !== null", badge);
        String componentName = (String) executeScript(
                "return Object.getPrototypeOf(arguments[0]).constructor.is",
                badge);

        Assert.assertTrue(hasShadowRoot);
        Assert.assertEquals("vaadin-badge", componentName);
        Assert.assertNotNull(badge.getText());
        Assert.assertFalse(badge.getText().isEmpty());
    }

    @Test
    public void badgeHasThemeVariants() {
        BadgeElement successBadge = $(BadgeElement.class)
                .id("badge-success");
        String theme = successBadge.getAttribute("theme");
        Assert.assertTrue(theme.contains("success"));
    }

    @Test
    public void badgeCanBeSmall() {
        BadgeElement smallBadge = $(BadgeElement.class).id("badge-small");
        String theme = smallBadge.getAttribute("theme");
        Assert.assertTrue(theme.contains("small"));
    }

    @Test
    public void badgeCanBePill() {
        BadgeElement pillBadge = $(BadgeElement.class).id("badge-pill");
        String theme = pillBadge.getAttribute("theme");
        Assert.assertTrue(theme.contains("pill"));
    }
}
