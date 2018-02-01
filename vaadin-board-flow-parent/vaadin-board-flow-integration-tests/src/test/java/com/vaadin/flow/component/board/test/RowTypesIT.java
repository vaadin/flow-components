package com.vaadin.flow.component.board.test;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.vaadin.flow.component.board.examples.RowTypes;
import com.vaadin.flow.component.board.testbench.RowElement;
import com.vaadin.testbench.TestBenchElement;

public class RowTypesIT extends AbstractParallelTest {

    private void scrollToLastComponent() {
        List<TestBenchElement> children = $(RowElement.class).last()
                .getChildren();
        children.get(children.size() - 1).scrollIntoView();
    }

    @Test
    public void small() throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        open(RowTypes.class);
        compareScreen("small");
    }

    @Test
    public void smallScrolledDown() throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        open(RowTypes.class);
        scrollToLastComponent();
        compareScreen("small_bottom");
    }

    @Test
    public void medium() throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_MEDIUM);
        open(RowTypes.class);
        compareScreen("medium");
    }

    @Test
    public void large() throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_LARGE);
        open(RowTypes.class);
        compareScreen("large");
    }
}
