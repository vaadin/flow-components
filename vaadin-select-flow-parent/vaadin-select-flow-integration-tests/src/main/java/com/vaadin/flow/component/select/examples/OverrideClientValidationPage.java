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
 *
 */

package com.vaadin.flow.component.select.examples;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-select/override-client-validation")
public class OverrideClientValidationPage extends Div {

    public static final String ID_BASIC_SELECT = "basic-select";
    public static final String ID_BASIC_SELECT_RESULT_SPAN = "basic-select-result-span";
    public static final String ID_SET_INVALID_BUTTON = "set-invalid-button";
    public static final String ID_LOG_BUTTON = "log-button";
    public static final String ID_DETACH_BUTTON = "detach-button";
    public static final String ID_REATTACH_BUTTON = "reattach-button";

    private Select<String> basicSelect;
    private Span basicSelectResultSpan;
    private Select<String> selectInGrid;

    public OverrideClientValidationPage() {
        createBasicSetup();
        createGridSetup();
        createActions();
    }

    private void createBasicSetup() {
        basicSelect = new Select<>("a", "b", "c");
        basicSelect.setId(ID_BASIC_SELECT);

        basicSelectResultSpan = new Span();
        basicSelectResultSpan.setId(ID_BASIC_SELECT_RESULT_SPAN);

        add(new H1("Basic select usage"), basicSelect, basicSelectResultSpan);
    }

    private void createGridSetup() {
        selectInGrid = new Select<>();
        Grid<String> grid = new Grid<>();
        grid.setItems("test");
        grid.addColumn(new ComponentRenderer<>(item -> selectInGrid,
                (component, item) -> component));
        add(new H1("Grid select usage"), grid);
    }

    private void createActions() {
        NativeButton setInvalidButton = new NativeButton("Set all invalid",
                e -> {
                    basicSelect.setInvalid(true);
                    selectInGrid.setInvalid(true);
                });
        setInvalidButton.setId(ID_SET_INVALID_BUTTON);

        NativeButton logButton = new NativeButton("Log validation state",
                e -> basicSelectResultSpan.setText(
                        basicSelect.isInvalid() ? "invalid" : "valid"));
        logButton.setId(ID_LOG_BUTTON);

        NativeButton detachButton = new NativeButton("Detach select",
                e -> this.remove(basicSelect));
        detachButton.setId(ID_DETACH_BUTTON);

        NativeButton reattachButton = new NativeButton("Reattach select",
                e -> this.addComponentAsFirst(basicSelect));
        reattachButton.setId(ID_REATTACH_BUTTON);

        addComponentAsFirst(new Div(setInvalidButton, logButton, detachButton,
                reattachButton));
    }
}
