package com.vaadin.flow.component.virtuallist.tests;

import com.vaadin.flow.component.virtuallist.testbench.VirtualListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Test;

@TestPath("vaadin-virtual-list/scroll-to")
public class VirtualListScrollToIT extends AbstractComponentIT {
    private VirtualListElement virtualList;

    public void open() {
        super.open();
        virtualList = $(VirtualListElement.class).waitForFirst();
    }

    public void openWithInitialPosition(String initialPosition) {
        super.open("initialPosition=" + initialPosition);
        virtualList = $(VirtualListElement.class).waitForFirst();
    }

    @Test
    public void scrollToEnd() {
        open();
        $("button").id("scroll-to-end").click();
        assertLastVisibleRowIndex(999);
    }

    @Test
    public void scrollToStart() {
        scrollToEnd();
        $("button").id("scroll-to-start").click();
        assertFirstVisibleRowIndex(0);
    }

    @Test
    public void scrollToIndex() {
        scrollToEnd();
        $("button").id("scroll-to-row-500").click();
        assertFirstVisibleRowIndex(500);
    }

    @Test
    public void initialScrollToIndex() {
        openWithInitialPosition("middle");
        assertFirstVisibleRowIndex(500);
    }

    @Test
    public void initialScrollToEnd() {
        openWithInitialPosition("end");
        assertLastVisibleRowIndex(999);
    }

    @Test
    public void scrollToEnd_addItemsAndScrollToItem() {
        scrollToEnd();
        $("button").id("add-items-and-scroll-to-item").click();
        assertFirstVisibleRowIndex(1500);
    }

    @Test
    public void scrollToEnd_addItemsAndScrollToEnd() {
        scrollToEnd();
        $("button").id("add-items-and-scroll-to-end").click();
        assertLastVisibleRowIndex(1999);
    }

    private void assertFirstVisibleRowIndex(int rowIndex) {
        waitUntil(driver -> virtualList.getFirstVisibleRowIndex() == rowIndex);
    }

    private void assertLastVisibleRowIndex(int rowIndex) {
        waitUntil(driver -> virtualList.getLastVisibleRowIndex() == rowIndex);
    }
}
