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
package com.vaadin.flow.component.shared;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.signals.BindingActiveException;
import com.vaadin.signals.ValueSignal;

public class HasThemeVariantTest {

    @Test
    public void addThemeVariant_themeNamesContainsThemeVariant() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);

        Assert.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT.getVariantName()),
                component.getThemeNames());
    }

    @Test
    public void addThemeVariant_removeThemeVariant_themeNamesDoesNotContainThemeVariant() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);
        component.removeThemeVariants(TestComponentVariant.TEST_VARIANT);

        Assert.assertTrue(component.getThemeNames().isEmpty());
    }

    @Test
    public void setThemeVariant_setTrue_addsThemeVariant() {
        TestComponent component = new TestComponent();
        component.setThemeVariant(TestComponentVariant.TEST_VARIANT, true);

        Assert.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT.getVariantName()),
                component.getThemeNames());
    }

    @Test
    public void setThemeVariant_setFalse_removesThemeVariant() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);
        component.setThemeVariant(TestComponentVariant.TEST_VARIANT, false);

        Assert.assertTrue(component.getThemeNames().isEmpty());
    }

    @Test
    public void setThemeVariants_overridesExisting() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);
        component.setThemeVariants(TestComponentVariant.TEST_VARIANT_2,
                TestComponentVariant.TEST_VARIANT_3);

        Assert.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT_2.getVariantName(),
                        TestComponentVariant.TEST_VARIANT_3.getVariantName()),
                component.getThemeNames());
    }

    @Test
    public void setThemeVariants_withoutArgs_clearsThemeVariants() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT);
        component.setThemeVariants();

        Assert.assertTrue(component.getThemeNames().isEmpty());
    }

    @Test
    public void setThemeVariants_setTrue_addsThemeVariants() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT_3);
        component.setThemeVariants(true, TestComponentVariant.TEST_VARIANT,
                TestComponentVariant.TEST_VARIANT_2);

        Assert.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT.getVariantName(),
                        TestComponentVariant.TEST_VARIANT_2.getVariantName(),
                        TestComponentVariant.TEST_VARIANT_3.getVariantName()),
                component.getThemeNames());
    }

    @Test
    public void setThemeVariants_setFalse_removesThemeVariants() {
        TestComponent component = new TestComponent();
        component.addThemeVariants(TestComponentVariant.TEST_VARIANT,
                TestComponentVariant.TEST_VARIANT_2,
                TestComponentVariant.TEST_VARIANT_3);
        component.setThemeVariants(false, TestComponentVariant.TEST_VARIANT,
                TestComponentVariant.TEST_VARIANT_2);

        Assert.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT_3.getVariantName()),
                component.getThemeNames());
    }

    @Test
    public void bindThemeVariant_setSignalValueTrue_themeNamesContainsThemeVariant() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindThemeVariant(TestComponentVariant.TEST_VARIANT, signal);

        Assert.assertEquals(
                Set.of(TestComponentVariant.TEST_VARIANT.getVariantName()),
                component.getThemeNames());
    }

    @Test
    public void bindThemeVariant_setSignalValueFalse_themeNamesDoesNotContainThemeVariant() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindThemeVariant(TestComponentVariant.TEST_VARIANT, signal);

        signal.value(false);
        Assert.assertTrue(component.getThemeNames().isEmpty());
    }

    @Test
    public void bindThemeVariant_setThemeVariants_removesBinding() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindThemeVariant(TestComponentVariant.TEST_VARIANT, signal);
        // setThemeVariants calls getThemeNames().clear() which removes the
        // binding
        component.setThemeVariants(); // clears all variants and biding

        Assert.assertTrue(component.getThemeNames().isEmpty());
        Assert.assertTrue(signal.peek());
        signal.value(true); // no effect
        Assert.assertTrue(component.getThemeNames().isEmpty());
    }

    @Test
    public void bindThemeVariant_withNullSignal_removesBinding() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindThemeVariant(TestComponentVariant.TEST_VARIANT, signal);
        Assert.assertFalse(component.getThemeNames().isEmpty());

        component.bindThemeVariant(TestComponentVariant.TEST_VARIANT, null);
        Assert.assertFalse(component.getThemeNames().isEmpty());
        signal.value(false); // no effect
        Assert.assertFalse(component.getThemeNames().isEmpty());
    }

    @Test
    public void bindThemeVariant_editWithActiveBinding_throwBindingActiveException() {
        TestComponent component = new TestComponent();
        UI.getCurrent().add(component);
        ValueSignal<Boolean> signal = new ValueSignal<>(true);
        component.bindThemeVariant(TestComponentVariant.TEST_VARIANT, signal);
        Assert.assertFalse(component.getThemeNames().isEmpty());

        Assert.assertThrows(BindingActiveException.class, () -> component
                .removeThemeVariants(TestComponentVariant.TEST_VARIANT));
        Assert.assertThrows(BindingActiveException.class, () -> component
                .addThemeVariants(TestComponentVariant.TEST_VARIANT));
        Assert.assertThrows(BindingActiveException.class, () -> component
                .setThemeVariant(TestComponentVariant.TEST_VARIANT, false));
        Assert.assertThrows(BindingActiveException.class, () -> component
                .setThemeVariant(TestComponentVariant.TEST_VARIANT, true));
        Assert.assertThrows(BindingActiveException.class, () -> component
                .setThemeVariants(false, TestComponentVariant.TEST_VARIANT));
    }

    private enum TestComponentVariant implements ThemeVariant {
        TEST_VARIANT("test1"), TEST_VARIANT_2("test2"), TEST_VARIANT_3("test3");

        private final String variant;

        TestComponentVariant(String variant) {
            this.variant = variant;
        }

        @Override
        public String getVariantName() {
            return variant;
        }
    }

    @Tag("test")
    private static class TestComponent extends Component
            implements HasThemeVariant<TestComponentVariant> {
    }

    private static class MockUI extends UI {

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

        private static class AlwaysLockedVaadinSession
                extends MockVaadinSession {

            private AlwaysLockedVaadinSession(VaadinService service) {
                super(service);
                lock();
            }

        }

        private static class MockVaadinSession extends VaadinSession {
            /*
             * Used to make sure there's at least one reference to the mock
             * session while it's locked. This is used to prevent the session
             * from being eaten by GC in tests where @Before creates a session
             * and sets it as the current instance without keeping any direct
             * reference to it. This pattern has a chance of leaking memory if
             * the session is not unlocked in the right way, but it should be
             * acceptable for testing use.
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

    @BeforeClass
    public static void setupUI() {
        var mockUI = new MockUI();
        UI.setCurrent(mockUI);
    }

    @AfterClass
    public static void teardownUI() {
        if (UI.getCurrent() != null) {
            UI.getCurrent().removeAll();
            UI.setCurrent(null);
        }
    }
}
