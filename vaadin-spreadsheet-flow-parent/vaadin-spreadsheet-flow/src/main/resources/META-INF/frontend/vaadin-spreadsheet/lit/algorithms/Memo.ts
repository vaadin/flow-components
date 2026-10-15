/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

/**
 * The result of a computation, kept until one of what it was computed from
 * changes. The parts of a render state are replaced when they change, never
 * changed in place, so they are compared by identity.
 */
export class Memo<T> {
  private from: readonly unknown[] | null = null;
  private result: T | undefined;

  /** The result for these inputs: the kept one while each input is the one it was computed from. */
  get(from: readonly unknown[], compute: () => T): T {
    if (!sameInputs(from, this.from)) {
      this.result = compute();
      this.from = from;
    }
    return this.result as T;
  }
}

/** Whether two lists of inputs hold the same ones, by identity, in the same order. */
export function sameInputs(a: readonly unknown[], b: readonly unknown[] | null): boolean {
  return b !== null && a.length === b.length && a.every((input, i) => input === b[i]);
}
