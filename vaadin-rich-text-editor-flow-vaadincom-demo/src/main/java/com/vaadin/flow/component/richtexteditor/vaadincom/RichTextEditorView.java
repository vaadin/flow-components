package com.vaadin.flow.component.richtexteditor.vaadincom;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-rich-text-editor")
public class RichTextEditorView extends DemoView {

    @Override
    protected void initView() {
        addCard("Basic RichTextEditor", new H1("Hello RichTextEditor!"));
    }
}
