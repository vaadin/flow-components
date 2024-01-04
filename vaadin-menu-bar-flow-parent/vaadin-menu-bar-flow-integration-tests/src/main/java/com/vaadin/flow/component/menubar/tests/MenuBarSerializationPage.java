/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.menubar.tests;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.router.Route;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
