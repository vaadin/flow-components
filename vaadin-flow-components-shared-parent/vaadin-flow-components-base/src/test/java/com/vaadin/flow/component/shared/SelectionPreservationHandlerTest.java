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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.data.provider.DataChangeEvent;
import com.vaadin.flow.data.provider.DataProvider;

/**
 * Tests for {@link SelectionPreservationHandler}.
 */
class SelectionPreservationHandlerTest {

    private SelectionPreservationMode selectionPreservationMode;

    private SelectionPreservationHandler<String> selectionPreservationHandler;

    @BeforeEach
    void setup() {
        selectionPreservationHandler = new SelectionPreservationHandler<>(
                SelectionPreservationMode.DISCARD) {
            @Override
            public void onPreserveAll(DataChangeEvent<String> dataChangeEvent) {
                selectionPreservationMode = SelectionPreservationMode.PRESERVE_ALL;
            }

            @Override
            public void onPreserveExisting(
                    DataChangeEvent<String> dataChangeEvent) {
                selectionPreservationMode = SelectionPreservationMode.PRESERVE_EXISTING;
            }

            @Override
            public void onDiscard(DataChangeEvent<String> dataChangeEvent) {
                selectionPreservationMode = SelectionPreservationMode.DISCARD;
            }
        };
    }

    @Test
    void runHandler_handlerUsesDefaultMode() {
        selectionPreservationHandler.handleDataChange(
                new DataChangeEvent<>(DataProvider.ofItems()));
        Assertions.assertEquals(SelectionPreservationMode.DISCARD,
                selectionPreservationMode);
    }

    @Test
    void updateMode_runHandler_handlerUsesCorrectMode() {
        List.of(SelectionPreservationMode.PRESERVE_ALL,
                SelectionPreservationMode.PRESERVE_EXISTING)
                .forEach(modeToSet -> {
                    selectionPreservationHandler
                            .setSelectionPreservationMode(modeToSet);
                    selectionPreservationHandler.handleDataChange(
                            new DataChangeEvent<>(DataProvider.ofItems()));
                    Assertions.assertEquals(modeToSet,
                            selectionPreservationMode);
                });
    }

    @Test
    void setModeNull_throwsNullPointerException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> selectionPreservationHandler
                        .setSelectionPreservationMode(null));
    }
}
