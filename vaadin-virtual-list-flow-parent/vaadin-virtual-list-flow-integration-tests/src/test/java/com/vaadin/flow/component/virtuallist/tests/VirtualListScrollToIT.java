package com.vaadin.flow.component.virtuallist.tests;

import com.vaadin.flow.component.virtuallist.testbench.VirtualListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-virtual-list/scroll-to")
public class VirtualListScrollToIT extends AbstractComponentIT {
    private VirtualListElement virtualList;

    @Before
    public void init() {
        open();
        virtualList = $(VirtualListElement.class).first();
    }

    @Test
    public void scrollToEnd() {
        $("button").id("scroll-to-end").click();
        Assert.assertEquals(1000, virtualList.getLastVisibleRowIndex());
    }

    @Test
    public void scrollToStart() {
        scrollToEnd();
        $("button").id("scroll-to-start").click();
        Assert.assertEquals(0, virtualList.getFirstVisibleRowIndex());
    }

    @Test
    public void scrollToIndex() {
        scrollToEnd();
        $("button").id("scroll-to-row-500").click();
        Assert.assertEquals(500, virtualList.getFirstVisibleRowIndex());
    }

}
