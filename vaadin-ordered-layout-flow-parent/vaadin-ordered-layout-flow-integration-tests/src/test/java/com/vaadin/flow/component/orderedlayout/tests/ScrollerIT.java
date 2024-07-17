package com.vaadin.flow.component.orderedlayout.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.tests.AbstractComponentIT;


import com.vaadin.flow.testutil.TestPath;
import com.vaadin.flow.component.orderedlayout.testbench.ScrollerElement;

@TestPath("vaadin-ordered-layout/scroller")
public class ScrollerIT extends AbstractComponentIT {

    private ScrollerElement scroller;

    @Before
    public void init() {
        open();
        scroller = $(ScrollerElement.class).first();
    }

    @Test
    public void scrollToEnd() {
        $("button").id("scroll-to-end-button").click();
        int scrollTop = scroller.getPropertyInteger("scrollTop");
        int scrollHeight = scroller.getPropertyInteger("scrollHeight");
        int clientHeight = scroller.getPropertyInteger("clientHeight");
        Assert.assertEquals(scrollHeight - clientHeight, scrollTop);
    }

    @Test
    public void scrollToStart() {
        $("button").id("scroll-to-end-button").click();
        $("button").id("scroll-to-start-button").click();
        int scrollTop = scroller.getPropertyInteger("scrollTop");
        Assert.assertEquals(0, scrollTop);
    }
}
