package com.vaadin.addon.board.testUI;

import static com.vaadin.addon.board.testUI.UIFunctions.testLayout;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CheckBoxGroup;
import com.vaadin.ui.ColorPicker;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Flash;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Video;

/**
 * http://localhost:8080/Dash24BasicComponents$ButtonUI
 */
public class CompatBasicComponents {

  public static class ButtonUI extends CompatBasicUI {
    @Override
    protected Class<? extends Component> nextClass() {
      return Button.class;
    }
  }

  public static class ComboBoxUI extends CompatBasicUI {
    @Override
    protected Class<? extends Component> nextClass() {
      return ComboBox.class;
    }
  }

  public static class CheckBoxUI extends CompatBasicUI {
    @Override
    protected Class<? extends Component> nextClass() {
      return CheckBox.class;
    }
  }

  public static class ColorPickerUI extends CompatBasicUI {
    @Override
    protected Class<? extends Component> nextClass() {
      return ColorPicker.class;
    }
  }

  public static class CheckBoxGroupUI extends AbstractTestUI {
    @Override
    protected void init(VaadinRequest vaadinRequest) {
      CheckBoxGroup<String> c1 = new CheckBoxGroup<>("A");
      c1.setItems("1", "2", "3");
      CheckBoxGroup<String> c2 = new CheckBoxGroup<>("B");
      c2.setItems("1", "2", "3");
      CheckBoxGroup<String> c3 = new CheckBoxGroup<>("C");
      c3.setItems("1", "2", "3");
      setContent(
          testLayout()
              .apply(
                  Stream.of(c1, c2, c3)));
    }
  }

  public static class RadioButtonGroupUI extends AbstractTestUI {
    @Override
    protected void init(VaadinRequest vaadinRequest) {
      RadioButtonGroup<String> c1 = new RadioButtonGroup<>("A");
      c1.setItems("1", "2", "3");
      RadioButtonGroup<String> c2 = new RadioButtonGroup<>("B");
      c2.setItems("1", "2", "3");
      RadioButtonGroup<String> c3 = new RadioButtonGroup<>("C");
      c3.setItems("1", "2", "3");
      setContent(
          testLayout()
              .apply(
                  Stream.of(c1, c2, c3)));
    }
  }

  public static class DateFieldUI extends CompatBasicUI {
    @Override
    protected Class<? extends Component> nextClass() {
      return DateField.class;
    }
  }

  //DASH-107
  public static class FlashUI extends CompatBasicUI {
    @Override
    protected Class<? extends Component> nextClass() {
      return Flash.class;
    }
  }


  //DASH-106
  public static class GridUI extends AbstractTestUI {

    private static class Person {
      private String name;
      private int birthYear;

      public Person(String name, int birthYear) {
        this.name = name;
        this.birthYear = birthYear;
      }

      public String getName() {
        return name;
      }

      public int getBirthYear() {
        return birthYear;
      }
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
      setContent(
          testLayout()
              .apply(
                  Stream.of(nextElement(), nextElement(), nextElement())));
//            setContent(nextGrid());
    }

    private Grid<Person> nextElement() {
      List<Person> people = Arrays.asList(
          new Person("Nicolaus Copernicus", 1543),
          new Person("Galileo Galilei", 1564),
          new Person("Johannes Kepler", 1571));
      Grid<Person> grid = new Grid<>();
      grid.setItems(people);
      grid.addColumn(Person::getName).setCaption("Name");
      grid.addColumn(Person::getBirthYear).setCaption("Year of birth");
      return grid;
    }
  }

  public static class LabelUI extends CompatBasicUI {
    @Override
    protected Class<? extends Component> nextClass() {
      return Label.class;
    }
  }

  public static class LinkUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
      setContent(
          testLayout()
              .apply(
                  Stream.of(nextElement(), nextElement(), nextElement())));
    }

    private Link nextElement() {
      return new Link("http://vaadin.com/",
          new ExternalResource("http://vaadin.com/"));
    }
  }


  public static class PanelUI extends CompatBasicUI {
    @Override
    protected Class<? extends Component> nextClass() {
      return Panel.class;
    }
  }

  public static class PasswordFieldUI extends CompatBasicUI {
    @Override
    protected Class<? extends Component> nextClass() {
      return PasswordField.class;
    }
  }

  public static class ProgressBarUI extends AbstractTestUI {
    @Override
    protected void init(VaadinRequest vaadinRequest) {
      setContent(
          testLayout()
              .apply(
                  Stream.of(nextElement(), nextElement(), nextElement())));
    }

    private ProgressBar nextElement() {
      return new ProgressBar(0.8f);
    }
  }

  public static class SliderUI extends CompatBasicUI {
    @Override
    protected Class<? extends Component> nextClass() {
      return Slider.class;
    }
  }

  //Todo resizing problem - video https://youtu.be/cLtxYeYXzVU
  //DASH-112
  public static class TabSheetUI extends AbstractTestUI {
    @Override
    protected void init(VaadinRequest vaadinRequest) {
      setContent(
          testLayout()
              .apply(
                  Stream.of(nextElement(), nextElement(), nextElement())));
    }

    private TabSheet nextElement() {
      TabSheet tabSheet = new TabSheet();
      tabSheet.addTab(nextTabElement()).setCaption("Tab 001");
      tabSheet.addTab(nextTabElement()).setCaption("Tab 002");
      return tabSheet;
    }

    private VerticalLayout nextTabElement() {
      VerticalLayout tab = new VerticalLayout();
      tab.setSizeFull();
      tab.addComponents(
          new Button("TabButton 1"),
          new Button("TabButton 2")
      );
      return tab;
    }
  }

  //Todo resizing problem - video https://youtu.be/CY3_h9cNCPQ
  //DASH-109
  public static class TwinColSelectUI extends CompatBasicUI {
    @Override
    protected Class<? extends Component> nextClass() {
      return TwinColSelect.class;
    }
  }

  //Todo resizing problem - video https://youtu.be/3IVZDP0PEa0
  //DASH-108
  public static class VideoUI extends CompatBasicUI {
    @Override
    protected Class<? extends Component> nextClass() {
      return Video.class;
    }
  }


  //Todo resizing problem - video https://youtu.be/zuW-5VkqdOk
  //DASH-115
  public static class RichTextAreaUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
      setContent(
          testLayout()
              .apply(
                  Stream.of(nextElement(), nextElement(), nextElement())));
    }

    private RichTextArea nextElement() {
      final RichTextArea rtarea = new RichTextArea();
      rtarea.setCaption("My Rich Text Area");
      rtarea.setValue("<h1>Hello</h1>\n" +
          "<p>This rich text area contains some text.</p>");
      return rtarea;
    }
  }


}
