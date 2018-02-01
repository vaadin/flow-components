package com.vaadin.flow.component.board.test;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.board.testbench.RowElement;
import com.vaadin.testbench.TestBenchElement;

public class ImageCollageIT extends AbstractParallelTest {

    private void scrollToLastComponent() {
        List<WebElement> divs = $(RowElement.class).last()
                .findElements(By.xpath("./div"));
        ((TestBenchElement) divs.get(divs.size() - 1)).scrollIntoView();
    }

    @Test
    public void testImageCollageJavaSampleScreenshot_windowSizeSmall()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        open(ImageCollageView.class);
        compareScreen("ImageCollageUI_small");
    }

    @Test
    public void testImageCollageJavaSampleScrolledScreenshot_windowSizeSmall()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_SMALL);
        open(ImageCollageView.class);
        scrollToLastComponent();
        compareScreen("ImageCollageUI_scrollToBottom_small");
    }

    @Test
    public void testImageCollageJavaSampleScreenshot_windowSizeMedium()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_MEDIUM);
        open(ImageCollageView.class);
        compareScreen("ImageCollageUI_medium");
    }

    @Test
    public void testImageCollageJavaSampleScrolledScreenshot_windowSizeMedium()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_MEDIUM);
        open(ImageCollageView.class);
        scrollToLastComponent();
        compareScreen("ImageCollageUI_scrollToBottom_medium");
    }

    @Test
    public void testImageCollageJavaSampleScreenshot_windowSizeLarge()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_LARGE);
        open(ImageCollageView.class);
        compareScreen("ImageCollageUI_large");
    }

    @Test
    public void testImageCollageJavaSampleScrolledScreenshot_windowSizeLarge()
            throws IOException {
        getDriver().manage().window().setSize(TestUtils.WINDOW_SIZE_LARGE);
        open(ImageCollageView.class);
        scrollToLastComponent();
        compareScreen("ImageCollageUI_scrollToBottom_large");
    }

}
