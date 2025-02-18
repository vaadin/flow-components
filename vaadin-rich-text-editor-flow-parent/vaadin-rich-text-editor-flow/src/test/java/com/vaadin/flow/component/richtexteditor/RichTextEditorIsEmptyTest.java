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
    public void emptyHtmlTags_isEmpty() {
        editor.setValue("""
                <h1> </h1>
                <p> </p>
                <h2> </h2>
                <ol>
                    <li> </li>
                    <li> </li>
                    <li> </li>
                </ol>
                <ul>
                    <li> </li>
                    <li> </li>
                    <li> </li>
                </ul>
                <blockquote> </blockquote>
                <pre>

                </pre>
                <a href="https://vaadin.com"> </a>
                """);
        Assert.assertTrue(editor.isEmpty());
        Assert.assertTrue(editor.asHtml().isEmpty());
    }

    @Test
    public void hasTextNode_notEmpty() {
        editor.setValue("value");
        Assert.assertFalse(editor.isEmpty());
        Assert.assertFalse(editor.asHtml().isEmpty());
    }

    @Test
    public void hasNestedTextNodes_notEmpty() {
        editor.setValue("""
                <h1>value</h1>
                """);
        Assert.assertFalse(editor.isEmpty());
        Assert.assertFalse(editor.asHtml().isEmpty());

        editor.setValue("""
                <ol>
                    <li>value</li>
                    <li>value</li>
                    <li>value</li>
                </ol>
                """);
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
