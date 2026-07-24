// Type augmentations for the non-public @vaadin/grid API that the grid
// connector files rely on. The public API types come from the @vaadin npm
// packages, resolved from the integration tests module's node_modules
// (see tsconfig.json in the module root).
import type { GridDefaultItem } from '@vaadin/grid/src/vaadin-grid.js';
import type { GridColumn } from '@vaadin/grid/src/vaadin-grid-column.js';

declare module '@vaadin/grid/src/vaadin-grid-column.js' {
  interface GridColumn<TItem = GridDefaultItem> {
    _grid: any;
  }
}

declare module '@vaadin/grid/src/vaadin-grid-selection-column-base-mixin.js' {
  interface GridSelectionColumnBaseMixinClass<TItem> {
    _defaultHeaderRenderer(root: HTMLElement, column: GridColumn): void;
  }
}
