import type { Grid } from '../../../node_modules/@vaadin/grid/vaadin-grid.js';

export function getBodyRowCount(grid: Grid): number {
  return (grid as any)._effectiveSize;
}

export function getBodyCellContent(grid: Grid, rowIndex: number, columnIndex: number): HTMLElement | null {
  const items = grid.shadowRoot!.querySelector(`#items`)!;

  const row = [...items.children].find((row) => (row as any).index === rowIndex);
  if (!row) {
    return null;
  }

  const cellsInVisualOrder = [...row.children].sort((a, b) => {
    const aOrder = parseInt(getComputedStyle(a).order) || 0;
    const bOrder = parseInt(getComputedStyle(b).order) || 0;
    return aOrder - bOrder;
  });

  return (cellsInVisualOrder[columnIndex] as any)._content;
}

export function getCellText(grid: Grid, rowIndex: number, columnIndex: number): string | null {
  return getBodyCellContent(grid, rowIndex, columnIndex)?.textContent || null;
}
