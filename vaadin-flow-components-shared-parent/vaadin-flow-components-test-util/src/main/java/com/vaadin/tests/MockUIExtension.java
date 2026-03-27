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
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

/**
 * JUnit extension to set up a UI and VaadinSession for testing.
 * <p>
 * Usage:
 *
 * <pre>
 * &#64;RegisterExtension
 * MockUIExtension ui = new MockUIExtension();
 * </pre>
 */
public class MockUIExtension implements BeforeEachCallback, AfterEachCallback {

    // Keep the session and UI as fields to ensure they are not garbage
    // collected during the test
    private UI ui;
    private VaadinSession session;
    private VaadinService service;

    @Override
    @SuppressWarnings("checkstyle:UiSetCurrentCheck")
    public void beforeEach(ExtensionContext context) {
        service = Mockito.mock(VaadinService.class);
        DeploymentConfiguration deploymentConfig = Mockito
                .mock(DeploymentConfiguration.class);
        Mockito.when(deploymentConfig.isProductionMode()).thenReturn(false);
        Mockito.when(service.getDeploymentConfiguration())
                .thenReturn(deploymentConfig);

        session = Mockito.spy(new AlwaysLockedVaadinSession(service));

        ui = new UI();
        ui.getInternals().setSession(session);

        UI.setCurrent(ui);
        VaadinSession.setCurrent(session);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        cleanup();
    }

    @SuppressWarnings("checkstyle:UiSetCurrentCheck")
    private void cleanup() {
        removeAll();
        UI.setCurrent(null);
        VaadinSession.setCurrent(null);
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
        cleanup();
    }

    /**
     * Replaces the current UI with a new instance.
     */
    public void replaceUI() {
        cleanup();
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
