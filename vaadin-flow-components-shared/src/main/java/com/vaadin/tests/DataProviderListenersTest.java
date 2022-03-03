/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.shared.Registration;

/**
 * Util class for testing how components with data provider handles the data
 * provider listener on attaching/detaching and on setting a new data provider.
 */
public final class DataProviderListenersTest {

    private DataProviderListenersTest() {
    }

    /**
     * Checks the old data provider listeners of the component are removed after
     * component attach/detach and a new ones are added.
     *
     * @param component
     *            to be attached/detached
     * @param expectedListenersCountAfterDataProviderSetup
     *            expected listeners count after setting a data provider to
     *            component
     * @param expectedListenersCountAfterComponentAttach
     *            expected listeners count after attaching the component
     * @param expectedRemovedListenerIndexes
     *            indexes of listeners which are supposed to be removed after
     *            attach and detach of component, index starts from 0.
     * @param mockUI
     *            UI the component is attached to
     * @param <C>
     *            component type
     */
    public static <C extends HasListDataView<Object, ?>> void checkOldListenersRemovedOnComponentAttachAndDetach(
            C component, int expectedListenersCountAfterDataProviderSetup,
            int expectedListenersCountAfterComponentAttach,
            int[] expectedRemovedListenerIndexes, UI mockUI) {

        // given
        DataProviderProxy dataProviderProxy = new DataProviderProxy();

        // when
        component.setItems(dataProviderProxy);

        // then
        Assert.assertEquals(
                "Unexpected count of added data provider listeners after "
                        + "setting a data provider to the component",
                expectedListenersCountAfterDataProviderSetup,
                dataProviderProxy.getListenersCounter());

        // given
        dataProviderProxy.resetListenersCounter();

        // when
        mockUI.add((Component) component);
        fakeClientCommunication(mockUI);

        // then
        Assert.assertEquals(
                "Unexpected count of added data provider "
                        + "listeners after attaching the component",
                expectedListenersCountAfterComponentAttach,
                dataProviderProxy.getListenersCounter());

        // when
        mockUI.remove((Component) component);
        fakeClientCommunication(mockUI);

        // then
        Arrays.stream(expectedRemovedListenerIndexes)
                .forEach(listenerIndex -> Assert.assertTrue(String.format(
                        "Expected old data provider listener with index '%d' to"
                                + " be removed",
                        listenerIndex),
                        dataProviderProxy.getListenerRemoved()
                                .get(listenerIndex)));
    }

    private static void fakeClientCommunication(UI ui) {
        ui.getInternals().getStateTree().runExecutionsBeforeClientResponse();
        ui.getInternals().getStateTree().collectChanges(ignore -> {
        });
    }

    private static class DataProviderProxy extends ListDataProvider<Object> {
        private final List<Boolean> listenerRemoved;
        private int listenersCounter = 0;
        private int registrationsCounter = 0;

        public DataProviderProxy() {
            super(Collections.emptyList());
            final int maxListenerNumberExpected = 4;
            listenerRemoved = Stream.of(new Boolean[maxListenerNumberExpected])
                    .map(item -> Boolean.FALSE).collect(Collectors.toList());
        }

        @Override
        public Registration addDataProviderListener(
                DataProviderListener<Object> listener) {
            listenersCounter++;
            int registrationIndex = registrationsCounter++;
            Registration registration = super.addDataProviderListener(listener);
            return Registration.combine(registration,
                    () -> listenerRemoved.set(registrationIndex, Boolean.TRUE));
        }

        public int getListenersCounter() {
            return listenersCounter;
        }

        public List<Boolean> getListenerRemoved() {
            return listenerRemoved;
        }

        public void resetListenersCounter() {
            listenersCounter = 0;
        }
    }
}
