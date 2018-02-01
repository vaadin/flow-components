package com.vaadin.flow.component.board.test;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.vaadin.flow.component.board.examples.SalesDashboard;
import com.vaadin.flow.component.board.testbench.RowElement;
import com.vaadin.testbench.TestBenchElement;

public class SalesDashboardIT extends AbstractParallelTest {

    private void scrollToLastComponent() {
        List<TestBenchElement> children = $(RowElement.class).last()
                .getChildren();
        children.get(children.size() - 1).scrollIntoView();
    }

    @Test
    public void testSalesDashboardJavaSampleScreenshot_windowSizeSmall()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        open(SalesDashboard.class);
        compareScreen("SalesDashboardJavaSample_small");
    }

    @Test
    public void testSalesDashboardJavaSampleScrolledMidScreenshot_windowSizeSmall()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        open(SalesDashboard.class);
        $(RowElement.class).get(1).$(RowElement.class).first().getChildren()
                .get(0).scrollIntoView();
        compareScreen("SalesDashboardUI_scrollToMid_small");
    }

    @Test
    public void testSalesDashboardJavaSampleScrolledScreenshot_windowSizeSmall()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        open(SalesDashboard.class);
        scrollToLastComponent();
        compareScreen("SalesDashboardUI_scrollToBottom_small");
    }

    @Test
    public void testSalesDashboardJavaSampleScreenshot_windowSizeMedium()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_MEDIUM);
        open(SalesDashboard.class);
        compareScreen("SalesDashboardUI_medium");
    }

    @Test
    public void testSalesDashboardJavaSampleScrolledScreenshot_windowSizeMedium()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_MEDIUM);
        open(SalesDashboard.class);
        scrollToLastComponent();
        compareScreen("SalesDashboardUI_scrollToBottom_medium");
    }

    @Test
    public void testSalesDashboardJavaSampleScreenshot_windowSizeLarge()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_LARGE);
        open(SalesDashboard.class);
        compareScreen("SalesDashboardUI_large");
    }

    @Test
    public void testSalesDashboardJavaSampleScrolledScreenshot_windowSizeLarge()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_LARGE);
        open(SalesDashboard.class);
        scrollToLastComponent();
        compareScreen("SalesDashboardUI_scrollToBottom_large");
    }
}
