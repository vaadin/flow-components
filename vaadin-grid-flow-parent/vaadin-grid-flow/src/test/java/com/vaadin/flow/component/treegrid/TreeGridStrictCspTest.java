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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.server.CspMode;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.tests.MockUIExtension;

/**
 * Covers that TreeGrid only runs declared JavaScript, so that it works with a
 * strict Content Security Policy. In the strict mode, sending JavaScript as a
 * string throws.
 * <p>
 * The hierarchy column is not covered yet, as its template is still sent as a
 * string.
 */
class TreeGridStrictCspTest {

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
    void strictCsp_expandAndScroll_onlyDeclaredJavaScript() {
        TreeGrid<String> treeGrid = new TreeGrid<>();
        treeGrid.setItems(List.of("Parent"),
                item -> item.equals("Parent") ? List.of("Child") : List.of());
        treeGrid.addColumn(item -> item).setHeader("Header");

        ui.add(treeGrid);
        treeGrid.expand("Parent");
        treeGrid.scrollToEnd();

        Assertions.assertTrue(
                getDeclaredCalls().contains("TreeGridJs.initConnector"));
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
