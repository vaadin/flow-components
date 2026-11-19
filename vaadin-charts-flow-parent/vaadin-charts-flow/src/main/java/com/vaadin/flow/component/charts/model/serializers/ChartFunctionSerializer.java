/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model.serializers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vaadin.flow.component.charts.model.ChartFunction;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Serializer for {@link ChartFunction} values. A function is not part of the
 * chart JSON: it is sent to the browser as an argument of its own, together
 * with the path of the option it belongs to.
 * <p>
 * When the serialization has a {@link Collector} as an attribute, the function
 * is added to it and a placeholder is written, which
 * {@link Collector#extract(JsonNode)} removes again. Without a collector, the
 * option is written as <code>null</code>.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
public class ChartFunctionSerializer extends ValueSerializer<ChartFunction> {

    public static JacksonModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(ChartFunction.class,
                new ChartFunctionSerializer());
        return module;
    }

    @Override
    public void serialize(ChartFunction value, JsonGenerator gen,
            SerializationContext context) {
        if (context
                .getAttribute(Collector.class) instanceof Collector collector) {
            gen.writeString(collector.add(value));
        } else {
            gen.writeNull();
        }
    }

    /**
     * Collects the functions of one serialization, and finds where they are in
     * the resulting JSON.
     * <p>
     * For internal use only. May be renamed or removed in a future release.
     */
    public static final class Collector implements Serializable {

        // Random for each serialization, so that a string in the chart data
        // can not be mistaken for a placeholder
        private final String placeholderPrefix = "\u0000chart-function:"
                + UUID.randomUUID() + ":";

        private final List<ChartFunction> collected = new ArrayList<>();

        private final ArrayNode paths = JacksonUtils.createArrayNode();

        private final List<ChartFunction> functions = new ArrayList<>();

        private String add(ChartFunction function) {
            collected.add(function);
            return placeholderPrefix + (collected.size() - 1);
        }

        /**
         * Removes the placeholders from the given JSON, and records the path
         * and the function of each. A placeholder in an object is removed with
         * its property, and one in an array is replaced with <code>null</code>.
         *
         * @param json
         *            the JSON written with this collector, not
         *            <code>null</code>
         */
        public void extract(JsonNode json) {
            if (!collected.isEmpty()) {
                extract(json, JacksonUtils.createArrayNode());
            }
        }

        /**
         * Gets the paths of the functions found by {@link #extract(JsonNode)},
         * in the order of {@link #getFunctions()}. Each path is an array of
         * property names and array indexes.
         *
         * @return the paths, not <code>null</code>
         */
        public ArrayNode getPaths() {
            return paths;
        }

        /**
         * Gets the functions found by {@link #extract(JsonNode)}.
         *
         * @return the functions, not <code>null</code>
         */
        public List<ChartFunction> getFunctions() {
            return functions;
        }

        private void extract(JsonNode node, ArrayNode path) {
            if (node instanceof ObjectNode object) {
                for (Map.Entry<String, JsonNode> property : List
                        .copyOf(object.properties())) {
                    ArrayNode childPath = path.deepCopy()
                            .add(property.getKey());
                    if (record(property.getValue(), childPath)) {
                        object.remove(property.getKey());
                    } else {
                        extract(property.getValue(), childPath);
                    }
                }
            } else if (node instanceof ArrayNode array) {
                for (int index = 0; index < array.size(); index++) {
                    ArrayNode childPath = path.deepCopy().add(index);
                    if (record(array.get(index), childPath)) {
                        array.set(index, NullNode.getInstance());
                    } else {
                        extract(array.get(index), childPath);
                    }
                }
            }
        }

        private boolean record(JsonNode value, ArrayNode path) {
            if (value == null || !value.isString()) {
                return false;
            }
            String text = value.asString();
            if (!text.startsWith(placeholderPrefix)) {
                return false;
            }
            int index = Integer
                    .parseInt(text.substring(placeholderPrefix.length()));
            paths.add(path);
            functions.add(collected.get(index));
            return true;
        }
    }
}
