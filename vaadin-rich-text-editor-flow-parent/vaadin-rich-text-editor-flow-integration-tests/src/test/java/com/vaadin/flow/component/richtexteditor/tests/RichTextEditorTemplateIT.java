/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.richtexteditor.testbench.RichTextEditorElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-rich-text-editor/template")
public class RichTextEditorTemplateIT extends AbstractComponentIT {

    private RichTextEditorElement editor;
    private TestBenchElement getValue;
    private TestBenchElement valueOutput;

    @Before
    public void init() {
        open();
        TestBenchElement template = $("rte-in-a-template").first();
        editor = template.$(RichTextEditorElement.class).first();
        getValue = $(TestBenchElement.class).id("get-value");
        valueOutput = $(TestBenchElement.class).id("value-output");
    }

    @Test
    public void richTextEditorInATemplate_settingAndGettingValueCorrectly() {
        editor.getEditor().sendKeys("Bar");
        editor.getEditor().dispatchEvent("focusout");
        getCommandExecutor().waitForVaadin();

        getValue.click();

        Assert.assertEquals("<p>Bar</p>", valueOutput.getText());
    }
}
