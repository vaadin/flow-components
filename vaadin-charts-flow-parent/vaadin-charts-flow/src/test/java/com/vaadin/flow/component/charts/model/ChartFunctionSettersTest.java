/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.component.charts.util.ChartSerialization.JsonWithFunctions;
import com.vaadin.flow.js.JsDefinition;
import com.vaadin.flow.js.JsExpression;
import com.vaadin.flow.js.JsSnippets;

/**
 * Checks the {@link ChartFunction} setter of every function option of the
 * model, against the String setter of the same option.
 */
class ChartFunctionSettersTest {

    @JsDefinition
    public interface Functions extends JsSnippets {
        @JsExpression("return this.y;")
        ChartFunction function();
    }

    private static final ChartFunction FUNCTION = JsSnippets.of(Functions.class)
            .function();

    private static final String STRING_FUNCTION = "function() { return 1; }";

    // String setters that are not named after their option
    private static final Map<String, String> STRING_SETTERS = Map
            .of("ExportingMenuItemDefinition.onclick", "setOnonclick");

    private record FunctionOption(Class<?> type, String option, Method getter,
            Method setter, Method stringGetter, Method stringSetter) {

        @Override
        public String toString() {
            return type.getSimpleName() + "." + option;
        }
    }

    @Test
    void everyFunctionOptionHasFunctionSetter() throws Exception {
        Assertions.assertEquals(53, functionOptions().size());
    }

    @Test
    void setFunction_clearsStringFunction() throws Exception {
        for (FunctionOption option : functionOptions()) {
            Object object = option.type().getConstructor().newInstance();
            option.stringSetter().invoke(object, STRING_FUNCTION);

            option.setter().invoke(object, FUNCTION);

            Assertions.assertSame(FUNCTION, option.getter().invoke(object),
                    option.toString());
            Assertions.assertNull(option.stringGetter().invoke(object),
                    option.toString());
        }
    }

    @Test
    void setStringFunction_clearsFunction() throws Exception {
        for (FunctionOption option : functionOptions()) {
            Object object = option.type().getConstructor().newInstance();
            option.setter().invoke(object, FUNCTION);

            option.stringSetter().invoke(object, STRING_FUNCTION);

            Assertions.assertNull(option.getter().invoke(object),
                    option.toString());
            Assertions.assertEquals(STRING_FUNCTION,
                    option.stringGetter().invoke(object), option.toString());
        }
    }

    @Test
    void setFunction_functionCollectedWithItsPath() throws Exception {
        for (FunctionOption option : functionOptions()) {
            AbstractConfigurationObject object = (AbstractConfigurationObject) option
                    .type().getConstructor().newInstance();
            option.setter().invoke(object, FUNCTION);

            JsonWithFunctions json = ChartSerialization
                    .toJsonWithFunctions(object);

            Assertions.assertEquals(List.of(FUNCTION), json.functions(),
                    option.toString());
            Assertions.assertEquals("[[\"" + option.option() + "\"]]",
                    json.functionPaths().toString(), option.toString());
            Assertions.assertFalse(json.json().has(option.option()),
                    option.toString());
            Assertions.assertFalse(json.json().has("_fn_" + option.option()),
                    option.toString());
        }
    }

    private static List<FunctionOption> functionOptions() throws Exception {
        List<FunctionOption> options = new ArrayList<>();
        for (Class<?> type : modelClasses()) {
            for (Method setter : type.getMethods()) {
                String name = setter.getName();
                if (!name.startsWith("set") || !name.endsWith("Function")
                        || setter.getParameterCount() != 1 || setter
                                .getParameterTypes()[0] != ChartFunction.class) {
                    continue;
                }
                String capitalized = name.substring(3,
                        name.length() - "Function".length());
                String option = Character.toLowerCase(capitalized.charAt(0))
                        + capitalized.substring(1);
                String stringSetter = STRING_SETTERS.getOrDefault(
                        type.getSimpleName() + "." + option,
                        "set" + capitalized);
                options.add(new FunctionOption(type, option,
                        type.getMethod("get" + capitalized + "Function"),
                        setter, type.getMethod("get" + capitalized),
                        type.getMethod(stringSetter, String.class)));
            }
        }
        return options;
    }

    /**
     * Gets the concrete configuration classes of the model package.
     */
    private static List<Class<?>> modelClasses() throws Exception {
        Path directory = Path
                .of(ChartFunction.class.getProtectionDomain().getCodeSource()
                        .getLocation().toURI())
                .resolve(
                        ChartFunction.class.getPackageName().replace('.', '/'));
        List<Class<?>> classes = new ArrayList<>();
        try (Stream<Path> files = Files.list(directory)) {
            for (Path file : files.toList()) {
                String fileName = file.getFileName().toString();
                if (!fileName.endsWith(".class") || fileName.contains("$")) {
                    continue;
                }
                Class<?> type = Class
                        .forName(ChartFunction.class.getPackageName() + "."
                                + fileName.substring(0,
                                        fileName.length() - ".class".length()));
                if (AbstractConfigurationObject.class.isAssignableFrom(type)
                        && !Modifier.isAbstract(type.getModifiers())) {
                    classes.add(type);
                }
            }
        }
        return classes;
    }
}
