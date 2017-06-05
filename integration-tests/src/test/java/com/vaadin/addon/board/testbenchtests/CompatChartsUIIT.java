package com.vaadin.addon.board.testbenchtests;

import java.util.function.Supplier;

import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.board.testUI.AbstractTestCompUI;
import com.vaadin.testbench.elements.ButtonElement;

/**
 *
 */
public abstract class CompatChartsUIIT extends AbstractParallelTest {

  //Unfortunatelly we can not used
  //Parametrized Runner, because Testbench already uses Parallel Runner
  Supplier<WebElement> smallSizeButton = () -> $(ButtonElement.class)
      .id(AbstractTestCompUI.SMALL_SIZE_BTN);

  @Test
  public void testScreenshot() throws Exception {
    String chartType= getUIClass().getSimpleName();
    compareScreen("initial"+chartType);

    $(ButtonElement.class).id(AbstractTestCompUI.MIDDLE_SIZE_BTN).click();
    compareScreen("middle"+chartType);

    $(ButtonElement.class).id(AbstractTestCompUI.SMALL_SIZE_BTN).click();
    compareScreen("small"+chartType);

  }


}
