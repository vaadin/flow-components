/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

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
