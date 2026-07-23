// Ambient declarations for the @vaadin modules imported by the grid connector
// files. The modules may not have resolvable types depending on the compilation
// context (e.g. the @vaadin npm packages are not installed at the repository
// root), so the required parts of their APIs, including the non-public ones,
// are declared here.

declare module '@vaadin/component-base/src/async.js' {
  export interface AsyncInterface {
    run(fn: (...args: unknown[]) => void, delay?: number): number;
    cancel(handle: number): void;
  }

  export namespace timeOut {
    function after(delay?: number): AsyncInterface;
  }
}

declare module '@vaadin/component-base/src/debounce.js' {
  import { AsyncInterface } from '@vaadin/component-base/src/async.js';

  export class Debouncer {
    static debounce(debouncer: Debouncer | null, asyncModule: AsyncInterface, callback: () => void): Debouncer;
    cancel(): void;
    flush(): void;
    isActive(): boolean;
  }
}

declare module '@vaadin/grid/src/vaadin-grid-active-item-mixin.js' {
  export function isFocusable(target: Element): boolean;
}

declare module '@vaadin/grid/src/vaadin-grid-column.js' {
  export class GridColumn extends HTMLElement {
    protected _grid: any;
  }
}

declare module '@vaadin/grid/src/vaadin-grid-selection-column-base-mixin.js' {
  import { GridColumn } from '@vaadin/grid/src/vaadin-grid-column.js';

  type Constructor<T> = new (...args: any[]) => T;

  export function GridSelectionColumnBaseMixin<TItem, T extends Constructor<HTMLElement>>(
    base: T
  ): Constructor<GridSelectionColumnBaseMixinClass<TItem>> & T;

  export class GridSelectionColumnBaseMixinClass<TItem> {
    selectAll: boolean;
    autoSelect: boolean;
    dragSelect: boolean;
    protected _shiftKeyDown: boolean;
    protected _defaultHeaderRenderer(root: HTMLElement, column: GridColumn): void;
    protected _selectAll(): void;
    protected _deselectAll(): void;
    protected _selectItem(item: TItem): void;
    protected _deselectItem(item: TItem): void;
  }
}

declare module '@vaadin/checkbox/src/vaadin-checkbox.js';
