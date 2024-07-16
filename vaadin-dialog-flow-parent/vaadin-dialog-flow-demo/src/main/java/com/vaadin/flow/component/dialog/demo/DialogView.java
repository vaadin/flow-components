/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.dialog.demo;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;

/**
 * View for {@link Dialog} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-dialog")
public class DialogView extends DemoView {

    private static final String BUTTON_CAPTION = "Open dialog";

    @Override
    public void initView() {
        addBasicDialog();
        addConfirmationDialog();
        addCloseFromServerSideDialog();
        addDialogWithFocusedElement();
        addStyledDialogContent();
        addModelessDraggableResizableDialog();
    }

    private void addBasicDialog() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        // begin-source-example
        // source-example-heading: Sized dialog
        Dialog dialog = new Dialog();
        dialog.add(new Text("Close me with the esc-key or an outside click"));

        dialog.setWidth("400px");
        dialog.setHeight("150px");

        button.addClickListener(event -> dialog.open());
        // end-source-example

        button.setId("basic-dialog-button");
        addCard("Sized dialog", button);
    }

    private void addConfirmationDialog() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        // begin-source-example
        // source-example-heading: Confirmation dialog
        Dialog dialog = new Dialog();
        dialog.add(new Text(
                "You have unsaved changes that will be discarded if you navigate away."));
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        Span message = new Span();

        Button confirmButton = new Button("Confirm", event -> {
            message.setText("Confirmed!");
            dialog.close();
        });
        Button cancelButton = new Button("Cancel", event -> {
            message.setText("Cancelled...");
            dialog.close();
        });
        // Cancel action on ESC press
        Shortcuts.addShortcutListener(dialog, () -> {
            message.setText("Cancelled...");
            dialog.close();
        }, Key.ESCAPE);

        dialog.add(new Div(confirmButton, cancelButton));
        // end-source-example
        button.addClickListener(event -> dialog.open());
        confirmButton.getStyle().set("margin-right", "15px");
        message.setId("confirmation-dialog-span");
        button.setId("confirmation-dialog-button");
        addCard("Confirmation dialog", button, message);
    }

    private void addCloseFromServerSideDialog() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        // begin-source-example
        // source-example-heading: Close from server-side
        Span message = new Span();

        Dialog dialog = new Dialog(new Text("Close me with the esc-key"));
        dialog.setCloseOnOutsideClick(false);

        dialog.addDialogCloseActionListener(e -> {
            message.setText("Closed from server-side");
            dialog.close();
        });
        // end-source-example

        button.addClickListener(event -> dialog.open());

        message.setId("server-side-close-dialog-span");
        button.setId("server-side-close-dialog-button");
        addCard("Close from server-side", button, message);
    }

    private void addDialogWithFocusedElement() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        // begin-source-example
        // source-example-heading: Focus internal Element
        Dialog dialog = new Dialog();
        Input input = new Input();

        dialog.add(input);

        button.addClickListener(event -> {
            dialog.open();
            input.focus();
        });
        // end-source-example

        button.setId("focus-dialog-button");
        addCard("Focus internal Element", button);
    }

    private void addStyledDialogContent() {
        NativeButton button = new NativeButton(BUTTON_CAPTION);

        // begin-source-example
        // source-example-heading: Dialog with styled content
        Dialog dialog = new Dialog();
        Div content = new Div();
        content.addClassName("my-style");

        content.setText("This component is styled using global styles");
        dialog.add(content);

        // @formatter:off
        String styles = ".my-style { "
                + "  color: red;"
                + " }";
        // @formatter:on

        /*
         * The code below register the style file dynamically. Normally you
         * use @StyleSheet annotation for the component class. This way is
         * chosen just to show the style file source code.
         */
        StreamRegistration resource = UI.getCurrent().getSession()
                .getResourceRegistry()
                .registerResource(new StreamResource("styles.css", () -> {
                    byte[] bytes = styles.getBytes(StandardCharsets.UTF_8);
                    return new ByteArrayInputStream(bytes);
                }));
        UI.getCurrent().getPage().addStyleSheet(
                "base://" + resource.getResourceUri().toString());

        dialog.setWidth("400px");
        dialog.setHeight("150px");

        button.addClickListener(event -> dialog.open());
        // end-source-example

        button.setId("styled-content-dialog-button");
        addCard("Dialog with styled content", button);
    }

    private void addModelessDraggableResizableDialog() {
        NativeButton openDialog = new NativeButton(BUTTON_CAPTION);
        NativeButton openSecondDialog = new NativeButton("Open another dialog");

        // begin-source-example
        // source-example-heading: Modeless Draggable Resizable Dialog
        Dialog firstDialog = new Dialog();
        firstDialog.add(new Text("This is the first dialog"),
                new Button("Close", e -> firstDialog.close()));
        firstDialog.setModal(false);
        firstDialog.setDraggable(true);
        firstDialog.setResizable(true);

        Dialog secondDialog = new Dialog();
        secondDialog.add(new Text("This is the second dialog"),
                new Button("Close", e -> secondDialog.close()));
        secondDialog.setModal(false);
        secondDialog.setDraggable(true);
        secondDialog.setResizable(true);

        openDialog.addClickListener(e -> firstDialog.open());
        openSecondDialog.addClickListener(e -> secondDialog.open());
        // end-source-example

        addCard("Modeless Draggable Resizable Dialog", openDialog,
                openSecondDialog, firstDialog);
    }
}
