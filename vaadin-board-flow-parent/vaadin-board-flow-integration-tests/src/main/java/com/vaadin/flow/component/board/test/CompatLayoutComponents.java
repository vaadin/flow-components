package com.vaadin.flow.component.board.test;

import java.awt.GridLayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

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

    public static class HorizontalLayoutUI extends AbstractComponentTestView {
        @Override
        protected Component[] createTestedComponents() {
            HorizontalLayout l1 = new HorizontalLayout(
                    createButton("A1", ID_PREFIX + 1));
            HorizontalLayout l2 = new HorizontalLayout(
                    createButton("A2", ID_PREFIX + 2));
            HorizontalLayout l3 = new HorizontalLayout(
                    createButton("A3", ID_PREFIX + 3));

            Component[] comps = { l1, l2, l3 };
            for (Component comp : comps) {
                CompatLayoutComponents.setSizeFull(comp);
            }
            return comps;
        }

    }

    private static void setSizeFull(Component comp) {
        comp.getElement().getStyle().set("width", "100%");
        comp.getElement().getStyle().set("height", "100%");
    }

    /**
     *
     */
    public static class VerticalLayoutView extends AbstractComponentTestView {
        @Override
        protected Component[] createTestedComponents() {
            VerticalLayout l1 = new VerticalLayout(
                    createButton("A1", ID_PREFIX + 1));
            VerticalLayout l2 = new VerticalLayout(
                    createButton("A2", ID_PREFIX + 2));
            VerticalLayout l3 = new VerticalLayout(
                    createButton("A3", ID_PREFIX + 3));

            VerticalLayout[] comps = { l1, l2, l3 };
            for (VerticalLayout comp : comps) {
                comp.setMargin(false);
                comp.setSizeFull();

            }
            return comps;
        }
    }

    public static class DivView extends AbstractComponentTestView {
        @Override
        protected Component[] createTestedComponents() {
            Div l1 = new Div(createButton("A1", ID_PREFIX + 1));
            Div l2 = new Div(createButton("A2", ID_PREFIX + 2));
            Div l3 = new Div(createButton("A3", ID_PREFIX + 3));

            Div[] comps = { l1, l2, l3 };
            for (Div comp : comps) {
                CompatLayoutComponents.setSizeFull(comp);
            }
            return comps;
        }
    }
}
