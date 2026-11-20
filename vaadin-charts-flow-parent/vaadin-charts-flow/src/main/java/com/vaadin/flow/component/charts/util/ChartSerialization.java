/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.util;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.vaadin.flow.component.charts.model.AbstractConfigurationObject;
import com.vaadin.flow.component.charts.model.ChartFunction;
import com.vaadin.flow.component.charts.model.serializers.AxisListSerializer;
import com.vaadin.flow.component.charts.model.serializers.ChartEnumSerializer;
import com.vaadin.flow.component.charts.model.serializers.ChartFunctionSerializer;
import com.vaadin.flow.component.charts.model.serializers.DateSerializer;
import com.vaadin.flow.component.charts.model.serializers.DefaultBeanSerializerModifier;
import com.vaadin.flow.component.charts.model.serializers.GradientColorStopsSerializer;
import com.vaadin.flow.component.charts.model.serializers.InstantSerializer;
import com.vaadin.flow.component.charts.model.serializers.PaneListSerializer;
import com.vaadin.flow.component.charts.model.serializers.SolidColorSerializer;
import com.vaadin.flow.component.charts.model.serializers.StopSerializer;
import com.vaadin.flow.component.charts.model.serializers.TimeUnitMultiplesSerializer;
import com.vaadin.flow.internal.CspViolationReporter;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.ser.SerializerFactory;
import tools.jackson.databind.ser.ValueSerializerModifier;

/**
 * Util class that handles the configuration needed for the model classes to be
 * serialized to JSON.
 * 
 * @since 6.0.1
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
                // A function is left out of JSON that does not collect
                // functions
                .withConfigOverride(ChartFunction.class,
                        override -> override.setInclude(JsonInclude.Value
                                .construct(JsonInclude.Include.NON_EMPTY,
                                        JsonInclude.Include.NON_EMPTY)))
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
                .addModule(InstantSerializer.getModule())
                .addModule(ChartFunctionSerializer.getModule());

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
     * <p>
     * A writer that is not built from {@link #createObjectMapper()} or
     * {@link #createObjectMapper(ValueSerializerModifier)} does not send the
     * {@link ChartFunction} values of a configuration to the browser.
     *
     * @param newObjectWriter
     * @see #createObjectMapper()
     */
    public static void setObjectMapperInstance(ObjectWriter newObjectWriter) {
        jsonWriter = newObjectWriter;
    }

    /**
     * Serializes the given configuration object to JSON. A
     * {@link ChartFunction} value is left out: use
     * {@link #toJsonWithFunctions(AbstractConfigurationObject)} to get the
     * functions as well.
     *
     * @param object
     *            the object to serialize
     * @return the JSON
     */
    public static String toJSON(AbstractConfigurationObject object) {
        try {
            return jsonWriter.writeValueAsString(object);
        } catch (JacksonException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while serializing "
                    + object.getClass().getSimpleName(), e);
        }
    }

    /**
     * Serializes the given configuration object to a JSON tree, and gets its
     * {@link ChartFunction} values separately, with the path of each in the
     * tree. The tree does not have the functions.
     * <p>
     * For internal use only. May be renamed or removed in a future release.
     *
     * @param object
     *            the object to serialize
     * @return the JSON tree and the functions
     */
    public static JsonWithFunctions toJsonWithFunctions(
            AbstractConfigurationObject object) {
        ChartFunctionSerializer.Collector collector = new ChartFunctionSerializer.Collector();
        JsonNode json;
        try {
            json = jsonWriter
                    .withAttribute(ChartFunctionSerializer.Collector.class,
                            collector)
                    .valueToTree(object);
        } catch (JacksonException e) {
            throw new RuntimeException("Error while serializing "
                    + object.getClass().getSimpleName(), e);
        }
        // Functions given as strings are only looked for when they are
        // reported
        collector.extract(json, CspViolationReporter.isEnabled());
        return new JsonWithFunctions(json, collector.getPaths(),
                List.copyOf(collector.getFunctions()),
                collector.hasStringFunction());
    }

    /**
     * A configuration object serialized to a JSON tree, and its
     * {@link ChartFunction} values.
     * <p>
     * For internal use only. May be renamed or removed in a future release.
     *
     * @param json
     *            the JSON tree, without the functions
     * @param functionPaths
     *            the path of each function in the tree, as an array of property
     *            names and array indexes
     * @param functions
     *            the functions, in the order of their paths
     * @param hasStringFunction
     *            whether the JSON has a function given as a string, which is
     *            only looked for when the CSP mode reports it
     */
    public record JsonWithFunctions(JsonNode json, ArrayNode functionPaths,
            List<ChartFunction> functions,
            boolean hasStringFunction) implements Serializable {

        /**
         * Gets whether the configuration object has any functions.
         *
         * @return <code>true</code> if there are functions
         */
        public boolean hasFunctions() {
            return !functions.isEmpty();
        }

        /**
         * Gets the functions as an array, for a varargs parameter.
         *
         * @return the functions, not <code>null</code>
         */
        public ChartFunction[] functionArray() {
            return functions.toArray(ChartFunction[]::new);
        }
    }
}
