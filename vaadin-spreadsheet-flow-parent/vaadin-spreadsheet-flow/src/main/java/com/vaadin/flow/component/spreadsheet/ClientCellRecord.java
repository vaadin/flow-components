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
import java.util.Arrays;

/**
 * What the client has of each cell, and which cells were built for it since
 * they were last all to be built again: one entry a cell, keyed by its column
 * and row packed in a {@code long}, in arrays of primitives.
 * <p>
 * There is an entry for every cell the client has, which for a sheet scrolled
 * through is every cell of it, so the entries are kept in arrays of primitives
 * rather than as objects: 24 to 48 bytes a cell, by how full the table is.
 * <p>
 * An entry holds two facts, each with its own reset:
 * <ul>
 * <li>what the client was last sent of the cell, as a hash of it, which
 * {@link #sent} and {@link #takenOff} keep, so that a cell the client has as it
 * is goes no more; the client forgets its cells as it is reloaded, see
 * {@link #clear()} and {@link #forgetClient()};
 * <li>that the cell was built for the client, plain or as a formula, which
 * {@link #build} and {@link #unbuild} keep, so that a cell built goes no more
 * as the sheet scrolls back; {@link #unbuildAll()} has every cell built again,
 * after a change that can change any of them, while the client keeps what it
 * has, which the hashes then send again only where it changed.
 * </ul>
 */
final class ClientCellRecord implements Serializable {

    /**
     * The client has the cell; the rest of the value is the hash of what it
     * got.
     */
    private static final long ON_CLIENT = 1;
    /** The cell was built for the client since {@link #unbuildAll()}. */
    private static final long BUILT = 2;
    /** The cell was built as a formula. */
    private static final long FORMULA = 4;
    /** The bits of a value that are not the hash. */
    private static final long STATE = ON_CLIENT | BUILT | FORMULA;

    /** Each cell's entry: what the client has, and whether it was built. */
    private final LongTable cells = new LongTable();
    /**
     * The formula cells built, apart, for the flush to go through: a few, where
     * the cells are many.
     */
    private final LongTable formulas = new LongTable();

    /** A cell's key: its column and row, 1-based, which are never 0. */
    static long keyOf(int col, int row) {
        return ((long) row << 32) | col;
    }

    /** The column of a key, 1-based. */
    static int colOf(long key) {
        return (int) key;
    }

    /** The row of a key, 1-based. */
    static int rowOf(long key) {
        return (int) (key >>> 32);
    }

    /** Whether the cell was built, plain or as a formula. */
    boolean isBuilt(long key) {
        return (cells.get(key) & BUILT) != 0;
    }

    /** Whether the cell was built as a formula. */
    boolean isBuiltFormula(long key) {
        return (cells.get(key) & (BUILT | FORMULA)) == (BUILT | FORMULA);
    }

    /** Whether the cell was built, and not as a formula. */
    boolean isBuiltPlain(long key) {
        return (cells.get(key) & (BUILT | FORMULA)) == BUILT;
    }

    /**
     * The cell was built for the client, as a formula or not: one or the other.
     */
    void build(long key, boolean formula) {
        long value = cells.get(key) & ~(BUILT | FORMULA);
        cells.put(key, value | BUILT | (formula ? FORMULA : 0));
        if (formula) {
            formulas.put(key, BUILT);
        } else {
            formulas.remove(key);
        }
    }

    /** The cell is to be built again; whether it was built. */
    boolean unbuild(long key) {
        long value = cells.get(key);
        if ((value & BUILT) == 0) {
            return false;
        }
        formulas.remove(key);
        keep(key, value & ~(BUILT | FORMULA));
        return true;
    }

    /**
     * The cells of the rows, 1-based and inclusive, are to be built again: the
     * keys of those that were built, in the order of the sheet.
     */
    long[] unbuildRows(int first, int last) {
        long[] found = cells.keysWhere((key, value) -> (value & BUILT) != 0
                && rowOf(key) >= first && rowOf(key) <= last);
        for (long key : found) {
            unbuild(key);
        }
        return inSheetOrder(found);
    }

    /** The cells of the column, 1-based, are to be built again. */
    void unbuildColumn(int col) {
        for (long key : cells.keysWhere(
                (key, value) -> (value & BUILT) != 0 && colOf(key) == col)) {
            unbuild(key);
        }
    }

    /** Every cell is to be built again; the client keeps what it has. */
    void unbuildAll() {
        for (long key : cells.keysWhere((key, value) -> (value & BUILT) != 0)) {
            unbuild(key);
        }
    }

    /** The keys of the formula cells built, in the order of the sheet. */
    long[] builtFormulas() {
        return inSheetOrder(formulas.keysWhere((key, value) -> true));
    }

    /** Whether the client has the cell. */
    boolean isOnClient(long key) {
        return (cells.get(key) & ON_CLIENT) != 0;
    }

    /**
     * The client is sent the cell, of which this is the hash: whether that is
     * other than what it has, or it has none.
     */
    boolean sent(long key, long hash) {
        long value = cells.get(key);
        long had = value & ~STATE;
        long now = hash & ~STATE;
        cells.put(key, now | (value & (BUILT | FORMULA)) | ON_CLIENT);
        return (value & ON_CLIENT) == 0 || had != now;
    }

    /** The cell is taken off the client: whether it had it. */
    boolean takenOff(long key) {
        long value = cells.get(key);
        if ((value & ON_CLIENT) == 0) {
            return false;
        }
        keep(key, value & (BUILT | FORMULA));
        return true;
    }

    /** The client has no cells any more; the cells built stay built. */
    void forgetClient() {
        for (long key : cells
                .keysWhere((key, value) -> (value & ON_CLIENT) != 0)) {
            keep(key, cells.get(key) & (BUILT | FORMULA));
        }
    }

    /** The client has no cells, and none is built. */
    void clear() {
        cells.clear();
        formulas.clear();
    }

    /** How many cells the record holds anything of. */
    int size() {
        return cells.size;
    }

    /** Keeps what is left of an entry, or drops it when nothing is. */
    private void keep(long key, long value) {
        if ((value & (ON_CLIENT | BUILT)) == 0) {
            cells.remove(key);
        } else {
            cells.put(key, value);
        }
    }

    /** Keys sorted by row, then column: a row is the high half. */
    private static long[] inSheetOrder(long[] keys) {
        Arrays.sort(keys);
        return keys;
    }

    /** What {@link LongTable#keysWhere} picks keys by. */
    @FunctionalInterface
    private interface EntryTest {
        boolean test(long key, long value);
    }

    /**
     * A map of {@code long} keys, none of them 0, to {@code long} values, none
     * of them 0 either: open addressing with linear probing, kept at most two
     * thirds full, an entry removed by moving the ones after it back.
     */
    private static final class LongTable implements Serializable {
        private long[] keys = new long[16];
        private long[] values = new long[16];
        private int size;

        /** The value of the key, or 0 when it has none. */
        long get(long key) {
            int slot = slotOf(key);
            return keys[slot] == key ? values[slot] : 0;
        }

        void put(long key, long value) {
            int slot = slotOf(key);
            if (keys[slot] == 0) {
                keys[slot] = key;
                size++;
            }
            values[slot] = value;
            if (size * 3 > keys.length * 2) {
                resize(keys.length * 2);
            }
        }

        void remove(long key) {
            int slot = slotOf(key);
            if (keys[slot] != key) {
                return;
            }
            int mask = keys.length - 1;
            int gap = slot;
            for (int next = (gap + 1) & mask; keys[next] != 0; next = (next + 1)
                    & mask) {
                // An entry whose own slot is not after the gap, on the way
                // round to it, moves into the gap.
                int home = home(keys[next], mask);
                if (((next - home) & mask) >= ((next - gap) & mask)) {
                    keys[gap] = keys[next];
                    values[gap] = values[next];
                    gap = next;
                }
            }
            keys[gap] = 0;
            values[gap] = 0;
            size--;
        }

        void clear() {
            keys = new long[16];
            values = new long[16];
            size = 0;
        }

        /** The keys whose entries pass the test, in no order. */
        long[] keysWhere(EntryTest test) {
            long[] found = new long[size];
            int count = 0;
            for (int slot = 0; slot < keys.length; slot++) {
                if (keys[slot] != 0 && test.test(keys[slot], values[slot])) {
                    found[count++] = keys[slot];
                }
            }
            return Arrays.copyOf(found, count);
        }

        /** The slot of the key, or the empty one it would go in. */
        private int slotOf(long key) {
            int mask = keys.length - 1;
            int slot = home(key, mask);
            while (keys[slot] != 0 && keys[slot] != key) {
                slot = (slot + 1) & mask;
            }
            return slot;
        }

        /**
         * Where a key goes first: its bits spread, as rows and columns are
         * small numbers.
         */
        private static int home(long key, int mask) {
            long mixed = key * 0x9E3779B97F4A7C15L;
            return (int) (mixed ^ (mixed >>> 32)) & mask;
        }

        private void resize(int capacity) {
            long[] oldKeys = keys;
            long[] oldValues = values;
            keys = new long[capacity];
            values = new long[capacity];
            int mask = capacity - 1;
            for (int slot = 0; slot < oldKeys.length; slot++) {
                if (oldKeys[slot] != 0) {
                    int to = home(oldKeys[slot], mask);
                    while (keys[to] != 0) {
                        to = (to + 1) & mask;
                    }
                    keys[to] = oldKeys[slot];
                    values[to] = oldValues[slot];
                }
            }
        }
    }
}
