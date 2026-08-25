// Types for the Flow-specific context menu target API. The public API types
// come from the @vaadin npm packages, resolved from the integration tests
// module's node_modules (see tsconfig.json in the module root).
import type { ContextMenuTargetConnector } from './contextMenuTargetConnector.js';

export type { ContextMenuTargetConnector };

/** The event that opens the context menu, extended with the captured composed path */
export interface OpenEvent extends Event {
  __composedPath?: EventTarget[];
}

/** An element a context menu is attached to */
export interface ContextMenuTarget extends HTMLElement {
  $contextMenuTargetConnector?: ContextMenuTargetConnector;
  /** Used by Grid to prevent context menu on selection column click */
  preventContextMenu?(e: OpenEvent): boolean;
  /** Provides the detail for the `vaadin-context-menu-before-open` event */
  getContextMenuBeforeOpenDetail?(e: OpenEvent): Record<string, unknown>;
}

declare global {
  // Augments the global Vaadin interface declared by @vaadin/component-base
  // with the Flow namespace used by the connector
  interface Vaadin {
    Flow: {
      contextMenuTargetConnector: { init(target: ContextMenuTarget): void };
    };
  }
}
