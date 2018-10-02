package com.vaadin.flow.component.richtexteditor.examples;

import com.vaadin.flow.component.richtexteditor.RichTextEditor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "")
public class Home extends Div {

    public Home() {
        add(new RichTextEditor("Foo bar"));
    }
}
