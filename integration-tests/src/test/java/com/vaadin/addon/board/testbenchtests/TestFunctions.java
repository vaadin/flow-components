package com.vaadin.addon.board.testbenchtests;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

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


  static Function<Supplier<WebElement>, Integer> width = (supplier) ->
      supplier.get().getSize().getWidth();

  static Function<Supplier<WebElement>, Integer> height = (supplier) ->
      supplier.get().getSize().getHeight();

  static Function<
      Function<Supplier<WebElement>, Integer>,
      BiConsumer<Supplier<WebElement>, Supplier<WebElement>>> genericAssert = (func)
      -> (buttonSwitchSupplier, middleElementSupplier) -> {

    int widthBefore = func.apply(middleElementSupplier);
    buttonSwitchSupplier.get().click();
    int widthAfter = func.apply(middleElementSupplier);
    buttonSwitchSupplier.get().click();
    int widthReset = func.apply(middleElementSupplier);

    Assert.assertTrue("before < after", widthBefore < widthAfter);
    Assert.assertTrue("after > reset", widthAfter > widthReset);
    Assert.assertTrue("after > before", widthAfter > widthBefore);
    Assert.assertTrue("before == reset", widthBefore == widthReset);
  };

  static BiConsumer<Supplier<WebElement>, Supplier<WebElement>> genericAssertWidth
      = (buttonSwitchSupplier, middleElementSupplier) -> genericAssert.apply(TestFunctions.width);

  static BiConsumer<Supplier<WebElement>, Supplier<WebElement>> genericAssertHeight
      = (buttonSwitchSupplier, middleElementSupplier) -> genericAssert.apply(TestFunctions.height);


}
