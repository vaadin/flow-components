/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.menubar.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.Route;

@Route("vaadin-menu-bar/serialization")
public class MenuBarSerializationPage extends Div {

    private MenuBar menuBar;
    private NativeButton serializeAndDeserializeUiButton;
    private Span exceptionMessageSpan;

    public MenuBarSerializationPage() {
        menuBar = new MenuBar();
        menuBar.addItem("Menu item");

        serializeAndDeserializeUiButton = new NativeButton(
                "Serialize&Deserialize UI");
        serializeAndDeserializeUiButton
                .addClickListener(event -> serializeAndDeserialize());
        serializeAndDeserializeUiButton.setId("serialize-and-deserialize-ui");

        exceptionMessageSpan = new Span();
        exceptionMessageSpan.setId("exception-message-span");

        add(serializeAndDeserializeUiButton, menuBar, exceptionMessageSpan);
    }

    private void serializeAndDeserialize() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bs)) {
            out.writeObject(UI.getCurrent());
        } catch (IOException ex) {
            exceptionMessageSpan.setText(ex.getMessage());
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bs.toByteArray()))) {
            in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            exceptionMessageSpan.setText(ex.getMessage());
        }
    }
}
