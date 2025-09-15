/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-crud/new-button")
public class NewButtonIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void newButtonNull_noNewButtonPresent() {
        CrudElement crud = $(CrudElement.class).id("crud-new-button-null");
        Assert.assertFalse("New button should not be rendered",
                verifyButtonRendered(crud));
    }

    @Test
    public void newButtonVisibleFalse_noNewButtonPresent() {
        CrudElement crud = $(CrudElement.class).id("crud-new-button-hidden");
        Assert.assertFalse("New button should not be rendered",
                verifyButtonRendered(crud));
    }

    private boolean verifyButtonRendered(CrudElement crud) {
        return crud.$("*").withAttribute("slot", "new-button").exists();
    }
}
