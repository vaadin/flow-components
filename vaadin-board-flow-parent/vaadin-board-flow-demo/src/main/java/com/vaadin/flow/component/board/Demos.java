package com.vaadin.flow.component.board;

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;

@Route("vaadin-board")
@StyleSheet("context://vaadin-board-flow-demo/vaadin-board-demo.css")
public class Demos extends DemoView {

    private static final String CATEGORY_STYLING = "Styling";
    private static String[] cellColors = new String[] { "#003E53", "#00506B",
            "#006C90", "#0090C0", "#00B4F0", "#33C3F3", "#66D2F6", "#99E1F9",
            "#CCF0FC", "#E5F7FD" };

    @Override
    protected void initView() {
        automaticResponsiveLayout();
        multipleRowsAndUpToFourColumnsSupported();
        spanningMultipleSlots();

        nestedRowsWrapAccordingToParentSize();

        stylingForDifferentScreenSizes();
        redefiningTheBreakpoints();
    }

    private void automaticResponsiveLayout() {
        // begin-source-example
        // source-example-heading: Automatic responsive layout
        Board board = new Board();
        Div child1 = createComponent("This could be chart 1");
        Div child2 = createComponent("This could be chart 2");
        Div child3 = createComponent("This could be chart 3");
        Div child4 = createComponent("This could be chart 4");

        board.addRow(child1, child2, child3, child4);
        add(board);
        // end-source-example

        setBreakpoints(board);
        setStyles(child1, child2, child3, child4);
        addCard("Automatic responsive layout", board);
    }

    // begin-source-example
    // source-example-heading: Automatic responsive layout
    private Div createComponent(String text) {
        Div div = new Div();
        div.setText(text);
        return div;
    }
    // end-source-example

    private void multipleRowsAndUpToFourColumnsSupported() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Multiple rows and up to four columns supported
        Board board = new Board();
        Div child1 = createComponent("This could be chart 1");
        Div child2 = createComponent("This could be chart 2");
        Div child3 = createComponent("This could be chart 3");
        Div child4 = createComponent("This could be chart 4");
        Div child5 = createComponent("This could be chart 5");
        Div child6 = createComponent("This could be chart 6");
        Div child7 = createComponent("This could be chart 7");
        Div child8 = createComponent("This could be chart 8");
        Div child9 = createComponent("This could be chart 9");
        Div child10 = createComponent("This could be chart 10");

        board.addRow(child1, child2, child3, child4);
        board.addRow(child5, child6, child7);
        board.addRow(child8, child9);
        board.addRow(child10);
        add(board);
        // end-source-example
        // @formatter:on

        setBreakpoints(board);
        setStyles(child1, child2, child3, child4, child5, child6, child7,
                child8, child9, child10);
        addCard("Multiple rows and up to four columns supported", board);
    }

    private void spanningMultipleSlots() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Spanning multiple slots
        Board board = new Board();
        Div child1 = createComponent("This could be big chart 1");
        Div child2 = createComponent("This could be chart 2");
        Div child3 = createComponent("This could be chart 3");
        Div child4 = createComponent("This could be big chart 4");

        Row row1 = board.addRow(child1, child2);
        row1.setComponentSpan(child1, 2);
        Row row2 = board.addRow(child3, child4);
        row2.setComponentSpan(child4, 2);
        add(board);
        // end-source-example
        // @formatter:on

        setBreakpoints(board);
        setStyles(child1, child2, child3, child4);
        addCard("Spanning multiple slots", board);
    }

    private void nestedRowsWrapAccordingToParentSize() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Nested rows wrap according to parent size
        Board board = new Board();
        Div child1 = createComponent("This could be chart 1");
        Div child2 = createComponent("This could be chart 2");
        Div child3 = createComponent("This could be chart 3");

        Div child4a = createComponent("This could be chart 4A");
        Div child4b = createComponent("This could be chart 4B");

        Row nested = new Row(child4a, child4b);
        board.addRow(child1, child2,child3,nested);
        add(board);
        // end-source-example
        // @formatter:on

        setBreakpoints(board);
        setStyles(child1, child2, child3, child4a, child4b);
        addCard("Nested Rows", "Nested rows wrap according to parent size",
                board);
    }

    private void stylingForDifferentScreenSizes() {

        // @formatter:off
        /*
        // begin-source-example
        // source-example-heading: Styling for different screen sizes
# Included stylesheet
.styled vaadin-board-row.large > div {
  font-size: 14px;
  height: 40px;
}

.styled vaadin-board-row.medium > div {
  font-size: 25px;
  height: 150px;
}

.styled vaadin-board-row.small > div {
  font-size: 40px;
  height: 300px;
}
        // end-source-example
        */
        // @formatter:on

        // begin-source-example
        // source-example-heading: Styling for different screen sizes
        Board board = new Board();
        board.addClassName("styled");
        Div child1 = createComponent("This could be chart 1");
        Div child2 = createComponent("This could be chart 2");
        Div child3 = createComponent("This could be chart 3");
        Div child4 = createComponent("This could be chart 4");

        board.addRow(child1, child2, child3, child4);
        add(board);
        // end-source-example

        setBreakpoints(board);
        setStyles(child1, child2, child3, child4);
        addCard(CATEGORY_STYLING, "Styling for different screen sizes", board);
    }

    private void redefiningTheBreakpoints() {
        Div layout = new Div();
        // begin-source-example
        // source-example-heading: Redefining the breakpoints
        layout.add(new Text("Break at 450px and 700px"));
        Board board = new Board();
        Div child1 = createComponent("This could be chart 1");
        Div child2 = createComponent("This could be chart 2");
        Div child3 = createComponent("This could be chart 3");
        Div child4 = createComponent("This could be chart 4");

        board.getStyle().set("--vaadin-board-width-medium", "700px");
        board.getStyle().set("--vaadin-board-width-small", "450px");

        board.addRow(child1, child2, child3, child4);
        layout.add(board);
        // end-source-example
        setStyles(child1, child2, child3, child4);

        // begin-source-example
        // source-example-heading: Redefining the breakpoints
        layout.add(new Text("Break at 400px and 2000px"));

        board = new Board();
        child1 = createComponent("This could be chart 1");
        child2 = createComponent("This could be chart 2");
        child3 = createComponent("This could be chart 3");
        child4 = createComponent("This could be chart 4");

        board.getStyle().set("--vaadin-board-width-medium", "2000px");
        board.getStyle().set("--vaadin-board-width-small", "400px");

        board.addRow(child1, child2, child3, child4);
        layout.add(board);
        add(layout);
        // end-source-example

        setStyles(child1, child2, child3, child4);
        addCard(CATEGORY_STYLING, "Redefining the breakpoints", layout);
    }

    private static void setBreakpoints(Board board) {
        board.getStyle().set("--vaadin-board-width-medium", "700px");
        board.getStyle().set("--vaadin-board-width-small", "375px");
    }

    private static void setStyles(HasStyle... components) {
        for (int i = 0; i < components.length; i++) {
            Style style = components[i].getStyle();
            style.set("padding", "1em");
            style.set("text-align", "center");
            style.set("background-color", cellColors[i]);

            if (i < 5) {
                style.set("color", "white");
            }
        }

    }

}
