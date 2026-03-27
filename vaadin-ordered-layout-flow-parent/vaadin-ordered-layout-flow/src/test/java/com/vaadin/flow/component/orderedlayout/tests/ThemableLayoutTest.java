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
package com.vaadin.flow.component.orderedlayout.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;

/**
 * @author Vaadin Ltd.
 */
class ThemableLayoutTest {
    private ThemableLayout layout;

    private class TestLayout implements ThemableLayout {

        private Element element = ElementFactory.createDiv();

        @Override
        public Element getElement() {
            return element;
        }
    }

    @BeforeEach
    void setup() {
        layout = new TestLayout();
    }

    @Test
    void checkMargin() {
        checkThemeToggling("margin", layout::isMargin, layout::setMargin);
    }

    @Test
    void checkPadding() {
        checkThemeToggling("padding", layout::isPadding, layout::setPadding);
    }

    @Test
    void checkSpacing() {
        checkThemeToggling("spacing", layout::isSpacing, layout::setSpacing);
    }

    @Test
    void checkWrap() {
        checkThemeToggling("wrap", layout::isWrap, layout::setWrap);
    }

    @Test
    void checkSpacingStringSetter() {
        layout.setSpacing("20px");
        assertTrue(layout.isSpacing(),
                "Expected spacing to be applied after setting it");
        assertEquals("20px", layout.getSpacing(),
                "Expected spacing to be '20px'");
    }

    @Test
    void checkSpacingUnitSetter() {
        layout.setSpacing(2, Unit.REM);
        assertTrue(layout.isSpacing(),
                "Expected spacing to be applied after setting it");
        assertEquals("2.0rem", layout.getSpacing(),
                "Expected spacing to be '2.0rem'");
    }

    @Test
    void checkIsSpacing() {
        layout.setSpacing("20px");
        assertTrue(layout.isSpacing(),
                "Expected spacing to be applied after setting it");
        layout.setSpacing(false);
        assertFalse(layout.isSpacing(),
                "Expected no spacing applied after removing it");
        layout.setSpacing(true);
        assertTrue(layout.isSpacing(),
                "Expected spacing to be applied after setting it");
        layout.setSpacing(false);
        assertFalse(layout.isSpacing(),
                "Expected no spacing applied after removing it");
    }

    @Test
    void removeSpacing_gapIsRemoved() {
        layout.setSpacing("20px");
        layout.setSpacing(false);
        assertFalse(layout.isSpacing(),
                "Expected spacing to be removed after setting it to false");
        assertNull(layout.getSpacing(), "Expected spacing to be null");
        assertNull(layout.getElement().getStyle().get("gap"),
                "Expected gap to be null");
    }

    private void checkThemeToggling(String themeName,
            Supplier<Boolean> themeGetter, Consumer<Boolean> themeSetter) {
        assertFalse(themeGetter.get(),
                String.format(
                        "Expected no '%s' theme applied initially to layout",
                        themeName));
        themeSetter.accept(true);
        assertTrue(themeGetter.get(), String.format(
                "Expected '%s' theme applied after setting it", themeName));
        themeSetter.accept(true);
        assertTrue(themeGetter.get(),
                String.format(
                        "Expected '%s' theme applied after setting it twice",
                        themeName));

        themeSetter.accept(false);
        assertFalse(themeGetter.get(), String.format(
                "Expected no '%s' theme applied after removing it", themeName));
        themeSetter.accept(false);
        assertFalse(themeGetter.get(), String.format(
                "Expected no '%s' theme applied after removing it twice",
                themeName));
    }
}
