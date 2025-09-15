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
package com.vaadin.flow.component.orderedlayout.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;

/**
 * @author Vaadin Ltd.
 */
public class ThemableLayoutTest {
    private ThemableLayout layout;

    private class TestLayout implements ThemableLayout {

        private Element element = ElementFactory.createDiv();

        @Override
        public Element getElement() {
            return element;
        }
    }

    @Before
    public void setup() {
        layout = new TestLayout();
    }

    @Test
    public void checkMargin() {
        checkThemeToggling("margin", layout::isMargin, layout::setMargin);
    }

    @Test
    public void checkPadding() {
        checkThemeToggling("padding", layout::isPadding, layout::setPadding);
    }

    @Test
    public void checkSpacing() {
        checkThemeToggling("spacing", layout::isSpacing, layout::setSpacing);
    }

    @Test
    public void checkWrap() {
        checkThemeToggling("wrap", layout::isWrap, layout::setWrap);
    }

    @Test
    public void checkSpacingStringSetter() {
        layout.setSpacing("20px");
        assertTrue("Expected spacing to be applied after setting it",
                layout.isSpacing());
        assertEquals("Expected spacing to be '20px'", "20px",
                layout.getSpacing());
    }

    @Test
    public void checkSpacingUnitSetter() {
        layout.setSpacing(2, Unit.REM);
        assertTrue("Expected spacing to be applied after setting it",
                layout.isSpacing());
        assertEquals("Expected spacing to be '2.0rem'", "2.0rem",
                layout.getSpacing());
    }

    @Test
    public void checkIsSpacing() {
        layout.setSpacing("20px");
        assertTrue("Expected spacing to be applied after setting it",
                layout.isSpacing());
        layout.setSpacing(false);
        assertFalse("Expected no spacing applied after removing it",
                layout.isSpacing());
        layout.setSpacing(true);
        assertTrue("Expected spacing to be applied after setting it",
                layout.isSpacing());
        layout.setSpacing(false);
        assertFalse("Expected no spacing applied after removing it",
                layout.isSpacing());
    }

    @Test
    public void removeSpacing_gapIsRemoved() {
        layout.setSpacing("20px");
        layout.setSpacing(false);
        assertFalse("Expected spacing to be removed after setting it to false",
                layout.isSpacing());
        assertNull("Expected spacing to be null", layout.getSpacing());
        assertNull("Expected gap to be null",
                layout.getElement().getStyle().get("gap"));
    }

    private void checkThemeToggling(String themeName,
            Supplier<Boolean> themeGetter, Consumer<Boolean> themeSetter) {
        assertFalse(String.format(
                "Expected no '%s' theme applied initially to layout",
                themeName), themeGetter.get());
        themeSetter.accept(true);
        assertTrue(String.format("Expected '%s' theme applied after setting it",
                themeName), themeGetter.get());
        themeSetter.accept(true);
        assertTrue(String.format(
                "Expected '%s' theme applied after setting it twice",
                themeName), themeGetter.get());

        themeSetter.accept(false);
        assertFalse(String.format(
                "Expected no '%s' theme applied after removing it", themeName),
                themeGetter.get());
        themeSetter.accept(false);
        assertFalse(String.format(
                "Expected no '%s' theme applied after removing it twice",
                themeName), themeGetter.get());
    }
}
