package com.vaadin.flow.component.dialog.tests;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-dialog/sub-dialog")
public class SubDialogPage extends VerticalLayout {

    public SubDialogPage() {
        // Create a dialog and a button to open it
        var dialog = new Dialog();
        var dialogButton = new Button("Open dialog", e -> {
            dialog.open();
        });
        dialogButton.setId("open-dialog");

        // Create a button to open a sub-dialog
        var subDialogButton = new Button("Open sub-dialog", e -> {
            var subDialog = new Dialog();
            subDialog.add(new Span("Sub-dialog"));
            subDialog.open();
        });
        subDialogButton.setId("open-sub-dialog");

        // Create a scroller with a long text
        var longText = new Span();
        longText.setText(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, "
                        + "nunc id aliquam ultricies, diam magna mollis nisl, ut aliquet elit "
                        + "nisl in diam. Nulla facilisi. Donec euismod, nunc id aliquam ultricies, "
                        + "diam magna mollis nisl, ut aliquet elit nisl in diam. Nulla facilisi. "
                        + "Donec euismod, nunc id aliquam ultricies, diam magna mollis nisl, ut "
                        + "aliquet elit nisl in diam. Nulla facilisi. Donec euismod, nunc id aliquam "
                        + "ultricies, diam magna mollis nisl, ut aliquet elit nisl in diam. Nulla "
                        + "facilisi. Donec euismod, nunc id aliquam ultricies, diam magna mollis "
                        + "nisl, ut aliquet elit nisl in diam. Nulla facilisi.");

        var scroller = new Scroller(longText);
        scroller.setId("scroller");
        scroller.setHeight("120px");
        scroller.setWidth("200px");

        dialog.add(subDialogButton, scroller);

        add(dialogButton);
    }

}
