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
import com.vaadin.flow.component.shared.Tooltip.TooltipPosition;
import com.vaadin.tests.MockUIExtension;

class MenuItemTooltipTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private ContextMenu contextMenu;
    private MenuItem item;

    @BeforeEach
    void setup() {
        contextMenu = new ContextMenu();
        item = contextMenu.addItem("item");
        ui.add(contextMenu);
    }

    @Test
    void setTooltipText_tooltipSynced() {
        flushPendingInvocations();

        item.setTooltipText("Tooltip");

        List<PendingJavaScriptInvocation> invocations = getSetTooltipInvocations();
        Assertions.assertEquals(1, invocations.size());
        Assertions.assertEquals("Tooltip", getParameter(invocations.get(0), 0));
    }

    @Test
    void hiddenItem_setTooltipText_syncDeferredUntilShown() {
        item.setVisible(false);
        flushPendingInvocations();

        item.setTooltipText("Tooltip");

        Assertions.assertEquals(0, getSetTooltipInvocations().size(),
                "Expected the sync to be held back while the item is hidden");

        item.setVisible(true);

        List<PendingJavaScriptInvocation> invocations = getSetTooltipInvocations();
        Assertions.assertEquals(1, invocations.size());
        Assertions.assertEquals("Tooltip", getParameter(invocations.get(0), 0));
    }

    @Test
    void hiddenItemWithTooltip_show_tooltipSynced() {
        MenuItem hiddenItem = contextMenu.addItem("hidden item");
        hiddenItem.setTooltipText("Tooltip");
        hiddenItem.setVisible(false);
        flushPendingInvocations();

        hiddenItem.setVisible(true);

        List<PendingJavaScriptInvocation> invocations = getSetTooltipInvocations();
        Assertions.assertEquals(1, invocations.size());
        Assertions.assertEquals("Tooltip", getParameter(invocations.get(0), 0));
    }

    @Test
    void hiddenItemWithTooltip_menuContentRegenerated_show_tooltipSynced() {
        MenuItem hiddenItem = contextMenu.addItem("hidden item");
        hiddenItem.setTooltipText("Tooltip");
        hiddenItem.setVisible(false);
        flushPendingInvocations();

        // Regenerating the content re-attaches every item element
        contextMenu.addItem("another item");
        flushPendingInvocations();

        hiddenItem.setVisible(true);

        List<PendingJavaScriptInvocation> invocations = getSetTooltipInvocations();
        Assertions.assertEquals(1, invocations.size());
        Assertions.assertEquals("Tooltip", getParameter(invocations.get(0), 0));
    }

    @Test
    void hiddenItemWithTooltip_menuDetachedAndAttached_show_tooltipSynced() {
        MenuItem hiddenItem = contextMenu.addItem("hidden item");
        hiddenItem.setTooltipText("Tooltip");
        hiddenItem.setVisible(false);
        flushPendingInvocations();

        ui.remove(contextMenu);
        ui.add(contextMenu);
        flushPendingInvocations();

        hiddenItem.setVisible(true);

        List<PendingJavaScriptInvocation> invocations = getSetTooltipInvocations();
        Assertions.assertEquals(1, invocations.size());
        Assertions.assertEquals("Tooltip", getParameter(invocations.get(0), 0));
    }

    @Test
    void setTooltipTextTwice_onlyLatestSynced() {
        flushPendingInvocations();

        item.setTooltipText("First");
        item.setTooltipText("Second");

        List<PendingJavaScriptInvocation> invocations = getSetTooltipInvocations();
        Assertions.assertEquals(1, invocations.size());
        Assertions.assertEquals("Second", getParameter(invocations.get(0), 0));
    }

    @Test
    void setTooltipPosition_positionSynced() {
        flushPendingInvocations();

        item.setTooltipPosition(TooltipPosition.END);

        List<PendingJavaScriptInvocation> invocations = getSetTooltipInvocations();
        Assertions.assertEquals(1, invocations.size());
        Assertions.assertEquals(TooltipPosition.END.getPosition(),
                getParameter(invocations.get(0), 1));
    }

    @Test
    void noTooltip_nothingSynced() {
        flushPendingInvocations();

        item.setText("Other text");

        Assertions.assertEquals(0, getSetTooltipInvocations().size());
    }

    private Object getParameter(PendingJavaScriptInvocation invocation,
            int index) {
        return invocation.getInvocation().getParameters().get(index);
    }

    private List<PendingJavaScriptInvocation> getSetTooltipInvocations() {
        return getPendingInvocations().stream()
                .filter(invocation -> invocation.getInvocation().getExpression()
                        .contains("contextMenuConnector.setTooltip"))
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
