/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.orderedlayout.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.orderedlayout.testbench.ScrollerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-ordered-layout/scroller")
public class ScrollerIT extends AbstractComponentIT {

    private ScrollerElement scroller;

    @Before
    public void init() {
        open();
        scroller = $(ScrollerElement.class).first();
    }

    @Test
    public void scrollToBottom() {
        $("button").id("scroll-to-bottom-button").click();
        int scrollTop = scroller.getPropertyInteger("scrollTop");
        int scrollHeight = scroller.getPropertyInteger("scrollHeight");
        int clientHeight = scroller.getPropertyInteger("clientHeight");
        Assert.assertEquals(scrollHeight - clientHeight, scrollTop);
        Assert.assertNotEquals(0, scrollTop);
    }

    @Test
    public void scrollToTop() {
        $("button").id("scroll-to-bottom-button").click();
        $("button").id("scroll-to-top-button").click();
        int scrollTop = scroller.getPropertyInteger("scrollTop");
        Assert.assertEquals(0, scrollTop);
    }
}
