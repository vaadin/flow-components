/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for MenuItem.
 */
public class MenuItemTest {

    private ContextMenu contextMenu;
    private MenuItem item;

    @Before
    public void init() {
        contextMenu = new ContextMenu();
        item = contextMenu.addItem("");
    }

    @Test(expected = IllegalStateException.class)
    public void nonCheckable_setChecked_throws() {
        item.setChecked(true);
    }

    @Test
    public void setCheckable_setChecked_isChecked() {
        item.setCheckable(true);
        Assert.assertFalse(item.isChecked());
        item.setChecked(true);
        Assert.assertTrue(item.isChecked());
    }

    @Test
    public void checked_setUnCheckable_unChecks() {
        item.setCheckable(true);
        item.setChecked(true);

        item.setCheckable(false);
        Assert.assertFalse(item.isCheckable());
    }

}
