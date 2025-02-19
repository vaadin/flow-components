/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RichTextEditorIsEmptyTest {
    private RichTextEditor editor;

    @Before
    public void init() {
        editor = new RichTextEditor();
    }

    @Test
    public void initialValue_isEmpty() {
        Assert.assertTrue(editor.isEmpty());
        Assert.assertTrue(editor.asHtml().isEmpty());
    }

    @Test
    public void quillDefaultHtml_isEmpty() {
        // This is the default value of the Quill editor when there is no text
        editor.setValue("<p><br></p>");
        Assert.assertTrue(editor.isEmpty());
        Assert.assertTrue(editor.asHtml().isEmpty());
    }

    @Test
    public void singleLineBreak_isEmpty() {
        editor.setValue("<h1><br></h1>");
        Assert.assertTrue(editor.isEmpty());
        Assert.assertTrue(editor.asHtml().isEmpty());

        editor.setValue("<blockquote><br></blockquote>");
        Assert.assertTrue(editor.isEmpty());
        Assert.assertTrue(editor.asHtml().isEmpty());

        editor.setValue("<ul><li><br></li></ul>");
        Assert.assertTrue(editor.isEmpty());
        Assert.assertTrue(editor.asHtml().isEmpty());
    }

    @Test
    public void hasText_notEmpty() {
        editor.setValue("<p>foo<br></p>");
        Assert.assertFalse(editor.isEmpty());
        Assert.assertFalse(editor.asHtml().isEmpty());

        editor.setValue("<blockquote>foo<br></blockquote>");
        Assert.assertFalse(editor.isEmpty());
        Assert.assertFalse(editor.asHtml().isEmpty());

        editor.setValue("<ul><li>foo<br></li></ul>");
        Assert.assertFalse(editor.isEmpty());
        Assert.assertFalse(editor.asHtml().isEmpty());
    }

    @Test
    public void hasWhitespace_notEmpty() {
        // Space character
        editor.setValue("<p> <br></p>");
        Assert.assertFalse(editor.isEmpty());
        Assert.assertFalse(editor.asHtml().isEmpty());

        editor.setValue("<blockquote> <br></blockquote>");
        Assert.assertFalse(editor.isEmpty());
        Assert.assertFalse(editor.asHtml().isEmpty());

        editor.setValue("<ul><li> <br></li></ul>");
        Assert.assertFalse(editor.isEmpty());
        Assert.assertFalse(editor.asHtml().isEmpty());

        // Multiple newlines
        editor.setValue("<p><br></p><p><br></p>");
        Assert.assertFalse(editor.isEmpty());
        Assert.assertFalse(editor.asHtml().isEmpty());

        editor.setValue(
                "<blockquote><br></blockquote><blockquote><br></blockquote>");
        Assert.assertFalse(editor.isEmpty());
        Assert.assertFalse(editor.asHtml().isEmpty());

        editor.setValue("<ul><li><br></li><li><br></li></ul>");
        Assert.assertFalse(editor.isEmpty());
        Assert.assertFalse(editor.asHtml().isEmpty());
    }

    @Test
    public void hasImage_notEmpty() {
        editor.setValue("<img src='https://vaadin.com'>");
        Assert.assertFalse(editor.isEmpty());
        Assert.assertFalse(editor.asHtml().isEmpty());
    }
}
