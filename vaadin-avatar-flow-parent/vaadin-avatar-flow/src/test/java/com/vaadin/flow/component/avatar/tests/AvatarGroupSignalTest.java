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
package com.vaadin.flow.component.avatar.tests;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.avatar.AvatarGroup;
import com.vaadin.flow.component.avatar.AvatarGroup.AvatarGroupItem;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

import tools.jackson.databind.node.ArrayNode;

class AvatarGroupSignalTest extends AbstractSignalsTest {

    private AvatarGroup avatarGroup;

    @BeforeEach
    void setup() {
        avatarGroup = new AvatarGroup();
    }

    @Test
    void signalConstructor_setsItemsFromSignal() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var item2Signal = new ValueSignal<>(new AvatarGroupItem("Bob"));
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        avatarGroup = new AvatarGroup(listSignal);
        ui.add(avatarGroup);

        Assertions.assertEquals(2, avatarGroup.getItems().size());
        Assertions.assertEquals("Alice",
                avatarGroup.getItems().get(0).getName());
        Assertions.assertEquals("Bob", avatarGroup.getItems().get(1).getName());
    }

    @Test
    void signalConstructor_updatesWhenSignalChanges() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup = new AvatarGroup(listSignal);
        ui.add(avatarGroup);

        var item2Signal = new ValueSignal<>(new AvatarGroupItem("Bob"));
        listSignal.set(List.of(item1Signal, item2Signal));

        Assertions.assertEquals(2, avatarGroup.getItems().size());

        ui.fakeClientCommunication();
        ArrayNode clientItems = (ArrayNode) avatarGroup.getElement()
                .getPropertyRaw("items");
        Assertions.assertEquals(2, clientItems.size());
    }

    @Test
    void signalConstructor_setItemsWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup = new AvatarGroup(listSignal);
        ui.add(avatarGroup);

        Assertions.assertThrows(BindingActiveException.class, () -> avatarGroup
                .setItems(List.of(new AvatarGroupItem("Bob"))));
    }

    @Test
    void bindItems_setsItemsFromSignal() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var item2Signal = new ValueSignal<>(new AvatarGroupItem("Bob"));
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        avatarGroup.bindItems(listSignal);
        ui.add(avatarGroup);

        Assertions.assertEquals(2, avatarGroup.getItems().size());
        Assertions.assertEquals("Alice",
                avatarGroup.getItems().get(0).getName());
        Assertions.assertEquals("Bob", avatarGroup.getItems().get(1).getName());
    }

    @Test
    void bindItems_updatesWhenListSignalChanges() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup.bindItems(listSignal);
        ui.add(avatarGroup);

        Assertions.assertEquals(1, avatarGroup.getItems().size());

        var item2Signal = new ValueSignal<>(new AvatarGroupItem("Bob"));
        var item3Signal = new ValueSignal<>(new AvatarGroupItem("Charlie"));
        listSignal.set(List.of(item1Signal, item2Signal, item3Signal));

        Assertions.assertEquals(3, avatarGroup.getItems().size());
        Assertions.assertEquals("Charlie",
                avatarGroup.getItems().get(2).getName());

        ui.fakeClientCommunication();
        ArrayNode clientItems = (ArrayNode) avatarGroup.getElement()
                .getPropertyRaw("items");
        Assertions.assertEquals(3, clientItems.size());
    }

    @Test
    void bindItems_updatesWhenItemSignalChanges() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup.bindItems(listSignal);
        ui.add(avatarGroup);

        Assertions.assertEquals("Alice",
                avatarGroup.getItems().get(0).getName());

        item1Signal.set(new AvatarGroupItem("Updated Alice"));

        Assertions.assertEquals("Updated Alice",
                avatarGroup.getItems().get(0).getName());

        ui.fakeClientCommunication();
        ArrayNode clientItems = (ArrayNode) avatarGroup.getElement()
                .getPropertyRaw("items");
        Assertions.assertEquals(1, clientItems.size());
        Assertions.assertEquals("Updated Alice",
                clientItems.get(0).get("name").asString());
    }

    @Test
    void bindItems_notAttached_initialValueApplied() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var item2Signal = new ValueSignal<>(new AvatarGroupItem("Bob"));
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        avatarGroup.bindItems(listSignal);

        // Initial value is applied immediately (effect runs on creation)
        Assertions.assertEquals(2, avatarGroup.getItems().size());

        ui.add(avatarGroup);

        Assertions.assertEquals(2, avatarGroup.getItems().size());
    }

    @Test
    void setItemsWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup.bindItems(listSignal);
        ui.add(avatarGroup);

        Assertions.assertThrows(BindingActiveException.class, () -> avatarGroup
                .setItems(List.of(new AvatarGroupItem("Bob"))));
    }

    @Test
    void addWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup.bindItems(listSignal);
        ui.add(avatarGroup);

        Assertions.assertThrows(BindingActiveException.class,
                () -> avatarGroup.add(new AvatarGroupItem("Bob")));
    }

    @Test
    void removeWhileBound_throws() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup.bindItems(listSignal);
        ui.add(avatarGroup);

        Assertions.assertThrows(BindingActiveException.class,
                () -> avatarGroup.remove(avatarGroup.getItems().get(0)));
    }

    @Test
    void bindItems_calledTwice_throws() {
        var item1Signal = new ValueSignal<>(new AvatarGroupItem("Alice"));
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        avatarGroup.bindItems(listSignal);
        Assertions.assertThrows(BindingActiveException.class,
                () -> avatarGroup.bindItems(listSignal));
    }

    @Test
    void bindItems_nullSignal_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> avatarGroup.bindItems(null));
    }
}
