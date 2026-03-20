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

class CrudI18nTest {

    @Test
    void createDefault() {
        CrudI18n i18n = CrudI18n.createDefault();

        Assertions.assertEquals("Cancel", i18n.getCancel());
        Assertions.assertEquals("Delete...", i18n.getDeleteItem());
        Assertions.assertEquals("Edit item", i18n.getEditItem());
        Assertions.assertEquals("Edit item", i18n.getEditLabel());
        Assertions.assertEquals("New item", i18n.getNewItem());
        Assertions.assertEquals("Save", i18n.getSaveItem());
        Assertions.assertEquals("Discard",
                i18n.getConfirm().getCancel().getButton().getConfirm());
        Assertions.assertEquals("Delete",
                i18n.getConfirm().getDelete().getButton().getConfirm());
    }
}
