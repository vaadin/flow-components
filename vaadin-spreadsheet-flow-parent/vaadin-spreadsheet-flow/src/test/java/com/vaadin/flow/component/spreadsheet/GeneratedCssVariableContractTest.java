/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Every CSS custom property the server writes into the style rules it generates
 * has to be defined by the component's stylesheet.
 * <p>
 * The colour converters emit declarations like
 * {@code background-color: var(--default-background-color)} into the per-cell
 * {@code cs<N>} rules. An undefined {@code var()} is invalid at computed-value
 * time, so the declaration is dropped and the property falls back to its
 * initial value — for a background that is transparent, not white. Nothing
 * errors; five conditional-formatting suites simply read the wrong colour,
 * because they check what an unformatted cell looks like.
 */
public class GeneratedCssVariableContractTest {

    private static final String STYLESHEET_RESOURCE = "META-INF/frontend/vaadin-spreadsheet/vaadin-spreadsheet-styles.js";

    private static final Path JAVA_SOURCES = Path.of("src", "main", "java",
            "com", "vaadin", "flow", "component", "spreadsheet");

    /** {@code var(--name}, in a Java string literal the server emits. */
    private static final Pattern VAR_USE = Pattern
            .compile("var\\((--[a-zA-Z0-9-]+)");

    @Test
    public void everyGeneratedVariable_isDefinedByTheStylesheet()
            throws IOException {
        String stylesheet = readResource(STYLESHEET_RESOURCE);

        List<String> undefined = new ArrayList<>();
        for (String name : generatedVariables()) {
            // A definition is `--name:`; a use is `var(--name`.
            if (!stylesheet.contains(name + ":")) {
                undefined.add(name);
            }
        }

        assertEquals(List.of(), undefined,
                "the stylesheet does not define custom properties the server writes into its generated CSS");
    }

    /** Custom properties named inside the module's own Java sources. */
    private Set<String> generatedVariables() throws IOException {
        Set<String> names = new LinkedHashSet<>();
        try (Stream<Path> sources = Files.walk(JAVA_SOURCES)) {
            for (Path source : sources
                    .filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher m = VAR_USE.matcher(Files.readString(source));
                while (m.find()) {
                    names.add(m.group(1));
                }
            }
        }
        assertTrue(!names.isEmpty(),
                "found no var(--...) uses in the server sources — has the "
                        + "generated-CSS shape changed?");
        return names;
    }

    private String readResource(String name) throws IOException {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream(name)) {
            assertTrue(in != null, name + " is not on the classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
