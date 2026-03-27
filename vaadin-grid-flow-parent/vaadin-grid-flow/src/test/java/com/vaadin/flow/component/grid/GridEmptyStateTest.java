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

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;

class GridEmptyStateTest {

    private Grid<String> grid;

    @BeforeEach
    void setup() {
        grid = new Grid<>();
    }

    @Test
    void setEmptyStateComponent_hasEmptyStateComponent() {
        var content = new Div();
        grid.setEmptyStateComponent(content);
        Assertions.assertEquals(content.getElement(), getEmptyStateElement());
    }

    @Test
    void setEmptyStateText_hasEmptyStateText() {
        var content = "empty";
        grid.setEmptyStateText(content);
        var emptyStateElement = getEmptyStateElement();
        Assertions.assertEquals(content, emptyStateElement.getText());
        Assertions.assertEquals("span", emptyStateElement.getTag());
    }

    @Test
    void setEmptyStateComponent_overridesEmptyStateText() {
        grid.setEmptyStateText("empty");
        var content = new Div();
        grid.setEmptyStateComponent(content);
        Assertions.assertEquals(content.getElement(), getEmptyStateElement());
    }

    @Test
    void setEmptyStateText_overridesEmptyStateComponent() {
        var content = "empty";
        grid.setEmptyStateComponent(new Div());
        grid.setEmptyStateText(content);
        var emptyStateElement = getEmptyStateElement();
        Assertions.assertEquals(content, emptyStateElement.getText());
        Assertions.assertEquals("span", emptyStateElement.getTag());
    }

    @Test
    void setEmptyStateComponentNull_noEmptyStateComponent() {
        grid.setEmptyStateText("empty");
        grid.setEmptyStateComponent(null);
        Assertions.assertTrue(getEmptyStateElementOptional().isEmpty());
    }

    @Test
    void setEmptyStateTextNull_noEmptyStateComponent() {
        grid.setEmptyStateText("empty");
        grid.setEmptyStateText(null);
        Assertions.assertTrue(getEmptyStateElementOptional().isEmpty());
    }

    @Test
    void setEmptyStateComponent_getEmptyStateComponent() {
        var content = new Div();
        grid.setEmptyStateComponent(content);
        Assertions.assertEquals(content, grid.getEmptyStateComponent());
    }

    @Test
    void setEmptyStateText_getEmptyStateText() {
        var content = "empty";
        grid.setEmptyStateText(content);
        Assertions.assertEquals(content, grid.getEmptyStateText());
    }

    @Test
    void setEmptyStateComponent_setEmptyStateText_getEmptyStateComponent() {
        grid.setEmptyStateComponent(new Div());
        grid.setEmptyStateText("empty");
        Assertions.assertNull(grid.getEmptyStateComponent());
    }

    @Test
    void setEmptyStateText_setEmptyStateComponent_getEmptyStateText() {
        grid.setEmptyStateText("empty");
        grid.setEmptyStateComponent(new Div());
        Assertions.assertNull(grid.getEmptyStateText());
    }

    @Test
    void setEmptyStateComponent_setEmptyStateTextNull_getEmptyStateComponent() {
        grid.setEmptyStateComponent(new Div());
        grid.setEmptyStateText(null);
        Assertions.assertNull(grid.getEmptyStateComponent());
        Assertions.assertNull(grid.getEmptyStateText());
    }

    @Test
    void setEmptyStateText_setEmptyStateComponentNull_getEmptyStateText() {
        grid.setEmptyStateText("empty");
        grid.setEmptyStateComponent(null);
        Assertions.assertNull(grid.getEmptyStateComponent());
        Assertions.assertNull(grid.getEmptyStateText());
    }

    private Element getEmptyStateElement() {
        return getEmptyStateElementOptional().orElse(null);
    }

    private Optional<Element> getEmptyStateElementOptional() {
        return grid.getElement().getChildren().filter(
                child -> child.getAttribute("slot").equals("empty-state"))
                .findFirst();
    }
}
