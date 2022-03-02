package com.vaadin.flow.component.confirmdialog.examples;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-confirm-dialog/modality")
public class ModalityPage extends Div {
    public ModalityPage() {
        ConfirmDialog dialog = new ConfirmDialog();

        Button addDialog = new Button("Add dialog to UI", e -> add(dialog));
        addDialog.setId("add-dialog");

        Button openDialog = new Button("Open dialog", e -> dialog.open());
        openDialog.setId("open-dialog");

        Span testClickResult = new Span();
        testClickResult.setId("test-click-result");
        Button testClick = new Button("Test click",
                event -> testClickResult.setText("Click event received"));
        testClick.setId("test-click");

        add(new Div(addDialog, openDialog));
        add(new Div(testClick, testClickResult));
    }
}
