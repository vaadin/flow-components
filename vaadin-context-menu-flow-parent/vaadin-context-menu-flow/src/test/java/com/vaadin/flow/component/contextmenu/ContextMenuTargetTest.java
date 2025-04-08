/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class ContextMenuTargetTest {
    private final UI ui = new UI();
    private final ContextMenu menu = new ContextMenu();

    @Before
    public void setup() {
        var mockSession = Mockito.mock(VaadinSession.class);
        var mockService = Mockito.mock(VaadinService.class);
        var mockContext = Mockito.mock(VaadinContext.class);
        var mockConfiguration = Mockito.mock(DeploymentConfiguration.class);

        Mockito.when(mockSession.getService()).thenReturn(mockService);
        Mockito.when(mockSession.getConfiguration())
                .thenReturn(mockConfiguration);
        Mockito.when(mockService.getContext()).thenReturn(mockContext);

        ui.getInternals().setSession(mockSession);
    }

    @Test
    public void setTarget_attachTarget_initTargetConnector() {
        var target = new Div();
        menu.setTarget(target);

        ui.add(target);

        assertTargetConnectorInit(getPendingInvocations(), target);
    }

    @Test
    public void attachTarget_setTarget_initTargetConnector() {
        var target = new Div();
        ui.add(target);

        menu.setTarget(target);

        assertTargetConnectorInit(getPendingInvocations(), target);
    }

    @Test
    public void attachTarget_detachTargetInBeforeClientResponse_cancelTargetConnectorInit() {
        var target = new Div();
        menu.setTarget(target);

        ui.add(target);
        ui.beforeClientResponse(target, context -> ui.remove(target));

        assertNoTargetConnectorInit(getPendingInvocations());
    }

    @Test
    public void clearTarget_removeTargetConnector() {
        var target = new Div();
        ui.add(target);

        menu.setTarget(target);

        assertTargetConnectorInit(getPendingInvocations(), target);

        menu.setTarget(null);

        assertTargetConnectorRemove(getPendingInvocations(), target);
    }

    @Test
    public void replaceTarget_updateTargetConnector() {
        var target = new Div();
        ui.add(target);

        menu.setTarget(target);

        assertTargetConnectorInit(getPendingInvocations(), target);

        var newTarget = new Div();
        ui.add(newTarget);

        menu.setTarget(newTarget);

        var invocations = getPendingInvocations();
        assertTargetConnectorInit(invocations, newTarget);
        assertTargetConnectorRemove(invocations, target);
    }

    private void assertTargetConnectorInit(
            List<PendingJavaScriptInvocation> invocations, Component target) {
        var initInvocations = filterTargetConnectorInitInvocations(invocations);

        Assert.assertEquals(1, initInvocations.size());

        var invocation = initInvocations.get(0);
        Assert.assertEquals(target.getElement().getNode(),
                invocation.getOwner());
        Assert.assertEquals(
                "return (async function() { window.Vaadin.Flow.contextMenuTargetConnector.init(this);this.$contextMenuTargetConnector.updateOpenOn($0);}).apply($1)",
                invocation.getInvocation().getExpression());
    }

    private void assertNoTargetConnectorInit(
            List<PendingJavaScriptInvocation> invocations) {
        var initInvocations = filterTargetConnectorInitInvocations(invocations);

        Assert.assertEquals(0, initInvocations.size());
    }

    private void assertTargetConnectorRemove(
            List<PendingJavaScriptInvocation> invocations, Component target) {
        var removeInvocations = filterTargetConnectorRemoveInvocations(
                invocations);

        Assert.assertEquals(1, removeInvocations.size());

        var invocation = removeInvocations.get(0);
        Assert.assertEquals(target.getElement().getNode(),
                invocation.getOwner());
        Assert.assertEquals(
                "return (async function() { if (this.$contextMenuTargetConnector) { this.$contextMenuTargetConnector.removeConnector() }}).apply($0)",
                invocation.getInvocation().getExpression());
    }

    private List<PendingJavaScriptInvocation> filterTargetConnectorInitInvocations(
            List<PendingJavaScriptInvocation> invocations) {
        return invocations.stream()
                .filter(invocation -> invocation.getInvocation().getExpression()
                        .contains("contextMenuTargetConnector.init"))
                .toList();
    }

    private List<PendingJavaScriptInvocation> filterTargetConnectorRemoveInvocations(
            List<PendingJavaScriptInvocation> invocations) {
        return invocations.stream()
                .filter(invocation -> invocation.getInvocation().getExpression()
                        .contains("contextMenuTargetConnector.removeConnector"))
                .toList();
    }

    protected List<PendingJavaScriptInvocation> getPendingInvocations() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
        return ui.getInternals().dumpPendingJavaScriptInvocations();
    }
}
