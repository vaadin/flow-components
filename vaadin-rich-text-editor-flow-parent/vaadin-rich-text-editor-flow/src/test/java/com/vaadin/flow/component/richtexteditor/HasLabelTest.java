package com.vaadin.flow.component.richtexteditor;

import com.vaadin.flow.component.HasLabel;
import org.junit.Assert;
import org.junit.Test;

public class HasLabelTest {

    @Test
    public void richTextEditor() {
        RichTextEditor c = new RichTextEditor();
        Assert.assertTrue(c instanceof HasLabel);
    }

}
