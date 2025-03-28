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

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-crud/composite")
public class CompositeIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void compositeTouchesDirtyState() {
        final CrudElement crud = $(CrudElement.class).waitForFirst();
        crud.getNewItemButton().get().click();

        Assert.assertFalse(crud.getEditorSaveButton().isEnabled());

        $("vaadin-crud-dialog-overlay").first().$("div")
                .withAttribute("editor-role", "language").first()
                .$(ButtonElement.class).first().click();

        $(TextFieldElement.class).withAttribute("editor-role", "language-field")
                .first().setValue("English");

        $(TestBenchElement.class).withId("overlay").last()
                .$(ButtonElement.class).first().click();

        Assert.assertTrue(
                $(CrudElement.class).first().getEditorSaveButton().isEnabled());
    }
}
