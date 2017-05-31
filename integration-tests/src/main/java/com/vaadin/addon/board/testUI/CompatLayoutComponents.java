package com.vaadin.addon.board.testUI;

import static com.vaadin.addon.board.testUI.UIFunctions.ID_PREFIX;
import static com.vaadin.addon.board.testUI.UIFunctions.testLayout;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 *
 */
public class CompatLayoutComponents {



  private static BiFunction<String, String, Button> btn() {
    return (caption, id) -> {
      Button b = new Button(caption);
      b.setSizeFull();
      b.setId(id);
      return b;
    };
  }


  /**
   *
   */
  public static class FormLayoutUI extends AbstractTestUI {
    @Override
    protected void init(VaadinRequest vaadinRequest) {
      setContent(
          testLayout()
              .apply(Stream.of(
                  new FormLayout(btn().apply("A1", ID_PREFIX + 1)),
                  new FormLayout(btn().apply("A2",ID_PREFIX + 2)),
                  new FormLayout(btn().apply("A3", ID_PREFIX + 3)))
              ));
    }
  }

  /**
   *
   */
  public static class HorizontalLayoutUI extends AbstractTestUI {
    @Override
    protected void init(VaadinRequest vaadinRequest) {
      setContent(
          testLayout()
              .apply(Stream.of(
                  new HorizontalLayout(btn().apply("A1", ID_PREFIX + 1)),
                  new HorizontalLayout(btn().apply("A2", ID_PREFIX + 2)),
                  new HorizontalLayout(btn().apply("A3", ID_PREFIX + 3)))
              ));
    }
  }

  /**
   *
   */
  public static class VerticalLayoutUI extends AbstractTestUI {
    @Override
    protected void init(VaadinRequest vaadinRequest) {
      setContent(
          testLayout()
              .apply(Stream.of(
                  new VerticalLayout(btn().apply("A1", ID_PREFIX + 1)),
                  new VerticalLayout(btn().apply("A2", ID_PREFIX + 2)),
                  new VerticalLayout(btn().apply("A3", ID_PREFIX + 3)))
              ));
    }
  }

  //DASH-114
  public static class GridLayoutUI extends AbstractTestUI {
    @Override
    protected void init(VaadinRequest vaadinRequest) {
      setContent(
          testLayout()
              .apply(Stream.of(
                  new GridLayout(1, 1, btn().apply("A1", ID_PREFIX + 1)),
                  new GridLayout(1, 1, btn().apply("A2", ID_PREFIX + 2)),
                  new GridLayout(1, 1, btn().apply("A3", ID_PREFIX + 3)))
              ));
    }
  }

  public static class CSSLayoutUI extends AbstractTestUI {
    @Override
    protected void init(VaadinRequest vaadinRequest) {
      setContent(
          testLayout()
              .apply(Stream.of(
                  new CssLayout(btn().apply("A1", ID_PREFIX + 1)),
                  new CssLayout(btn().apply("A2", ID_PREFIX + 2)),
                  new CssLayout(btn().apply("A3", ID_PREFIX + 3)))
              ));
    }
  }
}
