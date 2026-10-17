/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.shared.internal.BeforeClientResponseAction;
import com.vaadin.flow.component.spreadsheet.client.CellData;

/**
 * What the client has of each cell, and the cells to send it at the end of the
 * round trip. Every cell the client is sent goes through here, and goes once:
 * the last sent of a cell in a round trip is the one, a cell the client has as
 * it is is not sent again, and a cell is taken off only if the client has it.
 * <p>
 * The client forgets its cells when it is reloaded, and a new client has none:
 * {@link #reload()} forgets them here too, with the cells not yet sent, which
 * the client is given when it reports where it is.
 */
final class ClientCellMirror implements Serializable {

    private final Spreadsheet spreadsheet;

    /** What the client has of each cell, by key, as a hash of what it got. */
    private final Map<String, Long> onTheClient = new HashMap<>();

    /** The cells to send, by key, in the order first sent. */
    private final Map<String, CellData> pending = new LinkedHashMap<>();

    /** Whether the client is to draw its cells' styles again. */
    private boolean refreshStyles;

    /** Whether the cells sent this round trip are all the client is to have. */
    private boolean replacing;

    private BeforeClientResponseAction flushAction;

    ClientCellMirror(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /** Sends the cells at the end of the round trip, each as it is then. */
    void send(List<CellData> cells) {
        for (CellData cell : cells) {
            pending.put(SpreadsheetUtil.toKey(cell.col, cell.row), cell);
        }
        schedule();
    }

    /** Has the client draw its cells' styles again, after the cells. */
    void refreshStyles() {
        refreshStyles = true;
        schedule();
    }

    private void schedule() {
        if (flushAction == null) {
            flushAction = new BeforeClientResponseAction(spreadsheet,
                    this::flush);
        }
        flushAction.schedule();
    }

    /**
     * The client has no cells: it is reloaded, or new. The cells not yet sent
     * are not sent, so the record of what was sent forgets them too.
     */
    void reload() {
        onTheClient.clear();
        pending.clear();
        refreshStyles = false;
        // A replacement asked for in a round trip that reloads the client is
        // done by the reload: the cells the client then reports it shows.
        replacing = false;
        spreadsheet.getCellValueManager().clearCachedContent();
    }

    /**
     * The client is to drop every cell it has, and to have only the cells sent
     * from now to the end of the round trip, which go in one call.
     */
    void replace() {
        onTheClient.clear();
        pending.clear();
        replacing = true;
        schedule();
    }

    private void flush() {
        if (spreadsheet.isRerenderPending()) {
            // This round trip reloads the client; it gets its cells when it
            // reports where it is.
            reload();
            return;
        }
        List<CellData> changed = new ArrayList<>();
        for (Map.Entry<String, CellData> entry : pending.entrySet()) {
            String key = entry.getKey();
            CellData cell = entry.getValue();
            String sent = Serializer.serialize(cell);
            if (isRemoval(cell, sent)) {
                if (onTheClient.remove(key) != null) {
                    changed.add(cell);
                }
            } else {
                Long had = onTheClient.put(key, hash(sent));
                if (had == null || had != hash(sent)) {
                    changed.add(cell);
                }
            }
        }
        pending.clear();
        if (replacing) {
            replacing = false;
            spreadsheet.getElement().callJsFunction("cellsReplaced",
                    Serializer.serialize(changed));
        } else if (!changed.isEmpty()) {
            spreadsheet.getElement().callJsFunction("cellsUpdated",
                    Serializer.serialize(changed));
        }
        if (refreshStyles) {
            refreshStyles = false;
            spreadsheet.getElement().callJsFunction("refreshCellStyles");
        }
    }

    /** Whether the cell data is what takes a cell off the client: no data. */
    private static boolean isRemoval(CellData cell, String sent) {
        CellData removal = new CellData();
        removal.col = cell.col;
        removal.row = cell.row;
        return sent.equals(Serializer.serialize(removal));
    }

    /** FNV-1a, 64 bits: two different cells colliding is out of reach. */
    private static long hash(String text) {
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < text.length(); i++) {
            hash ^= text.charAt(i);
            hash *= 0x100000001b3L;
        }
        return hash;
    }
}
