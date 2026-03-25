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
package com.vaadin.flow.component.icon.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.icon.FontIcon;

class FontIconTest {

    @Test
    void constructor_hasIconClassNames() {
        var icon = new FontIcon("fa-solid", "fa-user");
        Assertions.assertEquals("fa-solid fa-user",
                String.join(" ", icon.getIconClassNames()));
        Assertions.assertEquals("fa-solid fa-user",
                icon.getElement().getProperty("iconClass"));
    }

    @Test
    void emptyConstructorArgs_hasNoIconClassNames() {
        var icon = new FontIcon();
        Assertions.assertArrayEquals(new String[0], icon.getIconClassNames());
        Assertions.assertNull(icon.getElement().getProperty("iconClass"));
    }

    @Test
    void setIconClassNames_hasIconClassNames() {
        var icon = new FontIcon();
        icon.setIconClassNames("fa-solid", "fa-user");
        Assertions.assertEquals("fa-solid fa-user",
                String.join(" ", icon.getIconClassNames()));
        Assertions.assertEquals("fa-solid fa-user",
                icon.getElement().getProperty("iconClass"));
    }

    @Test
    void modifyIconClassName_hasModifiedIconClassName() {
        var icon = new FontIcon();
        icon.setIconClassNames("fa-solid", "fa-user");
        icon.setIconClassNames("fa-solid");
        Assertions.assertEquals("fa-solid",
                String.join(" ", icon.getIconClassNames()));
        Assertions.assertEquals("fa-solid",
                icon.getElement().getProperty("iconClass"));
    }

    @Test
    void clearIconClassNames_hasNoIconClassNames() {
        var icon = new FontIcon();
        icon.setIconClassNames("fa-solid", "fa-user");
        icon.setIconClassNames();
        Assertions.assertArrayEquals(new String[0], icon.getIconClassNames());
        Assertions.assertNull(icon.getElement().getProperty("iconClass"));
    }

    @Test
    void setFontFamily_hasIconClassNamesFamily() {
        var icon = new FontIcon();
        icon.setFontFamily("lumo-icons");
        Assertions.assertEquals("lumo-icons", icon.getFontFamily());
        Assertions.assertEquals("lumo-icons",
                icon.getElement().getProperty("fontFamily"));
    }

    @Test
    void clearFontFamily_hasNoFontFamily() {
        var icon = new FontIcon();
        icon.setFontFamily("lumo-icons");
        icon.setFontFamily(null);
        Assertions.assertNull(icon.getFontFamily());
        Assertions.assertNull(icon.getElement().getProperty("fontFamily"));
    }

    @Test
    void setCharCode_hasCharCode() {
        var icon = new FontIcon();
        icon.setCharCode("ea0e");
        Assertions.assertEquals("ea0e", icon.getCharCode());
        Assertions.assertEquals("ea0e", icon.getElement().getProperty("char"));
    }

    @Test
    void clearCharCode_hasNoCharCode() {
        var icon = new FontIcon();
        icon.setCharCode("ea0e");
        icon.setCharCode(null);
        Assertions.assertNull(icon.getCharCode());
        Assertions.assertNull(icon.getElement().getProperty("char"));
    }

    @Test
    void setLigature_hasLigature() {
        var icon = new FontIcon();
        icon.setLigature("home");
        Assertions.assertEquals("home", icon.getLigature());
        Assertions.assertEquals("home",
                icon.getElement().getProperty("ligature"));
    }

    @Test
    void clearLigature_hasNoLigature() {
        var icon = new FontIcon();
        icon.setLigature("home");
        icon.setLigature(null);
        Assertions.assertNull(icon.getLigature());
        Assertions.assertNull(icon.getElement().getProperty("ligature"));
    }

    @Test
    void setColor_hasColor() {
        var icon = new FontIcon();
        icon.setColor("red");
        Assertions.assertEquals("red", icon.getColor());
        Assertions.assertEquals("red",
                icon.getElement().getStyle().get("color"));
    }

    @Test
    void clearColor_hasNoColor() {
        var icon = new FontIcon();
        icon.setColor("red");
        icon.setColor(null);
        Assertions.assertNull(icon.getColor());
        Assertions.assertNull(icon.getStyle().get("fill"));
    }
}
