package com.vaadin.addon.board.testbenchtests;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.board.testUI.AbstractTestCompUI;
import com.vaadin.addon.board.testUI.CompatBasicComponents;
import com.vaadin.addon.frp.Pair;
import com.vaadin.testbench.elements.AbstractComponentElement;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.CheckBoxElement;
import com.vaadin.testbench.elements.CheckBoxGroupElement;
import com.vaadin.testbench.elements.ColorPickerElement;
import com.vaadin.testbench.elements.ComboBoxElement;
import com.vaadin.testbench.elements.DateFieldElement;
import com.vaadin.testbench.elements.GridElement;
import com.vaadin.testbench.elements.LabelElement;
import com.vaadin.testbench.elements.LinkElement;
import com.vaadin.testbench.elements.PanelElement;
import com.vaadin.testbench.elements.PasswordFieldElement;
import com.vaadin.testbench.elements.ProgressBarElement;
import com.vaadin.testbench.elements.RadioButtonGroupElement;
import com.vaadin.testbench.elements.SliderElement;
import com.vaadin.testbench.elements.TwinColSelectElement;
import com.vaadin.testbench.elements.VideoElement;

/**
 *
 */
public class CompatBasicComponentsIT {

  static Stream<
      Pair<
          Class<? extends AbstractComponentElement>,
          Class<? extends AbstractTestCompUI>>> testCombos() {
    return Stream.of(
        new Pair<>(ButtonElement.class, CompatBasicComponents.ButtonUI.class),
        new Pair<>(CheckBoxGroupElement.class, CompatBasicComponents.CheckBoxGroupUI.class),
        new Pair<>(CheckBoxElement.class, CompatBasicComponents.CheckBoxUI.class),
        new Pair<>(ColorPickerElement.class, CompatBasicComponents.ColorPickerUI.class),
        new Pair<>(ComboBoxElement.class, CompatBasicComponents.ComboBoxUI.class),
        new Pair<>(DateFieldElement.class, CompatBasicComponents.DateFieldUI.class),
    //  Flash does not work
    //    new Pair<>(FlashElement.class, CompatBasicComponents.FlashUI.class),
        new Pair<>(GridElement.class, CompatBasicComponents.GridUI.class),
        new Pair<>(LabelElement.class, CompatBasicComponents.LabelUI.class),
        new Pair<>(LinkElement.class, CompatBasicComponents.LinkUI.class),
        new Pair<>(PanelElement.class, CompatBasicComponents.PanelUI.class),
        new Pair<>(PasswordFieldElement.class, CompatBasicComponents.PasswordFieldUI.class),
        new Pair<>(ProgressBarElement.class, CompatBasicComponents.ProgressBarUI.class),
        new Pair<>(RadioButtonGroupElement.class, CompatBasicComponents.RadioButtonGroupUI.class),
        new Pair<>(SliderElement.class, CompatBasicComponents.SliderUI.class),
//        new Pair<>(TreeElement.class, CompatBasicComponents.TreeUI.class), // Tree is a Composite
        new Pair<>(TwinColSelectElement.class, CompatBasicComponents.TwinColSelectUI.class),
        new Pair<>(VideoElement.class, CompatBasicComponents.VideoUI.class)
    );
  }


  @RunWith(value = Parameterized.class)
  public static class GenericTest extends AbstractParallelTest {

    @Parameterized.Parameter
    public Pair<
        Class<? extends AbstractComponentElement>,
        Class<? extends AbstractTestCompUI>> nextTestCombo;

    @Parameterized.Parameters(name = "{index}: nextTestCombo - {0}")
    public static Object[] data() {
      return testCombos().toArray();
    }

    public Supplier<WebElement> middleElementSupplier() {
      return () -> $(middleElementClass())
          .id(AbstractTestCompUI.ID_PREFIX + 1);
    }

    @Test
    public void testGenericWidth() throws Exception {
      WebElement controlElement = $(ButtonElement.class).caption(AbstractTestCompUI.SWITCH).first();
      TestFunctions.assertDimension(controlElement,middleElementSupplier().get(), (elem)->{
        return elem.getSize().width;
      });
    }

    @Override
    protected Class<? extends AbstractTestCompUI> getUIClass() {
      return nextTestCombo.getT2();
    }

    public Class<? extends AbstractComponentElement> middleElementClass() {
      return nextTestCombo.getT1();
    }



  }

}
