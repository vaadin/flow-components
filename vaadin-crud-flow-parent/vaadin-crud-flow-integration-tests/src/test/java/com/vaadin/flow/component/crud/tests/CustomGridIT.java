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
import com.vaadin.flow.component.confirmdialog.testbench.ConfirmDialogElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-crud/customgrid")
public class CustomGridIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void editTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        crud.openRowForEditing(0);
        Assert.assertTrue(crud.isEditorOpen());
        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class)
                .withAttribute("editor-role", "last-name").first();

        Assert.assertEquals("Sayo", lastNameField.getValue());

        lastNameField.setValue("Otto");
        crud.getEditorSaveButton().click();

        Assert.assertFalse(crud.isEditorOpen());
        Assert.assertEquals("Otto",
                $(GridElement.class).first().getCell(0, 2).getText());
    }

    @Test
    public void cancelChangesTest() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        crud.openRowForEditing(0);
        Assert.assertTrue(crud.isEditorOpen());
        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class)
                .withAttribute("editor-role", "last-name").first();

        Assert.assertEquals("Sayo", lastNameField.getValue());
        lastNameField.setValue("Otto");
        crud.getEditorCancelButton().click();

        ConfirmDialogElement confirmCancel = crud.getConfirmCancelDialog();
        Assert.assertEquals("Discard changes", confirmCancel.getHeaderText());
        confirmCancel.getConfirmButton().click();
        Assert.assertEquals("Sayo",
                $(GridElement.class).first().getCell(0, 2).getText());
    }

    @Test
    public void customGridDoesNotReactToThemeVariantChanges() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        GridElement grid = $(GridElement.class).first();

        Assert.assertNotEquals("no-border", crud.getDomAttribute("theme"));
        Assert.assertNotEquals("no-border", grid.getDomAttribute("theme"));

        toggleBordersButton().click();
        Assert.assertEquals("no-border", crud.getDomAttribute("theme"));
        Assert.assertNotEquals("no-border", grid.getDomAttribute("theme"));
    }

    private ButtonElement toggleBordersButton() {
        return $(ButtonElement.class).onPage().id("toggleBorders");
    }
}
