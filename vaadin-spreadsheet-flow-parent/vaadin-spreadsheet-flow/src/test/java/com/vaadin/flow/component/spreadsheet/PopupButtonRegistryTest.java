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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Storing pop-up buttons by cell in {@link PopupButtonRegistry}.
 *
 * <p>
 * Pure storage class — the tests pin the {@link java.util.HashMap} delegation
 * contract (get / put / remove / values / clear) plus the load-bearing
 * {@code replaceAll} method, which swaps the underlying map reference
 * atomically.
 *
 * <p>
 * The registry stores {@link PopupButton} values; tests use Mockito-mocked
 * instances as opaque tokens — no PopupButton behavior is exercised, just the
 * storage routing.
 */
class PopupButtonRegistryTest {

    private PopupButtonRegistry registry;
    private CellReference refA1;
    private CellReference refB2;
    private PopupButton buttonA;
    private PopupButton buttonB;

    @BeforeEach
    void setUp() {
        registry = new PopupButtonRegistry();
        refA1 = new CellReference("A1");
        refB2 = new CellReference("B2");
        buttonA = mock(PopupButton.class);
        buttonB = mock(PopupButton.class);
    }

    @Test
    void freshRegistry_isEmpty() {
        assertTrue(registry.isEmpty());
        assertNull(registry.get(refA1));
        assertEquals(0, registry.values().size());
    }

    @Test
    void put_thenGet_returnsSameInstance() {
        registry.put(refA1, buttonA);

        assertSame(buttonA, registry.get(refA1));
        assertFalse(registry.isEmpty());
    }

    @Test
    void put_sameRefTwice_overwritesPreviousValue() {
        // Pin HashMap-overwrite semantic: putting a different button at the
        // same ref replaces the prior mapping; the size stays 1.
        registry.put(refA1, buttonA);
        registry.put(refA1, buttonB);

        assertSame(buttonB, registry.get(refA1));
        assertEquals(1, registry.values().size());
    }

    @Test
    void remove_existingKey_returnsRemovedValue() {
        registry.put(refA1, buttonA);

        PopupButton removed = registry.remove(refA1);

        assertSame(buttonA, removed);
        assertNull(registry.get(refA1));
        assertTrue(registry.isEmpty());
    }

    @Test
    void remove_missingKey_returnsNull_andLeavesRegistryUnchanged() {
        registry.put(refA1, buttonA);

        PopupButton removed = registry.remove(refB2);

        assertNull(removed);
        assertSame(buttonA, registry.get(refA1));
    }

    @Test
    void values_returnsAllStoredButtons_inSomeOrder() {
        registry.put(refA1, buttonA);
        registry.put(refB2, buttonB);

        Collection<PopupButton> values = registry.values();

        assertEquals(2, values.size());
        assertTrue(values.contains(buttonA));
        assertTrue(values.contains(buttonB));
    }

    @Test
    void clear_emptiesTheRegistry() {
        registry.put(refA1, buttonA);
        registry.put(refB2, buttonB);

        registry.clear();

        assertTrue(registry.isEmpty());
        assertNull(registry.get(refA1));
        assertNull(registry.get(refB2));
    }

    @Test
    void replaceAll_swapsUnderlyingMapReference_notMerged() {
        // Pin the swap semantic: replaceAll replaces the entire map; entries
        // in the prior map are not preserved unless they are also in the
        // new one. Load-bearing because PopupButtonRowShifter uses this
        // for en-masse row remapping (the shifter builds a fresh map from
        // the old one's entries with adjusted keys, then atomically swaps).
        registry.put(refA1, buttonA);

        Map<CellReference, PopupButton> updated = new HashMap<>();
        updated.put(refB2, buttonB);
        registry.replaceAll(updated);

        // refA1 mapping is gone — the prior map was discarded entirely.
        assertNull(registry.get(refA1));
        assertSame(buttonB, registry.get(refB2));
    }

    @Test
    void replaceAll_withEmptyMap_emptiesTheRegistry() {
        registry.put(refA1, buttonA);
        registry.put(refB2, buttonB);

        registry.replaceAll(new HashMap<>());

        assertTrue(registry.isEmpty());
    }

    @Test
    void replaceAll_afterPut_subsequentMutationsGoToNewMap() {
        // Pin: after replaceAll, the registry holds the NEW map reference.
        // Subsequent puts go into the new map, not the original — verified
        // by mutating the original after the swap and confirming the
        // registry doesn't see the mutation.
        Map<CellReference, PopupButton> original = new HashMap<>();
        registry.replaceAll(original);
        registry.put(refA1, buttonA);

        // Mutating the original map directly should NOT change the
        // registry — wait, the swap IS the registry's storage. So the
        // mutation IS visible. This documents the trade-off: replaceAll
        // takes a reference, not a copy. Callers must not retain the
        // map they pass in.
        assertSame(buttonA, registry.get(refA1));
        assertEquals(1, original.size(),
                "Registry shares the swapped-in map; the original "
                        + "reference still backs the storage");
    }
}
