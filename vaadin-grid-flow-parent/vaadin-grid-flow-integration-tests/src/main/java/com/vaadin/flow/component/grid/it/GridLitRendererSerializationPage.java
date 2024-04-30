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
package com.vaadin.flow.component.grid.it;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/lit-renderer-serialization")
public class GridLitRendererSerializationPage extends Div {

    public static class Data implements Serializable {
        private Integer number;

        public Data(Integer number) {
            this.number = number;
        }

        public Integer getNumber() {
            return number;
        }

        public String getNumberAsString() {
            return String.valueOf(number);
        }

    }

    private Span exceptionMessageSpan;

    public GridLitRendererSerializationPage() {
        Grid<Data> grid = new Grid<>(Data.class, true);
        grid.addColumn(new TextRenderer<>(d -> String.valueOf(d.number * 100)))
                .setHeader("R1");
        grid.addColumn(new TextRenderer<>(d -> String.valueOf(d.number * 100)))
                .setHeader("R2");

        NativeButton serializeAndDeserializeUiButton = new NativeButton(
                "Serialize&Deserialize UI");
        serializeAndDeserializeUiButton
                .addClickListener(event -> serializeAndDeserialize());
        serializeAndDeserializeUiButton.setId("serialize-and-deserialize-ui");

        exceptionMessageSpan = new Span();
        exceptionMessageSpan.setId("exception-message-span");

        add(grid, exceptionMessageSpan, serializeAndDeserializeUiButton);
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
