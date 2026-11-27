/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.grid;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.ai.provider.QueryExecutor;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.LitRenderer;

class GridRendererTest {

    @Test
    void decimalColumn_showsAllFractionDigits() {
        assertNumberLabel("0.0725", new BigDecimal("0.0725"));
    }

    @Test
    void doubleColumn_showsAllFractionDigits() {
        assertNumberLabel("0.0004", 0.0004d);
    }

    @Test
    void floatColumn_showsNoWideningDigits() {
        assertNumberLabel("0.1", 0.1f);
    }

    @SuppressWarnings("unchecked")
    private static void assertNumberLabel(String expected, Number value) {
        Map<String, Object> queryRow = Map.of("rate", value);
        var grid = new Grid<AiDataRow>();
        GridRenderer.renderGrid(grid, new QueryExecutor() {
            @Override
            public String getSchema() {
                return "";
            }

            @Override
            public List<Map<String, Object>> executeQuery(String sql) {
                return List.of(queryRow);
            }
        }, "SELECT rate FROM products");

        var renderer = (LitRenderer<AiDataRow>) grid.getColumnByKey("rate")
                .getRenderer();
        var label = renderer.getValueProviders().get("label")
                .apply(new AiDataRow(queryRow));
        // The decimal separator depends on the default locale
        var separator = DecimalFormatSymbols.getInstance()
                .getDecimalSeparator();
        Assertions.assertEquals(expected.replace('.', separator), label);
    }
}
