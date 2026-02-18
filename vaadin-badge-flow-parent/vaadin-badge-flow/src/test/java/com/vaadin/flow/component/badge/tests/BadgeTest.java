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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.badge.Badge;

public class BadgeTest {

    private Badge badge;
    private UI ui;

    @Before
    public void setup() {
        ui = new UI();
        UI.setCurrent(ui);
        badge = new Badge();
        ui.add(badge);
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
        ui = null;
    }

    @Test
    public void defaultConstructor_emptyBadge() {
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
        var icon = createIcon();
        var badge = new Badge(icon);
        Assert.assertEquals(icon, badge.getIcon());
    }

    @Test
    public void iconAndTextConstructor_setsIconAndText() {
        var icon = createIcon();
        var badge = new Badge(icon, "New");
        Assert.assertEquals(icon, badge.getIcon());
        Assert.assertEquals("New", badge.getText());
    }

    @Test
    public void setText_getText() {
        badge.setText("Status");
        Assert.assertEquals("Status", badge.getText());
    }

    @Test
    public void setNumber_getNumber() {
        badge.setNumber(5);
        Assert.assertEquals(Integer.valueOf(5), badge.getNumber());
    }

    @Test
    public void setNumber_null_getNumberReturnsNull() {
        badge.setNumber(5);
        badge.setNumber(null);
        Assert.assertNull(badge.getNumber());
    }

    @Test
    public void setIcon_getIcon() {
        var icon = createIcon();
        badge.setIcon(icon);
        Assert.assertEquals(icon, badge.getIcon());
    }

    @Test
    public void setIcon_iconHasSlotAttribute() {
        var icon = createIcon();
        badge.setIcon(icon);
        Assert.assertEquals("icon", icon.getElement().getAttribute("slot"));
    }

    @Test
    public void setIcon_replaceIcon() {
        var icon1 = createIcon();
        var icon2 = createIcon();
        badge.setIcon(icon1);
        badge.setIcon(icon2);
        Assert.assertEquals(icon2, badge.getIcon());
        Assert.assertNull(icon1.getElement().getAttribute("slot"));
    }

    @Test
    public void setIcon_null_removesIcon() {
        var icon = createIcon();
        badge.setIcon(icon);
        badge.setIcon(null);
        Assert.assertNull(badge.getIcon());
    }

    @Tag("vaadin-icon")
    private static class TestIcon extends Component {
    }

    private TestIcon createIcon() {
        return new TestIcon();
    }
}
