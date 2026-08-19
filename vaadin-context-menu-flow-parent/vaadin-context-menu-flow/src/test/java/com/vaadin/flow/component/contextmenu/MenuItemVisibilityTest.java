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
import com.vaadin.tests.MockUIExtension;

class MenuItemVisibilityTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private ContextMenu contextMenu;

    @BeforeEach
    void setup() {
        contextMenu = new ContextMenu();
        ui.add(contextMenu);
    }

    @Test
    void hiddenParentItem_show_menuContentGenerated() {
        MenuItem item = contextMenu.addItem("item");
        item.getSubMenu().addItem("sub item");
        item.setVisible(false);
        flushPendingInvocations();

        item.setVisible(true);

        Assertions.assertEquals(1, getGenerateItemsInvocations().size());
    }

    @Test
    void hiddenNestedParentItem_show_menuContentGenerated() {
        MenuItem item = contextMenu.addItem("item");
        MenuItem subItem = item.getSubMenu().addItem("sub item");
        subItem.getSubMenu().addItem("sub sub item");
        subItem.setVisible(false);
        flushPendingInvocations();

        subItem.setVisible(true);

        Assertions.assertEquals(1, getGenerateItemsInvocations().size());
    }

    @Test
    void hiddenLeafItem_show_menuContentNotGenerated() {
        MenuItem item = contextMenu.addItem("item");
        item.setVisible(false);
        flushPendingInvocations();

        item.setVisible(true);

        Assertions.assertEquals(0, getGenerateItemsInvocations().size());
    }

    @Test
    void visibleParentItem_hide_menuContentNotGenerated() {
        MenuItem item = contextMenu.addItem("item");
        item.getSubMenu().addItem("sub item");
        flushPendingInvocations();

        item.setVisible(false);

        Assertions.assertEquals(0, getGenerateItemsInvocations().size());
    }

    @Test
    void visibleParentItem_showAgain_menuContentNotGenerated() {
        MenuItem item = contextMenu.addItem("item");
        item.getSubMenu().addItem("sub item");
        flushPendingInvocations();

        item.setVisible(true);

        Assertions.assertEquals(0, getGenerateItemsInvocations().size());
    }

    @Test
    void hiddenParentItem_showHideShow_menuContentGeneratedOnEachShow() {
        MenuItem item = contextMenu.addItem("item");
        item.getSubMenu().addItem("sub item");
        item.setVisible(false);
        flushPendingInvocations();

        item.setVisible(true);
        Assertions.assertEquals(1, getGenerateItemsInvocations().size());

        item.setVisible(false);
        Assertions.assertEquals(0, getGenerateItemsInvocations().size());

        item.setVisible(true);
        Assertions.assertEquals(1, getGenerateItemsInvocations().size());
    }

    @Test
    void hiddenParentItems_showAllInSameRoundTrip_menuContentGeneratedOnce() {
        MenuItem firstItem = contextMenu.addItem("first item");
        firstItem.getSubMenu().addItem("first sub item");
        MenuItem secondItem = contextMenu.addItem("second item");
        secondItem.getSubMenu().addItem("second sub item");
        firstItem.setVisible(false);
        secondItem.setVisible(false);
        flushPendingInvocations();

        firstItem.setVisible(true);
        secondItem.setVisible(true);

        Assertions.assertEquals(1, getGenerateItemsInvocations().size());
    }

    @Test
    void hiddenParentItem_showDuringBeforeClientResponse_menuContentGeneratedOnce() {
        MenuItem item = contextMenu.addItem("item");
        item.getSubMenu().addItem("sub item");
        item.setVisible(false);
        flushPendingInvocations();

        ui.getUI().beforeClientResponse(contextMenu,
                context -> item.setVisible(true));

        Assertions.assertEquals(1, getGenerateItemsInvocations().size());
    }

    private List<PendingJavaScriptInvocation> getGenerateItemsInvocations() {
        return getPendingInvocations()
                .stream().filter(invocation -> invocation.getInvocation()
                        .getExpression().contains("$connector.generateItems"))
                .toList();
    }

    private void flushPendingInvocations() {
        getPendingInvocations();
    }

    private List<PendingJavaScriptInvocation> getPendingInvocations() {
        ui.fakeClientCommunication();
        return ui.dumpPendingJavaScriptInvocations();
    }
}
