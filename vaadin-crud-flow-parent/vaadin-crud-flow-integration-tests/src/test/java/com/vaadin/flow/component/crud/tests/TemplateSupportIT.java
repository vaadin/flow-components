/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.crud.tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.ElementQuery;

public class TemplateSupportIT extends AbstractParallelTest {

    @Before
    public void init() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-crud") + "/crudintemplate";
        getDriver().get(url);
    }

    @After
    public void dismissDialog() {
        CrudElement crud = getCrud().first();
        if (crud.isEditorOpen()) {
            crud.getEditorCancelButton().click();
        }
    }

    @Test
    public void dataPresentInGrid() {
        Assert.assertEquals(3,
                getCrud().waitForFirst().getGrid().getRowCount());
    }

    @Test
    public void newTest() {
        CrudElement crud = getCrud().waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        crud.getNewItemButton().get().click();
        Assert.assertEquals(
                "New: Person{id=null, firstName='null', lastName='null'}",
                getLastEvent());
        Assert.assertTrue(crud.isEditorOpen());
    }

    @Test
    public void editTest() {
        CrudElement crud = getCrud().waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        crud.openRowForEditing(0);
        Assert.assertEquals(
                "Edit: Person{id=1, firstName='Sayo', lastName='Sayo'}",
                getLastEvent());
        Assert.assertTrue(crud.isEditorOpen());

        dismissDialog();

        crud.openRowForEditing(2);

        Assert.assertEquals(
                "Edit: Person{id=3, firstName='Guille', lastName='Guille'}",
                getLastEvent());

        Assert.assertEquals("Guille", crud.getEditor().$(TextFieldElement.class)
                .id("firstName").getValue());

        Assert.assertEquals("Guille", crud.getEditor().$(TextFieldElement.class)
                .id("lastName").getValue());
    }

    @Test
    public void saveTest() {
        CrudElement crud = getCrud().waitForFirst();
        crud.openRowForEditing(0);
        TextFieldElement lastNameField = crud.getEditor()
                .$(TextFieldElement.class).id("lastName");
        lastNameField.setValue("Oladeji");

        crud.getEditorSaveButton().click();

        Assert.assertFalse(crud.isEditorOpen());
        Assert.assertEquals("Oladeji", $("crud-app").first()
                .$(GridElement.class).first().getCell(0, 2).getText());
    }

    private ElementQuery<CrudElement> getCrud() {
        return $("crud-app").waitForFirst().$(CrudElement.class);
    }

    @Override
    protected String getLastEvent() {
        return $("crud-app").first().$(VerticalLayoutElement.class).last()
                .$("span").last().getText();
    }
}
