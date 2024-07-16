/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-dialog/dialog-template-test")
public class DialogWithTemplatePage extends Div {

    private Dialog dialog;

    public DialogWithTemplatePage() {
        dialog = new Dialog();
        TestTemplate testTemplate = new TestTemplate();
        testTemplate.setId("template");
        dialog.add(testTemplate);

        NativeButton open = new NativeButton("Open dialog");
        open.setId("open");
        open.addClickListener(event -> dialog.open());
        add(open);
    }

}
