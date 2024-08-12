/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
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

@TestPath("vaadin-rich-text-editor/i18n")
public class RichTextEditorI18nIT extends AbstractComponentIT {
    private RichTextEditorElement editor;
    private TestBenchElement i18nOutput;

    @Before
    public void init() {
        open();
        editor = $(RichTextEditorElement.class).waitForFirst();
        i18nOutput = $(TestBenchElement.class).id("i18n-output");
    }

    @Test
    public void initEditorWithCustomI18n_i18nUpdated() {
        Assert.assertEquals(i18nOutput.getText(),
                editor.getTitles().toString());
    }
}
