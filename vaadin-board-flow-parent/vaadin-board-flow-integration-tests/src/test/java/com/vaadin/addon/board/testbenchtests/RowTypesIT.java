package com.vaadin.addon.board.testbenchtests;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.board.testUI.RowTypesUI;
import com.vaadin.board.elements.RowElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elements.CssLayoutElement;

public class RowTypesIT extends AbstractParallelTest {

    private void scrollToLastComponent() {
        $(RowElement.class).last().$(CssLayoutElement.class).last()
                .scrollIntoView();
    }

    @Override
    protected Class<?> getUIClass() {
        return RowTypesUI.class;
    }

    @Test
    public void testRowTypesJavaSampleScreenshot_windowSizeSmall()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        openURL();
        compareScreen("RowTypesUI_small");
    }

    @Test
    public void testRowTypesJavaSampleScrolledScreenshot_windowSizeSmall()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        openURL();
        scrollToLastComponent();
        compareScreen("RowTypesUI_scrollToBottom_small");
    }

    @Test
    public void testRowTypesJavaSampleScreenshot_windowSizeMedium()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_MEDIUM);
        openURL();
        compareScreen("RowTypesUI_medium");
    }

    @Test
    public void testRowTypesJavaSampleScrolledScreenshot_windowSizeMedium()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_MEDIUM);
        openURL();
        scrollToLastComponent();
        compareScreen("RowTypesUI_scrollToBottom_medium");
    }

    @Test
    public void testRowTypesJavaSampleScreenshot_windowSizeLarge()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_LARGE);
        openURL();
        compareScreen("RowTypesUI_large");
    }
}
