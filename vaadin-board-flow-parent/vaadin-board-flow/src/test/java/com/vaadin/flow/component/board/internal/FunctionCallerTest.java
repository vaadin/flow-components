/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.board.internal;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals;
import com.vaadin.flow.server.VaadinSession;

public class FunctionCallerTest {

    @Test
    public void callsFunctionBeforeAttach_invokedOnce() throws Exception {
        Html html = new Html("<div>foo</div>");
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        FunctionCaller.callOnceOnClientReponse(html, "foo");

        UI ui = new UI();
        ui.add(html);

        assertPendingInvocations(ui, "return $0.foo()");
    }

    @Test
    public void callsFunctionAfterAttach_invokedOnce() throws Exception {
        Html html = new Html("<div>foo</div>");
        UI ui = new UI();
        ui.add(html);
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        FunctionCaller.callOnceOnClientReponse(html, "foo");

        assertPendingInvocations(ui, "return $0.foo()");
    }

    @Test
    public void callsFunctionBeforeAndAfterAttach_invokedOnce()
            throws Exception {

        Html html = new Html("<div>foo</div>");
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        UI ui = new UI();
        ui.add(html);
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        FunctionCaller.callOnceOnClientReponse(html, "foo");

        assertPendingInvocations(ui, "return $0.foo()");
    }

    @Test
    public void trackingPropertyRemoved() throws Exception {
        Html html = new Html("<div>foo</div>");
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        UI ui = new UI();
        ui.add(html);

        String trackingProperty = "CALLONCE_foo";
        Assert.assertTrue(html.getElement().hasProperty(trackingProperty));
        assertPendingInvocations(ui, "return $0.foo()");
        Assert.assertFalse(html.getElement().hasProperty(trackingProperty));
    }

    public static void assertPendingInvocations(UI ui, String expectedJS)
            throws Exception {
        UIInternals internals = ui.getInternals();
        VaadinSession session = Mockito.mock(VaadinSession.class);
        internals.setSession(session);
        internals.getStateTree().runExecutionsBeforeClientResponse();
        Method method = UIInternals.class
                .getDeclaredMethod("getPendingJavaScriptInvocations");
        method.setAccessible(true);
        Stream<PendingJavaScriptInvocation> pendingJS = (Stream<PendingJavaScriptInvocation>) method
                .invoke(internals);
        List<PendingJavaScriptInvocation> invocations = pendingJS
                .collect(Collectors.toList());
        Assert.assertEquals(1, invocations.size());
        Assert.assertEquals(expectedJS,
                invocations.get(0).getInvocation().getExpression());

    }
}
