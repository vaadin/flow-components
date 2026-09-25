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
package com.vaadin.flow.component.contextmenu;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.tests.JsFunctionCallUtil;
import com.vaadin.tests.MockUIExtension;

class MenuItemSetCheckedTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private ContextMenu contextMenu;
    private MenuItem item;

    @BeforeEach
    void setup() {
        contextMenu = new ContextMenu();
        item = contextMenu.addItem("Item");
        item.setCheckable(true);
        ui.add(contextMenu);
        ui.dumpPendingJavaScriptInvocations();
    }

    @Test
    void setChecked_callsConnector() {
        item.setChecked(true);

        var invocations = ui.dumpPendingJavaScriptInvocations();
        Assertions.assertEquals(1, countSetCheckedInvocations(invocations));
        var parameters = getSetCheckedInvocation(invocations).getInvocation()
                .getParameters();
        Assertions.assertEquals(item.getElement(), parameters.get(0));
        Assertions.assertEquals(true, parameters.get(1));
    }

    @Test
    void setChecked_removeItem_doesNotCallConnector() {
        item.setChecked(true);
        contextMenu.remove(item);

        var invocations = ui.dumpPendingJavaScriptInvocations();
        Assertions.assertEquals(0, countSetCheckedInvocations(invocations));
    }

    @Test
    void regenerateItems_setChecked_removeItem_doesNotCallConnector() {
        contextMenu.addItem("Other");
        item.setChecked(true);
        contextMenu.remove(item);

        var invocations = ui.dumpPendingJavaScriptInvocations();
        Assertions.assertEquals(0, countSetCheckedInvocations(invocations));
    }

    @Test
    void setChecked_regenerateItems_callsConnectorBeforeGenerateItems() {
        item.setChecked(true);
        contextMenu.addItem("Other");

        var invocations = ui.dumpPendingJavaScriptInvocations();
        Assertions.assertEquals(1, countSetCheckedInvocations(invocations));
        Assertions.assertTrue(invocations
                .indexOf(getSetCheckedInvocation(invocations)) < invocations
                        .indexOf(getGenerateItemsInvocation(invocations)));
    }

    @Test
    void addItem_setChecked_callsConnectorForNewItem() {
        var newItem = contextMenu.addItem("New");
        newItem.setCheckable(true);
        newItem.setChecked(true);

        var invocations = ui.dumpPendingJavaScriptInvocations();
        Assertions.assertEquals(1, countSetCheckedInvocations(invocations));
        Assertions.assertEquals(newItem.getElement(),
                getSetCheckedInvocation(invocations).getInvocation()
                        .getParameters().get(0));
    }

    private static long countSetCheckedInvocations(
            List<PendingJavaScriptInvocation> invocations) {
        return invocations.stream()
                .filter(MenuItemSetCheckedTest::isSetCheckedInvocation).count();
    }

    private static PendingJavaScriptInvocation getSetCheckedInvocation(
            List<PendingJavaScriptInvocation> invocations) {
        return invocations.stream()
                .filter(MenuItemSetCheckedTest::isSetCheckedInvocation)
                .findFirst().orElseThrow();
    }

    private static PendingJavaScriptInvocation getGenerateItemsInvocation(
            List<PendingJavaScriptInvocation> invocations) {
        return invocations.stream()
                .filter(invocation -> "$connector.generateItems"
                        .equals(JsFunctionCallUtil
                                .getFunctionName(invocation.getInvocation())))
                .findFirst().orElseThrow();
    }

    private static boolean isSetCheckedInvocation(
            PendingJavaScriptInvocation invocation) {
        return invocation.getInvocation().getExpression()
                .contains("contextMenuConnector.setChecked");
    }
}
