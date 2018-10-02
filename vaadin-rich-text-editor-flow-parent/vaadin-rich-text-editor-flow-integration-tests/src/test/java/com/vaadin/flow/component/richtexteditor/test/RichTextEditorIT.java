package com.vaadin.flow.component.richtexteditor.test;

import com.vaadin.flow.component.richtexteditor.testbench.RichTextEditorElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RichTextEditorIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void content() {
        Assert.assertEquals("Foo bar",
                $(RichTextEditorElement.class).waitForFirst().getContent().getText());
    }
}
