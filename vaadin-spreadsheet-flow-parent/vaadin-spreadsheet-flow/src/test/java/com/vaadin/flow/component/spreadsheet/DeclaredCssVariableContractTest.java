/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Every {@code --vaadin-spreadsheet-*} property the stylesheet declares has to
 * be read by something.
 * <p>
 * The stylesheet's {@code :host} block is where the documented styling
 * properties live, so declaring one is a promise that setting it changes
 * something. {@code --vaadin-spreadsheet-header-border-color} was declared,
 * documented, and read by nothing for the whole of the Lit rewrite: the headers
 * took an undocumented {@code --vaadin-spreadsheet-header-border} with a
 * different default, so the documented property did nothing and the header
 * borders came out a different grey from the cell borders next to them. Nothing
 * caught it, because a property that resolves to its own declared value looks
 * exactly like one that is working.
 */
public class DeclaredCssVariableContractTest {

    private static final Path STYLESHEET = Path.of("src", "main", "resources",
            "META-INF", "frontend", "vaadin-spreadsheet",
            "vaadin-spreadsheet-styles.js");

    private static final Path CLIENT = Path.of("src", "main", "resources",
            "META-INF", "frontend", "vaadin-spreadsheet");

    /** A declaration: {@code --name:} at the start of a line. */
    private static final Pattern DECLARATION = Pattern
            .compile("^\\s*(--vaadin-spreadsheet-[a-zA-Z0-9-]+)\\s*:");

    @Test
    public void everyDeclaredProperty_isReadBySomething() throws IOException {
        String client = readClientSources();

        List<String> unread = new ArrayList<>();
        for (String name : declaredProperties()) {
            if (!client.contains("var(" + name)) {
                unread.add(name);
            }
        }

        assertTrue(unread.isEmpty(),
                "Declared but never read, so setting them does nothing: "
                        + unread);
    }

    /** A read with a fallback: {@code var(--name, fallback)}. */
    private static final Pattern READ_WITH_FALLBACK = Pattern.compile(
            "var\\((--vaadin-spreadsheet-[a-zA-Z0-9-]+),\\s*([^)]+)\\)");

    @Test
    public void noProperty_isReadWithTwoDifferentDefaults() throws IOException {
        Map<String, Set<String>> defaults = new LinkedHashMap<>();
        Matcher matcher = READ_WITH_FALLBACK.matcher(readClientSources());
        while (matcher.find()) {
            defaults.computeIfAbsent(matcher.group(1),
                    key -> new LinkedHashSet<>())
                    .add(matcher.group(2).trim().replaceAll("\\s+", " "));
        }

        List<String> conflicting = defaults.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> entry.getKey() + " " + entry.getValue()).toList();

        assertTrue(conflicting.isEmpty(),
                "One property cannot mean two things: surfaces that share it"
                        + " render differently until someone sets it, and then"
                        + " they collapse into one colour. " + conflicting);
    }

    private Set<String> declaredProperties() throws IOException {
        Set<String> names = new LinkedHashSet<>();
        for (String line : Files.readAllLines(STYLESHEET,
                StandardCharsets.UTF_8)) {
            Matcher matcher = DECLARATION.matcher(line);
            if (matcher.find()) {
                names.add(matcher.group(1));
            }
        }
        return names;
    }

    /** Every client source but the stylesheet's own declarations. */
    private String readClientSources() throws IOException {
        StringBuilder all = new StringBuilder();
        try (Stream<Path> files = Files.walk(CLIENT)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                all.append(Files.readString(file, StandardCharsets.UTF_8));
            }
        }
        return all.toString();
    }
}
