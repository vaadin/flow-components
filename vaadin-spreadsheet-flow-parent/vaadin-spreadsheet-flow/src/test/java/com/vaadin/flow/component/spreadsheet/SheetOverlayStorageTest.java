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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The overlays {@link SheetOverlayStorage} holds.
 *
 * <p>
 * The set is lazy-initialized via {@link SheetOverlayStorage#initialize}.
 * Before initialize, {@code isEmpty} returns true and {@code all} returns
 * {@code null}; calling {@code add} or {@code clearOverlays} pre-initialize
 * NPEs.
 */
class SheetOverlayStorageTest {

    private SheetOverlayStorage storage;
    private SheetOverlayWrapper overlay1;
    private SheetOverlayWrapper overlay2;

    @BeforeEach
    void setUp() {
        storage = new SheetOverlayStorage();
        overlay1 = mock(SheetOverlayWrapper.class);
        overlay2 = mock(SheetOverlayWrapper.class);
    }

    // ----- overlays lifecycle -----------------------------------------------

    @Test
    void freshStorage_isEmpty() {
        // Pin pre-initialize state: matches the Spreadsheet's
        // pre-workbook-load state. isEmpty returns true (the storage is
        // conceptually empty) although no backing set exists yet.
        assertTrue(storage.isEmpty());
    }

    @Test
    void freshStorage_addOverlay_throws() {
        // Pin: add pre-initialize NPEs because the backing set is null. The
        // storage doesn't lazy-init in `add` — callers MUST call initialize
        // first. This is load-bearing: Spreadsheet's workbook-load path is
        // what triggers the initialize, and adding overlays before a
        // workbook is loaded is a usage error.
        assertThrows(NullPointerException.class, () -> storage.add(overlay1));
    }

    @Test
    void freshStorage_allReturnsNull() {
        // Pre-initialize, all() returns the null field directly. Pinned
        // because callers using `for (var w : storage.all())` would NPE,
        // matching the contract that all() is undefined before initialize.
        assertEquals(null, storage.all());
    }

    @Test
    void initialize_makesStorageEmpty() {
        storage.initialize();
        assertTrue(storage.isEmpty());
        assertEquals(0, storage.all().size());
    }

    @Test
    void initialize_resetsPreviousOverlays() {
        // Pin: initialize is idempotent in the sense that calling it twice
        // produces a fresh empty set. The prior overlays are discarded —
        // this is how Spreadsheet handles a workbook reload.
        storage.initialize();
        storage.add(overlay1);
        storage.add(overlay2);
        assertFalse(storage.isEmpty());

        storage.initialize();

        assertTrue(storage.isEmpty());
    }

    @Test
    void add_thenAll_containsTheOverlay() {
        storage.initialize();
        storage.add(overlay1);

        Collection<SheetOverlayWrapper> all = storage.all();
        assertEquals(1, all.size());
        assertTrue(all.contains(overlay1));
    }

    @Test
    void add_sameOverlayTwice_keepsOneEntry() {
        // HashSet semantic.
        storage.initialize();
        storage.add(overlay1);
        storage.add(overlay1);
        assertEquals(1, storage.all().size());
    }

    @Test
    void clearOverlays_emptiesTheSet_butKeepsItInitialized() {
        // Pin the asymmetry: clearOverlays only empties; the set reference
        // stays, matching the semantic "the workbook is loaded but has no
        // overlays."
        storage.initialize();
        storage.add(overlay1);
        storage.add(overlay2);

        storage.clearOverlays();

        assertTrue(storage.isEmpty());
        assertEquals(0, storage.all().size());
    }

    @Test
    void clearOverlays_preInitialize_throws() {
        // Pin: clearOverlays is undefined pre-initialize — like add, it
        // assumes the backing set exists. The storage doesn't lazy-init.
        assertThrows(NullPointerException.class, () -> storage.clearOverlays());
    }
}
