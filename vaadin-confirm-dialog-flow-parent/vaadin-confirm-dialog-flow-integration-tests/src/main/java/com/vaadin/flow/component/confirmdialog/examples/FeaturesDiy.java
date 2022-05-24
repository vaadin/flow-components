package com.vaadin.flow.component.confirmdialog.examples;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;

@Route("vaadin-confirm-dialog/FeaturesDiy")
public class FeaturesDiy extends Features {

    @Override
    protected ConfirmDialog createConfirmDialog(String sample) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setId(sample);
        dialog.setHeader(new H2("Meeting starting").getElement());
        dialog.setText(
                new Span("Your next meeting starts in 5 minutes").getElement());

        // This button will get discarded by the new confirm button added below.
        dialog.setConfirmButton(new Button("Discarded confirm button"));

        Button confirmButton = new Button("Confirm");
        confirmButton.setId("confirmDiy");
        confirmButton.setIcon(VaadinIcon.CALENDAR.create());
        confirmButton.addClickListener(e -> {
            this.onOK(new ConfirmDialog.ConfirmEvent(dialog, false));
            dialog.close();
        });
        dialog.setConfirmButton(confirmButton.getElement());

        return dialog;
    }

    @Override
    protected ConfirmDialog createConfirmDeleteDialog(String sample) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setId(sample);
        dialog.setHeader("Delete product");
        dialog.setText(new Html(
                "<span>Are you sure you want to delete? This operation <b>can not be undone</b></span>")
                        .getElement());
        Button deleteButton = new Button("Delete");
        deleteButton.setId("confirmDiy");
        deleteButton.setIconAfterText(true);
        deleteButton.setIcon(VaadinIcon.TRASH.create());
        deleteButton.addClickListener(e -> {
            this.onDelete(new ConfirmDialog.ConfirmEvent(dialog, false));
            dialog.close();
        });
        deleteButton.getElement().setAttribute("theme", "error primary");
        dialog.setConfirmButton(deleteButton.getElement());
        dialog.setCancelButton("Cancel", this::onCancel);

        return dialog;
    }

    @Override
    protected ConfirmDialog createConfirmPublishDialog(String sample) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setId(sample);
        dialog.setHeader("Ready to publish?");
        dialog.setText(
                new Label("Do you want to publish this post?").getElement());
        Button publishButton = new Button("Publish");
        publishButton.setId("confirmDiy");
        publishButton.getElement().setAttribute("theme", "primary");
        publishButton.addClickListener(e -> {
            this.onPublish(new ConfirmDialog.ConfirmEvent(dialog, false));
            dialog.close();
        });
        dialog.setConfirmButton(publishButton.getElement());
        Button cancelButton = new Button("Cancel");
        cancelButton.setId("cancelDiy");
        cancelButton.addClickListener(e -> {
            this.onCancel(new ConfirmDialog.CancelEvent(dialog, false));
            dialog.close();
        });
        cancelButton.getElement().setAttribute("theme", "tertiary");
        dialog.setCancelButton(cancelButton.getElement());

        return dialog;
    }

    @Override
    protected ConfirmDialog createUnsavedChangesDialog(String sample) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setId(sample);
        dialog.setHeader("Unsaved changes");
        dialog.setText(new Html(
                "<span>Do you want to <b>save</b> or <b>discard</b> your changes before navigating away?</span>")
                        .getElement());

        Button saveButton = new Button("Save");
        saveButton.setId("confirmDiy");
        saveButton.addClickListener(e -> {
            this.onSave(new ConfirmDialog.ConfirmEvent(dialog, false));
            dialog.close();
        });
        saveButton.getElement().setAttribute("theme", "primary");
        dialog.setConfirmButton(saveButton.getElement());

        Button discardButton = new Button("Discard");
        discardButton.setId("rejectDiy");
        discardButton.addClickListener(e -> {
            this.onDiscard(new ConfirmDialog.RejectEvent(dialog, false));
            dialog.close();
        });
        discardButton.getElement().setAttribute("theme", "error tertiary");
        dialog.setRejectButton(discardButton.getElement());

        Button cancelButton = new Button("Cancel");
        cancelButton.setId("cancelDiy");
        cancelButton.addClickListener(e -> {
            this.onCancel(new ConfirmDialog.CancelEvent(dialog, false));
            dialog.close();
        });
        cancelButton.getElement().setAttribute("theme", "tertiary");
        dialog.setCancelButton(cancelButton.getElement());

        return dialog;
    }

}
