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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/vaadin-button-inside-grid")
public class ButtonInGridPage extends Div {

    public ButtonInGridPage() {
        Grid<String> grid = new Grid<>();
        grid.setId("grid");

        Div div = new Div();
        div.setId("info");

        grid.addComponentColumn(item -> new Button("Show item", evt -> {
            div.setText(item);
        })).setHeader("Click to see an item");

        grid.setItems("foo", "bar");
        add(grid, div);
    }
}
