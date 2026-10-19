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

import org.junit.jupiter.api.Test;

/**
 * Pins the client-to-server payload keys.
 * <p>
 * The connector names the keys, {@link SpreadsheetEventListener} reads them,
 * and nothing connects the two: a key the connector sends but the listener
 * never reads makes the event silently do nothing, with no error on either
 * side. {@code sheetSelected} shipped sending {@code sheetName} while the
 * listener read {@code sheetIndex}, so every sheet-tab click activated sheet 0
 * and the failures showed up several steps away, as lost selections.
 * <p>
 * {@code SERVER_EVENT_PAYLOAD_KEYS} in {@code rpc-client-to-server.ts} is the
 * one source of truth. This test holds the listener to it; the wtr suite holds
 * the connector's dispatches to it.
 */
public class ServerEventPayloadContractTest {

    private static final String CONTRACT_RESOURCE = "META-INF/frontend/vaadin-spreadsheet/"
            + "lit/types/rpc-client-to-server.ts";

    private static final Path LISTENER_SOURCE = Path.of("src", "main", "java",
            "com", "vaadin", "flow", "component", "spreadsheet",
            "SpreadsheetEventListener.java");

    /**
     * {@code "<type>".equals(type)} opens a branch; it also ends the one
     * before.
     */
    private static final Pattern BRANCH = Pattern
            .compile("\"([a-zA-Z]+)\"\\.equals\\(type\\)");

    /**
     * Every payload key the listener reads is a string literal argument that
     * follows {@code pars} in a call to one of its accessor helpers.
     */
    private static final Pattern PAYLOAD_KEY = Pattern
            .compile("\\(\\s*pars\\s*,((?:\\s*\"[^\"]+\"\\s*,?)+)\\)");

    /** Java string literals use double quotes, TypeScript ones single. */
    private static final Pattern QUOTED = Pattern
            .compile("\"([^\"]+)\"|'([^']+)'");

    @Test
    public void listener_readsExactlyTheContractKeys() throws IOException {
        Map<String, Set<String>> contract = readContract();
        Map<String, Set<String>> listener = readListener();

        assertEquals(contract.keySet(), listener.keySet(),
                "The contract and the listener disagree about which events exist");

        List<String> problems = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : contract.entrySet()) {
            String event = entry.getKey();
            Set<String> declared = entry.getValue();
            Set<String> read = listener.get(event);
            if (!declared.equals(read)) {
                problems.add(event + ": contract " + new TreeSet<>(declared)
                        + " but the listener reads " + new TreeSet<>(read));
            }
        }
        assertEquals(List.of(), problems,
                "SpreadsheetEventListener does not read the keys the connector sends");
    }

    @Test
    public void contract_coversEveryDeclaredEventType() throws IOException {
        String source = readResource(CONTRACT_RESOURCE);
        int start = source.indexOf("export type SpreadsheetEventType =");
        String union = source.substring(start, source.indexOf(';', start));

        Set<String> declared = new LinkedHashSet<>();
        Matcher m = Pattern.compile("\\|\\s*'([a-zA-Z]+)'").matcher(union);
        while (m.find()) {
            declared.add(m.group(1));
        }

        assertTrue(declared.size() > 30,
                "failed to parse the event-type union: " + declared);
        assertEquals(declared, readContract().keySet(),
                "SERVER_EVENT_PAYLOAD_KEYS and SpreadsheetEventType disagree");
    }

    /** The {@code SERVER_EVENT_PAYLOAD_KEYS} map, read off the classpath. */
    private Map<String, Set<String>> readContract() throws IOException {
        String source = readResource(CONTRACT_RESOURCE);
        int start = source.indexOf("export const SERVER_EVENT_PAYLOAD_KEYS");
        assertTrue(start >= 0, "SERVER_EVENT_PAYLOAD_KEYS is missing");
        String body = source.substring(source.indexOf('{', start),
                source.indexOf("\n};", start));

        Map<String, Set<String>> contract = new LinkedHashMap<>();
        Matcher m = Pattern.compile("([a-zA-Z]+)\\s*:\\s*\\[([^\\]]*)\\]")
                .matcher(body);
        while (m.find()) {
            contract.put(m.group(1), quotedStrings(m.group(2)));
        }
        assertTrue(contract.size() > 30,
                "failed to parse the contract map: " + contract.keySet());
        return contract;
    }

    /** The keys each branch of the listener actually reads. */
    private Map<String, Set<String>> readListener() throws IOException {
        String source = Files.readString(LISTENER_SOURCE);
        String dispatch = source.substring(source.indexOf("onComponentEvent"));

        Map<String, Set<String>> listener = new LinkedHashMap<>();
        Matcher branches = BRANCH.matcher(dispatch);
        List<String> names = new ArrayList<>();
        List<Integer> starts = new ArrayList<>();
        while (branches.find()) {
            names.add(branches.group(1));
            starts.add(branches.end());
        }
        for (int i = 0; i < names.size(); i++) {
            int end = i + 1 < names.size() ? starts.get(i + 1)
                    : dispatch.length();
            String body = dispatch.substring(starts.get(i), end);
            Set<String> keys = new LinkedHashSet<>();
            Matcher calls = PAYLOAD_KEY.matcher(body);
            while (calls.find()) {
                keys.addAll(quotedStrings(calls.group(1)));
            }
            // A branch may read a key more than once (min/max over the same
            // index array); the set already collapses that.
            listener.merge(names.get(i), keys, (a, b) -> {
                a.addAll(b);
                return a;
            });
        }
        return listener;
    }

    private Set<String> quotedStrings(String text) {
        Set<String> out = new LinkedHashSet<>();
        Matcher m = QUOTED.matcher(text);
        while (m.find()) {
            out.add(m.group(1) != null ? m.group(1) : m.group(2));
        }
        return out;
    }

    private String readResource(String name) throws IOException {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream(name)) {
            assertTrue(in != null, name + " is not on the classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
