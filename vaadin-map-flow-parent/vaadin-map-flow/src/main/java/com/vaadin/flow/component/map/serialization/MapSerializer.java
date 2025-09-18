/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.map.serialization;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.BaseJsonNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.streams.DownloadHandler;

/**
 * Custom JSON serializer for the map component using a Jackson
 * {@link ObjectMapper}
 */
public class MapSerializer implements Serializable {

    private final ObjectMapper mapper;
    private final Map<Object, StreamRegistration> streamRegistrationCache = new HashMap<>();

    public MapSerializer(com.vaadin.flow.component.map.MapBase map) {
        // Create mapper that automatically registers stream resources and
        // download handlers in the current UI's stream resource registry
        SimpleModule streamResourceModule = new SimpleModule().addSerializer(
                StreamResource.class, new StreamResourceSerializer());
        SimpleModule downloadHandlerModule = new SimpleModule().addSerializer(
                DownloadHandler.class, new DownloadHandlerSerializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(streamResourceModule);
        mapper.registerModule(downloadHandlerModule);
        this.mapper = mapper;

        // Unregister stream registrations when the map is detached
        map.addDetachListener(event -> {
            streamRegistrationCache.values()
                    .forEach(StreamRegistration::unregister);
            streamRegistrationCache.clear();
        });
    }

    /**
     * Serializes a map configuration object to JSON using a custom Jackson
     * {@link ObjectMapper} that handles Flow {@link StreamResource} and
     * {@link DownloadHandler} to serialize those into URLs.
     *
     * @param value
     *            the map configuration object to be serialized into JSON
     * @return a {@link BaseJsonNode} representing the configuration object as
     *         JSON
     * @throws IllegalArgumentException
     *             if the object can not be serialized to JSON
     */
    public BaseJsonNode toJson(Object value) {
        return mapper.valueToTree(value);
    }

    /**
     * Custom Jackson serializer for {@link StreamResource}s. The serializer
     * guarantees that all stream resources encountered during serialization of
     * a configuration object are registered in a Flow session's stream resource
     * registry, and are available under a dynamic URL. The serializer also
     * returns the dynamic URL as serialized value.
     */
    @Deprecated(since = "24.8", forRemoval = true)
    private class StreamResourceSerializer
            extends StdSerializer<StreamResource> {

        public StreamResourceSerializer() {
            super(StreamResource.class);
        }

        @Override
        public void serialize(StreamResource value, JsonGenerator gen,
                SerializerProvider provider) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            URI uri = getURI(value);
            gen.writeString(uri.toString());
        }

        private URI getURI(StreamResource resource) {
            StreamRegistration registration = streamRegistrationCache
                    .get(resource);
            if (registration == null) {
                StreamResourceRegistry resourceRegistry = UI.getCurrent()
                        .getSession().getResourceRegistry();
                registration = resourceRegistry.registerResource(resource);
                streamRegistrationCache.put(resource, registration);
            }
            return registration.getResourceUri();
        }
    }

    /**
     * Custom Jackson serializer for {@link DownloadHandler}s. The serializer
     * guarantees that all stream resources encountered during serialization of
     * a configuration object are registered in a Flow session's stream resource
     * registry, and are available under a dynamic URL. The serializer also
     * returns the dynamic URL as serialized value.
     */
    private class DownloadHandlerSerializer
            extends StdSerializer<DownloadHandler> {

        public DownloadHandlerSerializer() {
            super(DownloadHandler.class);
        }

        @Override
        public void serialize(DownloadHandler value, JsonGenerator gen,
                SerializerProvider provider) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            URI uri = getURI(value);
            gen.writeString(uri.toString());
        }

        private URI getURI(DownloadHandler resource) {
            StreamRegistration registration = streamRegistrationCache
                    .get(resource);
            if (registration == null) {
                StreamResourceRegistry resourceRegistry = UI.getCurrent()
                        .getSession().getResourceRegistry();
                registration = resourceRegistry.registerResource(resource);
                streamRegistrationCache.put(resource, registration);
            }
            return registration.getResourceUri();
        }
    }
}
