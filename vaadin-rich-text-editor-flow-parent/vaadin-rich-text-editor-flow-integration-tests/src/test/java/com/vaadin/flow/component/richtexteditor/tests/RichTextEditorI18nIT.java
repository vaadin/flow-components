package com.vaadin.flow.component.richtexteditor.tests;

import com.vaadin.flow.component.richtexteditor.testbench.RichTextEditorElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
