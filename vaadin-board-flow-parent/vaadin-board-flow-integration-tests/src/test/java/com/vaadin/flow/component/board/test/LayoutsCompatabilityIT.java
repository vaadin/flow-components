package com.vaadin.flow.component.board.test;

import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.flow.component.board.test.AbstractTestCompUI;
import com.vaadin.flow.component.board.test.CompatLayoutComponents;
import com.vaadin.testbench.AbstractHasTestBenchCommandExecutor;
import com.vaadin.testbench.elements.ButtonElement;

public class LayoutsCompatabilityIT extends AbstractParallelTest {

  //Need own interface to throw exception
  @FunctionalInterface
  public interface CheckedConsumer<T> {
    void apply(T t) throws Exception;
  }
  CheckedConsumer<AbstractHasTestBenchCommandExecutor> testConsumer = e -> {
    ButtonElement testedElement = e.$(ButtonElement.class).id(AbstractTestCompUI.ID_PREFIX + 1);
    testGenericWidth(testedElement);
  };
  @Test
  public void testHorizontalLayout() throws Exception {
    setUIClass(CompatLayoutComponents.HorizontalLayoutUI.class);
    openURL();
    testConsumer.apply(this);
  }

  @Test
  public void testVerticalLayout() throws Exception {
    setUIClass(CompatLayoutComponents.VerticalLayoutUI.class);
    openURL();
    testConsumer.apply(this);
  }

  @Test
  public void testCSSLayout() throws Exception {
    setUIClass(CompatLayoutComponents.CSSLayoutUI.class);
    openURL();
    testConsumer.apply(this);
  }

  //Grid layout does not work correctly
  @Ignore
  @Test
  public void testGridLayout() throws Exception {
    setUIClass(CompatLayoutComponents.GridLayoutUI.class);
    openURL();
    testConsumer.apply(this);
  }
}
