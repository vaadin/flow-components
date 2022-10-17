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
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Test view for serializing and deserializing a grid with a data provider.
 */
@Route("vaadin-grid/grid-data-provider-serializable")
public class GridDataProviderSerializablePage extends VerticalLayout {

    public static final String SERIALIZE_BUTTON_ID = "serialize-button";
    public static final String RESULT_SPAN_ID = "serialize-result-span";
    public static final String OK_RESULT = "OK";

    public GridDataProviderSerializablePage() {
        setSizeUndefined();
        setWidthFull();

        Span result = new Span();
        result.setId(RESULT_SPAN_ID);

        Grid<String> grid = new Grid<>();

        Button serializeButton = new Button("Serialize", event -> {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                    oos.writeObject(GridDataProviderSerializablePage.this);
                    oos.flush();
                }
                try (ByteArrayInputStream bis = new ByteArrayInputStream(
                        bos.toByteArray())) {
                    try (ObjectInputStream ois = new ObjectInputStream(bis)) {
                        GridDataProviderSerializablePage readObject = (GridDataProviderSerializablePage) ois
                                .readObject();
                    }
                }
                result.setText(OK_RESULT);
            } catch (Exception exception) {
                result.setText("FAILED: " + exception.getMessage());
            }
        });
        serializeButton.setId(SERIALIZE_BUTTON_ID);

        add(grid, serializeButton, result);
    }

}
