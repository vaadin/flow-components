/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RichTextEditorHtmlIsEmptyTest {
    private RichTextEditor editor;

    @BeforeEach
    void init() {
        editor = new RichTextEditor();
    }

    @Test
    void initialValue_isEmpty() {
        Assertions.assertTrue(editor.isEmpty());
        Assertions.assertTrue(editor.asHtml().isEmpty());
    }

    @Test
    void hasQuillDefaultHtml_isEmpty() {
        // This is the default value of the Quill editor when there is no text
        editor.setValue("<p><br></p>");
        Assertions.assertTrue(editor.isEmpty());
        Assertions.assertTrue(editor.asHtml().isEmpty());
    }

    @Test
    void hasSingleLineBreak_isEmpty() {
        editor.setValue("<h1><br></h1>");
        Assertions.assertTrue(editor.isEmpty());
        Assertions.assertTrue(editor.asHtml().isEmpty());

        editor.setValue("<blockquote><br></blockquote>");
        Assertions.assertTrue(editor.isEmpty());
        Assertions.assertTrue(editor.asHtml().isEmpty());

        editor.setValue("<ul><li><br></li></ul>");
        Assertions.assertTrue(editor.isEmpty());
        Assertions.assertTrue(editor.asHtml().isEmpty());
    }

    @Test
    void hasText_notEmpty() {
        editor.setValue("<p>foo<br></p>");
        Assertions.assertFalse(editor.isEmpty());
        Assertions.assertFalse(editor.asHtml().isEmpty());

        editor.setValue("<blockquote>foo<br></blockquote>");
        Assertions.assertFalse(editor.isEmpty());
        Assertions.assertFalse(editor.asHtml().isEmpty());

        editor.setValue("<ul><li>foo<br></li></ul>");
        Assertions.assertFalse(editor.isEmpty());
        Assertions.assertFalse(editor.asHtml().isEmpty());
    }

    @Test
    void hasWhitespace_notEmpty() {
        // Space character
        editor.setValue("<p> <br></p>");
        Assertions.assertFalse(editor.isEmpty());
        Assertions.assertFalse(editor.asHtml().isEmpty());

        editor.setValue("<blockquote> <br></blockquote>");
        Assertions.assertFalse(editor.isEmpty());
        Assertions.assertFalse(editor.asHtml().isEmpty());

        editor.setValue("<ul><li> <br></li></ul>");
        Assertions.assertFalse(editor.isEmpty());
        Assertions.assertFalse(editor.asHtml().isEmpty());

        // Multiple newlines
        editor.setValue("<p><br></p><p><br></p>");
        Assertions.assertFalse(editor.isEmpty());
        Assertions.assertFalse(editor.asHtml().isEmpty());

        editor.setValue(
                "<blockquote><br></blockquote><blockquote><br></blockquote>");
        Assertions.assertFalse(editor.isEmpty());
        Assertions.assertFalse(editor.asHtml().isEmpty());

        editor.setValue("<ul><li><br></li><li><br></li></ul>");
        Assertions.assertFalse(editor.isEmpty());
        Assertions.assertFalse(editor.asHtml().isEmpty());
    }

    @Test
    void hasImage_notEmpty() {
        editor.setValue("<img>");
        Assertions.assertFalse(editor.isEmpty());
        Assertions.assertFalse(editor.asHtml().isEmpty());
    }
}
