package com.vaadin.flow.component.richtexteditor;

import com.vaadin.flow.component.AttachEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RichTextEditorTest {

    private RichTextEditor systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new RichTextEditor();
    }

    @Test
    public void onAttach_init() {
        systemUnderTest.onAttach(new AttachEvent(systemUnderTest, true));

        Assert.assertTrue(true);
    }
}
