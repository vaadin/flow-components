package com.vaadin.flow.component.richtexteditor;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.vaadin.flow.component.richtexteditor.RichTextEditor;

/**
 * Tests for the {@link RichTextEditor}.
 */
public class RichTextEditorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void setValueNull() {
        RichTextEditor rte = new RichTextEditor();
        assertEquals("Value should be an empty string", "",
                rte.getValue());

        thrown.expect(NullPointerException.class);
        thrown.expectMessage("Null value is not supported");

        rte.setValue(null);
    }

    @Test
    public void initialValuePropertyValue() {
        RichTextEditor rte = new RichTextEditor();
        Assert.assertEquals(rte.getEmptyValue(),
                rte.getElement().getProperty("value"));
    }

    @Test
    public void sanitizeScriptTag_scriptTagRemoved() {
        // Check if basic whitelist is applied
        RichTextEditor rte = new RichTextEditor();
        Assert.assertEquals("", rte.sanitize("<script>alert('Foo')</script>"));
    }

    @Test
    public void sanitizeImgTagWithHttpSource_srcAttributeRemoved() {
        RichTextEditor rte = new RichTextEditor();
        Assert.assertEquals("<img>", rte.sanitize("<img src='http://vaadin.com'>"));
    }

    @Test
    public void sanitizeImgTagWithHttpsSource_srcAttributeRemoved() {
        RichTextEditor rte = new RichTextEditor();
        Assert.assertEquals("<img>", rte.sanitize("<img src='https://vaadin.com'>"));
    }

    @Test
    public void sanitizeImgTagWithDataSource_srcAttributePersist() {
        RichTextEditor rte = new RichTextEditor();
        Assert.assertEquals("<img src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==\">",
                rte.sanitize("<img src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==\">"));
    }
}
