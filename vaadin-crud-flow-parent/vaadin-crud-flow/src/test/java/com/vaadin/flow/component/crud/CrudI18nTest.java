/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.crud;

import org.junit.Assert;
import org.junit.Test;

public class CrudI18nTest {

    @Test
    public void createDefault() {
        CrudI18n i18n = CrudI18n.createDefault();

        Assert.assertEquals("Cancel", i18n.getCancel());
        Assert.assertEquals("Delete...", i18n.getDeleteItem());
        Assert.assertEquals("Edit item", i18n.getEditItem());
        Assert.assertEquals("Edit item", i18n.getEditLabel());
        Assert.assertEquals("New item", i18n.getNewItem());
        Assert.assertEquals("Save", i18n.getSaveItem());
        Assert.assertEquals("Discard",
                i18n.getConfirm().getCancel().getButton().getConfirm());
        Assert.assertEquals("Delete",
                i18n.getConfirm().getDelete().getButton().getConfirm());
    }
}
