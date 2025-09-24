/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.util;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.serializers.AxisListSerializer;
import com.vaadin.flow.component.charts.model.serializers.ChartEnumSerializer;
import com.vaadin.flow.component.charts.model.serializers.DateSerializer;
import com.vaadin.flow.component.charts.model.serializers.DefaultBeanSerializerModifier;
import com.vaadin.flow.component.charts.model.serializers.GradientColorStopsSerializer;
import com.vaadin.flow.component.charts.model.serializers.InstantSerializer;
import com.vaadin.flow.component.charts.model.serializers.PaneListSerializer;
import com.vaadin.flow.component.charts.model.serializers.SolidColorSerializer;
import com.vaadin.flow.component.charts.model.serializers.StopSerializer;
import com.vaadin.flow.component.charts.model.serializers.TimeUnitMultiplesSerializer;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.SerializerFactory;
import tools.jackson.databind.ser.ValueSerializerModifier;

/**
 * Util class that handles the configuration needed for the model classes to be
 * serialized to JSON.
 */
public class ChartSerialization implements Serializable {

    private static ObjectWriter jsonWriter;

    static {
        // writer is thread safe so we can use a shared instance
        jsonWriter = createObjectMapper().writer();
    }

    private ChartSerialization() {
    }

    /**
     * Create the default {@link ObjectMapper} used for serialization.
     */
    public static ObjectMapper createObjectMapper() {
        // serializer modifier used when basic serializer isn't enough
        return createObjectMapper(new DefaultBeanSerializerModifier());
    }

    public static ObjectMapper createObjectMapper(
            ValueSerializerModifier modifier) {
        JsonMapper.Builder builder = JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl
                        .withValueInclusion(JsonInclude.Include.NON_NULL))
                .changeDefaultVisibility(handler -> handler
                        .withVisibility(PropertyAccessor.ALL, Visibility.NONE)
                        .withVisibility(PropertyAccessor.FIELD, Visibility.ANY))
                .addModule(ChartEnumSerializer.getModule())
                .addModule(StopSerializer.getModule())
                .addModule(TimeUnitMultiplesSerializer.getModule())
                .addModule(SolidColorSerializer.getModule())
                .addModule(GradientColorStopsSerializer.getModule())
                .addModule(AxisListSerializer.getModule())
                .addModule(PaneListSerializer.getModule())
                .addModule(DateSerializer.getModule())
                .addModule(InstantSerializer.getModule());

        // serializer modifier used when basic serializer isn't enough
        SerializerFactory serializerFactory = builder.serializerFactory()
                .withSerializerModifier(modifier);
        return builder.serializerFactory(serializerFactory).build();
    }

    /**
     * This method can be used to configure the {@link ObjectMapper} object used
     * to serialize configuration objects to client side. If users have made
     * their extensions to underlying library and wish to build a typed Java API
     * for it, adding custom serializers might be needed.
     *
     * @param newObjectWriter
     * @see #createObjectMapper()
     */
    public static void setObjectMapperInstance(ObjectWriter newObjectWriter) {
        jsonWriter = newObjectWriter;
    }

    public static String toJSON(AbstractConfigurationObject object) {
        try {
            return jsonWriter.writeValueAsString(object);
        } catch (JacksonException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while serializing "
                    + object.getClass().getSimpleName(), e);
        }
    }
}
