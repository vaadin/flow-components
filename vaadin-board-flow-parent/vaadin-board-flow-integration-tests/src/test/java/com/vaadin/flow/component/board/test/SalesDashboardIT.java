package com.vaadin.flow.component.board.test;

import java.io.IOException;

import org.junit.Test;

import com.vaadin.board.elements.RowElement;
import com.vaadin.flow.component.board.test.SalesDashboardUI;
import com.vaadin.testbench.elements.CssLayoutElement;

public class SalesDashboardIT extends AbstractParallelTest {

    private void scrollToLastComponent() {
        $(RowElement.class).last().$(CssLayoutElement.class).last()
                .scrollIntoView();
    }

    @Override
    protected Class<?> getUIClass() {
        return SalesDashboardUI.class;
    }

    @Test
    public void testSalesDashboardJavaSampleScreenshot_windowSizeSmall()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        openURL();
        compareScreen("SalesDashboardJavaSample_small");
    }

    @Test
    public void testSalesDashboardJavaSampleScrolledMidScreenshot_windowSizeSmall()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        openURL();
        $(RowElement.class).get(1).$(RowElement.class).first()
                .$(CssLayoutElement.class).first().scrollIntoView();
        compareScreen("SalesDashboardUI_scrollToMid_small");
    }

    @Test
    public void testSalesDashboardJavaSampleScrolledScreenshot_windowSizeSmall()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        openURL();
        scrollToLastComponent();
        compareScreen("SalesDashboardUI_scrollToBottom_small");
    }

    @Test
    public void testSalesDashboardJavaSampleScreenshot_windowSizeMedium()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_MEDIUM);
        openURL();
        compareScreen("SalesDashboardUI_medium");
    }

    @Test
    public void testSalesDashboardJavaSampleScrolledScreenshot_windowSizeMedium()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_MEDIUM);
        openURL();
        scrollToLastComponent();
        compareScreen("SalesDashboardUI_scrollToBottom_medium");
    }

    @Test
    public void testSalesDashboardJavaSampleScreenshot_windowSizeLarge()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_LARGE);
        openURL();
        compareScreen("SalesDashboardUI_large");
    }

    @Test
    public void testSalesDashboardJavaSampleScrolledScreenshot_windowSizeLarge()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_LARGE);
        openURL();
        scrollToLastComponent();
        compareScreen("SalesDashboardUI_scrollToBottom_large");
    }
}
