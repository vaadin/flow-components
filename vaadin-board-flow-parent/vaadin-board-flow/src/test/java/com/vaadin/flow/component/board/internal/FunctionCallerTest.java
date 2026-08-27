/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.board.internal;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals;
import com.vaadin.flow.server.VaadinSession;

public class FunctionCallerTest {

    @Test
    void callsFunctionBeforeAttach_invokedOnce() throws Exception {
        Html html = new Html("<div>foo</div>");
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        FunctionCaller.callOnceOnClientReponse(html, "foo");

        UI ui = createUI();
        ui.add(html);

        assertPendingInvocations(ui, "return $0.foo()");
    }

    @Test
    void callsFunctionAfterAttach_invokedOnce() throws Exception {
        Html html = new Html("<div>foo</div>");
        UI ui = createUI();
        ui.add(html);
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        FunctionCaller.callOnceOnClientReponse(html, "foo");

        assertPendingInvocations(ui, "return $0.foo()");
    }

    @Test
    void callsFunctionBeforeAndAfterAttach_invokedOnce() throws Exception {

        Html html = new Html("<div>foo</div>");
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        UI ui = createUI();
        ui.add(html);
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        FunctionCaller.callOnceOnClientReponse(html, "foo");

        assertPendingInvocations(ui, "return $0.foo()");
    }

    @Test
    void trackingPropertyRemoved() throws Exception {
        Html html = new Html("<div>foo</div>");
        FunctionCaller.callOnceOnClientReponse(html, "foo");
        UI ui = createUI();
        ui.add(html);

        String trackingProperty = "CALLONCE_foo";
        Assertions.assertTrue(html.getElement().hasProperty(trackingProperty));
        assertPendingInvocations(ui, "return $0.foo()");
        Assertions.assertFalse(html.getElement().hasProperty(trackingProperty));
    }

    /**
     * Creates a UI with a mocked session. Scheduling a JavaScript invocation on
     * an attached element requires the owning UI to have a session.
     *
     * @return the UI
     */
    public static UI createUI() {
        UI ui = new UI();
        ui.getInternals().setSession(Mockito.mock(VaadinSession.class));
        return ui;
    }

    public static void assertPendingInvocations(UI ui, String expectedJS)
            throws Exception {
        UIInternals internals = ui.getInternals();
        internals.getStateTree().runExecutionsBeforeClientResponse();
        Method method = UIInternals.class
                .getDeclaredMethod("getPendingJavaScriptInvocations");
        method.setAccessible(true);
        Stream<PendingJavaScriptInvocation> pendingJS = (Stream<PendingJavaScriptInvocation>) method
                .invoke(internals);
        List<PendingJavaScriptInvocation> invocations = pendingJS.toList();
        Assertions.assertEquals(1, invocations.size());
        Assertions.assertEquals(expectedJS,
                invocations.get(0).getInvocation().getExpression());

    }
}
