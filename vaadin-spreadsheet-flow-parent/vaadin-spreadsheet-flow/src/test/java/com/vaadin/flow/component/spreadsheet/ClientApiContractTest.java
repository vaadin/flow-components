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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Pins the server-to-client call names across the three files that have to
 * agree on them.
 * <p>
 * A server-to-client call passes through two hops:
 * {@code getElement().callJsFunction("x")} reaches the {@code
 * <vaadin-spreadsheet>} wrapper element, which forwards to the connector as
 * {@code this.$connector.x()}. Neither hop fails loudly when the name is wrong
 * — Flow logs nothing an integration test can see, and the feature simply never
 * happens.
 * <p>
 * {@code onPopupButtonOpen} shipped with the wrapper forwarding to
 * {@code onPopupButtonOpened}, so no popup button ever opened its popup, from a
 * click or from {@code PopupButton.openPopup()}.
 */
public class ClientApiContractTest {

    private static final String WRAPPER_RESOURCE = "META-INF/frontend/vaadin-spreadsheet/vaadin-spreadsheet.js";

    private static final String CONNECTOR_RESOURCE = "META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.ts";

    private static final Path JAVA_SOURCES = Path.of("src", "main", "java",
            "com", "vaadin", "flow", "component", "spreadsheet");

    /**
     * Element properties the wrapper deliberately does not declare.
     * {@code appId} is read straight off the element by the connector's
     * virtual-child resolver rather than pushed through a setter.
     */
    private static final Set<String> UNDECLARED_BY_DESIGN = Set.of("appId");

    /** {@code callJsFunction("name"} in any of the module's Java sources. */
    private static final Pattern CALL_JS = Pattern
            .compile("callJsFunction\\(\\s*\"([^\"]+)\"");

    private static final Pattern SET_PROPERTY = Pattern
            .compile("\\.setProperty\\(\\s*\"([^\"]+)\"");

    /** A method on the wrapper element — two-space indent, class body level. */
    private static final Pattern WRAPPER_METHOD = Pattern
            .compile("^ {2}([a-zA-Z_$][\\w$]*)\\s*\\(", Pattern.MULTILINE);

    private static final Pattern WRAPPER_FORWARD = Pattern.compile(
            "^ {2}([a-zA-Z_$][\\w$]*)\\s*\\([^)]*\\)\\s*\\{.*?this\\.\\$connector\\.([a-zA-Z_$][\\w$]*)\\s*\\(",
            Pattern.MULTILINE | Pattern.DOTALL);

    /** A property handled in the wrapper's {@code updated()} dispatch. */
    private static final Pattern PROPERTY_BRANCH = Pattern.compile(
            "'([a-zA-Z_$][\\w$]*)'\\s*==\\s*name\\s*\\)\\s*\\{\\s*this\\.\\$connector\\.([a-zA-Z_$][\\w$]*)\\s*\\(");

    private static final Pattern PROPERTY_DECLARED = Pattern
            .compile("^ {6}([a-zA-Z_$][\\w$]*)\\s*:\\s*\\{", Pattern.MULTILINE);

    /** A member of the connector class — two-space indent, method or arrow. */
    private static final Pattern CONNECTOR_MEMBER = Pattern.compile(
            "^ {2}(?:private\\s+|public\\s+|readonly\\s+)*([a-zA-Z_$][\\w$]*)\\s*[(=]",
            Pattern.MULTILINE);

    @Test
    public void everyServerCall_hasAMethodOnTheWrapperElement()
            throws IOException {
        Set<String> wrapperMethods = matches(WRAPPER_METHOD,
                readResource(WRAPPER_RESOURCE));

        List<String> missing = new ArrayList<>();
        for (String call : new TreeSet<>(serverCalls())) {
            if (!wrapperMethods.contains(call)) {
                missing.add(call);
            }
        }
        assertEquals(List.of(), missing,
                "callJsFunction names with nothing to call on <vaadin-spreadsheet>");
    }

    @Test
    public void everyWrapperForward_namesAConnectorMethod() throws IOException {
        Set<String> connectorMembers = matches(CONNECTOR_MEMBER,
                readResource(CONNECTOR_RESOURCE));
        String wrapper = readResource(WRAPPER_RESOURCE);

        Map<String, String> forwards = new LinkedHashMap<>();
        Matcher m = WRAPPER_FORWARD.matcher(wrapper);
        while (m.find()) {
            forwards.put(m.group(1), m.group(2));
        }
        assertTrue(forwards.size() > 10,
                "failed to parse the wrapper's forwards: " + forwards);

        List<String> broken = new ArrayList<>();
        forwards.forEach((method, target) -> {
            if (!connectorMembers.contains(target)) {
                broken.add(method + " -> $connector." + target);
            }
        });
        assertEquals(List.of(), broken,
                "the wrapper forwards to connector methods that do not exist");
    }

    @Test
    public void everyServerProperty_reachesTheConnector() throws IOException {
        String wrapper = readResource(WRAPPER_RESOURCE);
        Set<String> connectorMembers = matches(CONNECTOR_MEMBER,
                readResource(CONNECTOR_RESOURCE));
        Set<String> declared = matches(PROPERTY_DECLARED, wrapper);

        Map<String, String> handled = new LinkedHashMap<>();
        Matcher m = PROPERTY_BRANCH.matcher(wrapper);
        while (m.find()) {
            handled.put(m.group(1), m.group(2));
        }

        List<String> problems = new ArrayList<>();
        for (String property : new TreeSet<>(serverProperties())) {
            if (UNDECLARED_BY_DESIGN.contains(property)) {
                continue;
            }
            if (!declared.contains(property)) {
                problems.add(property + ": not a declared wrapper property");
            } else if (handled.containsKey(property)
                    && !connectorMembers.contains(handled.get(property))) {
                problems.add(property + ": forwarded to a missing connector "
                        + "method " + handled.get(property));
            }
        }
        assertEquals(List.of(), problems,
                "element properties the server writes that never reach the connector");
    }

    private Set<String> serverCalls() throws IOException {
        Set<String> calls = scanJava(CALL_JS);
        // The RPC interface's javadoc shows the call shape with a
        // placeholder name; it is documentation, not a call.
        calls.remove("methodName");
        assertTrue(calls.size() > 10, "failed to find callJsFunction uses");
        return calls;
    }

    private Set<String> serverProperties() throws IOException {
        Set<String> properties = scanJava(SET_PROPERTY);
        assertTrue(properties.size() > 10, "failed to find setProperty uses");
        return properties;
    }

    private Set<String> scanJava(Pattern pattern) throws IOException {
        Set<String> found = new LinkedHashSet<>();
        try (Stream<Path> sources = Files.walk(JAVA_SOURCES)) {
            for (Path source : sources
                    .filter(p -> p.toString().endsWith(".java")).toList()) {
                found.addAll(matches(pattern, Files.readString(source)));
            }
        }
        return found;
    }

    private Set<String> matches(Pattern pattern, String text) {
        Set<String> found = new LinkedHashSet<>();
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            found.add(m.group(1));
        }
        return found;
    }

    private String readResource(String name) throws IOException {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream(name)) {
            assertTrue(in != null, name + " is not on the classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
