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
package com.vaadin.flow.component.ai.tests.endpoints;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import de.bwaldvogel.mongo.bson.Document;
import de.bwaldvogel.mongo.wire.message.MongoMessage;

/**
 * An in-memory backend that logs every command clients send at {@code INFO},
 * with what was asked and what came back, once {@link #startLogging()} has been
 * called. The connection handshake and the driver's server monitoring carry no
 * data and are not logged.
 */
final class LoggingMongoBackend extends MemoryBackend {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LoggingMongoBackend.class);

    private static final Set<String> CONNECTION_COMMANDS = Set.of("hello",
            "ismaster", "ping", "buildinfo");

    private volatile boolean logging;

    /**
     * Logs the commands from now on.
     */
    void startLogging() {
        logging = true;
    }

    @Override
    public Document handleMessage(MongoMessage message) {
        var command = message.getDocument().keySet().iterator().next();
        if (!logging || CONNECTION_COMMANDS
                .contains(command.toLowerCase(Locale.ROOT))) {
            return super.handleMessage(message);
        }
        var call = describe(command, message);
        try {
            var response = super.handleMessage(message);
            LOGGER.info("{} -> {}", call, describe(response));
            return response;
        } catch (RuntimeException e) {
            LOGGER.info("{} -> error: {}", call, e.getMessage());
            throw e;
        }
    }

    /**
     * Describes a command for the log: its name, the collection or database it
     * runs on, and its arguments without the {@code $}-prefixed fields the
     * driver adds to every command.
     */
    private static String describe(String command, MongoMessage message) {
        var document = message.getDocument();
        var target = document.get(command) instanceof String collection
                ? message.getDatabaseName() + "." + collection
                : message.getDatabaseName();
        var arguments = new Document();
        for (var field : document.entrySet()) {
            if (!field.getKey().equals(command)
                    && !field.getKey().startsWith("$")) {
                arguments.put(field.getKey(), field.getValue());
            }
        }
        return command + " " + target + " " + arguments;
    }

    /**
     * Describes a response for the log: the number of documents of a cursor
     * batch, or the whole response of a command without a cursor.
     */
    private static String describe(Document response) {
        if (response.get("cursor") instanceof Document cursor) {
            var batch = cursor.containsKey("firstBatch")
                    ? cursor.get("firstBatch")
                    : cursor.get("nextBatch");
            if (batch instanceof List<?> documents) {
                return documents.size() + " documents";
            }
        }
        return response.toString();
    }
}
