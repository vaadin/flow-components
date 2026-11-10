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

        var call = getGenerateItemsCall();
        Assertions.assertEquals(3, call.size());
        Assertions.assertEquals(APP_ID, call.get(1));
        Assertions.assertInstanceOf(Integer.class, call.get(2));
    }

    @Test
    void addItem_generateItemsWithAppId() {
        ui.add(contextMenu);
        ui.dumpPendingJavaScriptInvocations();

        contextMenu.addItem("Other");

        var call = getGenerateItemsCall();
        Assertions.assertEquals(APP_ID, call.get(1));
    }

    /**
     * Gets the first call that generates the items: the function name, the app
     * id and the node id of the container.
     */
    private List<Object> getGenerateItemsCall() {
        return ui
                .dumpJsCalls(contextMenu.getElement(),
                        "$connector.generateItems")
                .getFirst().flattenArguments();
    }
}
