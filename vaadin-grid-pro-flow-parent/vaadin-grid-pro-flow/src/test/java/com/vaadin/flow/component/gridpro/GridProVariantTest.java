/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.gridpro;

import org.junit.Assert;
import org.junit.Test;

public class GridProVariantTest {

    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        var gridPro = new GridPro<>();
        gridPro.addThemeVariants(GridProVariant.LUMO_NO_BORDER);

        var themeNames = gridPro.getThemeNames();
        Assert.assertTrue(themeNames
                .contains(GridProVariant.LUMO_NO_BORDER.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        var gridPro = new GridPro<>();
        gridPro.addThemeVariants(GridProVariant.LUMO_NO_BORDER);
        gridPro.removeThemeVariants(GridProVariant.LUMO_NO_BORDER);

        var themeNames = gridPro.getThemeNames();
        Assert.assertFalse(themeNames
                .contains(GridProVariant.LUMO_NO_BORDER.getVariantName()));
    }
}
