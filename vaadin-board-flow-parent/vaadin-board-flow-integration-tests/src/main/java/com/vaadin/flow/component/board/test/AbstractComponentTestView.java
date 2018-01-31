package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractComponentTestView extends Div {
    public static String FULL_SIZE_BTN = "setSizeFull";
    public static String MIDDLE_SIZE_BTN = "700px";
    public static String SMALL_SIZE_BTN = "400px";
    public static String SWITCH = "switch";
    public static String ID_PREFIX = "test-component-";

    protected abstract Component[] createTestedComponents();

    public AbstractComponentTestView() {
        final Board board = new Board();
        Component[] components = createTestedComponents();
        int i = 0;
        for (final Component component : components) {
            component.setId(ID_PREFIX + i++);
        }

        final Row row = board.addRow(components);
        final VerticalLayout baseLayout = new VerticalLayout();
        final Button btnFullSize = new Button(FULL_SIZE_BTN,
                clickEvent -> setSizeFull());
        btnFullSize.setSizeFull();

        final Button btnMiddleSize = new Button(MIDDLE_SIZE_BTN,
                clickEvent -> setWidth("700px"));
        btnMiddleSize.setSizeFull();
        btnMiddleSize.setId(MIDDLE_SIZE_BTN);
        final Button btnSmallSize = new Button(SMALL_SIZE_BTN,
                clickEvent -> setWidth("400px"));
        btnSmallSize.setSizeFull();
        btnSmallSize.setId(SMALL_SIZE_BTN);
        final Button buttonSwitch = new Button(SWITCH,
                clickEvent -> row.setComponentSpan(components[1],
                        (row.getComponentSpan(components[1]) > 1) ? 1 : 2));
        buttonSwitch.setId(SWITCH);
        buttonSwitch.setSizeFull();
        baseLayout.add(board, btnFullSize, btnMiddleSize, btnSmallSize,
                buttonSwitch);

        add(baseLayout);
    }

}
