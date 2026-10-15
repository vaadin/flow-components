/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.HasAriaLabel;

/**
 * Unit tests for MenuItem.
 */
public class MenuItemTest {

    private ContextMenu contextMenu;
    private MenuItem item;

    @Before
    public void setup() {
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

    @Test
    public void implementsHasAriaLabel() {
        Assert.assertTrue(item instanceof HasAriaLabel);
    }

    @Test
    public void setAriaLabel() {
        item.setAriaLabel("aria-label");
        Assert.assertTrue(item.getAriaLabel().isPresent());
        Assert.assertEquals("aria-label", item.getAriaLabel().get());

        item.setAriaLabel(null);
        Assert.assertTrue(item.getAriaLabel().isEmpty());
    }

    @Test
    public void setAriaLabelledBy() {
        item.setAriaLabelledBy("aria-labelledby");
        Assert.assertTrue(item.getAriaLabelledBy().isPresent());
        Assert.assertEquals("aria-labelledby", item.getAriaLabelledBy().get());

        item.setAriaLabelledBy(null);
        Assert.assertTrue(item.getAriaLabelledBy().isEmpty());
    }

}
