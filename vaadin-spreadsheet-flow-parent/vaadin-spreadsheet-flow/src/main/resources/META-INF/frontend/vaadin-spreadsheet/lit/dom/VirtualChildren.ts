/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Placing server-side Vaadin components inside the sheet.
 *
 * Four features need this: chart and component overlays, custom cell editors,
 * custom components rendered in a cell, and popup-button content. In every
 * case the server attaches the component with
 * {@code Element.appendVirtualChild}, which creates the element on the client
 * but deliberately leaves it out of the document — placing it is the client's
 * job.
 *
 * The wire identifier is the component's Flow **node id**, and the only way
 * to turn one into an element is to ask the Flow client for the running
 * application. That lookup is the single impure part of this module, so it is
 * passed in as a {@link VirtualChildResolver} rather than reached for
 * directly: the placement logic below is then testable with plain elements.
 *
 * The placement itself is a named slot, matching what the GWT client did:
 * the component stays a light-DOM child of a host element and is projected
 * wherever the sheet renders a matching {@code <slot>}. Moving the element
 * into a shadow root instead would take it out of the server's reach.
 */

/**
 * Name of the slot an overlay component is projected through.
 *
 * Lives here because two layers have to agree on it and neither may import
 * the other: the connector sets it as the component's {@code slot}
 * attribute, and the cell-grid renders the matching {@code <slot name>} at
 * the overlay's position.
 */
export function overlayComponentSlotName(nodeId: string): string {
  return `overlay-component-${nodeId}`;
}

/**
 * Name of the slot a component rendered inside a cell is projected through.
 *
 * Separate from {@link overlayComponentSlotName} so the two placements never
 * compete for the same slot even though both are keyed by node id.
 */
export function cellComponentSlotName(nodeId: string): string {
  return `custom-component-${nodeId}`;
}

/** Resolves a Flow node id to the element Flow created for it. */
export type VirtualChildResolver = (nodeId: string) => HTMLElement | null;

/**
 * Build a resolver backed by the Flow client for the given application.
 *
 * Returns null for anything it cannot resolve — an id that is not a number,
 * an application that has not started yet, a node the server has since
 * detached. Callers render their placeholder and try again on the next
 * repaint rather than treating it as an error: the component and the overlay
 * geometry that positions it arrive in separate server responses, so a miss
 * on the first paint is ordinary.
 */
export function flowVirtualChildResolver(appId: string): VirtualChildResolver {
  return (nodeId: string): HTMLElement | null => {
    const parsed = Number.parseInt(nodeId, 10);
    if (!Number.isFinite(parsed)) {
      return null;
    }
    const flow = (window as any).Vaadin?.Flow;
    const client = flow?.clients?.[appId];
    if (!client || typeof client.getByNodeId !== 'function') {
      return null;
    }
    const element = client.getByNodeId(parsed);
    return element instanceof HTMLElement ? element : null;
  };
}

/**
 * Keeps a set of server-side components parented to one host element and
 * assigned to per-component named slots.
 *
 * {@link sync} is called after every repaint with the ids the sheet is
 * currently rendering slots for. It is deliberately idempotent — repainting
 * with an unchanged set must not touch the DOM, because re-parenting a live
 * Vaadin component tears down and rebuilds it.
 */
export class SlottedVirtualChildren {
  private readonly placed = new Map<string, HTMLElement>();

  /**
   * @param host the element the components become light-DOM children of.
   *   Slots that project them must live in this element's shadow root.
   * @param resolve turns a node id into the element Flow created.
   * @param slotName names the slot for a given node id. The same function
   *   must be used to name the slots in the template.
   */
  constructor(
    private readonly host: HTMLElement,
    private readonly resolve: VirtualChildResolver,
    private readonly slotName: (nodeId: string) => string
  ) {}

  /**
   * Place every id in {@code nodeIds} and remove anything placed earlier
   * that is no longer in the set.
   *
   * @returns the ids that are placed after this call — a subset of
   *   {@code nodeIds}, missing any the resolver could not find.
   */
  sync(nodeIds: Iterable<string>): string[] {
    const wanted = new Set(nodeIds);

    for (const [nodeId, element] of this.placed) {
      if (!wanted.has(nodeId)) {
        this.detach(element);
        this.placed.delete(nodeId);
      }
    }

    for (const nodeId of wanted) {
      if (this.placed.has(nodeId)) {
        continue;
      }
      const element = this.resolve(nodeId);
      if (element === null) {
        continue;
      }
      element.setAttribute('slot', this.slotName(nodeId));
      this.host.appendChild(element);
      this.placed.set(nodeId, element);
    }

    return [...this.placed.keys()];
  }

  /** The element currently placed for an id, if any. */
  get(nodeId: string): HTMLElement | null {
    return this.placed.get(nodeId) ?? null;
  }

  /** Remove every placed component. Call this when tearing the sheet down. */
  clear(): void {
    for (const element of this.placed.values()) {
      this.detach(element);
    }
    this.placed.clear();
  }

  /**
   * Take one component back out of the sheet.
   *
   * The slot attribute goes too. The server may place the same component
   * somewhere else later — a chart that scrolls out of view and returns — and
   * a stale slot name would leave it projected nowhere.
   */
  private detach(element: HTMLElement): void {
    element.removeAttribute('slot');
    element.remove();
  }
}
