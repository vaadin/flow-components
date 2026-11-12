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

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.server.CspMode;
import com.vaadin.flow.server.CspViolationException;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.tests.MockUIExtension;

/**
 * Covers that Grid only runs declared JavaScript, so that it works with a
 * strict Content Security Policy. In the strict mode, sending JavaScript as a
 * string throws.
 */
class GridStrictCspTest {

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    @BeforeEach
    void enableStrictCsp() {
        Mockito.when(ui.getService().getDeploymentConfiguration().getCspMode())
                .thenReturn(CspMode.STRICT);
        VaadinService.setCurrent(ui.getService());
    }

    @AfterEach
    void clearService() {
        VaadinService.setCurrent(null);
    }

    @Test
    void strictCsp_commonFeatures_onlyDeclaredJavaScript() {
        Grid<String> grid = new Grid<>();
        grid.setItems("Item 0", "Item 1");
        Grid.Column<String> column = grid.addColumn(item -> item)
                .setHeader("Header").setFooter(new Span("Footer"))
                .setEditorComponent(new Span("Editor"))
                .setTooltipGenerator(item -> item);
        grid.addColumn(item -> item).setHeader(new Span("Header"))
                .setFooter("Footer").setVisible(false);
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setPageSize(10);

        ui.add(grid);
        grid.sort(GridSortOrder.asc(column).build());
        grid.scrollToEnd();

        Assertions.assertTrue(getDeclaredCalls().containsAll(List.of(
                "GridJs.initConnector", "GridJs.setColumnFlowId",
                "GridJs.resetConnector", "GridJs.setSelectionMode",
                "GridJs.setSorterDirections", "GridJs.scrollToEnd",
                "GridJs.setHeaderRenderer", "GridJs.setFooterRenderer",
                "GridJs.initTooltipGenerator", "EditorJs.patchRenderer")));
    }

    @Test
    void strictCsp_rendererColumns_onlyDeclaredTemplates() {
        Grid<String> grid = new Grid<>();
        grid.setItems("Item 0", "Item 1");
        grid.addComponentColumn(Span::new);
        grid.addColumn(new TextRenderer<>(item -> item));
        grid.addColumn(new NumberRenderer<>(String::length, "%d"));
        grid.addColumn(new LocalDateRenderer<>(item -> LocalDate.now()));
        grid.addColumn(new NativeButtonRenderer<>("Click", item -> {
        }));
        grid.setItemDetailsRenderer(
                new ComponentRenderer<Span, String>(item -> new Span(item)));

        ui.add(grid);

        List<String> calls = getDeclaredCalls();
        Assertions.assertEquals(6,
                calls.stream()
                        .filter("LitRendererJs.setLitRendererTemplate"::equals)
                        .count());
        Assertions.assertFalse(calls.contains("LitRendererJs.setLitRenderer"));
    }

    @Test
    void strictCsp_stringTemplate_throws() {
        Grid<String> grid = new Grid<>();
        grid.addColumn(LitRenderer.<String> of("${index}"));

        Assertions.assertThrows(CspViolationException.class,
                () -> ui.add(grid));
    }

    private List<String> getDeclaredCalls() {
        return ui.dumpPendingJavaScriptInvocations().stream()
                .map(PendingJavaScriptInvocation::getInvocation)
                .map(invocation -> invocation.getJsCall())
                .filter(Objects::nonNull)
                .map(call -> call.definitionType().getSimpleName() + "."
                        + call.methodName())
                .toList();
    }
}
