package com.vaadin.flow.component.orderedlayout.tests;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void setUp() {
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
