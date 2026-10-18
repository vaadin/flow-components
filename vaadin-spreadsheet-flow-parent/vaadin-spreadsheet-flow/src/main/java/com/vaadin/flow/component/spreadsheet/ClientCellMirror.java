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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.spreadsheet.client.CellData;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

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
        spreadsheet.clientChanges().cells();
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

    /**
     * Sends the cells, as {@link ClientChanges} does at the end of the round
     * trip: none after a reload, which forgot them.
     */
    void flush() {
        ArrayNode changed = Serializer.array();
        for (Map.Entry<String, CellData> entry : pending.entrySet()) {
            String key = entry.getKey();
            JsonNode sent = Serializer.toJson(entry.getValue());
            if (isRemoval(sent)) {
                if (onTheClient.remove(key) != null) {
                    changed.add(sent);
                }
            } else {
                long hash = hash(sent.toString());
                Long had = onTheClient.put(key, hash);
                if (had == null || had != hash) {
                    changed.add(sent);
                }
            }
        }
        pending.clear();
        if (replacing) {
            replacing = false;
            spreadsheet.getElement().callJsFunction("cellsReplaced", changed);
        } else if (!changed.isEmpty()) {
            spreadsheet.getElement().callJsFunction("cellsUpdated", changed);
        }
        if (refreshStyles) {
            refreshStyles = false;
            spreadsheet.getElement().callJsFunction("refreshCellStyles");
        }
    }

    /**
     * What a cell taken off the client is sent as, besides its column and row:
     * what a new {@link CellData} has.
     */
    private static final JsonNode REMOVAL = Serializer.toJson(new CellData());

    /**
     * Whether the cell data is what takes a cell off the client: its column and
     * row, which are never 0, and nothing a new {@link CellData} does not have.
     */
    private static boolean isRemoval(JsonNode sent) {
        if (sent.size() != REMOVAL.size() + 2) {
            return false;
        }
        for (Map.Entry<String, JsonNode> field : REMOVAL.properties()) {
            if (!field.getValue().equals(sent.get(field.getKey()))) {
                return false;
            }
        }
        return true;
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
