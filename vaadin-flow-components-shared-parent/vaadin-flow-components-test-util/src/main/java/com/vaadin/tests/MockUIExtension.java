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
package com.vaadin.tests;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation;
import com.vaadin.flow.component.internal.UIInternals.JavaScriptInvocation;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.js.JsCall;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.CspMode;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceEventBus;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

/**
 * JUnit extension to set up a UI and VaadinSession for testing.
 * <p>
 * Usage:
 *
 * <pre>
 * &#64;RegisterExtension
 * MockUIExtension ui = new MockUIExtension();
 * </pre>
 * <p>
 * In a module whose <code>csp.mode</code> Maven property is
 * <code>strict</code>, the UI is set up with {@link CspMode#STRICT}, see
 * {@link #setCspMode(CspMode)}. The mode reaches the tests as the
 * {@value #CSP_MODE_PROPERTY} system property.
 */
public class MockUIExtension implements BeforeEachCallback, AfterEachCallback {

    /**
     * The system property with the Content Security Policy mode that every UI
     * is set up with, <code>off</code> or <code>strict</code>. Set from the
     * <code>csp.mode</code> Maven property of the module.
     */
    public static final String CSP_MODE_PROPERTY = "vaadin.test.csp";

    // Keep the session and UI as fields to ensure they are not garbage
    // collected during the test
    private UI ui;
    private VaadinSession session;
    private VaadinService service;
    private VaadinContext vaadinContext;
    private boolean serviceSetAsCurrent;

    @Override
    public void beforeEach(ExtensionContext context) {
        setUp();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        tearDown();
    }

    /**
     * Sets up a UI for testing. Only use this method directly in contexts where
     * registering the extension is not possible, for example when using
     * {@link org.junit.jupiter.api.DynamicTest}.
     */
    @SuppressWarnings("checkstyle:UiSetCurrentCheck")
    public void setUp() {
        service = Mockito.mock(VaadinService.class);
        DeploymentConfiguration deploymentConfig = Mockito
                .mock(DeploymentConfiguration.class);
        Mockito.when(deploymentConfig.isProductionMode()).thenReturn(false);
        Mockito.when(service.getDeploymentConfiguration())
                .thenReturn(deploymentConfig);
        vaadinContext = Mockito.mock(VaadinContext.class);
        var applicationConfiguration = Mockito
                .mock(ApplicationConfiguration.class);
        // Derive from the deployment configuration so a test that re-stubs
        // production mode there gets a consistent answer on both lookup
        // paths.
        Mockito.when(applicationConfiguration.isProductionMode())
                .thenAnswer(query -> deploymentConfig.isProductionMode());
        // Mockito does not run real default methods on mocks, so the
        // delegating one-arg getAttribute(Class) overload must be stubbed
        // separately from the two-arg one that
        // ApplicationConfiguration.get(context) calls. An unstubbed overload
        // returns null and every caller resolving the configuration (Binder
        // validation, session serialization) fails with a
        // NullPointerException.
        Mockito.when(vaadinContext.getAttribute(
                Mockito.eq(ApplicationConfiguration.class), Mockito.any()))
                .thenReturn(applicationConfiguration);
        Mockito.when(vaadinContext.getAttribute(ApplicationConfiguration.class))
                .thenReturn(applicationConfiguration);
        Mockito.when(service.getContext()).thenReturn(vaadinContext);
        var eventBus = new VaadinServiceEventBus(service);
        Mockito.when(service.getEventBus()).thenReturn(eventBus);

        session = Mockito.spy(new AlwaysLockedVaadinSession(service));

        ui = new UI();
        ui.getInternals().setSession(session);

        UI.setCurrent(ui);
        VaadinSession.setCurrent(session);

        if ("strict".equalsIgnoreCase(System.getProperty(CSP_MODE_PROPERTY))) {
            setCspMode(CspMode.STRICT);
        }
    }

    /**
     * Tears down a UI that was previously set up. Only use this method directly
     * in contexts where registering the extension is not possible, for example
     * when using {@link org.junit.jupiter.api.DynamicTest}.
     */
    @SuppressWarnings("checkstyle:UiSetCurrentCheck")
    public void tearDown() {
        removeAll();
        UI.setCurrent(null);
        VaadinSession.setCurrent(null);
        if (serviceSetAsCurrent) {
            VaadinService.setCurrent(null);
            serviceSetAsCurrent = false;
        }
    }

    /**
     * Sets the Content Security Policy mode that the mocked
     * {@link VaadinService} reports, and makes it the current service, which is
     * where the mode is read from.
     * <p>
     * With {@link CspMode#STRICT}, every API that sends JavaScript to the
     * browser as a string, such as
     * {@link Element#executeJs(String, Object...)}, throws a
     * {@link com.vaadin.flow.server.CspViolationException}. A test can use this
     * to check that a component only runs declared JavaScript, see
     * {@link Element#executeJs(Class)}. The current service is cleared again
     * when the UI is torn down.
     *
     * @param mode
     *            the mode to use, not {@code null}
     */
    public void setCspMode(CspMode mode) {
        Objects.requireNonNull(mode, "CSP mode cannot be null");
        Mockito.when(service.getDeploymentConfiguration().getCspMode())
                .thenReturn(mode);
        VaadinService.setCurrent(service);
        serviceSetAsCurrent = true;
    }

    /**
     * Get the UI instance that is set up by this extension.
     *
     * @return the UI instance
     */
    public UI getUI() {
        return ui;
    }

    /**
     * Clears the current the UI.
     */
    public void clearUI() {
        tearDown();
    }

    /**
     * Replaces the current UI with a new instance.
     */
    public void replaceUI() {
        tearDown();
        beforeEach(null);
    }

    /**
     * Get the VaadinSession instance that is set up by this extension.
     * <p>
     * Note that this returns a Mockito spy, so it supports stubbing and
     * verification.
     *
     * @return the VaadinSession instance
     */
    public VaadinSession getSession() {
        return session;
    }

    /**
     * Get the VaadinService instance that is set up by this extension.
     * <p>
     * Note that this returns a Mockito mock, so it supports stubbing and
     * verification.
     *
     * @return the VaadinService instance
     */
    public VaadinService getService() {
        return service;
    }

    /**
     * Get the VaadinContext instance that the mocked {@link VaadinService}
     * returns.
     * <p>
     * Note that this returns a Mockito mock, so it supports stubbing and
     * verification. An {@link ApplicationConfiguration} attribute is stubbed on
     * it already.
     *
     * @return the VaadinContext instance
     */
    public VaadinContext getContext() {
        return vaadinContext;
    }

    /**
     * Add a component to the UI. Delegates to {@link UI#add(Component...)} for
     * convenience.
     *
     * @param component
     *            the component to add
     */
    public void add(Component component) {
        ui.add(component);
    }

    /**
     * Remove a component from the UI. Delegates to
     * {@link UI#remove(Component...)} for convenience.
     *
     * @param component
     *            the component to remove
     */
    public void remove(Component component) {
        ui.remove(component);
    }

    /**
     * Remove all components from the UI. Delegates to {@link UI#removeAll()}
     * for convenience.
     */
    public void removeAll() {
        ui.removeAll();
    }

    /**
     * Set the locale of the UI. Delegates to {@link UI#setLocale(Locale)} for
     * convenience.
     *
     * @param locale
     *            the locale to set
     */
    public void setLocale(Locale locale) {
        ui.setLocale(locale);
    }

    /**
     * Simulate client communication by running pending executions and
     * collecting changes.
     */
    public void fakeClientCommunication() {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    /**
     * Dump the pending JavaScript invocations from the UI internals.
     *
     * @return a list of pending JavaScript invocations
     */
    public List<PendingJavaScriptInvocation> dumpPendingJavaScriptInvocations() {
        this.fakeClientCommunication();
        return ui.getInternals().dumpPendingJavaScriptInvocations();
    }

    /**
     * Dump the pending JavaScript invocations from the UI internals, and get
     * the calls among them that were made on the given element through the
     * given JavaScript definition with {@link Element#executeJs(Class)
     * executeJs(Class)}.
     * <p>
     * Like {@link #dumpPendingJavaScriptInvocations()}, this clears all pending
     * invocations, including those that are not returned.
     *
     * @param owner
     *            the element that the calls were made on, use the UI element
     *            for calls made on the UI
     * @param definitionType
     *            the JavaScript definition interface of the calls
     * @return a list of the matching calls, in the order they were made
     */
    public List<JsCall> dumpJsCalls(Element owner, Class<?> definitionType) {
        return dumpPendingJavaScriptInvocations().stream()
                .filter(pending -> pending.getOwner() == owner.getNode())
                .map(PendingJavaScriptInvocation::getInvocation)
                .map(JavaScriptInvocation::getJsCall).filter(Objects::nonNull)
                .filter(call -> call.definitionType() == definitionType)
                .toList();
    }

    /**
     * Dump the pending JavaScript invocations from the UI internals, and get
     * the calls among them that were made on the given element with
     * {@link Element#callJsFunction(String, Object...) callJsFunction} for the
     * given function.
     * <p>
     * {@link JsCall#flattenArguments()} of a returned call is the function name
     * followed by the arguments of the function. To get the calls of all
     * functions, use {@link #dumpJsCalls(Element, Class)} with
     * {@link Element.CallFunctionJs}.
     * <p>
     * Like {@link #dumpPendingJavaScriptInvocations()}, this clears all pending
     * invocations, including those that are not returned.
     *
     * @param owner
     *            the element that the function was called on
     * @param functionName
     *            the name of the called function
     * @return a list of the matching calls, in the order they were made
     */
    public List<JsCall> dumpJsCalls(Element owner, String functionName) {
        return dumpJsCalls(owner, Element.CallFunctionJs.class).stream()
                .filter(call -> functionName.equals(call.arguments().get(0)))
                .toList();
    }

    private static class AlwaysLockedVaadinSession extends VaadinSession {
        private final ReentrantLock lock = new ReentrantLock();

        private AlwaysLockedVaadinSession(VaadinService service) {
            super(service);
            lock();
        }

        @Override
        public Lock getLockInstance() {
            return lock;
        }

        @Override
        public Future<Void> access(Command command) {
            command.execute();
            return CompletableFuture.completedFuture(null);
        }
    }
}
