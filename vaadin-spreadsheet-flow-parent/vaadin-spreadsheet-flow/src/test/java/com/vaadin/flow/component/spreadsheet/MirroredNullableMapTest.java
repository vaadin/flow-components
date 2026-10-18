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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.dom.Element;

import tools.jackson.databind.node.BaseJsonNode;

/**
 * Storing a map and mirroring it to the client with
 * {@link MirroredNullableMap}.
 *
 * <p>
 * The block has tricky semantics worth pinning explicitly: {@code set(null)}
 * clears the existing map IN PLACE (preserving the reference) while still
 * pushing serialized {@code null} to the client; {@code set(nonNull)} replaces
 * the reference and pushes the serialized non-null map. These branches must
 * behave differently for the migration to preserve the original
 * {@link Spreadsheet} setter contract.
 */
class MirroredNullableMapTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private Element element;

    private MirroredNullableMap<String, String> map;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(spreadsheet.getElement()).thenReturn(element);
        map = new MirroredNullableMap<>(spreadsheet, "myProp");
    }

    @Test
    void freshMap_isEmptyButNotNull() {
        // Pin: the field defaults to `new HashMap()`, not null. Callers
        // holding the reference can rely on get() never returning null.
        assertNotNull(map.get());
        assertTrue(map.get().isEmpty());
    }

    @Test
    void construction_doesNotEagerlyMirror() {
        verify(element, never()).setPropertyJson(anyString(),
                any(BaseJsonNode.class));
    }

    @Test
    void setNonNull_replacesReferenceAndMirrors() {
        HashMap<String, String> input = new HashMap<>();
        input.put("k", "v");

        map.set(input);

        // Reference replaced — get() returns the same object the caller
        // passed in.
        assertSame(input, map.get());
        // Element gets the map as JSON.
        verify(element).setPropertyJson(eq("myProp"),
                argThat(json -> "{\"k\":\"v\"}".equals(json.toString())));
    }

    @Test
    void setNull_clearsExistingMapInPlace_preservingReference() {
        // Pin the most surprising semantic: set(null) does NOT null the
        // field; it CLEARS the existing map IN PLACE. The caller's reference
        // to the prior map survives, now empty.
        HashMap<String, String> initial = new HashMap<>();
        initial.put("k", "v");
        map.set(initial);

        HashMap<String, String> preNullRef = map.get();
        assertSame(initial, preNullRef);

        map.set(null);

        // Same reference as before the null call.
        assertSame(preNullRef, map.get());
        // But now empty.
        assertTrue(preNullRef.isEmpty());
    }

    @Test
    void setNull_stillPushesSerializedNullToElement() {
        // Pin: even though the server-side state is "empty map", the client
        // receives JSON's null. The client-side handler must treat null and {}
        // equivalently.
        map.set(null);
        verify(element).setPropertyJson(eq("myProp"),
                argThat(BaseJsonNode::isNull));
    }

    @Test
    void setNonNull_pushesJson() {
        HashMap<String, String> input = new HashMap<>();
        input.put("k", "v");
        map.set(input);
        // The push is the map as JSON, not text holding it.
        verify(element).setPropertyJson(eq("myProp"),
                argThat(json -> json.isObject()
                        && "v".equals(json.get("k").asString())));
    }

    @Test
    void repeatedSetNull_doesNotNpe() {
        // Pin: set(null) is safe to call repeatedly. The clear() call on
        // an already-empty map is a no-op.
        map.set(null);
        map.set(null);
        // Two element pushes, both JSON's null.
        verify(element, org.mockito.Mockito.times(2))
                .setPropertyJson(eq("myProp"), argThat(BaseJsonNode::isNull));
        assertNotNull(map.get());
        assertTrue(map.get().isEmpty());
    }

    @Test
    void setNonNull_overwritesPriorContent() {
        HashMap<String, String> first = new HashMap<>();
        first.put("a", "1");
        map.set(first);

        HashMap<String, String> second = new HashMap<>();
        second.put("b", "2");
        map.set(second);

        // get() returns the second map, not a merge.
        assertSame(second, map.get());
        assertEquals(1, map.get().size());
        assertEquals("2", map.get().get("b"));
    }
}
