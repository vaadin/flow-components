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

import elemental.json.JsonValue;

/**
 * Custom JSON serializer for the map component using a Jackson
 * {@link ObjectMapper}
 */
public class MapSerializer implements Serializable {

    private final ObjectMapper mapper;

    public MapSerializer() {
        // Add map-instance specific serializers to handle Flow stream resources
        SimpleModule streamResourceModule = new SimpleModule().addSerializer(
                StreamResource.class, new StreamResourceSerializer());
        SimpleModule downloadHandlerModule = new SimpleModule().addSerializer(
                DownloadHandler.class, new DownloadHandlerSerializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(streamResourceModule);
        mapper.registerModule(downloadHandlerModule);
        this.mapper = mapper;
    }

    /**
     * Serializes a map configuration object to JSON using a custom Jackson
     * {@link ObjectMapper} that handles Flow {@link StreamResource} and
     * {@link DownloadHandler} to serialize those into URLs.
     *
     * @param value
     *            the map configuration object to be serialized into JSON
     * @return a {@link JsonValue} representing the configuration object as JSON
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
    private static class StreamResourceSerializer
            extends StdSerializer<StreamResource> {
        private final Map<StreamResource, URI> streamResourceURICache = new HashMap<>();

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
            URI uri = streamResourceURICache.get(resource);
            if (uri == null) {
                StreamResourceRegistry resourceRegistry = UI.getCurrent()
                        .getSession().getResourceRegistry();
                StreamRegistration streamRegistration = resourceRegistry
                        .registerResource(resource);
                uri = streamRegistration.getResourceUri();
                streamResourceURICache.put(resource, uri);
            }
            return uri;
        }
    }

    /**
     * Custom Jackson serializer for {@link DownloadHandler}s. The serializer
     * guarantees that all stream resources encountered during serialization of
     * a configuration object are registered in a Flow session's stream resource
     * registry, and are available under a dynamic URL. The serializer also
     * returns the dynamic URL as serialized value.
     */
    private static class DownloadHandlerSerializer
            extends StdSerializer<DownloadHandler> {
        private final Map<DownloadHandler, URI> streamResourceURICache = new HashMap<>();

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
            URI uri = streamResourceURICache.get(resource);
            if (uri == null) {
                StreamResourceRegistry resourceRegistry = UI.getCurrent()
                        .getSession().getResourceRegistry();
                StreamRegistration streamRegistration = resourceRegistry
                        .registerResource(resource);
                uri = streamRegistration.getResourceUri();
                streamResourceURICache.put(resource, uri);
            }
            return uri;
        }
    }
}
