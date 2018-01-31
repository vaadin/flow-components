package com.vaadin.addon.board.testUI;

import com.vaadin.board.Board;
import com.vaadin.board.Row;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;

public class RowRemoveUI extends AbstractTestUI {

    public final static String RMV_BUTTON_ID="rmvbutton";
    @Override
    protected void init(VaadinRequest request) {

        VerticalLayout layout = new VerticalLayout();
        Board board = new Board();
        board.setWidth(700, Unit.PIXELS);
        Button btn1 = new Button("Button 1");
        Button btn2 = new Button("Button 2");
        Button btn3 = new Button("Button 3");
        Button btn4 = new Button("Button 4");

        Row row1 =board.addRow(btn1, btn2);
        board.addRow(btn3, btn4);

        Button rmvButton= new Button("Remove row",e->{
            board.removeRow(row1);
        });
        rmvButton.setId(RMV_BUTTON_ID);
        layout.addComponents(board, rmvButton);
        setContent(layout);
    }

}
