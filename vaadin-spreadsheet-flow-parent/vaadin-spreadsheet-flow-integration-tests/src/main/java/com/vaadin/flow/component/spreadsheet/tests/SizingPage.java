package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
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

        spreadsheetList.add(new ListItem(new Span("Height: "),
                getButton("200px", "spreadsheetHeight200",
                        e -> spreadsheet.setHeight("200px")),
                getButton("600px", "spreadsheetHeight600",
                        e -> spreadsheet.setHeight("600px")),
                getButton("Default (100%)", "spreadsheetHeightDefault",
                        e -> spreadsheet.setHeight(null))));

        spreadsheetList.add(new ListItem(new Span("Attached: "),
                getButton("Toggle", "spreadsheetAttachedToggle", e -> {
                    if (spreadsheet.getParent().isPresent()) {
                        layout.remove(spreadsheet);
                    } else {
                        layout.add(spreadsheet);
                    }
                })));

        add(spreadsheetList);

        add(new H2("Layout"));

        var layoutList = new UnorderedList();

        layoutList.add(new ListItem(new Span("Height: "),
                getButton("200px", "layoutHeight200",
                        e -> layout.setHeight("200px")),
                getButton("600px", "layoutHeight600",
                        e -> layout.setHeight("600px")),
                getButton("Default (auto)", "layoutHeightDefault",
                        e -> layout.setHeight(null))));

        layoutList.add(new ListItem(new Span("Display: "),
                getButton("flex", "layoutDisplayFlex",
                        e -> layout.getStyle().set("display", "flex")),
                getButton("Default (block)", "layoutDisplayDefault",
                        e -> layout.getStyle().remove("display"))));

        layoutList.add(new ListItem(new Span("Flex: "), getButton(
                "Column / align-start", "layoutFlexColumnStart", e -> {
                    layout.getStyle().set("flex-direction", "column");
                    layout.getStyle().set("align-items", "start");
                }), getButton("Default", "layoutFlexColumnStartDefault", e -> {
                    layout.getStyle().remove("flex-direction");
                    layout.getStyle().remove("align-items");
                })));

        add(layoutList);

        add(layout);
    }

    private NativeButton getButton(String title, String id,
            ComponentEventListener<ClickEvent<NativeButton>> clickListener) {
        var button = new NativeButton(title, clickListener);
        button.setId(id);
        return button;
    }

}