package com.example.application.views.demo.views;

import java.io.IOException;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class BasicExample extends Div {

    public BasicExample(String fileName) {
        setSizeFull();
        try {
            add(new Spreadsheet(BasicExample.class.getResourceAsStream("/testsheets/" + fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
