package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-spreadsheet/sizing")
public class SizingPage extends Div {

    private Spreadsheet spreadsheet;
    private Div layout;

    public SizingPage() {
        super();
        setSizeFull();

        spreadsheet = new Spreadsheet();
        spreadsheet.getStyle().set("outline", "1px dashed red");

        layout = new Div(spreadsheet);
        layout.setId("layout");
        layout.getStyle().set("outline", "1px dashed green");
        layout.getStyle().set("overflow", "auto");

        add(new H2("Spreadsheet"));
        var spreadsheetList = new UnorderedList();

        var spreadsheetHeight200 = new NativeButton("200px",
                e -> spreadsheet.setHeight("200px"));
        spreadsheetHeight200.setId("spreadsheetHeight200");

        var spreadsheetHeight600 = new NativeButton("600px",
                e -> spreadsheet.setHeight("600px"));
        spreadsheetHeight600.setId("spreadsheetHeight600");

        var spreadsheetHeightDefault = new NativeButton("default (100%)",
                e -> spreadsheet.getStyle().remove("height"));
        spreadsheetHeightDefault.setId("spreadsheetHeightDefault");

        spreadsheetList
                .add(new ListItem(new Span("Height: "), spreadsheetHeight200,
                        spreadsheetHeight600, spreadsheetHeightDefault));

        add(spreadsheetList);

        add(new H2("Layout"));
        var layoutList = new UnorderedList();

        var layoutHeight200 = new NativeButton("200px",
                e -> layout.setHeight("200px"));
        layoutHeight200.setId("layoutHeight200");
        add(layoutHeight200);

        var layoutHeight600 = new NativeButton("600px",
                e -> layout.setHeight("600px"));
        layoutHeight600.setId("layoutHeight600");
        add(layoutHeight600);

        var layoutHeightDefault = new NativeButton("default (auto)",
                e -> layout.getStyle().remove("height"));
        layoutHeightDefault.setId("layoutHeightDefault");
        add(layoutHeightDefault);

        layoutList.add(new ListItem(new Span("Height: "), layoutHeight200,
                layoutHeight600, layoutHeightDefault));

        var layoutDisplayFlex = new NativeButton("flex",
                e -> layout.getStyle().set("display", "flex"));
        layoutDisplayFlex.setId("layoutDisplayFlex");
        add(layoutDisplayFlex);

        var layoutDisplayDefault = new NativeButton("default (block)",
                e -> layout.getStyle().remove("display"));
        layoutDisplayDefault.setId("layoutDisplayDefault");
        add(layoutDisplayDefault);

        layoutList.add(new ListItem(new Span("Display: "), layoutDisplayFlex,
                layoutDisplayDefault));

        add(layoutList);

        add(layout);
    }

}