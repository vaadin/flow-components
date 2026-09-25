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
package com.vaadin.flow.component.shared.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.tests.MockUIExtension;

class BeforeClientResponseActionTest {

    @RegisterExtension
    final MockUIExtension ui = new MockUIExtension();

    private TestComponent component;
    private BeforeClientResponseAction action;
    // The UI the component was attached to each time the action ran
    private List<UI> runs;

    @BeforeEach
    void setup() {
        component = new TestComponent();
        ui.add(component);
        ui.fakeClientCommunication();

        runs = new ArrayList<>();
        action = new BeforeClientResponseAction(component,
                () -> runs.add(component.getUI().orElse(null)));
    }

    @Test
    void constructor_nullArguments_throw() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new BeforeClientResponseAction(null, () -> {
                }));
        Assertions.assertThrows(NullPointerException.class,
                () -> new BeforeClientResponseAction(component,
                        (SerializableRunnable) null));
        Assertions.assertThrows(NullPointerException.class,
                () -> new BeforeClientResponseAction(component,
                        (SerializableConsumer<ExecutionContext>) null));
    }

    @Test
    void contextAction_receivesClientSideInitializedState() {
        List<Boolean> initialized = new ArrayList<>();
        TestComponent freshComponent = new TestComponent();
        BeforeClientResponseAction contextAction = new BeforeClientResponseAction(
                freshComponent,
                context -> initialized.add(context.isClientSideInitialized()));

        ui.add(freshComponent);
        contextAction.schedule();
        ui.fakeClientCommunication();

        contextAction.schedule();
        ui.fakeClientCommunication();

        Assertions.assertEquals(List.of(false, true), initialized);
    }

    @Test
    void notScheduled_actionDoesNotRun() {
        ui.fakeClientCommunication();

        Assertions.assertEquals(List.of(), runs);
    }

    @Test
    void schedule_actionRunsOnceBeforeResponse() {
        action.schedule();
        Assertions.assertEquals(List.of(), runs);

        ui.fakeClientCommunication();
        Assertions.assertEquals(List.of(ui.getUI()), runs);

        ui.fakeClientCommunication();
        Assertions.assertEquals(List.of(ui.getUI()), runs);
    }

    @Test
    void scheduleMultipleTimes_actionRunsOnce() {
        action.schedule();
        action.schedule();
        action.schedule();
        ui.fakeClientCommunication();

        Assertions.assertEquals(List.of(ui.getUI()), runs);
    }

    @Test
    void scheduleAndCancel_actionDoesNotRun() {
        action.schedule();
        action.cancel();
        ui.fakeClientCommunication();

        Assertions.assertEquals(List.of(), runs);
    }

    @Test
    void cancelWithoutSchedule_nothingHappens() {
        action.cancel();
        ui.fakeClientCommunication();
        Assertions.assertEquals(List.of(), runs);

        action.schedule();
        ui.fakeClientCommunication();
        Assertions.assertEquals(List.of(ui.getUI()), runs);
    }

    @Test
    void cancelAndScheduleAgain_actionRunsOnce() {
        action.schedule();
        action.cancel();
        action.schedule();
        ui.fakeClientCommunication();

        Assertions.assertEquals(List.of(ui.getUI()), runs);
    }

    @Test
    void scheduleAgainAfterRun_actionRunsBeforeFollowingResponse() {
        action.schedule();
        ui.fakeClientCommunication();

        action.schedule();
        ui.fakeClientCommunication();

        Assertions.assertEquals(List.of(ui.getUI(), ui.getUI()), runs);
    }

    @Test
    void scheduleWhileDetached_actionRunsAfterAttach() {
        ui.remove(component);
        ui.fakeClientCommunication();

        action.schedule();
        ui.fakeClientCommunication();
        Assertions.assertEquals(List.of(), runs);

        ui.add(component);
        ui.fakeClientCommunication();
        Assertions.assertEquals(List.of(ui.getUI()), runs);
    }

    @Test
    void scheduleWhileDetached_cancel_actionDoesNotRunAfterAttach() {
        ui.remove(component);
        action.schedule();
        action.cancel();

        ui.add(component);
        ui.fakeClientCommunication();

        Assertions.assertEquals(List.of(), runs);
    }

    @Test
    void schedule_detached_actionRunsOnceAfterAttachedToSameUi() {
        action.schedule();
        ui.remove(component);
        ui.fakeClientCommunication();
        Assertions.assertEquals(List.of(), runs);

        // Several detach / attach cycles within one round trip
        ui.add(component);
        ui.remove(component);
        ui.add(component);
        ui.fakeClientCommunication();
        Assertions.assertEquals(List.of(ui.getUI()), runs);

        // Nothing left scheduled
        ui.remove(component);
        ui.add(component);
        ui.fakeClientCommunication();
        Assertions.assertEquals(List.of(ui.getUI()), runs);
    }

    @Test
    void schedule_movedToDifferentUiBeforeResponse_actionRunsInNewUi() {
        action.schedule();

        // Simulates what the router does for @PreserveOnRefresh before moving
        // components to the UI created for the reloaded page
        component.getElement().removeFromTree(false);
        ui.replaceUI();
        UI newUi = ui.getUI();
        ui.add(component);
        ui.fakeClientCommunication();

        Assertions.assertEquals(List.of(newUi), runs);
    }

    @Test
    void schedule_detachedInSameRoundTrip_actionRunsAfterAttachedToNewUi() {
        action.schedule();
        ui.remove(component);
        ui.fakeClientCommunication();
        Assertions.assertEquals(List.of(), runs);

        // A detached node still belongs to the state tree of its previous UI,
        // it has to be removed from that tree before attaching it to a new UI
        component.getElement().removeFromTree(false);
        ui.replaceUI();
        UI newUi = ui.getUI();
        ui.add(component);
        ui.fakeClientCommunication();

        Assertions.assertEquals(List.of(newUi), runs);
    }

    @Test
    void scheduleAndDetach_serializeAndDeserialize_actionRunsAfterAttach()
            throws Exception {
        // Use a UI without a session, as the mocked session is not
        // serializable. Removing the session from a UI detaches the UI.
        var serializableUi = new UI();
        var component = new SerializableTestComponent();
        serializableUi.add(component);
        component.update.schedule();
        // After the detach, the UI does not have the component as a child, so
        // deserialization reaches the component through the lambdas of the
        // scheduled action. This is the order that caused
        // https://github.com/vaadin/flow-components/issues/6555
        serializableUi.remove(component);

        Object[] copies = serializeAndDeserialize(serializableUi, component);
        UI uiCopy = (UI) copies[0];
        var componentCopy = (SerializableTestComponent) copies[1];
        uiCopy.add(componentCopy);
        uiCopy.getInternals().getStateTree()
                .runExecutionsBeforeClientResponse();

        Assertions.assertEquals(1, componentCopy.updateCount);
    }

    private static Object[] serializeAndDeserialize(Object... objects)
            throws IOException, ClassNotFoundException {
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(objects);
        }
        try (var in = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            return (Object[]) in.readObject();
        }
    }

    @Tag("test")
    private static class TestComponent extends Component {
    }

    @Tag("test")
    private static class SerializableTestComponent extends Component {
        private final BeforeClientResponseAction update = new BeforeClientResponseAction(
                this, this::update);
        private int updateCount;

        private void update() {
            updateCount++;
        }
    }
}
