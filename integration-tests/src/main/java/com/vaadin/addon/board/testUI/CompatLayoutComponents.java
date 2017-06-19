package com.vaadin.addon.board.testUI;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 *
 */
public class CompatLayoutComponents {

  private static Button createButton(String caption, String id) {
    Button b = new Button(caption);
    b.setSizeFull();
    b.setId(id);
    return b;
  }

  /**
   *
   */
  public static class FormLayoutUI extends AbstractTestCompUI {
    @Override
    protected Component[] createTestedComponents() {
      FormLayout l1 = new FormLayout(createButton("A1", ID_PREFIX + 1));
      FormLayout l2 = new FormLayout(createButton("A2", ID_PREFIX + 2));
      FormLayout l3 = new FormLayout(createButton("A3", ID_PREFIX + 3));

      Component[] comps = { l1, l2, l3 };
      return comps;
    }
  }

  public static class HorizontalLayoutUI extends AbstractTestCompUI {
    @Override
    protected Component[] createTestedComponents() {
      HorizontalLayout l1 = new HorizontalLayout(createButton("A1", ID_PREFIX + 1));
      HorizontalLayout l2 = new HorizontalLayout(createButton("A2", ID_PREFIX + 2));
      HorizontalLayout l3 = new HorizontalLayout(createButton("A3", ID_PREFIX + 3));

      Component[] comps = {l1,l2,l3};
      for(Component comp:comps) {
        comp.setSizeFull();
      }
      return comps;
    }
  }

  /**
   *
   */
  public static class VerticalLayoutUI extends AbstractTestCompUI {
    @Override
    protected Component[] createTestedComponents() {
      VerticalLayout l1 = new VerticalLayout(createButton("A1", ID_PREFIX + 1));
      VerticalLayout l2 = new VerticalLayout(createButton("A2", ID_PREFIX + 2));
      VerticalLayout l3 = new VerticalLayout(createButton("A3", ID_PREFIX + 3));

      Component[] comps = { l1, l2, l3 };
      return comps;
    }
  }

  public static class GridLayoutUI extends AbstractTestCompUI {
      @Override
      protected Component[] createTestedComponents() {
        GridLayout l1 = (new GridLayout(1, 1, createButton("A1", ID_PREFIX + 1)));
        GridLayout l2 = (new GridLayout(1, 1, createButton("A2", ID_PREFIX + 2)));
        GridLayout l3 = (new GridLayout(1, 1, createButton("A3", ID_PREFIX + 3)));

        Component[] comps = { l1, l2, l3 };
        return comps;
      }
  }

  public static class CSSLayoutUI extends AbstractTestCompUI {
    @Override
    protected Component[] createTestedComponents() {
      CssLayout l1 = new CssLayout(createButton("A1", ID_PREFIX + 1));
      CssLayout l2 = new CssLayout(createButton("A2", ID_PREFIX + 2));
      CssLayout l3 = new CssLayout(createButton("A3", ID_PREFIX + 3));

      Component[] comps = { l1, l2, l3 };
      return comps;
    }
  }
}
