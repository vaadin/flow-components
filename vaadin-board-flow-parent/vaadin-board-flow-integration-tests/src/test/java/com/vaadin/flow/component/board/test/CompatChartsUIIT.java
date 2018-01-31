package com.vaadin.flow.component.board.test;

import java.util.function.Supplier;

import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.board.test.AbstractComponentTestView;
import com.vaadin.testbench.elements.ButtonElement;

/**
 *
 */
public abstract class CompatChartsUIIT extends AbstractParallelTest {

  //Unfortunatelly we can not used
  //Parametrized Runner, because Testbench already uses Parallel Runner
  Supplier<WebElement> smallSizeButton = () -> $(ButtonElement.class)
      .id(AbstractComponentTestView.SMALL_SIZE_BTN);

  @Test
  public void testScreenshot() throws Exception {
    openURL();
    String chartType= getUIClass().getSimpleName();
    compareScreen("initial"+chartType);

    $(ButtonElement.class).id(AbstractComponentTestView.MIDDLE_SIZE_BTN).click();
    compareScreen("middle"+chartType);

    $(ButtonElement.class).id(AbstractComponentTestView.SMALL_SIZE_BTN).click();
    compareScreen("small"+chartType);

  }


}
