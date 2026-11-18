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
package com.vaadin.flow.component.treegrid;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.js.JsCall;
import com.vaadin.tests.MockUIExtension;

class TreeGridHierarchyColumnTest {

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    private TreeGrid<String> treeGrid;

    @BeforeEach
    void setup() {
        treeGrid = new TreeGrid<>();
        var treeData = new TreeData<String>();
        treeData.addRootItems("Item 0");
        treeData.addItems("Item 0", "Item 0-0");
        treeGrid.setDataProvider(new TreeDataProvider<>(treeData));
    }

    @Test
    void addComponentHierarchyColumn_rendersWithDeclaredTemplate() {
        Column<String> column = treeGrid.addComponentHierarchyColumn(Span::new);
        ui.add(treeGrid);

        assertRendersWithDeclaredTemplate(column);
    }

    @Test
    void addHierarchyColumn_rendersWithDeclaredTemplate() {
        Column<String> column = treeGrid.addHierarchyColumn(item -> item);
        ui.add(treeGrid);

        assertRendersWithDeclaredTemplate(column);
    }

    private void assertRendersWithDeclaredTemplate(Column<String> column) {
        List<String> calls = ui.dumpPendingJavaScriptInvocations().stream()
                .filter(pending -> pending.getOwner() == column.getElement()
                        .getNode())
                .map(pending -> pending.getInvocation().getJsCall())
                .filter(Objects::nonNull).map(JsCall::methodName).toList();
        Assertions.assertTrue(calls.contains("setCompiledLitRenderer"),
                "The column should render with a declared template: " + calls);
        Assertions.assertFalse(calls.contains("setLitRenderer"),
                "The column should not render with a string template: "
                        + calls);
    }
}
