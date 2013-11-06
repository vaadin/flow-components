package com.vaadin.addon.spreadsheet.client;

public class MergedRegionUtil {

    public interface MergedRegionContainer {
        /**
         * indexes 1-based
         * 
         * @param column
         * @param row
         * @return
         */
        public MergedRegion getMergedRegionStartingFrom(int column, int row);

        /**
         * indexes 1-based
         * 
         * @param column
         * @param row
         * @return
         */
        public MergedRegion getMergedRegion(int column, int row);

    }

    /**
     * Goes through the given selection and checks that the cells on the edges
     * of the selection are not in "the beginning / middle / end" of a merged
     * cell. Returns the correct increased selection, after taking the merged
     * cells into account.
     * 
     * Parameters 1-based.
     * 
     * @param topRow
     * @param bottomRow
     * @param leftColumn
     * @param rightColumn
     * @return
     */
    public static MergedRegion findIncreasingSelection(
            MergedRegionContainer container, int topRow, int bottomRow,
            int leftColumn, int rightColumn) {
        if (topRow == bottomRow && leftColumn == rightColumn) {
            MergedRegion mergedRegion = container.getMergedRegion(leftColumn,
                    topRow);
            if (mergedRegion == null) {
                mergedRegion = new MergedRegion();
                mergedRegion.col1 = leftColumn;
                mergedRegion.col2 = rightColumn;
                mergedRegion.row1 = topRow;
                mergedRegion.row2 = bottomRow;
            }
            return mergedRegion;
        } else {
            MergedRegion merged = container.getMergedRegionStartingFrom(
                    leftColumn, topRow);
            if (merged != null && merged.col2 >= rightColumn
                    && merged.row2 >= bottomRow) {
                return merged;
            }
        }
        boolean trouble = false;
        int i = leftColumn;
        // go through top edge
        while (i <= rightColumn) {
            MergedRegion region = container.getMergedRegion(i, topRow);
            if (region != null) {
                i = region.col2 + 1;
                if (leftColumn > region.col1) {
                    leftColumn = region.col1;
                    trouble = true;
                }
                if (rightColumn < region.col2) {
                    rightColumn = region.col2;
                    trouble = true;
                }
                if (topRow > region.row1) {
                    topRow = region.row1;
                    trouble = true;
                }
            } else {
                i++;
            }
        }
        if (topRow > bottomRow) {
            topRow = bottomRow;
        }
        // go through right edge
        i = topRow;
        while (i <= bottomRow) {
            MergedRegion region = container.getMergedRegion(rightColumn, i);
            if (region != null) {
                i = region.row2 + 1;
                if (rightColumn < region.col2) {
                    rightColumn = region.col2;
                    trouble = true;
                }
                if (topRow > region.row1) {
                    topRow = region.row1;
                    trouble = true;
                }
                if (bottomRow < region.row2) {
                    bottomRow = region.row2;
                    trouble = true;

                }
            } else {
                i++;
            }
        }
        if (rightColumn < leftColumn) {
            rightColumn = leftColumn;
        }
        // go through bottom edge
        i = leftColumn;
        while (i <= rightColumn) {
            MergedRegion region = container.getMergedRegion(i, bottomRow);
            if (region != null) {
                i = region.col2 + 1;
                if (leftColumn > region.col1) {
                    leftColumn = region.col1;
                    trouble = true;
                }
                if (rightColumn < region.col2) {
                    rightColumn = region.col2;
                    trouble = true;

                }
                if (bottomRow < region.row2) {
                    bottomRow = region.row2;
                    trouble = true;
                }
            } else {
                i++;
            }
        }
        if (bottomRow < topRow) {
            bottomRow = topRow;
        }
        // go through left edge
        i = topRow;
        while (i <= bottomRow) {
            MergedRegion region = container.getMergedRegion(leftColumn, i);
            if (region != null) {
                i = region.row2 + 1;
                if (leftColumn > region.col1) {
                    leftColumn = region.col1;
                    trouble = true;
                }
                if (topRow > region.row1) {
                    topRow = region.row1;
                    trouble = true;
                }
                if (bottomRow < region.row2) {
                    bottomRow = region.row2;
                    trouble = true;
                }
            } else {
                i++;
            }
        }
        if (leftColumn > rightColumn) {
            leftColumn = rightColumn;
        }

        if (trouble) {
            return findIncreasingSelection(container, topRow, bottomRow,
                    leftColumn, rightColumn);
        } else if (topRow == bottomRow && leftColumn == rightColumn) {
            MergedRegion mergedRegion = container.getMergedRegion(leftColumn,
                    topRow);
            if (mergedRegion == null) {
                mergedRegion = new MergedRegion();
                mergedRegion.col1 = leftColumn;
                mergedRegion.col2 = rightColumn;
                mergedRegion.row1 = topRow;
                mergedRegion.row2 = bottomRow;
            }
            return mergedRegion;
        } else {
            MergedRegion merged = container.getMergedRegionStartingFrom(
                    leftColumn, topRow);
            if (merged != null && merged.col2 >= rightColumn
                    && merged.row2 >= bottomRow) {
                return merged;
            }
        }
        MergedRegion result = new MergedRegion();
        result.col1 = leftColumn;
        result.col2 = rightColumn;
        result.row1 = topRow;
        result.row2 = bottomRow;
        return result;
    }

}
