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
package com.vaadin.tests.dataprovider;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;

// Originally from com.vaadin.flow.data.provider.DataCommunicationTest.MockUI
// If this breaks, check there for updates
public class MockUI extends UI {

    public MockUI() {
        this(findOrcreateSession());
    }

    public MockUI(VaadinSession session) {
        getInternals().setSession(session);
        setCurrent(this);
    }

    @Override
    protected void init(VaadinRequest request) {
        // Do nothing
    }

    private static VaadinSession findOrcreateSession() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            MockService service = Mockito.mock(MockService.class);
            Mockito.when(service.getRouteRegistry())
                    .thenReturn(Mockito.mock(RouteRegistry.class));
            session = new AlwaysLockedVaadinSession(service);
            VaadinSession.setCurrent(session);
        }
        return session;
    }

    private static class MockService extends VaadinServletService {

        @Override
        public RouteRegistry getRouteRegistry() {
            return super.getRouteRegistry();
        }
    }

    private static class AlwaysLockedVaadinSession extends MockVaadinSession {

        private AlwaysLockedVaadinSession(VaadinService service) {
            super(service);
            lock();
        }

    }

    private static class MockVaadinSession extends VaadinSession {
        /*
         * Used to make sure there's at least one reference to the mock session
         * while it's locked. This is used to prevent the session from being
         * eaten by GC in tests where @Before creates a session and sets it as
         * the current instance without keeping any direct reference to it. This
         * pattern has a chance of leaking memory if the session is not unlocked
         * in the right way, but it should be acceptable for testing use.
         */
        private static final ThreadLocal<MockVaadinSession> referenceKeeper = new ThreadLocal<>();

        private MockVaadinSession(VaadinService service) {
            super(service);
        }

        @Override
        public void close() {
            super.close();
            closeCount++;
        }

        public int getCloseCount() {
            return closeCount;
        }

        @Override
        public Lock getLockInstance() {
            return lock;
        }

        @Override
        public void lock() {
            super.lock();
            referenceKeeper.set(this);
        }

        @Override
        public void unlock() {
            super.unlock();
            referenceKeeper.remove();
        }

        private int closeCount;

        private ReentrantLock lock = new ReentrantLock();
    }
}
