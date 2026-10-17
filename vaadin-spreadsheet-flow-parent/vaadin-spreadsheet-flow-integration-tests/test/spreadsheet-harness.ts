/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * Shared helpers for the client tests: a sheet on a plain host, a batch of
 * server properties applied as the wrapper applies them, and a wait for what
 * is rendered. Not a test file itself: the wtr glob takes lit-*.test.ts only.
 */

import { Spreadsheet } from '../../vaadin-spreadsheet-flow/src/main/resources/META-INF/frontend/vaadin-spreadsheet/lit/Spreadsheet.js';

/** An event the sheet sends the server, as the host is told it. */
export interface Sent {
  type: string;
  data?: any;
}

export interface AttachedSheet {
  host: HTMLElement;
  sheet: Spreadsheet;
  /** What the sheet sent since it was attached, or since the list was emptied. */
  sent: Sent[];
  /** The types of what was sent, in order. */
  types(): string[];
  cleanup(): void;
}

/**
 * A sheet on a plain host of the given size, its columns and rows of the given
 * sizes, drawn once: drawn, it introduces itself to the server and asks for
 * the cells it shows. Row heights are in points, 15 pt being 20 px.
 */
export function attachSheet(
  options: {
    width?: number;
    height?: number;
    rows?: number;
    cols?: number;
    colW?: number;
    rowH?: number;
  } = {}
): AttachedSheet {
  const { width = 400, height = 300, rows = 20, cols = 10, colW = 50, rowH = 15 } = options;
  const host = document.createElement('div');
  host.style.cssText = `width:${width}px;height:${height}px;display:flex;flex-direction:column;`;
  document.body.appendChild(host);
  const sheet = new Spreadsheet(host);
  sheet.setRows(rows);
  sheet.setCols(cols);
  sheet.setColW(new Array(cols).fill(colW));
  sheet.setRowH(new Array(rows).fill(rowH));
  const sent: Sent[] = [];
  host.addEventListener('spreadsheet-event', (e) => sent.push((e as CustomEvent).detail));
  sheet.recomputeViewport();
  return {
    host,
    sheet,
    sent,
    types: () => sent.map((event) => event.type),
    cleanup: () => {
      sheet.dispose();
      host.remove();
    }
  };
}

/**
 * Applies server properties as the wrapper's updated() does: a set<Name> for
 * each, in order, the reload and the resources apart, then notifyStateChanges.
 */
export function serverBatch(
  attached: AttachedSheet,
  properties: Record<string, unknown>,
  options: { firstUpdate?: boolean } = {}
): void {
  const sheet = attached.sheet as any;
  for (const [name, value] of Object.entries(properties)) {
    if (name === 'reload') {
      sheet.setReload(true);
    } else if (name === 'resources') {
      sheet.setResources(attached.host, value);
    } else {
      sheet[`set${name.charAt(0).toUpperCase()}${name.slice(1)}`](value);
    }
  }
  attached.sheet.notifyStateChanges(Object.keys(properties), options.firstUpdate ?? false);
}

/** Waits for a scheduled render, and for the parts to have drawn it. */
export async function rendered(sheet: Spreadsheet): Promise<void> {
  await Promise.resolve();
  await Promise.resolve();
  await Promise.all([
    sheet.getCellGrid().updateComplete,
    sheet.getColumnHeader().updateComplete,
    sheet.getRowHeader().updateComplete,
    sheet.getFormulaBar().updateComplete
  ]);
}

/** Waits for two animation frames: what a frame later does has run. */
export function afterFrames(): Promise<void> {
  return new Promise((resolve) => {
    requestAnimationFrame(() => requestAnimationFrame(() => resolve()));
  });
}

/** Presses a key on the sheet, as the browser sends it to the scroll container. */
export function pressOnSheet(sheet: Spreadsheet, key: string, init: KeyboardEventInit = {}): void {
  sheet
    .getScrollContainer()
    .dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true, composed: true, ...init }));
}

/** The input of the open cell editor, if one is open. */
export function editorInput(sheet: Spreadsheet): HTMLInputElement | null {
  return sheet.getCellGrid().shadowRoot!.querySelector('[part~="cell-input"]');
}
