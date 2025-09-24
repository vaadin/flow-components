/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.richtexteditor.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.richtexteditor.testbench.RichTextEditorElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-rich-text-editor/i18n")
public class RichTextEditorI18nIT extends AbstractComponentIT {
    private RichTextEditorElement editor;

    @Before
    public void init() {
        open();
        editor = $(RichTextEditorElement.class).waitForFirst();
    }

    @Test
    public void setFullI18n_updatesAllTexts() {
        $("button").id("set-full-i18n").click();

        Assert.assertEquals("Undo custom", getToolbarButtonAriaLabel("undo"));
        Assert.assertEquals("Redo custom", getToolbarButtonAriaLabel("redo"));
        Assert.assertEquals("Bold custom", getToolbarButtonAriaLabel("bold"));
        Assert.assertEquals("Italic custom",
                getToolbarButtonAriaLabel("italic"));
        Assert.assertEquals("Underline custom",
                getToolbarButtonAriaLabel("underline"));
        Assert.assertEquals("Strike custom",
                getToolbarButtonAriaLabel("strike"));
        Assert.assertEquals("Color custom", getToolbarButtonAriaLabel("color"));
        Assert.assertEquals("Background custom",
                getToolbarButtonAriaLabel("background"));
        Assert.assertEquals("Header 1 custom", getToolbarButtonAriaLabel("h1"));
        Assert.assertEquals("Header 2 custom", getToolbarButtonAriaLabel("h2"));
        Assert.assertEquals("Header 3 custom", getToolbarButtonAriaLabel("h3"));
        Assert.assertEquals("Subscript custom",
                getToolbarButtonAriaLabel("subscript"));
        Assert.assertEquals("Superscript custom",
                getToolbarButtonAriaLabel("superscript"));
        Assert.assertEquals("Ordered list custom",
                getToolbarButtonAriaLabel("ol"));
        Assert.assertEquals("Bullet list custom",
                getToolbarButtonAriaLabel("ul"));
        Assert.assertEquals("Align left custom",
                getToolbarButtonAriaLabel("left"));
        Assert.assertEquals("Align center custom",
                getToolbarButtonAriaLabel("center"));
        Assert.assertEquals("Align right custom",
                getToolbarButtonAriaLabel("right"));
        Assert.assertEquals("Image custom", getToolbarButtonAriaLabel("image"));
        Assert.assertEquals("Link custom", getToolbarButtonAriaLabel("link"));
        Assert.assertEquals("Blockquote custom",
                getToolbarButtonAriaLabel("blockquote"));
        Assert.assertEquals("Code block custom",
                getToolbarButtonAriaLabel("code"));
        Assert.assertEquals("Clean custom", getToolbarButtonAriaLabel("clean"));
    }

    @Test
    public void setPartialI18n_mergesWithExistingI18n() {
        $("button").id("set-partial-i18n").click();

        Assert.assertEquals("Undo custom", getToolbarButtonAriaLabel("undo"));
        Assert.assertEquals("Redo custom", getToolbarButtonAriaLabel("redo"));
        Assert.assertEquals("bold", getToolbarButtonAriaLabel("bold"));
        Assert.assertEquals("italic", getToolbarButtonAriaLabel("italic"));
    }

    @Test
    public void setI18n_detach_attach_i18nRestored() {
        $("button").id("set-full-i18n").click();
        $("button").id("detach").click();
        $("button").id("attach").click();

        editor = $(RichTextEditorElement.class).waitForFirst();
        Assert.assertEquals("Undo custom", getToolbarButtonAriaLabel("undo"));
        Assert.assertEquals("Redo custom", getToolbarButtonAriaLabel("redo"));
    }

    private String getToolbarButtonAriaLabel(String buttonId) {
        TestBenchElement button = editor.$("button")
                .withAttribute("id", "btn-" + buttonId).first();
        return button.getDomAttribute("aria-label");
    }
}
