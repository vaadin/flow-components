/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.charts.model.Labels;
import com.vaadin.flow.component.charts.model.Legend;
import com.vaadin.flow.component.charts.model.PlotOptionsLine;
import com.vaadin.flow.component.charts.model.Tooltip;

class ChartFunctionScannerTest {

    @SuppressWarnings("unused")
    static class Fixtures {
        static final String CONSTANT = "function () { return 'constant'; }";

        void literal(Tooltip tooltip) {
            tooltip.setFormatter("function () { return this.x; }");
        }

        void concatenatedLiterals(Labels labels) {
            labels.setFormatter("function () { " + "return this.value; }");
        }

        void staticFinalConstant(Legend legend) {
            legend.setLabelFormatter(CONSTANT);
        }

        void localVariable(Tooltip tooltip) {
            String formatter = "return this.y;";
            tooltip.setPointFormatter(formatter);
        }

        void inheritedSetter(PlotOptionsLine plotOptions) {
            plotOptions.setPointDescriptionFormatter("return 'line';");
        }

        void builtAtRuntime(Tooltip tooltip, String unit) {
            tooltip.setFormatter("return this.y + '" + unit + "';");
        }

        void notAFunctionProperty(Tooltip tooltip) {
            tooltip.setHeaderFormat("{point.key}");
        }
    }

    @Test
    void scanClass_findsConstantSourcesOfFunctionSetters() throws IOException {
        ChartFunctionScanner scanner = new ChartFunctionScanner(name -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        });

        Set<String> sources;
        try (InputStream in = Fixtures.class
                .getResourceAsStream(Fixtures.class.getName().replace('.', '/')
                        .replaceFirst("^.*/", "") + ".class")) {
            sources = scanner.scanClass(in);
        }

        Assertions.assertEquals(Set.of("function () { return this.x; }",
                "function () { return this.value; }",
                "function () { return 'constant'; }", "return this.y;",
                "return 'line';"), sources);
    }

    @Test
    void generate_registersExpressionsAndBodies() {
        String content = ChartFunctionsBootstrapModifier.generate(Set.of(
                "function () { return this.x; };", "return this.y + \"°\";"));

        Assertions.assertTrue(content.contains(
                "registry.set(\"function () { return this.x; };\", (\nfunction () { return this.x; }\n));"),
                content);
        Assertions.assertTrue(content.contains(
                "registry.set(\"return this.y + \\\"°\\\";\", function () {\nreturn this.y + \"°\";\n});"),
                content);
    }

    @Test
    void toFunction_recognizesArrowFunctions() {
        Assertions.assertEquals("(\n(a, b) => a + b\n)",
                ChartFunctionsBootstrapModifier.toFunction("(a, b) => a + b"));
        Assertions.assertEquals("(\npoint => point.x\n)",
                ChartFunctionsBootstrapModifier.toFunction("point => point.x"));
    }
}
