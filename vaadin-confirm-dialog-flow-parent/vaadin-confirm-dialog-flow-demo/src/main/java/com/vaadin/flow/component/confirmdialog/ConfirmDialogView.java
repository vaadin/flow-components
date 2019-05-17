package com.vaadin.flow.component.confirmdialog;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-confirm-dialog")
public class ConfirmDialogView extends DemoView {

    @Override
    protected void initView() {
        alertDialog();
        confirmationDialog();
        confirmationDialogForDangerousAction();
        confirmationDialogWithRejectOption();
        customButtons();
    }

    private void createCard(String heading, String buttonText, ConfirmDialog dialog) {
        Span message = new Span();
        message.getStyle().set("margin-left", "var(--lumo-space-m)");

        Button button = new Button(buttonText);
        button.addClickListener(event -> {
            message.setText("");
            dialog.open();
        });

        dialog.addConfirmListener(event -> message.setText("Confirmed"));
        dialog.addRejectListener(event -> message.setText("Rejected"));
        dialog.addCancelListener(event -> message.setText("Cancelled"));

        addCard(heading, new Div(button, message));
    }

    private void alertDialog() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Alert Dialog
        ConfirmDialog dialog = new ConfirmDialog("Meeting starting",
                "Your next meeting starts in 5 minutes", "OK", this::onOK);
        // end-source-example
        // @formatter:on

        createCard("Alert Dialog", "Open dialog", dialog);
    }

    private void confirmationDialog() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Confirmation Dialog
        ConfirmDialog dialog = new ConfirmDialog("Confirm publish",
                "Are you sure you want to publish the article?", "Publish", this::onPublish,
                "Cancel", this::onCancel);
        // end-source-example
        // @formatter:on

        createCard("Confirmation Dialog", "Open dialog", dialog);
    }

    private void confirmationDialogForDangerousAction() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Confirmation Dialog for Dangerous Actions
        ConfirmDialog dialog = new ConfirmDialog("Confirm delete",
                "Are you sure you want to delete the item?",
                "Delete", this::onDelete, "Cancel", this::onCancel);
        dialog.setConfirmButtonTheme("error primary");
        // end-source-example
        // @formatter:on

        createCard("Confirmation Dialog for Dangerous Actions", "Open dialog", dialog);
    }

    private void confirmationDialogWithRejectOption() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Confirmation Dialog with a Reject Option
        ConfirmDialog dialog = new ConfirmDialog("Unsaved changes",
                "Do you want to save or discard your changes before navigating away?",
                "Save", this::onSave, "Discard", this::onDiscard, "Cancel", this::onCancel);
        // end-source-example
        // @formatter:on

        createCard("Confirmation Dialog with a Reject Option", "Open dialog", dialog);
    }

    private void customButtons() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Custom Buttons
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Unsaved changes");
        String textHtml = "<p>Do you want to <b>save</b> or <b>discard</b> " +
                "your changes before navigating away?</p>";
        dialog.setText(new Html(textHtml).getElement());

        Button saveButton = new Button("Save", VaadinIcon.ENVELOPE_OPEN.create());
        saveButton.addClickListener(e -> dialog.close());
        saveButton.getElement().setAttribute("theme", "primary");
        dialog.setConfirmButton(saveButton.getElement());

        Button rejectButton = new Button("Discard", VaadinIcon.TRASH.create());
        rejectButton.addClickListener(e -> dialog.close());
        rejectButton.getElement().setAttribute("theme", "error tertiary");
        dialog.setRejectButton(rejectButton.getElement());

        dialog.setCancelButton("Cancel", this::onCancel);
        // end-source-example
        // @formatter:on

        createCard("Custom Buttons", "Open dialog", dialog);
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
}
