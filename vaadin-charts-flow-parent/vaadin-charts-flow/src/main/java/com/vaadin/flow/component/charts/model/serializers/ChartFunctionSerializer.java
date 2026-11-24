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
 * {@link Collector#extract(JsonNode, boolean)} removes again. Without a
 * collector, the option is left out: the function counts as empty, and
 * {@link com.vaadin.flow.component.charts.util.ChartSerialization} includes
 * properties of type {@link ChartFunction} only when they are not empty.
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

    @Override
    public boolean isEmpty(SerializationContext context, ChartFunction value) {
        return value == null || !(context
                .getAttribute(Collector.class) instanceof Collector);
    }

    /**
     * Collects the functions of one serialization, and finds where they are in
     * the resulting JSON.
     * <p>
     * The path of a function can not be taken while serializing: the generator
     * that builds a JSON tree does not keep track of array indexes. So a
     * placeholder is written instead, and found in the tree afterwards, in the
     * same walk that looks for functions given as strings.
     * <p>
     * For internal use only. May be renamed or removed in a future release.
     */
    public static final class Collector implements Serializable {

        /**
         * The prefix of the JSON keys whose string value the browser compiles
         * into a function.
         */
        private static final String STRING_FUNCTION_PREFIX = "_fn_";

        // Random for each serialization, so that a string in the chart data
        // can not be mistaken for a placeholder
        private final String placeholderPrefix = "\u0000chart-function:"
                + UUID.randomUUID() + ":";

        private final List<ChartFunction> collected = new ArrayList<>();

        private final ArrayNode paths = JacksonUtils.createArrayNode();

        private final List<ChartFunction> functions = new ArrayList<>();

        private boolean stringFunction;

        private boolean findStringFunctions;

        private String add(ChartFunction function) {
            collected.add(function);
            return placeholderPrefix + (collected.size() - 1);
        }

        /**
         * Removes the placeholders from the given JSON, and records the path
         * and the function of each. A placeholder in an object is removed with
         * its property, and one in an array is replaced with <code>null</code>.
         * In the same walk, optionally looks for a function given as a string,
         * under a key that starts with <code>_fn_</code>.
         * <p>
         * Does not walk the JSON when there are no placeholders and no string
         * functions to look for.
         *
         * @param json
         *            the JSON written with this collector, not
         *            <code>null</code>
         * @param findStringFunctions
         *            whether to look for functions given as strings
         */
        public void extract(JsonNode json, boolean findStringFunctions) {
            this.findStringFunctions = findStringFunctions;
            if (!collected.isEmpty() || findStringFunctions) {
                extract(json, new ArrayList<>());
            }
        }

        /**
         * Gets the paths of the functions found by
         * {@link #extract(JsonNode, boolean)}, in the order of
         * {@link #getFunctions()}. Each path is an array of property names and
         * array indexes.
         *
         * @return the paths, not <code>null</code>
         */
        public ArrayNode getPaths() {
            return paths;
        }

        /**
         * Gets the functions found by {@link #extract(JsonNode, boolean)}.
         *
         * @return the functions, not <code>null</code>
         */
        public List<ChartFunction> getFunctions() {
            return functions;
        }

        /**
         * Gets whether {@link #extract(JsonNode, boolean)} found a function
         * given as a string.
         *
         * @return <code>true</code> if the JSON has a function as a string
         */
        public boolean hasStringFunction() {
            return stringFunction;
        }

        /**
         * Walks the given node. The path of the node is kept in a list shared
         * by the whole walk, and is copied only for a placeholder.
         */
        private void extract(JsonNode node, List<Object> path) {
            if (node instanceof ObjectNode object) {
                List<String> placeholderKeys = null;
                for (Map.Entry<String, JsonNode> property : object
                        .properties()) {
                    String key = property.getKey();
                    JsonNode value = property.getValue();
                    if (findStringFunctions
                            && key.startsWith(STRING_FUNCTION_PREFIX)
                            && value.isString()) {
                        stringFunction = true;
                    } else if (isPlaceholder(value)) {
                        record(value, path, key);
                        if (placeholderKeys == null) {
                            placeholderKeys = new ArrayList<>();
                        }
                        placeholderKeys.add(key);
                    } else if (value.isContainer()) {
                        path.add(key);
                        extract(value, path);
                        path.removeLast();
                    }
                }
                if (placeholderKeys != null) {
                    object.remove(placeholderKeys);
                }
            } else if (node instanceof ArrayNode array) {
                for (int index = 0; index < array.size(); index++) {
                    JsonNode value = array.get(index);
                    if (isPlaceholder(value)) {
                        record(value, path, index);
                        array.set(index, NullNode.getInstance());
                    } else if (value.isContainer()) {
                        path.add(index);
                        extract(value, path);
                        path.removeLast();
                    }
                }
            }
        }

        private boolean isPlaceholder(JsonNode value) {
            return !collected.isEmpty() && value.isString()
                    && value.asString().startsWith(placeholderPrefix);
        }

        private void record(JsonNode placeholder, List<Object> path,
                Object last) {
            ArrayNode fullPath = paths.addArray();
            for (Object segment : path) {
                addSegment(fullPath, segment);
            }
            addSegment(fullPath, last);
            int index = Integer.parseInt(placeholder.asString()
                    .substring(placeholderPrefix.length()));
            functions.add(collected.get(index));
        }

        private static void addSegment(ArrayNode path, Object segment) {
            if (segment instanceof Integer index) {
                path.add(index);
            } else {
                path.add((String) segment);
            }
        }
    }
}
