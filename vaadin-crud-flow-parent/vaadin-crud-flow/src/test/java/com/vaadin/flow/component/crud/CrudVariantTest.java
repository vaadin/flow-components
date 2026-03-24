/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CrudVariantTest {

    @Test
    void addThemeVariant_themeNamesContainsThemeVariant() {
        var crud = new Crud<>();
        crud.addThemeVariants(CrudVariant.NO_BORDER);

        var themeNames = crud.getThemeNames();
        Assertions.assertTrue(
                themeNames.contains(CrudVariant.NO_BORDER.getVariantName()));
    }

    @Test
    void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        var crud = new Crud<>();
        crud.addThemeVariants(CrudVariant.NO_BORDER);
        crud.removeThemeVariants(CrudVariant.NO_BORDER);

        var themeNames = crud.getThemeNames();
        Assertions.assertFalse(
                themeNames.contains(CrudVariant.NO_BORDER.getVariantName()));
    }
}
