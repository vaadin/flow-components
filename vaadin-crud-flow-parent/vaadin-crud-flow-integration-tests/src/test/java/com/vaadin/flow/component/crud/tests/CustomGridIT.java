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
import com.vaadin.testbench.TestBenchElement;
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

    @Test
    public void editorShouldHaveRightTitleWhenOpenedInExistingItemMode() {
        CrudElement crud = $(CrudElement.class).waitForFirst();
        GridElement grid = $(GridElement.class).first();

        customGridClickToEditButton().click();

        crud.getNewItemButton().ifPresent(button -> button.click());
        Assert.assertEquals("New item", getEditorHeaderText(crud));

        crud.getEditorCancelButton().click();
        grid.getCell(0, 0).click();

        waitUntil((c) -> "Edit item".equals(getEditorHeaderText(crud)), 200);
    }

    @Test
    @org.junit.Ignore("Does not pass in mono-repo - 100% failure")
    public void editorShouldHaveRightTitleWhenOpenedInNewItemMode() {
        CrudElement crud = $(CrudElement.class).waitForFirst();

        newItemButton().click();
        Assert.assertEquals("New item", getEditorHeaderText(crud));
        crud.getEditorCancelButton().click();

        crud.$("vaadin-crud-edit").first().click();
        Assert.assertEquals("Edit item", getEditorHeaderText(crud));

        crud.getEditorCancelButton().click();

        newItemButton().click();
        waitUntil((c) -> crud.getEditor().isDisplayed(), 100);
        Assert.assertEquals("New item", getEditorHeaderText(crud));
    }

    private String getEditorHeaderText(CrudElement crud) {
        return crud.getEditor().$(TestBenchElement.class)
                .withAttribute("slot", "header").first().getText();
    }

    private ButtonElement customGridClickToEditButton() {
        return $(ButtonElement.class).onPage().id("clickToEdit");
    }

    private ButtonElement newItemButton() {
        return $(ButtonElement.class).onPage().id("newItemEditor");
    }

    private ButtonElement toggleBordersButton() {
        return $(ButtonElement.class).onPage().id("toggleBorders");
    }
}
