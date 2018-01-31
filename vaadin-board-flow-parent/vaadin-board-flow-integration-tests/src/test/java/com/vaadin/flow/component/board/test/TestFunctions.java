package com.vaadin.flow.component.board.test;

import java.util.function.Function;

import org.junit.Assert;
import org.openqa.selenium.WebElement;

/**
 *
 */
public class TestFunctions {


  static void assertDimension(WebElement controlElement, WebElement testedElement, Function<WebElement, Integer> callback) {

    int valueBefore = callback.apply(testedElement);
    controlElement.click();
    int valueAfter = callback.apply(testedElement);
    controlElement.click();
    int valueReset = callback.apply(testedElement);

    Assert.assertTrue("before < after", valueBefore < valueAfter);
    Assert.assertTrue("after > reset", valueAfter > valueReset);
    Assert.assertTrue("after > before", valueAfter > valueBefore);
    Assert.assertTrue("before == reset", valueBefore == valueReset);
  }

}
