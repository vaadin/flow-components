/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { LitElement } from 'lit';

/**
 * One of the sheet's internal elements, which shows the parts it draws to
 * the tree above it.
 *
 * `::part()` crosses exactly one shadow boundary. Every part the
 * spreadsheet documents is drawn inside one of these elements, which sit
 * inside the component's own shadow root — two boundaries away from an
 * application's stylesheet. Without re-exporting them,
 * `vaadin-spreadsheet::part(cell)` matches nothing, and the whole
 * documented styling API silently does nothing.
 */
export abstract class SpreadsheetPart extends LitElement {
  /** The parts this element draws, shown above it under the same names. */
  static readonly EXPORTED_PARTS: string = '';

  connectedCallback(): void {
    super.connectedCallback();
    // Set on attach rather than in the constructor: a custom element
    // may not have attributes when it is created.
    this.setAttribute('exportparts', (this.constructor as typeof SpreadsheetPart).EXPORTED_PARTS);
  }
}
