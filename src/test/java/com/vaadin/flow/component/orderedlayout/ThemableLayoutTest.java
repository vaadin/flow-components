package com.vaadin.flow.component.orderedlayout;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Vaadin Ltd.
 */
public class ThemableLayoutTest {
    private ThemableLayout layout;

    @Before
    public void setUp() {
        layout = new VerticalLayout();
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

    private void checkThemeToggling(String themeName, Supplier<Boolean> themeGetter, Consumer<Boolean> themeSetter) {
        assertFalse(String.format("Expected no '%s' theme applied initially to layout", themeName), themeGetter.get());
        themeSetter.accept( true);
        assertTrue(String.format("Expected '%s' theme applied after setting it", themeName), themeGetter.get());
        themeSetter.accept(true);
        assertTrue(String.format("Expected '%s' theme applied after setting it twice", themeName), themeGetter.get());

        themeSetter.accept(false);
        assertFalse(String.format("Expected no '%s' theme applied after removing it", themeName), themeGetter.get());
        themeSetter.accept(false);
        assertFalse(String.format("Expected no '%s' theme applied after removing it twice", themeName), themeGetter.get());
    }
}
