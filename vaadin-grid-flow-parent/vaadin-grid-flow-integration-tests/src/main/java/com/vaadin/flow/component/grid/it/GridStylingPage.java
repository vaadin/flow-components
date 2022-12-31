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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-styling")
public class GridStylingPage extends Div {

    public GridStylingPage() {
        Grid<String> grid = new Grid<>();

        List<String> items = IntStream.range(0, 20).mapToObj(String::valueOf)
                .collect(Collectors.toList());
        grid.setItems(items);

        Grid.Column<String> col0 = grid.addColumn(i -> i).setHeader("text");
        Grid.Column<String> col1 = grid
                .addColumn(i -> String.valueOf(i.length())).setHeader("length");

        // Class name generator
        NativeButton gridClassNameGenerator = new NativeButton(
                "grid class name generator", e -> {
                    grid.setClassNameGenerator(item -> "grid" + item);
                });
        gridClassNameGenerator.setId("grid-generator");

        NativeButton columnClassNameGenerator = new NativeButton(
                "column class name generator", e -> {
                    col0.setClassNameGenerator(item -> "col" + item);
                });
        columnClassNameGenerator.setId("column-generator");

        NativeButton resetGridClassNameGenerator = new NativeButton(
                "grid: reset generator", e -> {
                    grid.setClassNameGenerator(item -> null);
                });
        resetGridClassNameGenerator.setId("reset-grid-generator");

        NativeButton resetColumnClassNameGenerator = new NativeButton(
                "column: reset generator", e -> {
                    col0.setClassNameGenerator(item -> null);
                });
        resetColumnClassNameGenerator.setId("reset-column-generator");

        NativeButton gridMultipleClasses = new NativeButton(
                "grid: generate multiple classes", e -> {
                    grid.setClassNameGenerator(item -> "grid foo");
                });
        gridMultipleClasses.setId("grid-multiple-classes");

        NativeButton columnMultipleClasses = new NativeButton(
                "column: generate multiple classes", e -> {
                    col0.setClassNameGenerator(item -> "col bar");
                });
        columnMultipleClasses.setId("column-multiple-classes");

        NativeButton secondColumnClassNameGenerator = new NativeButton(
                "second column class name generator", e -> {
                    col1.setClassNameGenerator(item -> "baz");
                });
        secondColumnClassNameGenerator.setId("second-column-generator");

        // Part name generator
        NativeButton gridPartNameGenerator = new NativeButton(
                "grid part name generator", e -> {
                    grid.setPartNameGenerator(item -> "grid" + item);
                });
        gridPartNameGenerator.setId("grid-part-generator");

        NativeButton columnPartNameGenerator = new NativeButton(
                "column part name generator", e -> {
                    col0.setPartNameGenerator(item -> "col" + item);
                });
        columnPartNameGenerator.setId("column-part-generator");

        NativeButton resetGridPartNameGenerator = new NativeButton(
                "grid: reset part generator", e -> {
                    grid.setPartNameGenerator(item -> null);
                });
        resetGridPartNameGenerator.setId("reset-grid-part-generator");

        NativeButton resetColumnPartNameGenerator = new NativeButton(
                "column: reset part generator", e -> {
                    col0.setPartNameGenerator(item -> null);
                });
        resetColumnPartNameGenerator.setId("reset-column-part-generator");

        NativeButton gridMultipleParts = new NativeButton(
                "grid: generate multiple parts", e -> {
                    grid.setPartNameGenerator(item -> "grid foo");
                });
        gridMultipleParts.setId("grid-multiple-parts");

        NativeButton columnMultipleParts = new NativeButton(
                "column: generate multiple parts", e -> {
                    col0.setPartNameGenerator(item -> "col bar");
                });
        columnMultipleParts.setId("column-multiple-parts");

        NativeButton secondColumnPartNameGenerator = new NativeButton(
                "second column part name generator", e -> {
                    col1.setPartNameGenerator(item -> "baz");
                });
        secondColumnPartNameGenerator.setId("second-column-part-generator");

        NativeButton toggleAttached = new NativeButton("detach/attach grid",
                e -> {
                    if (grid.getParent().isPresent()) {
                        remove(grid);
                    } else {
                        add(grid);
                    }
                });
        toggleAttached.setId("toggle-attached");

        NativeButton setDetailsRenderer = new NativeButton(
                "set item details renderer", e -> {
                    grid.setItemDetailsRenderer(
                            new TextRenderer<>(item -> "details " + item));
                });
        setDetailsRenderer.setId("details-renderer");

        add(grid,
                new Div(gridClassNameGenerator, columnClassNameGenerator,
                        secondColumnClassNameGenerator),
                new Div(resetGridClassNameGenerator,
                        resetColumnClassNameGenerator),
                new Div(gridMultipleClasses, columnMultipleClasses),
                new Div(gridPartNameGenerator, columnPartNameGenerator,
                        secondColumnPartNameGenerator),
                new Div(resetGridPartNameGenerator,
                        resetColumnPartNameGenerator),
                new Div(gridMultipleParts, columnMultipleParts),
                new Div(toggleAttached), //
                new Div(setDetailsRenderer));
    }

}
