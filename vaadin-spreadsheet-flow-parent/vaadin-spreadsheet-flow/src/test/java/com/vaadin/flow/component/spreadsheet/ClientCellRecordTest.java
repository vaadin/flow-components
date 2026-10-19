/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The one record of the cells the client has and of the cells built for it,
 * keyed by column and row packed in a {@code long}.
 */
class ClientCellRecordTest {

    private ClientCellRecord record;

    @BeforeEach
    void setUp() {
        record = new ClientCellRecord();
    }

    private static long key(int col, int row) {
        return ClientCellRecord.keyOf(col, row);
    }

    @Test
    void aKey_givesBackItsColumnAndRow_upToTheLastOfAnExcelSheet() {
        long last = key(16_384, 1_048_576);

        assertEquals(16_384, ClientCellRecord.colOf(last));
        assertEquals(1_048_576, ClientCellRecord.rowOf(last));
        assertEquals(1, ClientCellRecord.colOf(key(1, 7)));
        assertEquals(7, ClientCellRecord.rowOf(key(1, 7)));
    }

    @Test
    void aCellBuilt_isPlainOrAFormula_oneOrTheOther() {
        assertFalse(record.isBuilt(key(2, 3)));

        record.build(key(2, 3), false);
        assertTrue(record.isBuiltPlain(key(2, 3)));
        assertFalse(record.isBuiltFormula(key(2, 3)));

        record.build(key(2, 3), true);
        assertTrue(record.isBuiltFormula(key(2, 3)));
        assertFalse(record.isBuiltPlain(key(2, 3)));
        assertTrue(record.isBuilt(key(2, 3)));
    }

    @Test
    void aCellUnbuilt_saysWhetherItWasBuilt_andIsNeither() {
        record.build(key(2, 3), true);

        assertTrue(record.unbuild(key(2, 3)));
        assertFalse(record.unbuild(key(2, 3)));
        assertFalse(record.isBuilt(key(2, 3)));
        assertEquals(0, record.builtFormulas().length);
    }

    @Test
    void aCellSent_isSentAgainOnlyOtherwise() {
        assertTrue(record.sent(key(1, 1), 0xABCDEF00L), "the first time");
        assertFalse(record.sent(key(1, 1), 0xABCDEF00L), "the same");
        assertTrue(record.sent(key(1, 1), 0x12345600L), "otherwise");
        assertTrue(record.isOnClient(key(1, 1)));
    }

    @Test
    void aCellTakenOff_goesOnlyIfTheClientHasIt_andIsSentAgainAfter() {
        assertFalse(record.takenOff(key(1, 1)), "never sent");
        record.sent(key(1, 1), 0xABCDEF00L);

        assertTrue(record.takenOff(key(1, 1)));
        assertFalse(record.takenOff(key(1, 1)), "taken off already");
        assertFalse(record.isOnClient(key(1, 1)));
        assertTrue(record.sent(key(1, 1), 0xABCDEF00L), "sent again");
    }

    @Test
    void whatTheClientHas_andWhatWasBuilt_areKeptApart() {
        record.build(key(1, 1), false);
        record.sent(key(1, 1), 0xABCDEF00L);

        record.unbuild(key(1, 1));
        assertTrue(record.isOnClient(key(1, 1)), "still on the client");
        assertFalse(record.sent(key(1, 1), 0xABCDEF00L), "with what it had");

        record.build(key(1, 1), false);
        record.takenOff(key(1, 1));
        assertTrue(record.isBuilt(key(1, 1)), "still built");
    }

    @Test
    void aCellNeitherOnTheClientNorBuilt_takesNoRoom() {
        record.build(key(1, 1), false);
        record.sent(key(2, 2), 0xABCDEF00L);
        assertEquals(2, record.size());

        record.unbuild(key(1, 1));
        record.takenOff(key(2, 2));

        assertEquals(0, record.size());
    }

    @Test
    void theRowsUnbuilt_giveTheCellsBuiltInThem_inTheOrderOfTheSheet() {
        record.build(key(3, 5), false);
        record.build(key(1, 5), true);
        record.build(key(2, 4), false);
        record.build(key(9, 9), false);
        record.sent(key(7, 5), 0xABCDEF00L);

        long[] unbuilt = record.unbuildRows(4, 5);

        assertArrayEquals(new long[] { key(2, 4), key(1, 5), key(3, 5) },
                unbuilt);
        assertFalse(record.isBuilt(key(1, 5)));
        assertTrue(record.isBuilt(key(9, 9)), "another row");
        assertTrue(record.isOnClient(key(7, 5)), "on the client, unbuilt");
    }

    @Test
    void aColumnUnbuilt_leavesTheOthers() {
        record.build(key(3, 1), false);
        record.build(key(3, 7), true);
        record.build(key(30, 1), false);

        record.unbuildColumn(3);

        assertFalse(record.isBuilt(key(3, 1)));
        assertFalse(record.isBuilt(key(3, 7)));
        assertTrue(record.isBuilt(key(30, 1)), "column 30 is not column 3");
    }

    @Test
    void everyCellUnbuilt_isBuiltAgain_whileTheClientKeepsWhatItHas() {
        record.build(key(1, 1), true);
        record.build(key(2, 1), false);
        record.sent(key(1, 1), 0xABCDEF00L);

        record.unbuildAll();

        assertFalse(record.isBuilt(key(1, 1)));
        assertFalse(record.isBuilt(key(2, 1)));
        assertEquals(0, record.builtFormulas().length);
        assertFalse(record.sent(key(1, 1), 0xABCDEF00L), "what it had");
        assertEquals(1, record.size());
    }

    @Test
    void theFormulasBuilt_areThoseBuiltAsFormulas_inTheOrderOfTheSheet() {
        record.build(key(5, 2), true);
        record.build(key(1, 2), true);
        record.build(key(3, 1), true);
        record.build(key(4, 4), false);
        record.build(key(9, 9), true);
        record.build(key(9, 9), false);

        assertArrayEquals(new long[] { key(3, 1), key(1, 2), key(5, 2) },
                record.builtFormulas());
    }

    @Test
    void theClientForgotten_hasNothing_andTheCellsBuiltStayBuilt() {
        record.build(key(1, 1), false);
        record.sent(key(1, 1), 0xABCDEF00L);
        record.sent(key(2, 2), 0xABCDEF00L);

        record.forgetClient();

        assertFalse(record.isOnClient(key(1, 1)));
        assertFalse(record.isOnClient(key(2, 2)));
        assertTrue(record.isBuilt(key(1, 1)));
        assertEquals(1, record.size());
    }

    @Test
    void aRecordCleared_holdsNothing() {
        record.build(key(1, 1), true);
        record.sent(key(2, 2), 0xABCDEF00L);

        record.clear();

        assertEquals(0, record.size());
        assertFalse(record.isOnClient(key(2, 2)));
        assertEquals(0, record.builtFormulas().length);
    }

    @Test
    void manyCellsComeAndGo_asAMapOfThemWouldHaveThem() {
        // A model of the record, against two hundred thousand random
        // changes on a small sheet, so that cells meet in the table and
        // leave gaps in it.
        Map<Long, long[]> model = new HashMap<>();
        Random random = new Random(7);
        for (int round = 0; round < 200_000; round++) {
            long key = key(1 + random.nextInt(40), 1 + random.nextInt(60));
            long hash = (random.nextInt(4) + 1L) << 8;
            long[] state = model.computeIfAbsent(key, k -> new long[3]);
            switch (random.nextInt(5)) {
            case 0 -> {
                boolean formula = random.nextBoolean();
                record.build(key, formula);
                state[1] = formula ? 2 : 1;
            }
            case 1 -> {
                assertEquals(state[1] != 0, record.unbuild(key));
                state[1] = 0;
            }
            case 2 -> {
                assertEquals(state[0] == 0 || state[2] != hash,
                        record.sent(key, hash));
                state[0] = 1;
                state[2] = hash;
            }
            case 3 -> {
                assertEquals(state[0] != 0, record.takenOff(key));
                state[0] = 0;
            }
            default -> {
                assertEquals(state[0] != 0, record.isOnClient(key));
                assertEquals(state[1] == 1, record.isBuiltPlain(key));
                assertEquals(state[1] == 2, record.isBuiltFormula(key));
            }
            }
            if (state[0] == 0 && state[1] == 0) {
                model.remove(key);
            }
        }
        assertEquals(model.size(), record.size());
        long formulas = model.values().stream().filter(s -> s[1] == 2).count();
        assertEquals(formulas, record.builtFormulas().length);
    }

    @Test
    void aRecordOfManyCells_growsAndKeepsThemAll() {
        for (int row = 1; row <= 1000; row++) {
            for (int col = 1; col <= 100; col++) {
                record.sent(key(col, row), (long) col * row << 3);
            }
        }

        assertEquals(100_000, record.size());
        assertFalse(record.sent(key(57, 813), 57L * 813 << 3));
        assertFalse(record.isOnClient(key(101, 1)));
    }

    @Test
    void aRecord_isSerializable() throws Exception {
        record.build(key(2, 3), true);
        record.sent(key(2, 3), 0xABCDEF00L);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(record);
        }

        ClientCellRecord read;
        try (ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            read = (ClientCellRecord) in.readObject();
        }

        assertTrue(read.isBuiltFormula(key(2, 3)));
        assertFalse(read.sent(key(2, 3), 0xABCDEF00L));
    }
}
