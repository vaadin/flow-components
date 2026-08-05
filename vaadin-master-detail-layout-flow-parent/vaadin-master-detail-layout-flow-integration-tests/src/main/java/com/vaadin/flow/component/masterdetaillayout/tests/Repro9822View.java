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
package com.vaadin.flow.component.masterdetaillayout.tests;

import java.util.List;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("repro-9822")
public class Repro9822View extends Div {

    private int backdropClicks = 0;
    private boolean closeOnBackdropClick = true;

    public Repro9822View() {
        MasterDetailLayout layout = new MasterDetailLayout();
        layout.setId("layout");
        // Small layout with sizes that don't fit side by side -> overlay mode
        layout.setWidth("500px");
        layout.setHeight("450px");
        layout.setMasterSize("400px");
        layout.setDetailSize("400px");

        Span backdropCount = new Span("backdrop clicks: 0");
        backdropCount.setId("backdrop-count");

        Span detailState = new Span("detail: none");
        detailState.setId("detail-state");

        NativeButton openDetail = new NativeButton("Open detail", e -> {
            layout.setDetail(createDetail());
            detailState.setText("detail: open");
        });
        openDetail.setId("open-detail");

        NativeButton closeDetail = new NativeButton("Close detail", e -> {
            layout.setDetail(null);
            detailState.setText("detail: none");
        });
        closeDetail.setId("close-detail");

        NativeButton toggleAutoClose = new NativeButton(
                "auto-close on backdrop click: ON", e -> {
                    closeOnBackdropClick = !closeOnBackdropClick;
                    e.getSource().setText("auto-close on backdrop click: "
                            + (closeOnBackdropClick ? "ON" : "OFF"));
                });
        toggleAutoClose.setId("toggle-auto-close");

        NativeButton toggleContainment = new NativeButton("containment: LAYOUT",
                e -> {
                    var next = layout
                            .getOverlayContainment() == MasterDetailLayout.OverlayContainment.LAYOUT
                                    ? MasterDetailLayout.OverlayContainment.PAGE
                                    : MasterDetailLayout.OverlayContainment.LAYOUT;
                    layout.setOverlayContainment(next);
                    e.getSource().setText("containment: " + next.name());
                });
        toggleContainment.setId("toggle-containment");

        Div master = new Div(new Div(openDetail, closeDetail),
                new Div(toggleAutoClose, toggleContainment),
                new Div(backdropCount), new Div(detailState));
        master.setId("master");
        layout.setMaster(master);

        layout.addBackdropClickListener(e -> {
            backdropClicks++;
            backdropCount.setText("backdrop clicks: " + backdropClicks);
            if (closeOnBackdropClick) {
                layout.setDetail(null);
                detailState.setText("detail: none");
            }
        });

        add(new Div("Issue 9822 reproduction"), layout);
    }

    private Div createDetail() {
        MultiSelectComboBox<String> roles = new MultiSelectComboBox<>("Roles");
        roles.setId("mscb");
        roles.setItems(List.of("Admin", "User", "Manager"));

        ComboBox<String> singleSelect = new ComboBox<>("Single combo box");
        singleSelect.setId("combo-box");
        singleSelect.setItems(List.of("Admin", "User", "Manager"));

        DatePicker datePicker = new DatePicker("Date picker");
        datePicker.setId("date-picker");

        TextField anotherField = new TextField("Another field");
        anotherField.setId("another-field");

        Div detail = new Div(roles, singleSelect, datePicker, anotherField);
        detail.setId("detail");
        return detail;
    }
}
