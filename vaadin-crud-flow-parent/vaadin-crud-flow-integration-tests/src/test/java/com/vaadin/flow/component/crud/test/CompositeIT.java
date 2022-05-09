package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;

import org.junit.Assert;
import org.junit.Test;

public class CompositeIT extends AbstractParallelTest {

    @Test
    public void compositeTouchesDirtyState() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-crud") + "/composite";
        getDriver().get(url);

        final CrudElement crud = $(CrudElement.class).waitForFirst();
        crud.getNewItemButton().get().click();

        Assert.assertFalse(crud.getEditorSaveButton().isEnabled());

        $("vaadin-crud-dialog-overlay").first().$("div")
                .attribute("editor-role", "language").first()
                .$(ButtonElement.class).first().click();

        $(TextFieldElement.class).attribute("editor-role", "language-field")
                .first().setValue("English");

        $(TestBenchElement.class).id("overlay").$(ButtonElement.class).first()
                .click();

        Assert.assertTrue(
                $(CrudElement.class).first().getEditorSaveButton().isEnabled());
    }
}
