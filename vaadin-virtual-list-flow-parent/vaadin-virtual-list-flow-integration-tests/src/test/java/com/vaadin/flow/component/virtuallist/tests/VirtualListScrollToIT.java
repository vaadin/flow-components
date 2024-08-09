/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.virtuallist.tests;

import org.junit.Test;

import com.vaadin.flow.component.virtuallist.testbench.VirtualListElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

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
