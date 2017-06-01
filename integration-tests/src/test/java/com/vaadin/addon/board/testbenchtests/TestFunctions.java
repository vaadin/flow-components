package com.vaadin.addon.board.testbenchtests;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.frp.Result;
import com.vaadin.testbench.By;

/**
 *
 */
public class TestFunctions {


  static Function<WebDriver, WebElement> board() {
    return (webDriver) -> webDriver.findElement(By.tagName("vaadin-board"));
  }

  static Function<WebDriver, List<WebElement>> rows() {
    return (webDriver) -> board()
        .apply(webDriver)
        .findElements(By.xpath("//vaadin-board/vaadin-board-row"));
  }


  static BiFunction<WebDriver, String, Result<WebElement>> testComponent() {
    return (webDriver, id) -> Result.ofNullable(
        board().apply(webDriver).findElement(By.id(id)),
        "id was not found " + id
    );
  }

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
