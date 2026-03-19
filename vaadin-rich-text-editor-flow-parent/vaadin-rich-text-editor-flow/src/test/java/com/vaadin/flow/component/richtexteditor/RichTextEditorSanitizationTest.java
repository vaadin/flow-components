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
import org.junit.jupiter.api.Test;

class RichTextEditorSanitizationTest {
    // Decoration group sanitization

    @Test
    void sanitizeStrongTag_StrongTagPersist() {
        Assertions.assertEquals("<strong>Foo</strong>",
                RichTextEditor.sanitize("<strong>Foo</strong>"));
    }

    @Test
    void sanitizeEmTag_EmTagPersist() {
        Assertions.assertEquals("<em>Foo</em>",
                RichTextEditor.sanitize("<em>Foo</em>"));
    }

    @Test
    void sanitizeUTag_UTagPersist() {
        Assertions.assertEquals("<u>Foo</u>",
                RichTextEditor.sanitize("<u>Foo</u>"));
    }

    @Test
    void sanitizeSTag_STagPersist() {
        Assertions.assertEquals("<s>Foo</s>",
                RichTextEditor.sanitize("<s>Foo</s>"));
    }

    @Test
    void sanitizeCombinedDecorationTags_AllTagsPersist() {
        Assertions.assertEquals(
                "<strong><em><s><u>123123</u></s></em></strong>",
                RichTextEditor.sanitize(
                        "<strong><em><s><u>123123</u></s></em></strong>"));
    }

    // Headers group sanitization

    @Test
    void sanitizeH1Tag_H1TagPersist() {
        Assertions.assertEquals("<h1>Foo</h1>",
                RichTextEditor.sanitize("<h1>Foo</h1>"));
    }

    @Test
    void sanitizeH2Tag_H2TagPersist() {
        Assertions.assertEquals("<h2>Foo</h2>",
                RichTextEditor.sanitize("<h2>Foo</h2>"));
    }

    @Test
    void sanitizeH3Tag_H3TagPersist() {
        Assertions.assertEquals("<h3>Foo</h3>",
                RichTextEditor.sanitize("<h3>Foo</h3>"));
    }

    // Style group sanitization

    @Test
    void sanitizeStyleColor_StyleColorPersist() {
        Assertions.assertEquals(
                "<p><span style=\"color: rgb(230, 0, 0);\">Foo</span></p>",
                RichTextEditor.sanitize(
                        "<p><span style=\"color: rgb(230, 0, 0);\">Foo</span></p>"));
    }

    @Test
    void sanitizeStyleBackgroundColor_StyleBackgroundColorPersist() {
        Assertions.assertEquals(
                "<p><span style=\"background-color: rgb(230, 0, 0);\">Foo</span></p>",
                RichTextEditor.sanitize(
                        "<p><span style=\"background-color: rgb(230, 0, 0);\">Foo</span></p>"));
    }

    // Super - / Sub - scripts group sanitization

    @Test
    void sanitizeSupTag_SupTagPersist() {
        Assertions.assertEquals("<sup>Foo</sup>",
                RichTextEditor.sanitize("<sup>Foo</sup>"));
    }

    @Test
    void sanitizeSubTag_SubTagPersist() {
        Assertions.assertEquals("<sub>Foo</sub>",
                RichTextEditor.sanitize("<sub>Foo</sub>"));
    }

    // Lists group sanitization

    @Test
    void sanitizeOrderedListTag_OrderedListTagPersist() {
        Assertions.assertEquals("<ol>\n Foo\n</ol>",
                RichTextEditor.sanitize("<ol>\n Foo\n</ol>"));
    }

    @Test
    void sanitizeBulletListTag_BulletListTagPersist() {
        Assertions.assertEquals("<ul>\n Foo\n</ul>",
                RichTextEditor.sanitize("<ul>\n Foo\n</ul>"));
    }

    @Test
    void sanitizeListElementTag_listElementTagPersist() {
        Assertions.assertEquals("<li>Foo</li>",
                RichTextEditor.sanitize("<li>Foo</li>"));
    }

    // Alignment group sanitization

    @Test
    void sanitizeStyleTextAlign_StyleTextAlignPersist() {
        Assertions.assertEquals("<p style=\"text-align: center\">Foo</p>",
                RichTextEditor
                        .sanitize("<p style=\"text-align: center\">Foo</p>"));
    }

    // Script sanitization

    @Test
    void sanitizeScriptTag_scriptTagRemoved() {
        Assertions.assertEquals("",
                RichTextEditor.sanitize("<script>alert('Foo')</script>"));
    }

    // Image sanitization

    @Test
    void sanitizeImgTagWithHttpSource_srcAttributeRemoved() {
        Assertions.assertEquals("<img>",
                RichTextEditor.sanitize("<img src='http://vaadin.com'>"));
    }

    @Test
    void sanitizeImgTagWithHttpsSource_srcAttributeRemoved() {
        Assertions.assertEquals("<img>",
                RichTextEditor.sanitize("<img src='https://vaadin.com'>"));
    }

    @Test
    void sanitizeImgTagWithHttpsSource_onloadAttributeRemoved() {
        Assertions.assertEquals("<img>",
                RichTextEditor.sanitize("<img onload='https://vaadin.com'>"));
    }

    @Test
    void sanitizeImgTagWithDataSource_srcAttributePersist() {
        Assertions.assertEquals(
                "<img src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==\">",
                RichTextEditor.sanitize(
                        "<img src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==\">"));
    }

    // Blockquote sanitization

    @Test
    void sanitizeBlockquoteTag_blockquoteTagPersist() {
        Assertions.assertEquals("<blockquote>\n Foo\n</blockquote>",
                RichTextEditor.sanitize("<blockquote>\n Foo\n</blockquote>"));
    }

    // Code block sanitization

    @Test
    void sanitizePreTag_preTagPersist() {
        Assertions.assertEquals("<pre>Foo</pre>",
                RichTextEditor.sanitize("<pre>Foo</pre>"));
    }

    @Test
    void sanitizeWhiteSpacesNotRemoved() {
        var testHtml = "<p><strong>Line 1</strong></p>\n<p>        Indent 1</p>\n<p>         Indent 2</p>\n<p>Last line with extra     spaces and a\ttab</p>";
        Assertions.assertEquals(testHtml, RichTextEditor.sanitize(testHtml));
    }
}
