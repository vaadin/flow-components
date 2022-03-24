package com.vaadin.flow.component.map.serialization;

/*
 * #%L
 * Vaadin Map
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceRegistry;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonFactory;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom JSON serializer for the map component using a Jackson
 * {@link ObjectMapper}
 */
public class MapSerializer {

    private final ObjectWriter writer;

    public MapSerializer() {
        // Add map-instance specific serializer to handle Flow stream resources
        SimpleModule module = new SimpleModule().addSerializer(
                StreamResource.class, new StreamResourceSerializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        writer = mapper.writer();
    }

    /**
     * Serializes a map configuration object to JSON using a Jackson
     * {@link ObjectMapper}, and returns the value as a {@link JsonValue} to
     * provide it in a type that is compatible with Flow.
     * <p>
     * Throws a runtime exception if the object can not be serialized to JSON.
     *
     * @param value
     *            the map configuration object to be serialized into JSON
     * @return a {@link JsonValue} representing the configuration object as JSON
     */
    public JsonValue toJson(Object value) {
        String json;
        try {
            json = writer.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while serializing "
                    + value.getClass().getSimpleName(), e);
        }

        return new JreJsonFactory().parse(json);
    }

    /**
     * Custom Jackson serializer for {@link StreamResource}s. The serializer
     * guarantees that all stream resources encountered during serialization of
     * a configuration object are registered in a Flow session's stream resource
     * registry, and are available under a dynamic URL. The serializer also
     * returns the dynamic URL as serialized value.
     */
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
}
