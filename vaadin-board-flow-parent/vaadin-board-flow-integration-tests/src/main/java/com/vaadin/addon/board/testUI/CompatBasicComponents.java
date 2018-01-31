package com.vaadin.addon.board.testUI;

import java.util.Arrays;
import java.util.List;

import com.vaadin.server.ExternalResource;
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
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.Video;

/**
 * http://localhost:8080/Dash24BasicComponents$ButtonUI
 */
public class CompatBasicComponents {

  public static class ButtonUI extends CompatBasicUI {
    @Override
    protected Component createTestComponent() {
      return new Button();
    }
  }

  public static class ComboBoxUI extends CompatBasicUI {
    @Override
    protected Component createTestComponent() {
      return new ComboBox<>();
    }
  }

  public static class CheckBoxUI extends CompatBasicUI {
    @Override
    protected Component createTestComponent() {
      return new CheckBox();
    }
  }

  public static class ColorPickerUI extends CompatBasicUI {
    @Override
    protected Component createTestComponent() {
      return new ColorPicker();
    }
  }

  public static class CheckBoxGroupUI extends AbstractTestCompUI {
    @Override
    protected Component[] createTestedComponents() {
      CheckBoxGroup<String> c1 = new CheckBoxGroup<>("A");
      c1.setItems("1", "2", "3");
      CheckBoxGroup<String> c2 = new CheckBoxGroup<>("B");
      c2.setItems("1", "2", "3");
      CheckBoxGroup<String> c3 = new CheckBoxGroup<>("C");
      c3.setItems("1", "2", "3");
      Component[] comps = { c1, c2, c3 };
      return comps;
    }
  }

  public static class RadioButtonGroupUI extends AbstractTestCompUI {
    @Override
    protected Component[] createTestedComponents() {

      RadioButtonGroup<String> c1 = new RadioButtonGroup<>("A");
      c1.setItems("1", "2", "3");
      RadioButtonGroup<String> c2 = new RadioButtonGroup<>("B");
      c2.setItems("1", "2", "3");
      RadioButtonGroup<String> c3 = new RadioButtonGroup<>("C");
      c3.setItems("1", "2", "3");
      Component[] comps = { c1, c2, c3 };
      return comps;
    }
  }

  public static class DateFieldUI extends CompatBasicUI {
    @Override
    protected Component createTestComponent() {
      return new DateField();
    }
  }

  //DASH-107
  public static class FlashUI extends CompatBasicUI {
    @Override
    protected Component createTestComponent() {
      return new Flash();
    }
  }


  //DASH-106
  public static class GridUI extends CompatBasicUI {

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
    protected Component createTestComponent() {
      List<Person> people = Arrays.asList(
          new Person("Nicolaus Copernicus", 1543),
          new Person("Galileo Galilei", 1564),
          new Person("Johannes Kepler", 1571));
      Grid<Person> grid = new Grid<>();
      grid.setItems(people);
      grid.addColumn(Person::getName).setCaption("Name");
      grid.addColumn(Person::getBirthYear).setCaption("Year of birth");
      grid.setWidth("100%");
      return grid;
    }

  }

  public static class LabelUI extends CompatBasicUI {
    protected Component createTestComponent() {
      return new Label();
    }
  }

  public static class LinkUI extends CompatBasicUI {

    protected Component createTestComponent() {
      return new Link("http://vaadin.com/",
          new ExternalResource("http://vaadin.com/"));
    }
  }


  public static class PanelUI extends CompatBasicUI {
    @Override
    protected Component createTestComponent() {
      return new Panel();
    }
  }

  public static class PasswordFieldUI extends CompatBasicUI {
    @Override
    protected Component createTestComponent() {
      return new PasswordField();
    }
  }

  public static class ProgressBarUI extends CompatBasicUI {
    @Override
    protected Component createTestComponent() {
      return new ProgressBar(0.8f);
    }

  }

  public static class SliderUI extends CompatBasicUI {
    @Override
    protected Component createTestComponent() {
      return new Slider();
    }
  }



  //Todo resizing problem - video https://youtu.be/CY3_h9cNCPQ
  //DASH-109
  public static class TwinColSelectUI extends CompatBasicUI {
    @Override
    protected Component createTestComponent() {
      return new TwinColSelect<>();
    }
  }

  //Todo resizing problem - video https://youtu.be/3IVZDP0PEa0
  //DASH-108
  public static class VideoUI extends CompatBasicUI {
    @Override
    protected Component createTestComponent() {
      return new Video();
    }
  }


  //Todo resizing problem - video https://youtu.be/zuW-5VkqdOk
  //DASH-115
  public static class RichTextAreaUI extends CompatBasicUI {

    @Override
    protected Component createTestComponent() {
      final RichTextArea rtarea = new RichTextArea();
      rtarea.setCaption("My Rich Text Area");
      rtarea.setValue("<h1>Hello</h1>\n" +
          "<p>This rich text area contains some text.</p>");
      return rtarea;
    }
  }
}
