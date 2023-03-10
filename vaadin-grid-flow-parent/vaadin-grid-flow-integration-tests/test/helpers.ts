import type { FlowGrid } from './shared.js';

export function getBodyRowCount(grid: FlowGrid): number {
  return grid._effectiveSize;
}

export function getBodyCellContent(grid: FlowGrid, rowIndex: number, columnIndex: number): HTMLElement | null {
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

export function getCellText(grid: FlowGrid, rowIndex: number, columnIndex: number): string | null {
  return getBodyCellContent(grid, rowIndex, columnIndex)?.textContent || null;
}
