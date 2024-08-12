/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.crud.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-crud/detach-attach")
public class DetachAttachIT extends AbstractComponentIT {

    CrudElement crud;

    @Before
    public void init() {
        open();
    }

    @Test
    public void disableSaveBtn_detach_reattach_btnDisabled() {
        editItemAndChangeField();
        Assert.assertTrue(crud.getEditorSaveButton().isEnabled());

        findElement(By.id("disable-save-button")).click();

        Assert.assertFalse(crud.getEditorSaveButton().isEnabled());

        findElement(By.id("detach")).click();
        findElement(By.id("attach")).click();

        editItemAndChangeField();
        Assert.assertFalse(crud.getEditorSaveButton().isEnabled());
    }

    private void editItemAndChangeField() {
        crud = $(CrudElement.class).waitForFirst();
        crud.openRowForEditing(0);
        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class).attribute("editor-role", "last-name")
                .first();
        lastNameField.setValue("Otto");
    }
}
