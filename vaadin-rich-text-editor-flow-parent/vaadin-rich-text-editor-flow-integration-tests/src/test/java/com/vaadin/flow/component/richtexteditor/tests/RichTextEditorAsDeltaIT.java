/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.richtexteditor.testbench.RichTextEditorElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-rich-text-editor/as-delta")
public class RichTextEditorAsDeltaIT extends AbstractComponentIT {

    private RichTextEditorElement simpleRte;
    private TestBenchElement simpleOutput;

    private RichTextEditorElement binderRte;
    private TestBenchElement binderOutput;
    private ButtonElement binderSave;
    private ButtonElement binderReset;

    @Before
    public void init() {
        open();
        simpleRte = $(RichTextEditorElement.class).id("simple-rte");
        simpleOutput = $(TestBenchElement.class).id("simple-output");

        binderRte = $(RichTextEditorElement.class).id("binder-rte");
        binderOutput = $(TestBenchElement.class).id("binder-output");
        binderSave = $(ButtonElement.class).id("binder-save");
        binderReset = $(ButtonElement.class).id("binder-reset");
    }

    @Test
    public void simple_setServerValue() {
        $(ButtonElement.class).id("set-server-value").click();

        Assert.assertEquals("<p>Foo</p>",
                simpleRte.getEditor().getProperty("innerHTML"));
    }

    @Test
    public void simple_enterValue() {
        simpleRte.getEditor().sendKeys("Foo");

        waitUntil(e -> !simpleOutput.getText().isEmpty());

        Assert.assertEquals("[{\"insert\":\"Foo\\n\"}]",
                simpleOutput.getText());
    }

    @Test
    public void binder_emptyValue_error() {
        binderSave.click();

        waitUntil(e -> !binderOutput.getText().isEmpty());

        Assert.assertEquals(
                "There are errors: Delta value should contain something",
                binderOutput.getText());
    }

    @Test
    public void binder_enterValue_saved() {
        binderRte.getEditor().sendKeys("Foo");

        waitUntil(e -> {
            binderSave.click();
            return !binderOutput.getText().startsWith("There are errors");
        });

        Assert.assertEquals("Saved: [{\"insert\":\"Foo\\n\"}]",
                binderOutput.getText());
    }

    @Test
    public void binder_enterValue_reset_empty() {
        binderRte.getEditor().sendKeys("Foo");
        Assert.assertEquals("<p>Foo</p>",
                binderRte.getEditor().getProperty("innerHTML"));

        waitUntil(e -> {
            binderSave.click();
            return !binderOutput.getText().startsWith("There are errors");
        });
        binderReset.click();

        Assert.assertEquals("<p><br></p>",
                binderRte.getEditor().getProperty("innerHTML"));
    }
}
