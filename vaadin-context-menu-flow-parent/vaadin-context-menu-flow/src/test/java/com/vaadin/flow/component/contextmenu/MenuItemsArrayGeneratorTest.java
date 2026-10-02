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

class MenuItemsArrayGeneratorTest {
    private static final String APP_ID = "test-app";

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private ContextMenu contextMenu;

    @BeforeEach
    void setup() {
        ui.getUI().getInternals().setFullAppId(APP_ID);
        contextMenu = new ContextMenu();
        contextMenu.addItem("Item");
    }

    @Test
    void attach_generateItemsWithAppIdAndContainerNodeId() {
        ui.add(contextMenu);

        var arguments = getGenerateItemsArguments(
                ui.dumpPendingJavaScriptInvocations());
        Assertions.assertEquals(2, arguments.size());
        Assertions.assertEquals(APP_ID, arguments.get(0));
        Assertions.assertInstanceOf(Integer.class, arguments.get(1));
    }

    @Test
    void addItem_generateItemsWithAppId() {
        ui.add(contextMenu);
        ui.dumpPendingJavaScriptInvocations();

        contextMenu.addItem("Other");

        var arguments = getGenerateItemsArguments(
                ui.dumpPendingJavaScriptInvocations());
        Assertions.assertEquals(APP_ID, arguments.get(0));
    }

    private static List<Object> getGenerateItemsArguments(
            List<PendingJavaScriptInvocation> invocations) {
        var invocation = invocations.stream().filter(
                pending -> "$connector.generateItems".equals(JsFunctionCallUtil
                        .getFunctionName(pending.getInvocation())))
                .findFirst().orElseThrow();
        return JsFunctionCallUtil.getArguments(invocation.getInvocation());
    }
}
