package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.grid.testbench.GridElement;
import com.vaadin.flow.component.orderedlayout.testbench.VerticalLayoutElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.ElementQuery;
import com.vaadin.testbench.parallel.BrowserUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TemplateSupportIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL() + "/crudintemplate");
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
        Assert.assertEquals(3, getCrud().waitForFirst().getGrid().getRowCount());
    }

    @Test
    public void newTest() {
        CrudElement crud = getCrud().waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        crud.getNewItemButton().click();
        Assert.assertEquals("New: Person{id=null, firstName='null', lastName='null'}",
                getLastEvent());
        Assert.assertTrue(crud.isEditorOpen());
    }

    @Test
    public void editTest() {
        CrudElement crud = getCrud().waitForFirst();
        Assert.assertFalse(crud.isEditorOpen());
        crud.openRowForEditing(0);
        Assert.assertEquals("Edit: Person{id=1, firstName='Sayo', lastName='Sayo'}",
                getLastEvent());
        Assert.assertTrue(crud.isEditorOpen());

        dismissDialog();

        crud.openRowForEditing(2);

        Assert.assertEquals("Edit: Person{id=3, firstName='Guille', lastName='Guille'}",
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
        TextFieldElement lastNameField = crud.getEditor().$(TextFieldElement.class).id("lastName");
        lastNameField.setValue("Oladeji");

        crud.getEditorSaveButton().click();

        if (BrowserUtil.isIE(getDesiredCapabilities())) {
            // TODO(oluwasayo): Investigate why editor sometimes doesn't disappear on first click in IE
            // especially when server-side validation is involved
            return;
        }

        Assert.assertFalse(crud.isEditorOpen());
        Assert.assertEquals("Oladeji", $("crud-app").first().$(GridElement.class)
                .first().getCell(0, 2).getText());
    }

    private ElementQuery<CrudElement> getCrud() {
        return $("crud-app").waitForFirst().$(CrudElement.class);
    }

    protected String getLastEvent() {
        return $("crud-app").first().$(VerticalLayoutElement.class)
                .last().$("span").last().getText();
    }
}
