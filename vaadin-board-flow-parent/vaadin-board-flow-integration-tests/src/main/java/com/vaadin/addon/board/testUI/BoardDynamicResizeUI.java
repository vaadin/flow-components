package com.vaadin.addon.board.testUI;

import com.vaadin.board.Board;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class BoardDynamicResizeUI extends AbstractTestUI {

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout();
        Board board = new Board();
        board.setSizeFull();

        Label lbl1 = new Label("Label 1");
        Label lbl2 = new Label("Label 2");
        Label lbl3 = new Label("Label 3");
        board.addRow(lbl1, lbl2, lbl3);

        Button button = new Button("resize");
        button.addClickListener(e->{
            board.setWidth("300px");
        });
        layout.addComponents(board, button);
        setContent(layout);
    }

}
