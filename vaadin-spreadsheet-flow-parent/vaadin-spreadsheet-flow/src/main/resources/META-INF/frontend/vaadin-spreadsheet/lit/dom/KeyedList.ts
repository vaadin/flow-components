/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import { type ChildPart, noChange } from 'lit';
import { directive, Directive, type DirectiveResult, type PartInfo, PartType } from 'lit/directive.js';
import {
  getCommittedValue,
  insertPart,
  removePart,
  setChildPartValue,
  setCommittedValue
} from 'lit/directive-helpers.js';

/**
 * Lit's `repeat`, for the lists a sheet scrolls through: each item keeps its
 * elements by its key, in the order given.
 *
 * `repeat` gives each item a marker before it and one after it, and since
 * lit-html 3.3.1 it removes only the first with an item that leaves
 * (https://github.com/lit/lit/issues/5298). Every row, cell and header a
 * sheet scrolled past left a comment in the page, and a session of
 * scrolling piled up thousands. This list removes both.
 */
class KeyedListDirective extends Directive {
  /** The keys of the items in the page, in order: those of the parts committed. */
  private keys: unknown[] = [];

  constructor(partInfo: PartInfo) {
    super(partInfo);
    if (partInfo.type !== PartType.CHILD) {
      throw new Error('keyedList() can only be used in child expressions');
    }
  }

  render<T>(
    items: Iterable<T>,
    _keyOf: (item: T, index: number) => unknown,
    template: (item: T, index: number) => unknown
  ): unknown[] {
    return Array.from(items, (item, index) => template(item, index));
  }

  override update<T>(
    containerPart: ChildPart,
    [items, keyOf, template]: [Iterable<T>, (item: T, index: number) => unknown, (item: T, index: number) => unknown]
  ): unknown {
    const list = Array.from(items);
    const newKeys = list.map((item, index) => keyOf(item, index));
    const newValues = list.map((item, index) => template(item, index));
    const oldParts = getCommittedValue(containerPart);
    if (!Array.isArray(oldParts)) {
      // The first time, lit renders the list itself: a part for each item.
      this.keys = newKeys;
      return newValues;
    }
    // The same items as before, in the same order - a drawing that did not
    // scroll - is only the items' values to update.
    if (sameKeys(this.keys, newKeys)) {
      (oldParts as ChildPart[]).forEach((part, index) => setChildPartValue(part, newValues[index]));
      return noChange;
    }
    const partByKey = new Map<unknown, ChildPart>();
    this.keys.forEach((key, index) => partByKey.set(key, (oldParts as ChildPart[])[index]));
    const wanted = new Set(newKeys);
    for (const [key, part] of partByKey) {
      if (!wanted.has(key)) {
        removeWholePart(part);
      }
    }
    // From the last item back, each in front of the one after it: lit moves
    // an item only where it is not there already, and puts a new one in.
    const newParts: ChildPart[] = new Array(newKeys.length);
    let after: ChildPart | undefined;
    for (let index = newKeys.length - 1; index >= 0; index--) {
      const kept = partByKey.get(newKeys[index]);
      const part = kept === undefined ? insertPart(containerPart, after) : insertPart(containerPart, after, kept);
      setChildPartValue(part, newValues[index]);
      newParts[index] = part;
      after = part;
    }
    setCommittedValue(containerPart, newParts);
    this.keys = newKeys;
    return noChange;
  }
}

/** Whether two lists of keys hold the same keys in the same order. */
function sameKeys(a: readonly unknown[], b: readonly unknown[]): boolean {
  return a.length === b.length && a.every((key, index) => key === b[index]);
}

/**
 * Removes an item's part with both its markers. `removePart` takes the
 * content and the marker before it, and since lit-html 3.3.1 leaves the
 * one after it; with an earlier lit-html, that one has already gone too.
 */
function removeWholePart(part: ChildPart): void {
  const end = part.endNode;
  removePart(part);
  end?.parentNode?.removeChild(end);
}

/**
 * Renders the items of a list in order, each keeping its elements by its
 * key from one rendering to the next - see {@link KeyedListDirective}.
 */
export const keyedList = directive(KeyedListDirective) as <T>(
  items: Iterable<T>,
  keyOf: (item: T, index: number) => unknown,
  template: (item: T, index: number) => unknown
) => DirectiveResult<typeof KeyedListDirective>;
