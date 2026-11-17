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
package com.vaadin.flow.component.grid;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.tests.MockUIExtension;

class GridItemDetailsDataTest {

    @RegisterExtension
    private MockUIExtension ui = new MockUIExtension();

    private Grid<String> grid;
    private List<String> detailsItems = new ArrayList<>();

    @BeforeEach
    void setup() {
        grid = new Grid<>();
        grid.setItems("Item 0", "Item 1", "Item 2");
        grid.addColumn(item -> item);
    }

    @Test
    void litRenderer_detailsDataGeneratedOnlyForVisibleDetails() {
        grid.setItemDetailsRenderer(LitRenderer.<String> of("${item.details}")
                .withProperty("details", item -> {
                    detailsItems.add(item);
                    return "Details of " + item;
                }));

        assertDetailsDataGeneratedOnlyForVisibleDetails();
    }

    @Test
    void componentRenderer_detailsDataGeneratedOnlyForVisibleDetails() {
        grid.setItemDetailsRenderer(new ComponentRenderer<>(item -> {
            detailsItems.add(item);
            return new Span("Details of " + item);
        }));

        assertDetailsDataGeneratedOnlyForVisibleDetails();
    }

    private void assertDetailsDataGeneratedOnlyForVisibleDetails() {
        ui.add(grid);
        ui.fakeClientCommunication();
        Assertions.assertEquals(List.of(), detailsItems);

        grid.setDetailsVisible("Item 0", true);
        ui.fakeClientCommunication();
        Assertions.assertEquals(List.of("Item 0"), detailsItems);
    }
}
