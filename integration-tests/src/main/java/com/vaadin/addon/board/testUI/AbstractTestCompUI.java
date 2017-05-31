package com.vaadin.addon.board.testUI;

import static com.vaadin.server.Sizeable.Unit.PIXELS;

import com.vaadin.board.Board;
import com.vaadin.board.Row;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public abstract class AbstractTestCompUI extends AbstractTestUI {
    String PX1024 = "setSizeFull";
    String PX0512 = (3 * 128) + "px";
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
        final Button button1024 = new Button(PX1024,
            (Button.ClickListener) clickEvent
                -> UI.getCurrent().setSizeFull());
        button1024.setSizeFull();

        final Button button0512 = new Button(PX0512,
            (Button.ClickListener) clickEvent
                -> UI.getCurrent().setWidth((2 * 128), PIXELS));
        button0512.setSizeFull();

        final Button buttonSwitch = new Button(SWITCH,
            (Button.ClickListener) clickEvent -> row.setCols(components[1],
                (row.getCols(components[1]) > 1) ? 1 : 2));
        buttonSwitch.setSizeFull();
        baseLayout.addComponents(board, button1024, button0512, buttonSwitch);

        setContent(baseLayout);
    }
}
