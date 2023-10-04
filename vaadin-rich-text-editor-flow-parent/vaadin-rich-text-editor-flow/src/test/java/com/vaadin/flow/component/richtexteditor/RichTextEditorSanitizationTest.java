package com.vaadin.flow.component.richtexteditor;

import org.junit.Assert;
import org.junit.Test;

public class RichTextEditorSanitizationTest {

    private static final String TESTHTML = "<p><strong>Line 1</strong></p>\n<p>        Indent 1</p>\n<p>         Indent 2</p>\n<p>Last line with extra     spaces and a\ttab</p>";

    // Decoration group sanitization

    @Test
    public void sanitizeStrongTag_StrongTagPersist() {
        Assert.assertEquals("<strong>Foo</strong>",
                RichTextEditor.sanitize("<strong>Foo</strong>"));
    }

    @Test
    public void sanitizeEmTag_EmTagPersist() {
        Assert.assertEquals("<em>Foo</em>",
                RichTextEditor.sanitize("<em>Foo</em>"));
    }

    @Test
    public void sanitizeUTag_UTagPersist() {
        Assert.assertEquals("<u>Foo</u>",
                RichTextEditor.sanitize("<u>Foo</u>"));
    }

    @Test
    public void sanitizeSTag_STagPersist() {
        Assert.assertEquals("<s>Foo</s>",
                RichTextEditor.sanitize("<s>Foo</s>"));
    }

    @Test
    public void sanitizeCombinedDecorationTags_AllTagsPersist() {
        Assert.assertEquals("<strong><em><s><u>123123</u></s></em></strong>",
                RichTextEditor.sanitize(
                        "<strong><em><s><u>123123</u></s></em></strong>"));
    }

    // Headers group sanitization

    @Test
    public void sanitizeH1Tag_H1TagPersist() {
        Assert.assertEquals("<h1>Foo</h1>",
                RichTextEditor.sanitize("<h1>Foo</h1>"));
    }

    @Test
    public void sanitizeH2Tag_H2TagPersist() {
        Assert.assertEquals("<h2>Foo</h2>",
                RichTextEditor.sanitize("<h2>Foo</h2>"));
    }

    @Test
    public void sanitizeH3Tag_H3TagPersist() {
        Assert.assertEquals("<h3>Foo</h3>",
                RichTextEditor.sanitize("<h3>Foo</h3>"));
    }

    // Super - / Sub - scripts group sanitization

    @Test
    public void sanitizeSupTag_SupTagPersist() {
        Assert.assertEquals("<sup>Foo</sup>",
                RichTextEditor.sanitize("<sup>Foo</sup>"));
    }

    @Test
    public void sanitizeSubTag_SubTagPersist() {
        Assert.assertEquals("<sub>Foo</sub>",
                RichTextEditor.sanitize("<sub>Foo</sub>"));
    }

    // Lists group sanitization

    @Test
    public void sanitizeOrderedListTag_OrderedListTagPersist() {
        Assert.assertEquals("<ol>\n Foo\n</ol>",
                RichTextEditor.sanitize("<ol>\n Foo\n</ol>"));
    }

    @Test
    public void sanitizeBulletListTag_BulletListTagPersist() {
        Assert.assertEquals("<ul>\n Foo\n</ul>",
                RichTextEditor.sanitize("<ul>\n Foo\n</ul>"));
    }

    @Test
    public void sanitizeListElementTag_listElementTagPersist() {
        Assert.assertEquals("<li>Foo</li>",
                RichTextEditor.sanitize("<li>Foo</li>"));
    }

    // Alignment group sanitization

    @Test
    public void sanitizeStyleTextAlign_StyleTextAlignPersist() {
        Assert.assertEquals("<p style=\"text-align: center\">Foo</p>",
                RichTextEditor
                        .sanitize("<p style=\"text-align: center\">Foo</p>"));
    }

    // Script sanitization

    @Test
    public void sanitizeScriptTag_scriptTagRemoved() {
        Assert.assertEquals("",
                RichTextEditor.sanitize("<script>alert('Foo')</script>"));
    }

    // Image sanitization

    @Test
    public void sanitizeImgTagWithHttpSource_srcAttributeRemoved() {
        Assert.assertEquals("<img>",
                RichTextEditor.sanitize("<img src='http://vaadin.com'>"));
    }

    @Test
    public void sanitizeImgTagWithHttpsSource_srcAttributeRemoved() {
        Assert.assertEquals("<img>",
                RichTextEditor.sanitize("<img src='https://vaadin.com'>"));
    }

    @Test
    public void sanitizeImgTagWithHttpsSource_onloadAttributeRemoved() {
        Assert.assertEquals("<img>",
                RichTextEditor.sanitize("<img onload='https://vaadin.com'>"));
    }

    @Test
    public void sanitizeImgTagWithDataSource_srcAttributePersist() {
        Assert.assertEquals(
                "<img src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==\">",
                RichTextEditor.sanitize(
                        "<img src=\"data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==\">"));
    }

    // Blockquote sanitization

    @Test
    public void sanitizeBlockquoteTag_blockquoteTagPersist() {
        Assert.assertEquals("<blockquote>\n Foo\n</blockquote>",
                RichTextEditor.sanitize("<blockquote>\n Foo\n</blockquote>"));
    }

    // Code block sanitization

    @Test
    public void sanitizePreTag_preTagPersist() {
        Assert.assertEquals("<pre>Foo</pre>",
                RichTextEditor.sanitize("<pre>Foo</pre>"));
    }

    @Test
    public void sanitizeWhiteSpacesNotRemoved() {
        String html = TESTHTML;
        Assert.assertEquals(TESTHTML, RichTextEditor.sanitize(html));
    }
}
