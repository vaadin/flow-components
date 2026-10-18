/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { composing } from '../algorithms/KeyboardMoves.js';
import { listen } from '../dom/Listen.js';

/** What the way out of the sheet needs of the sheet. */
export interface KeyboardExitHost {
  /** Whether a menu or a pop-up is open, which Escape closes. */
  showsMenuOrPopup(): boolean;
}

/** What assistive technology is told about the way out. */
export const KEYBOARD_EXIT_HINT = 'Press Escape, then Tab or Shift+Tab, to leave the sheet.';

/** Keys held down for another key. Pressed between Escape and Tab, they do not count. */
const MODIFIER_KEYS = new Set(['Shift', 'Control', 'Alt', 'Meta']);

/**
 * Numbers the hints. A sheet without a shadow root of its own shares the
 * page's ids with every other sheet.
 */
let hintCount = 0;

/**
 * Escape, then Tab or Shift+Tab, moves the keyboard out of the sheet.
 *
 * Tab and Shift+Tab move the active cell, as in Excel, so the keys that
 * leave any other control do not leave the sheet, and without another way
 * out a keyboard user is trapped in it (WCAG 2.1.2). Escape on the cells
 * arms the way out for the next key: Tab then moves the focus to the
 * control after the sheet, and Shift+Tab to the one before it. Any other
 * key, a press, or the focus moving disarms it, and Tab moves the active
 * cell again. The sheet's description tells assistive technology.
 *
 * The sheet's own buttons (pop-up buttons, grouping toggles, outline
 * levels) are no trap either: Tab moves on from them as from any button.
 */
export class KeyboardExit {
  /** Whether Escape was the last key pressed on the cells, so Tab leaves. */
  private armed = false;

  /**
   * Where leaving forwards starts, right after the sheet. The buttons and
   * components on the cells come after the cells in the order Tab follows,
   * so the browser moving on from the cells would land on one of them. Only
   * code focuses it; it is never a Tab stop of its own.
   */
  private readonly exitPoint = document.createElement('div');

  /** The way out, in words, for assistive technology. */
  private readonly hint = document.createElement('div');

  constructor(private readonly cells: HTMLElement, private readonly host: KeyboardExitHost, signal: AbortSignal) {
    this.exitPoint.className = 'keyboard-exit';
    this.exitPoint.tabIndex = -1;
    this.exitPoint.setAttribute('aria-hidden', 'true');
    this.exitPoint.style.cssText = 'position:absolute;width:0;height:0;overflow:hidden;outline:none;';
    this.hint.id = `spreadsheet-keyboard-exit-hint-${++hintCount}`;
    this.hint.hidden = true;
    this.hint.textContent = KEYBOARD_EXIT_HINT;
    cells.setAttribute('aria-describedby', this.hint.id);
    // Only the cells arm it, so once armed, any focus move is the cells losing the focus.
    listen(cells, 'pointerdown', this.disarm, signal);
    listen(cells, 'focusout', this.disarm, signal);
  }

  /** The elements to place right after the cells, in the same tree. */
  elements(): HTMLElement[] {
    return [this.exitPoint, this.hint];
  }

  /**
   * Takes a key pressed in the sheet if it is a step on the way out, and
   * says whether it did. A key it takes is not the sheet's to act on: the
   * browser moves the focus with it.
   */
  takes(e: KeyboardEvent): boolean {
    if (e.target !== this.cells) {
      // One of the sheet's own buttons has the focus, not the cells.
      return e.key === 'Tab';
    }
    if (e.key === 'Escape') {
      if (!composing(e)) {
        // Escape that closes a menu or a pop-up does only that.
        this.armed = !this.host.showsMenuOrPopup();
      }
      return true;
    }
    if (!this.armed || MODIFIER_KEYS.has(e.key)) {
      return false;
    }
    this.armed = false;
    if (e.key !== 'Tab') {
      return false;
    }
    if (!e.shiftKey) {
      this.exitPoint.focus({ preventScroll: true });
    }
    return true;
  }

  private readonly disarm = (): void => {
    this.armed = false;
  };
}
