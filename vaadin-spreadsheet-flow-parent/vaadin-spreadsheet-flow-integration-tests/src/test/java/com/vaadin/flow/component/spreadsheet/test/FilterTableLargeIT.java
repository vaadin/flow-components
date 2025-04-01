/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

public class FilterTableLargeIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        loadFile("autofilter_with_large_table.xlsx");
    }

    @Test
    public void filteringShouldNotTakeForever() {
    	
        // Select MaritalStatus filter cell
        getSpreadsheet().getCellAt(1, 15).popupButtonClick();

        ExecutorService exec = Executors.newFixedThreadPool(1);
        Future<?> doFilter = exec.submit(() -> {
            // De-select "married"
            getSpreadsheet().getContextMenu().getItem("Married").click();
        });

        try {
            // Wait for a maximum of 5 seconds for operation to complete.
            // The original code took minutes to complete. The optimized
            // version takes milliseconds. The test timing should be
            // tightened as necessary.
            doFilter.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException tex) {
            doFilter.cancel(true);
            fail("Execution timeout while waiting for large table filter;" +
                  " 5 second timeout implies filter operation optimization " +
                  " is not in place");
        } catch (InterruptedException e) {
            fail("Execution of large table filter interrupted");
            e.printStackTrace();
        } catch (ExecutionException e) {
            fail("Excecution exception occurred while filtering large table");
            e.printStackTrace();
        }

        // Completing the filter event without running into TimeoutException
        // qualifies as test pass.
    }

}
