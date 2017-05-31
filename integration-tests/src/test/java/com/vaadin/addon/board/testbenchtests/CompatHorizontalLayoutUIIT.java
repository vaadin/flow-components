package com.vaadin.addon.board.testbenchtests;

import static com.vaadin.addon.board.testbenchtests.TestFunctions.genericAssertHeight;
import static com.vaadin.addon.board.testbenchtests.TestFunctions.genericAssertWidth;
import java.util.function.Supplier;

import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.board.testUI.AbstractTestCompUI;
import com.vaadin.addon.board.testUI.CompatLayoutComponents;
import com.vaadin.testbench.elements.ButtonElement;

/**
 *
 */
public class CompatHorizontalLayoutUIIT extends AbstractParallelTest {
  @Override
  protected Class<?> getUIClass() {
    return CompatLayoutComponents.HorizontalLayoutUI.class;
  }


  Supplier<WebElement> buttonMiddleSupplier = () -> $(ButtonElement.class)
      .id(AbstractTestCompUI.ID_PREFIX + 2);

  @Test
  public void testGenericWidth()
      throws Exception {
    genericAssertWidth.accept(buttonSwitchSupplier, buttonMiddleSupplier);
  }

  @Test
  public void testGenericHeight()
      throws Exception {
    genericAssertHeight.accept(buttonSwitchSupplier, buttonMiddleSupplier);
  }



}
