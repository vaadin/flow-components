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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.server.VaadinService;

/**
 * Quarkus reads the Jandex indexes of the components and of Flow with the same
 * Jandex. Flow picks the index format that this Jandex reads, so the components
 * have to write the same format as Flow. All component modules get the format
 * from the root POM, so checking this module covers them.
 */
class JandexIndexTest {

    // The first bytes of every Jandex index, followed by the format version
    private static final int INDEX_MAGIC = 0xBABE1F15;

    @Test
    void indexFormat_matchesFlowServer()
            throws IOException, URISyntaxException {
        assertEquals(readIndexFormat(VaadinService.class),
                readIndexFormat(SlotUtils.class),
                "The components write a different Jandex index format"
                        + " than flow-server. Set jandex.format.version to"
                        + " the value in the Flow root POM.");
    }

    private static int readIndexFormat(Class<?> type)
            throws IOException, URISyntaxException {
        URI location = type.getProtectionDomain().getCodeSource().getLocation()
                .toURI();
        URI index = location.getPath().endsWith(".jar")
                ? URI.create("jar:" + location + "!/META-INF/jandex.idx")
                : location.resolve("META-INF/jandex.idx");
        try (DataInputStream in = new DataInputStream(
                index.toURL().openStream())) {
            assertEquals(INDEX_MAGIC, in.readInt(),
                    index + " is not a Jandex index");
            return in.readUnsignedByte();
        }
    }
}
