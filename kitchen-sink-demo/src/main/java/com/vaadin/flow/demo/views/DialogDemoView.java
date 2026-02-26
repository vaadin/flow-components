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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.demo.MainLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demo view for Dialog component.
 */
@Route(value = "dialog", layout = MainLayout.class)
@PageTitle("Dialog | Vaadin Kitchen Sink")
public class DialogDemoView extends VerticalLayout {

    public DialogDemoView() {
        setSpacing(true);
        setPadding(true);

        add(new H2("Dialog Component"));
        add(new Paragraph("Dialog displays modal content overlaying the main view."));

        // Basic dialog
        Button basicBtn = new Button("Open Basic Dialog", e -> {
            Dialog dialog = new Dialog();
            dialog.add(new Paragraph("This is a basic dialog with some text content."));
            dialog.add(new Button("Close", event -> dialog.close()));
            dialog.open();
        });
        addSection("Basic Dialog", basicBtn);

        // Dialog with header and footer
        Button headerFooterBtn = new Button("Dialog with Header/Footer", e -> {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Dialog Title");
            dialog.add(new Paragraph("This dialog has a header with title and footer with actions."));

            Button cancelBtn = new Button("Cancel", event -> dialog.close());
            Button saveBtn = new Button("Save", event -> {
                Notification.show("Saved!");
                dialog.close();
            });
            saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dialog.getFooter().add(cancelBtn, saveBtn);

            dialog.open();
        });
        addSection("With Header and Footer", headerFooterBtn);

        // Form dialog
        Button formBtn = new Button("Open Form Dialog", e -> {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("User Information");

            VerticalLayout form = new VerticalLayout();
            form.setPadding(false);
            TextField name = new TextField("Name");
            name.setWidthFull();
            TextField email = new TextField("Email");
            email.setWidthFull();
            form.add(name, email);
            dialog.add(form);

            Button cancelBtn = new Button("Cancel", event -> dialog.close());
            Button submitBtn = new Button("Submit", event -> {
                Notification.show("Submitted: " + name.getValue());
                dialog.close();
            });
            submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dialog.getFooter().add(cancelBtn, submitBtn);

            dialog.open();
        });
        addSection("Form Dialog", formBtn);

        // Confirmation dialog
        Button confirmBtn = new Button("Delete Item", e -> {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Confirm Deletion");
            dialog.add(new Paragraph("Are you sure you want to delete this item? This action cannot be undone."));

            Button cancelBtn = new Button("Cancel", event -> dialog.close());
            Button deleteBtn = new Button("Delete", event -> {
                Notification.show("Item deleted!");
                dialog.close();
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
            dialog.getFooter().add(cancelBtn, deleteBtn);

            dialog.open();
        });
        addSection("Confirmation Dialog", confirmBtn);

        // Non-modal dialog
        Button nonModalBtn = new Button("Open Non-Modal Dialog", e -> {
            Dialog dialog = new Dialog();
            dialog.setModal(false);
            dialog.setDraggable(true);
            dialog.setHeaderTitle("Non-Modal Dialog");
            dialog.add(new Paragraph("This dialog is non-modal - you can interact with the page behind it. It's also draggable!"));
            dialog.add(new Button("Close", event -> dialog.close()));
            dialog.open();
        });
        addSection("Non-Modal Draggable Dialog", nonModalBtn);

        // Resizable dialog
        Button resizableBtn = new Button("Open Resizable Dialog", e -> {
            Dialog dialog = new Dialog();
            dialog.setResizable(true);
            dialog.setDraggable(true);
            dialog.setHeaderTitle("Resizable Dialog");
            dialog.setWidth("400px");
            dialog.setHeight("300px");
            dialog.add(new Paragraph("This dialog can be resized by dragging its edges. It's also draggable from the header."));
            dialog.add(new Button("Close", event -> dialog.close()));
            dialog.open();
        });
        addSection("Resizable Dialog", resizableBtn);

        // Dialog that closes on outside click
        Button outsideClickBtn = new Button("Click Outside to Close", e -> {
            Dialog dialog = new Dialog();
            dialog.setCloseOnOutsideClick(true);
            dialog.setHeaderTitle("Click Outside");
            dialog.add(new Paragraph("Click anywhere outside this dialog to close it."));
            dialog.open();
        });
        addSection("Close on Outside Click", outsideClickBtn);

        // Dialog that closes on Escape
        Button escapeBtn = new Button("Press Escape to Close", e -> {
            Dialog dialog = new Dialog();
            dialog.setCloseOnEsc(true);
            dialog.setHeaderTitle("Press Escape");
            dialog.add(new Paragraph("Press the Escape key to close this dialog."));
            dialog.open();
        });
        addSection("Close on Escape Key", escapeBtn);
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
