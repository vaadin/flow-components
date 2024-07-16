/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.crud.test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.crud.testbench.CrudElement;
import org.junit.Test;

public class EditorPositionIT extends AbstractParallelTest {

    @Test
    public void compositeTouchesDirtyState() {
        String url = getBaseURL().replace(super.getBaseURL(),
                super.getBaseURL() + "/vaadin-crud") + "/editorposition";
        getDriver().get(url);

        final CrudElement crud = $(CrudElement.class).waitForFirst();

        setPositionButton("Bottom").click();
        assertPositionEquals("bottom");

        setPositionButton("Aside").click();
        assertPositionEquals("aside");

        setPositionButton("Overlay").click();
        assertPositionEquals("");
    }

    private ButtonElement setPositionButton(String position) {
        return $(ButtonElement.class).onPage().id("position" + position);
    }

    private ButtonElement getPositionButton() {
        return $(ButtonElement.class).onPage().id("getEditorPosition");
    }

    private void assertPositionEquals(String position) {
        waitUntil(driver -> {
            getPositionButton().click();
            return position.equals($("#editorPositionLabel").first().getText());
        });
    }
}
