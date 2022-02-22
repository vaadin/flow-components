package com.vaadin.flow.component.confirmdialog.examples;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-confirm-dialog/modality")
public class ModalityPage extends Div {
    public ModalityPage() {
        // Dialog automatically adds itself to UI on open
        ConfirmDialog autoAddedDialog = new ConfirmDialog();
        autoAddedDialog.setId("auto-added-dialog");
        Button openAutoAddedDialog = new Button("Open auto-added dialog",
                e -> autoAddedDialog.open());
        openAutoAddedDialog.setId("open-auto-added-dialog");

        // Dialog is manually added to the UI
        ConfirmDialog manuallyAddedDialog = new ConfirmDialog();
        manuallyAddedDialog.setId("manually-added-dialog");
        Button openManuallyAddedDialog = new Button("Open auto-added dialog",
                e -> manuallyAddedDialog.open());
        openManuallyAddedDialog.setId("open-manually-added-dialog");
        add(manuallyAddedDialog);

        Span testClickResult = new Span();
        testClickResult.setId("test-click-result");
        Button testClick = new Button("Test click",
                event -> testClickResult.setText("Click event received"));
        testClick.setId("test-click");

        add(new Div(openAutoAddedDialog, openManuallyAddedDialog));
        add(new Div(testClick, testClickResult));
    }
}
