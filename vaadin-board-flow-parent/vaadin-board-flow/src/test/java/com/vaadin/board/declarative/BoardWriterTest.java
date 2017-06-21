package com.vaadin.board.declarative;

import java.io.ByteArrayOutputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.board.Board;
import com.vaadin.board.Row;
import com.vaadin.ui.Label;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.declarative.DesignContext;

public class BoardWriterTest {

    private static final String BOARD_WITH_ONE_ROW_CONFIG=
        "<vaadin-board size-full> \n" +
        " <vaadin-board-row> \n" +
        "  <vaadin-label plain-text>\n" +
        "    1-1 \n" +
        "  </vaadin-label> \n" +
        " </vaadin-board-row> \n" +
        "</vaadin-board>";

    private static final String BOARD_WITH_BOARD_COLS_CONFIG=
    "<vaadin-board size-full> \n" +
    " <vaadin-board-row> \n" +
    "  <vaadin-label size-full plain-text :cols=\"2\">\n" +
    "    1-1 \n" +
    "  </vaadin-label> \n" +
    " </vaadin-board-row> \n" +
    "</vaadin-board>";

    private static final  String BOARD_WITH_INNER_ROW_CONFIG =
        "<vaadin-board size-full> \n" +
        " <vaadin-board-row> \n" +
        "  <vaadin-board-row> \n" +
        "   <vaadin-label plain-text>\n" +
        "     1-1 \n" +
        "   </vaadin-label> \n" +
        "  </vaadin-board-row> \n" +
        " </vaadin-board-row> \n" +
        "</vaadin-board>";

    private static final  String BOARD_WITH_INNER_ROW_WITH_BOARDCOLS_CONFIG =
        "<vaadin-board size-full> \n" +
            " <vaadin-board-row> \n" +
            "  <vaadin-board-row :cols=\"2\"> \n" +
            "   <vaadin-label plain-text :cols=\"2\">\n" +
            "     1-1 \n" +
            "   </vaadin-label> \n" +
            "  </vaadin-board-row> \n" +
            " </vaadin-board-row> \n" +
            "</vaadin-board>";

    @Test
    public void writeConfiguration_boardWithOneRow_written() {
        Board board = new Board();
        board.setSizeFull();
        Label label = new Label("1-1");
        board.addRow(label);
        String declarative = write(board);
        Assert.assertEquals("Board has one row", BOARD_WITH_ONE_ROW_CONFIG, declarative);
    }

    @Test
    public void writeConfiguration_rowHasAttributes_parsed() {
        Board board = new Board();
        board.setSizeFull();
        Label label = new Label("1-1");
        label.setSizeFull();
        Row row = board.addRow(label);
        row.setComponentSpan(label, 2);
        String declarative = write(board);
        Assert.assertEquals("Board has one row", BOARD_WITH_BOARD_COLS_CONFIG, declarative);
    }

    @Test
    public void writeConfiguration_rowWithInnerRows_parsed() {
        Board board = new Board();
        board.setSizeFull();
        Row innerRow = new Row();
        board.addRow(innerRow);
        Label label = new Label("1-1");
        innerRow.addComponent(label);
        String declarative = write(board);
        Assert.assertEquals("Board has one row", BOARD_WITH_INNER_ROW_CONFIG, declarative);
    }

    @Test
    public void writeConfiguration_rowWithInnerRowsWithBoardCols_parsed() {
        Board board = new Board();
        board.setSizeFull();
        Row innerRow = new Row();
        Row outterRow = board.addRow(innerRow);
        outterRow.setComponentSpan(innerRow, 2);
        Label label = new Label("1-1");
        innerRow.addComponent(label, 2);
        String declarative = write(board);
        Assert.assertEquals("Board has one row", BOARD_WITH_INNER_ROW_WITH_BOARDCOLS_CONFIG, declarative);
    }


    protected String write(Board object) {
        DesignContext dc = new DesignContext();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            dc.setRootComponent(object);
            Design.write(dc, outputStream);
            String doc = outputStream.toString("UTF-8");

            doc = doc.replace("com_vaadin_board-board","vaadin-board");
            doc = doc.replace("com_vaadin_board-row","vaadin-board-row");
            //Extract board component, don't take into account headers
            //And all the other boiler plate html
            Element board = Jsoup.parseBodyFragment(doc).body().child(1);
            return board.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
