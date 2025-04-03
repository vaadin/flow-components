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

        Assert.assertEquals("Undo custom", getToolbarButtonTooltipText("undo"));
        Assert.assertEquals("Redo custom", getToolbarButtonTooltipText("redo"));
        Assert.assertEquals("Bold custom", getToolbarButtonTooltipText("bold"));
        Assert.assertEquals("Italic custom",
                getToolbarButtonTooltipText("italic"));
        Assert.assertEquals("Underline custom",
                getToolbarButtonTooltipText("underline"));
        Assert.assertEquals("Strike custom",
                getToolbarButtonTooltipText("strike"));
        Assert.assertEquals("Color custom",
                getToolbarButtonTooltipText("color"));
        Assert.assertEquals("Background custom",
                getToolbarButtonTooltipText("background"));
        Assert.assertEquals("Header 1 custom",
                getToolbarButtonTooltipText("h1"));
        Assert.assertEquals("Header 2 custom",
                getToolbarButtonTooltipText("h2"));
        Assert.assertEquals("Header 3 custom",
                getToolbarButtonTooltipText("h3"));
        Assert.assertEquals("Subscript custom",
                getToolbarButtonTooltipText("subscript"));
        Assert.assertEquals("Superscript custom",
                getToolbarButtonTooltipText("superscript"));
        Assert.assertEquals("Ordered list custom",
                getToolbarButtonTooltipText("ol"));
        Assert.assertEquals("Bullet list custom",
                getToolbarButtonTooltipText("ul"));
        Assert.assertEquals("Align left custom",
                getToolbarButtonTooltipText("left"));
        Assert.assertEquals("Align center custom",
                getToolbarButtonTooltipText("center"));
        Assert.assertEquals("Align right custom",
                getToolbarButtonTooltipText("right"));
        Assert.assertEquals("Image custom",
                getToolbarButtonTooltipText("image"));
        Assert.assertEquals("Link custom", getToolbarButtonTooltipText("link"));
        Assert.assertEquals("Blockquote custom",
                getToolbarButtonTooltipText("blockquote"));
        Assert.assertEquals("Code block custom",
                getToolbarButtonTooltipText("code"));
        Assert.assertEquals("Clean custom",
                getToolbarButtonTooltipText("clean"));
    }

    @Test
    public void setPartialI18n_mergesWithExistingI18n() {
        $("button").id("set-partial-i18n").click();

        Assert.assertEquals("Undo custom", getToolbarButtonTooltipText("undo"));
        Assert.assertEquals("Redo custom", getToolbarButtonTooltipText("redo"));
        Assert.assertEquals("bold", getToolbarButtonTooltipText("bold"));
        Assert.assertEquals("italic", getToolbarButtonTooltipText("italic"));
    }

    @Test
    public void setI18n_detach_attach_i18nRestored() {
        $("button").id("set-full-i18n").click();
        $("button").id("detach").click();
        $("button").id("attach").click();

        editor = $(RichTextEditorElement.class).waitForFirst();
        Assert.assertEquals("Undo custom", getToolbarButtonTooltipText("undo"));
        Assert.assertEquals("Redo custom", getToolbarButtonTooltipText("redo"));
    }

    private String getToolbarButtonTooltipText(String buttonId) {
        TestBenchElement tooltip = editor.$("vaadin-tooltip")
                .withAttribute("for", "btn-" + buttonId).first();
        return tooltip.getPropertyString("text");
    }
}
