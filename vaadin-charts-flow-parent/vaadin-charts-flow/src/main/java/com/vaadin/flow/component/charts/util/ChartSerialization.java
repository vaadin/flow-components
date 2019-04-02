package com.vaadin.flow.component.charts.util;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright (C) 2014 - 2019 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file licensing.txt distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <https://vaadin.com/license/cval-3>.
 * #L%
 */


import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.serializers.AxisListSerializer;
import com.vaadin.flow.component.charts.model.serializers.ChartEnumSerializer;
import com.vaadin.flow.component.charts.model.serializers.DateSerializer;
import com.vaadin.flow.component.charts.model.serializers.DefaultBeanSerializerModifier;
import com.vaadin.flow.component.charts.model.serializers.InstantSerializer;
import com.vaadin.flow.component.charts.model.serializers.PaneListSerializer;
import com.vaadin.flow.component.charts.model.serializers.SolidColorSerializer;
import com.vaadin.flow.component.charts.model.serializers.StopSerializer;
import com.vaadin.flow.component.charts.model.serializers.TimeUnitMultiplesSerializer;

import java.io.Serializable;

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

    /**
     * Create the default {@link ObjectMapper} used for serialization.
     */
    public static ObjectMapper createObjectMapper() {
        // serializer modifier used when basic serializer isn't enough
        return createObjectMapper(new DefaultBeanSerializerModifier());
    }

    public static ObjectMapper createObjectMapper(
            BeanSerializerModifier modifier) {
        ObjectMapper mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
                .registerModule(ChartEnumSerializer.getModule())
                .registerModule(StopSerializer.getModule())
                .registerModule(TimeUnitMultiplesSerializer.getModule())
                .registerModule(SolidColorSerializer.getModule())
                .registerModule(AxisListSerializer.getModule())
                .registerModule(PaneListSerializer.getModule())
                .registerModule(DateSerializer.getModule())
                .registerModule(InstantSerializer.getModule());

        // serializer modifier used when basic serializer isn't enough
        return mapper.setSerializerFactory(
                mapper.getSerializerFactory().withSerializerModifier(modifier));
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
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while serializing "
                    + object.getClass().getSimpleName(), e);
        }
    }
}
