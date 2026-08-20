/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.gridpro;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GridProVariantTest {

    @Test
    void addThemeVariant_themeNamesContainsThemeVariant() {
        var gridPro = new GridPro<>();
        gridPro.addThemeVariants(GridProVariant.NO_BORDER);

        var themeNames = gridPro.getThemeNames();
        Assertions.assertTrue(
                themeNames.contains(GridProVariant.NO_BORDER.getVariantName()));
    }

    @Test
    void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        var gridPro = new GridPro<>();
        gridPro.addThemeVariants(GridProVariant.NO_BORDER);
        gridPro.removeThemeVariants(GridProVariant.NO_BORDER);

        var themeNames = gridPro.getThemeNames();
        Assertions.assertFalse(
                themeNames.contains(GridProVariant.NO_BORDER.getVariantName()));
    }
}
