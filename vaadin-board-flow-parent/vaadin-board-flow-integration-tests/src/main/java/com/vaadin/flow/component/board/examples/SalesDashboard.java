package com.vaadin.flow.component.board.examples;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;

@Route("vaadin-board/SalesDashboard")
@BodySize
public class SalesDashboard extends Div {

        private final String YELLOW = "#F9DD51";
        private final String GREEN = "#98DF58";
        private final String BLUE = "#3090F0";
        private final String MAGENTA = "#EC64A5";
        private final String PURPLE = "#685CB0";

        private final String HEIGHT_166 = "166.66px";
        private final String HEIGHT_300 = "300px";
        private final String HEIGHT_400 = "400px";
        private final String HEIGHT_500 = "500px";

        public SalesDashboard() {
                Board board = new Board();
                board.addClassName("sales-dashboard-demo-area");

                // First row
                board.addRow(createColoredBox("Total Revenue / 1 k$", BLUE, HEIGHT_300),
                                createColoredBox("Billed / 1k$", GREEN, HEIGHT_300),
                                createColoredBox("Outstanding / 1k$", GREEN, HEIGHT_300),
                                createColoredBox("Refunded / 1k$", GREEN, HEIGHT_300));

                // Second row
                Row lineBoxesInnerRow = new Row();
                lineBoxesInnerRow.add(
                                createColoredBox("Customers - ↑501", BLUE, HEIGHT_166),
                                createColoredBox("ROI - ↑75%", BLUE, HEIGHT_166),
                                createColoredBox("Churn - ↓32", MAGENTA, HEIGHT_166));

                Div midColumnBox = createColoredBox("Q1 Product Sales", MAGENTA, HEIGHT_500);

                Row secondLine = board.addRow(midColumnBox, lineBoxesInnerRow);

                secondLine.setComponentSpan(midColumnBox, 3);

                // Third row
                board.addRow(
                        createColoredBox("Sales & Marketing pipeline", PURPLE, HEIGHT_400), 
                        createColoredBox("Working Today", YELLOW, HEIGHT_400));

                add(board);
        }

        private Div createColoredBox(String title, String color, String height) {
                Div container = new Div();
                container.setHeight(height);

                Span text = new Span(title);
                text.getElement().getStyle().set("color", "#FFF");
                container.add(text);

                Style containerStyle = container.getElement().getStyle();
                containerStyle.set("background-color", color);
                containerStyle.set("border", "1px solid");
                containerStyle.set("padding", "10px");

                return container;
        }
}
