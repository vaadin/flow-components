/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.board.test;

import java.util.List;

import org.junit.Test;

import com.vaadin.flow.component.board.testbench.RowElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractParallelTest;

public abstract class AbstractDemoIT extends AbstractParallelTest {

    protected abstract Class<?> getView();

    private void scrollToBottom() {
        List<TestBenchElement> children = $(RowElement.class).last()
                .getChildren();
        children.get(children.size() - 1).scrollIntoView();
    }

    @Test
    public void windowSizeSmall() throws Exception {
        open(getView(), WINDOW_SIZE_SMALL);
        compareScreen("small_top");
        scrollToBottom();
        compareScreen("small_bottom");
    }

    @Test
    public void windowSizeMedium() throws Exception {
        open(getView(), WINDOW_SIZE_MEDIUM);
        compareScreen("medium_top");
        scrollToBottom();
        compareScreen("medium_bottom");
    }

    @Test
    public void windowSizeLarge() throws Exception {
        open(getView(), WINDOW_SIZE_LARGE);
        compareScreen("large_top");
    }

}
