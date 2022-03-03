/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid;

import java.util.Collection;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.AbstractRow.AbstractCell;
import com.vaadin.flow.component.grid.FooterRow.FooterCell;

/**
 * One row of {@link FooterCell}s in a Grid.
 *
 * @author Vaadin Ltd.
 */
public class FooterRow extends AbstractRow<FooterCell> {

    /**
     * A footer cell in a Grid.
     *
     * @author Vaadin Ltd.
     */
    public static class FooterCell extends AbstractCell {

        FooterCell(AbstractColumn<?> column) {
            super(column);
            if (column.getFooterRenderer() == null) {
                column.setFooterText("");
            }
        }

        @Override
        public void setText(String text) {
            getColumn().setFooterText(text);
        }

        @Override
        public void setComponent(Component component) {
            getColumn().setFooterComponent(component);
        }

    }

    /**
     * Creates a new footer row from the layer of column elements.
     *
     * @param layer
     */
    FooterRow(ColumnLayer layer) {
        super(layer, FooterCell::new);
    }

    @Override
    public FooterCell join(Collection<FooterCell> cells) {
        if (layer.getGrid().getColumnLayers().indexOf(layer) == 0) {
            throw new UnsupportedOperationException(
                    "Cells cannot be joined on the top-most footer row. "
                            + "This row is used as the default row for setting column "
                            + "footers, so each cell in it should have maximum one "
                            + "related column.");
        }
        return super.join(cells);
    }

    @Override
    protected boolean isOutmostRow() {
        List<ColumnLayer> layers = layer.getGrid().getColumnLayers();

        for (int i = layers.size() - 1; i >= 0; i--) {
            ColumnLayer layer = layers.get(i);
            if (layer.isFooterRow()) {
                return equals(layer.asFooterRow());
            }
        }
        return false;
    }
}
