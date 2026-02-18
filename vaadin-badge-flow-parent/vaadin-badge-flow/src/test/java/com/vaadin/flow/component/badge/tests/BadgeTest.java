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
package com.vaadin.flow.component.badge.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.html.Span;

public class BadgeTest {

    @Test
    public void defaultConstructor_emptyBadge() {
        var badge = new Badge();
        Assert.assertEquals("", badge.getText());
        Assert.assertNull(badge.getNumber());
        Assert.assertNull(badge.getIcon());
    }

    @Test
    public void textConstructor_setsText() {
        var badge = new Badge("New");
        Assert.assertEquals("New", badge.getText());
    }

    @Test
    public void iconConstructor_setsIcon() {
        var icon = new Span();
        var badge = new Badge(icon);
        Assert.assertEquals(icon, badge.getIcon());
    }

    @Test
    public void iconAndTextConstructor_setsIconAndText() {
        var icon = new Span();
        var badge = new Badge(icon, "New");
        Assert.assertEquals(icon, badge.getIcon());
        Assert.assertEquals("New", badge.getText());
    }

    @Test
    public void setText_getText() {
        var badge = new Badge();

        badge.setText("Status");
        Assert.assertEquals("Status", badge.getText());
        Assert.assertEquals("Status", badge.getElement().getText());

        badge.setText("");
        Assert.assertEquals("", badge.getText());
        Assert.assertEquals("", badge.getElement().getText());

        badge.setText("Status");
        badge.setText(null);
        Assert.assertEquals("", badge.getText());
        Assert.assertEquals("", badge.getElement().getText());
    }

    @Test
    public void setNumber_getNumber() {
        var badge = new Badge();

        badge.setNumber(5);
        Assert.assertEquals((Integer) 5, badge.getNumber());
        Assert.assertEquals("5", badge.getElement().getProperty("number"));

        badge.setNumber(null);
        Assert.assertNull(badge.getNumber());
        Assert.assertFalse(badge.getElement().hasProperty("number"));
    }

    @Test
    public void setIcon_getIcon() {
        var badge = new Badge();
        var icon0 = new Span();
        var icon1 = new Span();

        badge.setIcon(icon0);
        Assert.assertEquals(icon0, badge.getIcon());
        Assert.assertEquals(badge, icon0.getParent().get());
        Assert.assertEquals("icon", icon0.getElement().getAttribute("slot"));

        badge.setIcon(icon1);
        Assert.assertEquals(icon1, badge.getIcon());
        Assert.assertEquals(badge, icon1.getParent().get());
        Assert.assertEquals("icon", icon1.getElement().getAttribute("slot"));
        Assert.assertFalse(icon0.getParent().isPresent());
        Assert.assertFalse(icon0.getElement().hasAttribute("slot"));

        badge.setIcon(null);
        Assert.assertNull(badge.getIcon());
        Assert.assertFalse(icon1.getParent().isPresent());
        Assert.assertFalse(icon1.getElement().hasAttribute("slot"));
    }
}
