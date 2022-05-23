/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSelection;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;

/**
 * Test view for {@link TextField}.
 */
@Route("vaadin-text-field/selection-test")
public class SelectionPage extends Div {

    private HasSelection field;

    private Div display = new Div();

    /**
     * Constructs a basic layout with a text field.
     */
    public SelectionPage() {
        initView();
    }

    private void initView() {
        display.setId("display");

        TextField textField = new TextField();
        textField.setId("field");
        textField.setValue("12345");
        field = textField;
        add(display, textField);

        var textArea = new NativeButton("textArea");
        textArea.setId("textArea");
        textArea.addClickListener(e -> {
            var f = new TextArea();
            f.setId("field");
            f.setValue("12345");
            remove((Component) field);
            field = f;
            addComponentAtIndex(1, f);
        });

        var passwordField = new NativeButton("passwordField");
        passwordField.setId("passwordField");
        passwordField.addClickListener(e -> {
            var f = new PasswordField();
            f.setId("field");
            f.setValue("12345");
            remove((Component) field);
            field = f;
            addComponentAtIndex(1, f);
        });

        var integerField = new NativeButton("integerField");
        integerField.setId("integerField");
        integerField.addClickListener(e -> {
            var f = new IntegerField();
            f.setId("field");
            f.setValue(12345);
            remove((Component) field);
            field = f;
            addComponentAtIndex(1, f);
        });

        var numberField = new NativeButton("numberField");
        numberField.setId("numberField");
        numberField.addClickListener(e -> {
            var f = new NumberField();
            f.setId("field");
            f.setValue(12345.0);
            remove((Component) field);
            field = f;
            addComponentAtIndex(1, f);
        });

        var emailField = new NativeButton("emailField");
        emailField.setId("emailField");
        emailField.addClickListener(e -> {
            var f = new EmailField();
            f.setId("field");
            f.setValue("test@test.com");
            remove((Component) field);
            field = f;
            addComponentAtIndex(1, f);
        });

        var bigDesimalField = new NativeButton("bigDecimalField");
        bigDesimalField.setId("bigDecimalField");
        bigDesimalField.addClickListener(e -> {
            var f = new BigDecimalField();
            f.setId("field");
            f.setValue(BigDecimal.valueOf(12345));
            remove((Component) field);
            field = f;
            addComponentAtIndex(1, f);
        });

        add(textArea, passwordField, integerField, bigDesimalField, numberField, emailField);

        NativeButton selectAllText = new NativeButton(
                "Select all text");
        selectAllText.setId("selectall");
        selectAllText.addClickListener(event -> {
            field.selectAll();
        });
        add(selectAllText);

        NativeButton selectionRange = new NativeButton(
                "Set selection range 0,4 (exclusive)");
        selectionRange.setId("selectionrange");
        selectionRange.addClickListener(
                event -> {
                    field.setSelectionRange(0, 4);
                });
        add(selectionRange);

        NativeButton selection = new NativeButton(
                "select 1,3 + get selection");
        selection.setId("selection");
        selection.addClickListener(
                event -> {
                    field.setSelectionRange(1, 3);
                    field.getSelectionRange((start, end, content) -> {
                        display.setText(start + "," + end + ":" + content);
                    });
                });
        add(selection);

    }

}
