package com.vaadin.flow.component.board.test;

import static com.vaadin.server.Sizeable.Unit.PIXELS;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public abstract class AbstractTestCompUI extends AbstractTestUI {
    public static String FULL_SIZE_BTN = "setSizeFull";
    public static String MIDDLE_SIZE_BTN = "700px";
    public static String SMALL_SIZE_BTN = "400px";
    public static String SWITCH = "switch";
    public static String ID_PREFIX = "test-component-";

    protected abstract Component[] createTestedComponents();

    @Override
    protected void init(VaadinRequest request) {
        final Board board = new Board();
        Component[] components = createTestedComponents();
        int i = 0;
        for (final Component component : components) {
            component.setId(ID_PREFIX + i++);
        }

        final Row row = board.addRow(components);
        final AbstractOrderedLayout baseLayout = new VerticalLayout();
        final Button btnFullSize = new Button(FULL_SIZE_BTN,
            (Button.ClickListener) clickEvent
                -> UI.getCurrent().setSizeFull());
        btnFullSize.setSizeFull();

        final Button btnMiddleSize = new Button(MIDDLE_SIZE_BTN,
            (Button.ClickListener) clickEvent
                -> UI.getCurrent().setWidth(700, PIXELS));
        btnMiddleSize.setSizeFull();
        btnMiddleSize.setId(MIDDLE_SIZE_BTN);
        final Button btnSmallSize = new Button(SMALL_SIZE_BTN,
            (Button.ClickListener) clickEvent
                -> UI.getCurrent().setWidth(400, PIXELS));
        btnSmallSize.setSizeFull();
        btnSmallSize.setId(SMALL_SIZE_BTN);
        final Button buttonSwitch = new Button(SWITCH,
            (Button.ClickListener) clickEvent -> row.setComponentSpan(components[1],
                (row.getComponentSpan(components[1]) > 1) ? 1 : 2));
        buttonSwitch.setSizeFull();
        baseLayout.addComponents(board, btnFullSize,btnMiddleSize, btnSmallSize, buttonSwitch);

        setContent(baseLayout);
    }
}
