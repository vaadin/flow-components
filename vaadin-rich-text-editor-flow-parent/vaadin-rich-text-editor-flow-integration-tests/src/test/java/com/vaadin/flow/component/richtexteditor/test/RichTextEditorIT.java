package com.vaadin.flow.component.richtexteditor.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RichTextEditorIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void pass() {
        Assert.assertEquals("Hello RichTextEditor!", $("h1").first().getText());
    }
}
