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

package com.vaadin.flow.component.grid;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;

public class GridEmptyStateTest {

    private Grid<String> grid;

    @Before
    public void setup() {
        grid = new Grid<>();
    }

    @Test
    public void setEmptyStateComponent_hasEmptyStateComponent() {
        var content = new Div();
        grid.setEmptyStateComponent(content);
        Assert.assertEquals(content.getElement(), getEmptyStateElement());
    }

    @Test
    public void setEmptyStateText_hasEmptyStateText() {
        var content = "empty";
        grid.setEmptyStateText(content);
        var emptyStateElement = getEmptyStateElement();
        Assert.assertEquals(content, emptyStateElement.getText());
        Assert.assertEquals("span", emptyStateElement.getTag());
    }

    @Test
    public void setEmptyStateText_wrapperHasClassName() {
        grid.setEmptyStateText("empty");
        var emptyStateElement = getEmptyStateElement();
        Assert.assertEquals("empty-state-text",
                emptyStateElement.getAttribute("class"));
    }

    @Test
    public void setEmptyStateComponent_overridesEmptyStateText() {
        grid.setEmptyStateText("empty");
        var content = new Div();
        grid.setEmptyStateComponent(content);
        Assert.assertEquals(content.getElement(), getEmptyStateElement());
    }

    @Test
    public void setEmptyStateText_overridesEmptyStateComponent() {
        var content = "empty";
        grid.setEmptyStateComponent(new Div());
        grid.setEmptyStateText(content);
        var emptyStateElement = getEmptyStateElement();
        Assert.assertEquals(content, emptyStateElement.getText());
        Assert.assertEquals("span", emptyStateElement.getTag());
    }

    @Test
    public void setEmptyStateComponentNull_noEmptyStateComponent() {
        grid.setEmptyStateText("empty");
        grid.setEmptyStateComponent(null);
        Assert.assertTrue(getEmptyStateElementOptional().isEmpty());
    }

    @Test
    public void setEmptyStateTextNull_noEmptyStateComponent() {
        grid.setEmptyStateText("empty");
        grid.setEmptyStateText(null);
        Assert.assertTrue(getEmptyStateElementOptional().isEmpty());
    }

    @Test
    public void setEmptyStateComponent_getEmptyStateComponent() {
        var content = new Div();
        grid.setEmptyStateComponent(content);
        Assert.assertEquals(content, grid.getEmptyStateComponent());
    }

    @Test
    public void setEmptyStateText_getEmptyStateText() {
        var content = "empty";
        grid.setEmptyStateText(content);
        Assert.assertEquals(content, grid.getEmptyStateText());
    }

    @Test
    public void setEmptyStateComponent_setEmptyStateText_getEmptyStateComponent() {
        grid.setEmptyStateComponent(new Div());
        grid.setEmptyStateText("empty");
        Assert.assertNull(grid.getEmptyStateComponent());
    }

    @Test
    public void setEmptyStateText_setEmptyStateComponent_getEmptyStateText() {
        grid.setEmptyStateText("empty");
        grid.setEmptyStateComponent(new Div());
        Assert.assertNull(grid.getEmptyStateText());
    }

    @Test
    public void setEmptyStateComponent_setEmptyStateTextNull_getEmptyStateComponent() {
        grid.setEmptyStateComponent(new Div());
        grid.setEmptyStateText(null);
        Assert.assertNull(grid.getEmptyStateComponent());
        Assert.assertNull(grid.getEmptyStateText());
    }

    @Test
    public void setEmptyStateText_setEmptyStateComponentNull_getEmptyStateText() {
        grid.setEmptyStateText("empty");
        grid.setEmptyStateComponent(null);
        Assert.assertNull(grid.getEmptyStateComponent());
        Assert.assertNull(grid.getEmptyStateText());
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
