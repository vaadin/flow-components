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
package com.vaadin.flow.component.icon.tests;

import com.vaadin.flow.component.icon.FontIcon;

import org.junit.Assert;
import org.junit.Test;

public class FontIconTest {

    @Test
    public void constructor_hasIconClassNames() {
        var icon = new FontIcon("fa-solid", "fa-user");
        Assert.assertEquals("fa-solid fa-user",
                String.join(" ", icon.getIconClassNames()));
        Assert.assertEquals("fa-solid fa-user",
                icon.getElement().getProperty("icon-class"));
    }

    @Test
    public void emptyConstructorArgs_hasNoIconClassNames() {
        var icon = new FontIcon();
        Assert.assertArrayEquals(new String[0], icon.getIconClassNames());
        Assert.assertNull(icon.getElement().getProperty("icon-class"));
    }

    @Test
    public void setIconClassNames_hasIconClassNames() {
        var icon = new FontIcon();
        icon.setIconClassNames("fa-solid", "fa-user");
        Assert.assertEquals("fa-solid fa-user",
                String.join(" ", icon.getIconClassNames()));
        Assert.assertEquals("fa-solid fa-user",
                icon.getElement().getProperty("icon-class"));
    }

    @Test
    public void modifyIconClassName_hasModifiedIconClassName() {
        var icon = new FontIcon();
        icon.setIconClassNames("fa-solid", "fa-user");
        icon.setIconClassNames("fa-solid");
        Assert.assertEquals("fa-solid",
                String.join(" ", icon.getIconClassNames()));
        Assert.assertEquals("fa-solid",
                icon.getElement().getProperty("icon-class"));
    }

    @Test
    public void clearIconClassNames_hasNoIconClassNames() {
        var icon = new FontIcon();
        icon.setIconClassNames("fa-solid", "fa-user");
        icon.setIconClassNames();
        Assert.assertArrayEquals(new String[0], icon.getIconClassNames());
        Assert.assertNull(icon.getElement().getProperty("icon-class"));
    }

    @Test
    public void setFontFamily_hasIconClassNamesFamily() {
        var icon = new FontIcon();
        icon.setFontFamily("lumo-icons");
        Assert.assertEquals("lumo-icons", icon.getFontFamily());
        Assert.assertEquals("lumo-icons",
                icon.getElement().getProperty("fontFamily"));
    }

    @Test
    public void clearFontFamily_hasNoFontFamily() {
        var icon = new FontIcon();
        icon.setFontFamily("lumo-icons");
        icon.setFontFamily(null);
        Assert.assertNull(icon.getFontFamily());
        Assert.assertNull(icon.getElement().getProperty("fontFamily"));
    }

    @Test
    public void setCharCode_hasCharCode() {
        var icon = new FontIcon();
        icon.setCharCode("\uea0e");
        Assert.assertEquals("\uea0e", icon.getCharCode());
        Assert.assertEquals("\uea0e", icon.getElement().getProperty("char"));
    }

    @Test
    public void clearCharCode_hasNoCharCode() {
        var icon = new FontIcon();
        icon.setCharCode("\uea0e");
        icon.setCharCode(null);
        Assert.assertNull(icon.getCharCode());
        Assert.assertNull(icon.getElement().getProperty("char"));
    }

    @Test
    public void setColor_hasColor() {
        var icon = new FontIcon();
        icon.setColor("red");
        Assert.assertEquals("red", icon.getColor());
        Assert.assertEquals("red", icon.getElement().getStyle().get("color"));
    }

    @Test
    public void clearColor_hasNoColor() {
        var icon = new FontIcon();
        icon.setColor("red");
        icon.setColor(null);
        Assert.assertNull(icon.getColor());
        Assert.assertNull(icon.getStyle().get("fill"));
    }
}
