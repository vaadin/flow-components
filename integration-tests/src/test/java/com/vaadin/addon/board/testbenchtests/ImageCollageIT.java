package com.vaadin.addon.board.testbenchtests;

import java.io.IOException;

import org.junit.Test;

import com.vaadin.addon.board.testUI.ImageCollageUI;
import com.vaadin.board.elements.RowElement;
import com.vaadin.testbench.elements.CssLayoutElement;

public class ImageCollageIT extends AbstractParallelTest {

    private void scrollToLastComponent() {
        $(RowElement.class).last().$(CssLayoutElement.class).last()
                .scrollIntoView();
    }

    @Override
    protected Class<?> getUIClass() {
        return ImageCollageUI.class;
    }

    @Test
    public void testImageCollageJavaSampleScreenshot_windowSizeSmall()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        openURL();
        compareScreen("ImageCollageUI_small");
    }

    @Test
    public void testImageCollageJavaSampleScrolledScreenshot_windowSizeSmall()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        openURL();
        scrollToLastComponent();
        compareScreen("ImageCollageUI_scrollToBottom_small");
    }

    @Test
    public void testImageCollageJavaSampleScreenshot_windowSizeMedium()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_MEDIUM);
        openURL();
        compareScreen("ImageCollageUI_medium");
    }

    @Test
    public void testImageCollageJavaSampleScrolledScreenshot_windowSizeMedium()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_MEDIUM);
        openURL();
        scrollToLastComponent();
        compareScreen("ImageCollageUI_scrollToBottom_medium");
    }

    @Test
    public void testImageCollageJavaSampleScreenshot_windowSizeLarge()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_LARGE);
        openURL();
        compareScreen("ImageCollageUI_large");
    }

    @Test
    public void testImageCollageJavaSampleScrolledScreenshot_windowSizeLarge()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_LARGE);
        openURL();
        scrollToLastComponent();
        compareScreen("ImageCollageUI_scrollToBottom_large");
    }

}
