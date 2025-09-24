/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.crud.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-crud/editorbuttons")
public class EditorButtonsIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void saveBtnIsAlwaysEnabled() {
        getTestButton("enable-save-button").click();
        CrudElement crud = getCrud();
        crud.openRowForEditing(0);
        Assert.assertTrue(crud.getEditorSaveButton().isEnabled());
    }

    @Test
    public void saveBtnIsAlwaysDisabled() {
        getTestButton("disable-save-button").click();
        CrudElement crud = getCrud();
        crud.openRowForEditing(0);
        TextFieldElement lastNameField = crud.getForm()
                .$(TextFieldElement.class)
                .withAttribute("editor-role", "last-name").first();
        lastNameField.setValue("Otto");

        Assert.assertFalse(crud.getEditorSaveButton().isEnabled());
    }

    @Test
    public void saveButtonCanBeDisabledOnTheServer() {
        CrudElement crud = getCrud();

        crud.getNewItemButton().get().click();
        assertTrue(crud.isEditorOpen());

        TextFieldElement lastNameField = crud.getForm()
                .$(TextFieldElement.class)
                .withAttribute("editor-role", "last-name").first();
        lastNameField.setValue("Otto");

        ButtonElement saveButton = crud.getEditorSaveButton();
        assertTrue("Save button should be enabled", saveButton.isEnabled());
        getTestButton("disable-save-button").click();
        assertFalse("Save button should be disabled", saveButton.isEnabled());
    }

    @Test
    public void saveButtonBecomesEnabledWhenFormBecomesDirty() {
        CrudElement crud = getCrud();

        crud.getNewItemButton().get().click();
        ButtonElement saveButton = crud.getEditorSaveButton();
        assertFalse(saveButton.isEnabled());

        TextFieldElement lastNameField = crud.getForm()
                .$(TextFieldElement.class)
                .withAttribute("editor-role", "last-name").first();
        lastNameField.setValue("Otto");

        assertTrue(saveButton.isEnabled());
    }

    @Test
    public void saveButtonDisabledStateCanBeControlledOnTheServer() {
        CrudElement crud = getCrud();

        crud.getNewItemButton().get().click();
        assertTrue(crud.isEditorOpen());

        getTestButton("disable-save-button").click();
        ButtonElement saveButton = crud.getEditorSaveButton();
        assertFalse("Save button should be disabled", saveButton.isEnabled());

        TextFieldElement lastNameField = crud.getForm()
                .$(TextFieldElement.class)
                .withAttribute("editor-role", "last-name").first();
        lastNameField.setValue("Otto");

        assertFalse("Save button should remain disabled",
                saveButton.isEnabled());

        getTestButton("enable-save-button").click();
        assertTrue("Save button should be enabled", saveButton.isEnabled());
    }

    @Test
    public void cancelButtonDisabledStateCanBeControlledOnTheServer() {
        CrudElement crud = getCrud();

        crud.getNewItemButton().get().click();
        assertTrue(crud.isEditorOpen());

        getTestButton("disable-cancel-button").click();
        ButtonElement cancelButton = crud.getEditorCancelButton();
        assertFalse("Cancel button should be disabled",
                cancelButton.isEnabled());

        TextFieldElement lastNameField = crud.getForm()
                .$(TextFieldElement.class)
                .withAttribute("editor-role", "last-name").first();
        lastNameField.setValue("Otto");

        assertFalse("Cancel button should remain disabled",
                cancelButton.isEnabled());

        getTestButton("enable-cancel-button").click();
        assertTrue("Cancel button should be enabled", cancelButton.isEnabled());
    }

    @Test
    public void deleteButtonDisabledStateCanBeControlledOnTheServer() {
        CrudElement crud = getCrud();

        crud.openRowForEditing(0);
        assertTrue(crud.isEditorOpen());

        getTestButton("disable-delete-button").click();
        ButtonElement deleteButton = crud.getEditorDeleteButton();
        assertFalse("Delete button should be disabled",
                deleteButton.isEnabled());

        TextFieldElement lastNameField = crud.getForm()
                .$(TextFieldElement.class)
                .withAttribute("editor-role", "last-name").first();
        lastNameField.setValue("Otto");

        assertFalse("Delete button should remain disabled",
                deleteButton.isEnabled());

        getTestButton("enable-delete-button").click();
        assertTrue("Delete button should be enabled", deleteButton.isEnabled());
    }

    @Test
    public void buttonsCanWorkWithShortcutsDefinedOnServer() {
        String lastNameExpected = "OTTO";
        CrudElement crud = getCrud();
        crud.openRowForEditing(0);
        assertTrue(crud.isEditorOpen());

        getTestButton("add-enter-shortcut-button").click();

        TextFieldElement lastNameField = crud.getForm()
                .$(TextFieldElement.class)
                .withAttribute("editor-role", "last-name").first();
        lastNameField.setValue(lastNameExpected);

        // invoke shortcut
        WebElement body = getDriver().findElement(By.xpath("//body"));
        body.sendKeys(Keys.ENTER);

        String lastNameActual = crud.getGrid().getCell(0, 2).getText();

        assertFalse("Editor is closed", crud.isEditorOpen());
        Assert.assertEquals("Last name is updated", lastNameExpected,
                lastNameActual);
    }

    private CrudElement getCrud() {
        return $(CrudElement.class).waitForFirst();
    }

    private ButtonElement getTestButton(String id) {
        return $(ButtonElement.class).onPage().id(id);
    }
}
