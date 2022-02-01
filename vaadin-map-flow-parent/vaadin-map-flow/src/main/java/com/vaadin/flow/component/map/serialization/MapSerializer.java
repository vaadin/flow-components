package com.vaadin.flow.component.map.serialization;

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
