/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud;

import org.junit.Assert;
import org.junit.Test;

public class CrudVariantTest {

    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        var crud = new Crud<>();
        crud.addThemeVariants(CrudVariant.NO_BORDER);

        var themeNames = crud.getThemeNames();
        Assert.assertTrue(
                themeNames.contains(CrudVariant.NO_BORDER.getVariantName()));
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        var crud = new Crud<>();
        crud.addThemeVariants(CrudVariant.NO_BORDER);
        crud.removeThemeVariants(CrudVariant.NO_BORDER);

        var themeNames = crud.getThemeNames();
        Assert.assertFalse(
                themeNames.contains(CrudVariant.NO_BORDER.getVariantName()));
    }
}
