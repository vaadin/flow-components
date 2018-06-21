package com.vaadin.flow.component.confirmdialog.vaadincom;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-confirm-dialog")
public class ConfirmDialogView extends DemoView {

    @Override
    protected void initView() {
        unsavedChangesDialog();
        confirmPublishDialog();
        confirmDeleteDialog();
        meetingStartingAlert();
    }

    private void createCard(String heading, String buttonText, ConfirmDialog dialog) {
        Div messageDiv = createMessageDiv();
        Button button = new Button(buttonText);
        button.addClickListener(event -> dialog.open());
        dialog.addConfirmListener(event -> messageDiv.setText("Confirmed"));
        dialog.addRejectListener(event -> messageDiv.setText("Rejected"));
        dialog.addCancelListener(event -> messageDiv.setText("Cancelled"));
        addCard(heading, button, messageDiv);
    }

    private void meetingStartingAlert() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Basic Alert Dialog Example
        ConfirmDialog dialog = new ConfirmDialog("Meeting starting",
                "Your next meeting starts in 5 minutes", "OK", this::onOK);
        // end-source-example
        // @formatter:on

        createCard("Basic Alert Dialog Example", "Open dialog", dialog);
    }

    private void confirmDeleteDialog() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Confirm Delete Dialog Example
        ConfirmDialog dialog = new ConfirmDialog("Delete product",
                "Are you sure you want to delete? This operation can not be undone.",
                "Delete", this::onDelete, "Cancel", this::onCancel);
        dialog.setConfirmButtonTheme("error primary");
        // end-source-example
        // @formatter:on

        createCard("Confirm Delete Dialog Example", "Open dialog", dialog);
    }

    private void confirmPublishDialog() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Confirmation Dialog Example
        ConfirmDialog dialog = new ConfirmDialog("Ready to publish?",
                "Do you want to publish this post?", "Publish", this::onPublish,
                "Cancel", this::onCancel);
        // end-source-example
        // @formatter:on

        createCard("Confirmation Dialog Example", "Open dialog", dialog);
    }

    private void unsavedChangesDialog() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Save or Discard Dialog Example
        ConfirmDialog dialog = new ConfirmDialog("Unsaved changes",
                "Do you want to save or discard your changes before navigating away?",
                "Save", this::onSave, "Discard", this::onDiscard, "Cancel",
                this::onCancel);
        // end-source-example
        // @formatter:on

        createCard("Save or Discard Dialog Example", "Open dialog", dialog);
    }

    private void onOK(ConfirmDialog.ConfirmEvent event) {
    }

    private void onPublish(ConfirmDialog.ConfirmEvent event) {
    }

    private void onSave(ConfirmDialog.ConfirmEvent event) {
    }

    private void onDiscard(ConfirmDialog.RejectEvent event) {
    }

    private void onDelete(ConfirmDialog.ConfirmEvent event) {
    }

    private void onCancel(ConfirmDialog.CancelEvent event) {
    }

    private Div createMessageDiv() {
        Div message = new Div();
        message.getStyle().set("whiteSpace", "pre");
        return message;
    }
}
