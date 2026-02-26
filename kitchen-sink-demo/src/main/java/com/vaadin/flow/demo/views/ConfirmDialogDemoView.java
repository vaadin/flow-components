/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.demo.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for ConfirmDialog component.
 */
@Route(value = "confirm-dialog", layout = MainLayout.class)
@PageTitle("Confirm Dialog | Vaadin Kitchen Sink")
public class ConfirmDialogDemoView extends VerticalLayout {

    public ConfirmDialogDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Confirm Dialog Component"));
        add(new Paragraph("ConfirmDialog is a specialized dialog for confirmation actions."));

        // Basic confirm dialog
        Button basicBtn = new Button("Show Confirmation", e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Confirm action");
            dialog.setText("Are you sure you want to proceed?");
            dialog.setConfirmText("Confirm");
            dialog.addConfirmListener(event ->
                Notification.show("Confirmed!"));
            dialog.open();
        });
        addSection("Basic Confirmation", basicBtn);

        // Confirm and cancel
        Button confirmCancelBtn = new Button("Confirm or Cancel", e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Save changes?");
            dialog.setText("Do you want to save the changes you made?");
            dialog.setCancelable(true);
            dialog.setCancelText("Cancel");
            dialog.setConfirmText("Save");
            dialog.addConfirmListener(event ->
                Notification.show("Changes saved!").addThemeVariants(NotificationVariant.LUMO_SUCCESS));
            dialog.addCancelListener(event ->
                Notification.show("Cancelled"));
            dialog.open();
        });
        addSection("With Cancel Button", confirmCancelBtn);

        // Delete confirmation (danger)
        Button deleteBtn = new Button("Delete Item", e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Delete item?");
            dialog.setText("Are you sure you want to delete this item? This action cannot be undone.");
            dialog.setCancelable(true);
            dialog.setConfirmText("Delete");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(event ->
                Notification.show("Item deleted!").addThemeVariants(NotificationVariant.LUMO_ERROR));
            dialog.open();
        });
        addSection("Delete Confirmation (Danger)", deleteBtn);

        // With reject button
        Button rejectBtn = new Button("Save, Discard, or Cancel", e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Unsaved changes");
            dialog.setText("You have unsaved changes. What would you like to do?");
            dialog.setCancelable(true);
            dialog.setCancelText("Cancel");
            dialog.setRejectable(true);
            dialog.setRejectText("Discard");
            dialog.setConfirmText("Save");
            dialog.addConfirmListener(event ->
                Notification.show("Saved!").addThemeVariants(NotificationVariant.LUMO_SUCCESS));
            dialog.addRejectListener(event ->
                Notification.show("Discarded changes").addThemeVariants(NotificationVariant.LUMO_CONTRAST));
            dialog.addCancelListener(event ->
                Notification.show("Cancelled"));
            dialog.open();
        });
        addSection("With Reject Button", rejectBtn);

        // Information dialog
        Button infoBtn = new Button("Show Information", e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Information");
            dialog.setText("Your order has been successfully placed! You will receive a confirmation email shortly.");
            dialog.setConfirmText("OK");
            dialog.open();
        });
        addSection("Information Dialog", infoBtn);

        // Warning dialog
        Button warningBtn = new Button("Show Warning", e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Warning");
            dialog.setText("This action will affect all users in your organization. Are you sure you want to continue?");
            dialog.setCancelable(true);
            dialog.setConfirmText("Continue");
            dialog.setConfirmButtonTheme("warning");
            dialog.addConfirmListener(event ->
                Notification.show("Action completed"));
            dialog.open();
        });
        addSection("Warning Dialog", warningBtn);

        // Logout confirmation
        Button logoutBtn = new Button("Logout", e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Sign out?");
            dialog.setText("Are you sure you want to sign out?");
            dialog.setCancelable(true);
            dialog.setConfirmText("Sign out");
            dialog.addConfirmListener(event ->
                Notification.show("Signed out successfully"));
            dialog.open();
        });
        addSection("Logout Confirmation", logoutBtn);

        // Bulk action confirmation
        Button bulkBtn = new Button("Delete 5 Selected Items", e -> {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setHeader("Delete 5 items?");
            dialog.setText("You are about to delete 5 items. This action cannot be undone.");
            dialog.setCancelable(true);
            dialog.setConfirmText("Delete 5 items");
            dialog.setConfirmButtonTheme("error primary");
            dialog.addConfirmListener(event ->
                Notification.show("5 items deleted").addThemeVariants(NotificationVariant.LUMO_ERROR));
            dialog.open();
        });
        addSection("Bulk Action Confirmation", bulkBtn);
    }

    private void addSection(String title, com.vaadin.flow.component.Component... components) {
        Div section = new Div();
        section.add(new H2(title));
        VerticalLayout layout = new VerticalLayout(components);
        layout.setSpacing(true);
        layout.setPadding(false);
        section.add(layout);
        add(section);
    }
}
