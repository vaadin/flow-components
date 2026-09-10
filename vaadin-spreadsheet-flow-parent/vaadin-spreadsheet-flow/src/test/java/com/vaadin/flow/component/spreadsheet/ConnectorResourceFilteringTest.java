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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Guards the module's Maven resource filtering.
 * <p>
 * {@code src/main/resources} holds both {@code spreadsheet.properties}, which
 * needs {@code ${version}} expanded at build time, and the connector's
 * TypeScript under {@code META-INF/frontend}, which must be copied verbatim.
 * Filtering the whole tree rewrites {@code ${...}} inside template literals
 * too: a binding such as <code>${name}</code> resolves against the POM and
 * becomes the literal project name, so the sheet tab renders "Vaadin
 * Spreadsheet" instead of the sheet's name. Nothing about that failure points
 * at the build, so these tests pin the split down.
 */
public class ConnectorResourceFilteringTest {

    private static final Path SOURCE = Path.of("src", "main", "resources",
            "META-INF", "frontend");

    private static final Path CLASSES = Path.of("target", "classes", "META-INF",
            "frontend");

    @Test
    public void frontendResources_copiedToClasses_areByteIdenticalToSources()
            throws IOException {
        assertTrue(Files.isDirectory(CLASSES),
                CLASSES + " is missing — run process-resources first");

        List<String> rewritten = new ArrayList<>();
        try (Stream<Path> sources = Files.walk(SOURCE)) {
            for (Path source : sources.filter(Files::isRegularFile).toList()) {
                Path copy = CLASSES.resolve(SOURCE.relativize(source));
                assertTrue(Files.exists(copy), "not copied: " + copy);
                if (Files.mismatch(source, copy) != -1L) {
                    rewritten.add(SOURCE.relativize(source).toString());
                }
            }
        }

        assertEquals(List.of(), rewritten,
                "Maven rewrote connector sources on the way to target/classes. "
                        + "Resource filtering must not cover META-INF/frontend.");
    }

    @Test
    public void frontendResources_onClasspath_keepUnexpandedBindings()
            throws IOException {
        // A binding that collides with a POM element, read the way Flow reads
        // it: off the classpath, not off disk.
        String formulaBar = readClasspathResource(
                "META-INF/frontend/vaadin-spreadsheet/lit/elements/SpreadsheetFormulaBar.ts");
        assertTrue(formulaBar.contains("${name}"),
                "SpreadsheetFormulaBar.ts lost its ${name} bindings");
        assertFalse(formulaBar.contains("<option value=${name}>Vaadin"),
                "SpreadsheetFormulaBar.ts was filtered by Maven");
    }

    @Test
    public void spreadsheetProperties_stillFiltered() throws IOException {
        Properties properties = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(
                "com/vaadin/flow/component/spreadsheet/spreadsheet.properties")) {
            assertTrue(in != null,
                    "spreadsheet.properties is not on classpath");
            properties.load(in);
        }

        String version = properties.getProperty("spreadsheet.version");
        assertFalse(version == null || version.isBlank(),
                "spreadsheet.version is missing");
        assertFalse(version.contains("${"),
                "spreadsheet.version was not filtered: " + version);
    }

    private String readClasspathResource(String name) throws IOException {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream(name)) {
            assertTrue(in != null, name + " is not on the classpath");
            return new String(in.readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
