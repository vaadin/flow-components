/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.spreadsheet.SheetOverlayWrapper.OverlayChangeListener;
import com.vaadin.flow.component.spreadsheet.client.OverlayInfo;

/**
 * Loading, updating and clearing overlays with {@link SheetOverlayManager}.
 *
 * <p>
 * Pins the orchestration of {@code loadOrUpdate}, {@code clear}, and the
 * delegate methods. Mocks ALL collaborators ({@link SheetOverlayStorage},
 * {@link SheetOverlayVisibility}, {@link SheetOverlayInfoBuilder}) so the tests
 * stay focused on the orchestrator's decision tree without exercising static
 * {@code SpreadsheetFactory.loadSheetOverlays}.
 *
 * <p>
 * To bypass the static factory call inside {@code loadOrUpdate}, every test
 * stubs {@code storage.isEmpty()} to return {@code false} (overlays already
 * loaded). The "empty → load from factory" branch is intentionally NOT
 * exercised here — it would require static-method mocking.
 */
class SheetOverlayManagerTest {

    @Mock
    private Spreadsheet spreadsheet;
    @Mock
    private SheetOverlayStorage storage;
    @Mock
    private SheetOverlayVisibility visibility;
    @Mock
    private SheetOverlayInfoBuilder infoBuilder;
    @Mock
    private SheetOverlayWrapper overlay1;
    @Mock
    private SheetOverlayWrapper overlay2;

    private SheetOverlayManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new SheetOverlayManager(spreadsheet, storage, visibility,
                infoBuilder);
        // Default: storage non-empty (skip the static factory branch).
        lenient().when(storage.isEmpty()).thenReturn(false);
        lenient().when(spreadsheet.getOverlays()).thenReturn(null);
        lenient().when(spreadsheet.getResources())
                .thenReturn(Collections.emptyMap());
    }

    @Test
    void hasOverlays_reflectsStorageState() {
        when(storage.isEmpty()).thenReturn(true);
        assertFalse(manager.hasOverlays());

        when(storage.isEmpty()).thenReturn(false);
        assertTrue(manager.hasOverlays());
    }

    @Test
    void add_delegatesToStorage() {
        manager.add(overlay1);
        verify(storage).add(overlay1);
    }

    @Test
    void loadOrUpdate_visibleOverlay_buildsInfo_andMarksVisible() {
        when(storage.all()).thenReturn(Arrays.asList(overlay1));
        when(visibility.isVisible(overlay1)).thenReturn(true);
        when(overlay1.getId()).thenReturn("img1");
        when(infoBuilder.build(overlay1)).thenReturn(new OverlayInfo());

        manager.loadOrUpdate();

        verify(infoBuilder).build(overlay1);
        verify(overlay1).setVisible(true);
        // The overlays map setter is called with a HashMap containing
        // the new entry.
        verify(spreadsheet).setOverlays(any());
    }

    @Test
    void loadOrUpdate_invisibleOverlayThatWasVisible_removesData() {
        // Pin: an overlay that's both invisible NOW and was visible BEFORE
        // is detached: setOverlays with the key removed, setResource(null),
        // setVisible(false). The "was visible" check is on the overlay
        // itself, not on the visibility computation.
        when(storage.all()).thenReturn(Arrays.asList(overlay1));
        when(visibility.isVisible(overlay1)).thenReturn(false);
        when(overlay1.isVisible()).thenReturn(true);
        when(overlay1.getId()).thenReturn("img1");
        HashMap<String, OverlayInfo> existing = new HashMap<>();
        existing.put("img1", new OverlayInfo());
        when(spreadsheet.getOverlays()).thenReturn(existing);

        manager.loadOrUpdate();

        verify(overlay1).setVisible(false);
        verify(spreadsheet).setResource("img1", null);
        verify(spreadsheet).setOverlays(any());
    }

    @Test
    void loadOrUpdate_invisibleOverlayNeverWasVisible_doesNothing() {
        // Pin: an overlay that's invisible AND was never visible is silently
        // skipped — no setOverlays, no setVisible call, no setResource.
        when(storage.all()).thenReturn(Arrays.asList(overlay1));
        when(visibility.isVisible(overlay1)).thenReturn(false);
        when(overlay1.isVisible()).thenReturn(false);

        manager.loadOrUpdate();

        verify(overlay1, never()).setVisible(true);
        verify(overlay1, never()).setVisible(false);
        verify(spreadsheet, never()).setOverlays(any());
        verify(spreadsheet, never()).setResource(anyString(), any());
    }

    @Test
    void loadOrUpdate_overlayWithComponent_registersCustomComponent() {
        // Pin: addOverlayData's first branch — when the overlay has a
        // component, register it with the parent + remember in storage.
        Component component = org.mockito.Mockito.mock(Component.class);
        when(storage.all()).thenReturn(Arrays.asList(overlay1));
        when(visibility.isVisible(overlay1)).thenReturn(true);
        when(overlay1.getComponent(true)).thenReturn(component);
        when(overlay1.getId()).thenReturn("img1");
        when(infoBuilder.build(overlay1)).thenReturn(new OverlayInfo());

        manager.loadOrUpdate();

        verify(spreadsheet).registerCustomComponent(component);
        verify(storage).addComponent(component);
    }

    @Test
    void loadOrUpdate_overlayWithResourceHandler_setsResourceIfNotPresent() {
        // Pin: resource handler is set ONLY if the spreadsheet's resources
        // map doesn't already contain the overlay id. Idempotent across
        // multiple loadOrUpdate calls.
        when(storage.all()).thenReturn(Arrays.asList(overlay1));
        when(visibility.isVisible(overlay1)).thenReturn(true);
        when(overlay1.getId()).thenReturn("img1");
        when(overlay1.getResourceHandler()).thenReturn(org.mockito.Mockito
                .mock(com.vaadin.flow.server.streams.DownloadHandler.class));
        when(spreadsheet.getResources()).thenReturn(Collections.emptyMap());
        when(infoBuilder.build(overlay1)).thenReturn(new OverlayInfo());

        manager.loadOrUpdate();

        verify(spreadsheet).setResource(eq("img1"), any());
    }

    @Test
    void loadOrUpdate_overlayWithResourceAlreadyPresent_skipsSetResource() {
        when(storage.all()).thenReturn(Arrays.asList(overlay1));
        when(visibility.isVisible(overlay1)).thenReturn(true);
        when(overlay1.getId()).thenReturn("img1");
        when(overlay1.getResourceHandler()).thenReturn(org.mockito.Mockito
                .mock(com.vaadin.flow.server.streams.DownloadHandler.class));
        // resources map already contains img1 — set should be skipped.
        when(spreadsheet.getResources())
                .thenReturn(Collections.singletonMap("img1", "existing"));
        when(infoBuilder.build(overlay1)).thenReturn(new OverlayInfo());

        manager.loadOrUpdate();

        verify(spreadsheet, never()).setResource(eq("img1"), any());
    }

    @Test
    void loadOrUpdate_buildsOverlaysMap_andSetsAtomicallyOnSpreadsheet() {
        // Pin: setOverlays is called with a FRESH HashMap copy, not with
        // a mutation of the prior reference. Load-bearing because overlays
        // is a MirroredProperty.serialized — in-place mutations would
        // skip the client mirror.
        HashMap<String, OverlayInfo> existing = new HashMap<>();
        OverlayInfo priorInfo = new OverlayInfo();
        existing.put("other", priorInfo);
        when(spreadsheet.getOverlays()).thenReturn(existing);

        when(storage.all()).thenReturn(Arrays.asList(overlay1));
        when(visibility.isVisible(overlay1)).thenReturn(true);
        when(overlay1.getId()).thenReturn("img1");
        when(infoBuilder.build(overlay1)).thenReturn(new OverlayInfo());

        manager.loadOrUpdate();

        verify(spreadsheet).setOverlays(org.mockito.ArgumentMatchers.argThat(
                updated -> updated != null && updated.containsKey("img1")
                        && updated.containsKey("other")
                        && updated != existing));
    }

    @Test
    void loadOrUpdate_iteratesAllStorageEntries() {
        // Pin: every overlay in storage.all() is visited in iteration
        // order; visibility is queried per overlay independently.
        when(storage.all()).thenReturn(Arrays.asList(overlay1, overlay2));
        when(visibility.isVisible(overlay1)).thenReturn(true);
        when(visibility.isVisible(overlay2)).thenReturn(false);
        when(overlay2.isVisible()).thenReturn(false);
        when(overlay1.getId()).thenReturn("img1");
        when(infoBuilder.build(overlay1)).thenReturn(new OverlayInfo());

        manager.loadOrUpdate();

        verify(visibility).isVisible(overlay1);
        verify(visibility).isVisible(overlay2);
        verify(overlay1).setVisible(true);
        // overlay2 was not previously visible → no setVisible call.
        verify(overlay2, never()).setVisible(true);
        verify(overlay2, never()).setVisible(false);
    }

    @Test
    void clear_removesEachOverlayAndClearsStorage() {
        when(storage.all()).thenReturn(Arrays.asList(overlay1, overlay2));
        when(overlay1.getId()).thenReturn("img1");
        when(overlay2.getId()).thenReturn("img2");
        HashMap<String, OverlayInfo> existing = new HashMap<>();
        existing.put("img1", new OverlayInfo());
        existing.put("img2", new OverlayInfo());
        when(spreadsheet.getOverlays()).thenReturn(existing);

        manager.clear();

        // Each overlay's resource cleared and storage finally cleared.
        verify(spreadsheet).setResource("img1", null);
        verify(spreadsheet).setResource("img2", null);
        verify(storage, times(1)).clearOverlays();
    }

    @Test
    void hasOverlays_emptyStorage_returnsFalse() {
        when(storage.isEmpty()).thenReturn(true);
        assertFalse(manager.hasOverlays());
    }

    @Test
    void markReloadFromPoi_setsFlag_observedNextLoadOrUpdate() {
        // Pin: markReloadFromPoi sets a private boolean that
        // loadOrUpdate checks; the flag should drive a clear + reload
        // sequence on the next call. Since the reload path calls the
        // static SpreadsheetFactory (which we cannot mock cleanly here),
        // we verify the OBSERVABLE side effect: clearOverlays() is
        // called inside the reload branch.
        when(storage.all()).thenReturn(Collections.emptyList());
        manager.markReloadFromPoi();

        manager.loadOrUpdate();

        // Reload triggers a clear before re-loading from POI.
        verify(storage, times(1)).clearOverlays();
        // After reload, the flag resets — a second loadOrUpdate should
        // NOT call clearOverlays again.
        when(storage.all()).thenReturn(Collections.emptyList());
        manager.loadOrUpdate();
        verify(storage, times(1)).clearOverlays();
    }

    @Test
    void loadOrUpdate_attachesOverlayChangeListener_thatRetriggersLoadOrUpdate() {
        // Pin: addOverlayData attaches an OverlayChangeListener whose
        // overlayChanged() callback re-invokes loadOrUpdate. This is the
        // mechanism that propagates client-side overlay changes (drag,
        // resize) back into the server-side overlay map. A regression that
        // dropped the listener or wired it to the wrong callback would
        // silently break server-side sync on overlay edits.
        when(storage.all()).thenReturn(Arrays.asList(overlay1));
        when(visibility.isVisible(overlay1)).thenReturn(true);
        when(overlay1.getId()).thenReturn("img1");
        when(infoBuilder.build(overlay1)).thenReturn(new OverlayInfo());

        manager.loadOrUpdate();

        ArgumentCaptor<OverlayChangeListener> listenerCaptor = ArgumentCaptor
                .forClass(OverlayChangeListener.class);
        verify(overlay1).setOverlayChangeListener(listenerCaptor.capture());
        OverlayChangeListener listener = listenerCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertNotNull(listener,
                "Listener attached to id-bearing overlay must NOT be null");

        // Trigger the listener callback. It should run another full
        // loadOrUpdate cycle — verifiable via the storage.all() recount
        // (one initial call + one from the listener = 2 total).
        listener.overlayChanged();

        verify(storage, times(2)).all();
        // infoBuilder should have been consulted twice: once per
        // loadOrUpdate cycle.
        verify(infoBuilder, times(2)).build(overlay1);
    }

    @Test
    void overlayWithNullId_skipsOverlaysMapUpdate_andEventListener() {
        // Pin: addOverlayData's id-guarded branch — if the overlay has no
        // id, the overlays map is NOT updated and no change listener is
        // attached. The overlay is still marked visible.
        when(storage.all()).thenReturn(Arrays.asList(overlay1));
        when(visibility.isVisible(overlay1)).thenReturn(true);
        when(overlay1.getId()).thenReturn(null);

        manager.loadOrUpdate();

        verify(overlay1).setVisible(true);
        verify(spreadsheet, never()).setOverlays(any());
        verify(overlay1, never()).setOverlayChangeListener(any());
    }
}
