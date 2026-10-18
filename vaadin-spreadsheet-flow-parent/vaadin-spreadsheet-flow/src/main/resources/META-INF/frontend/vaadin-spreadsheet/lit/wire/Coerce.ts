/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type { SizeRuns } from '../types/wire.js';

/**
 * Values as the server sends them, read into what the client works with. The
 * server sends collections as JSON values. Anything else reads as none: text
 * in particular, which a collection never is, would be walked character by
 * character.
 */

/** A list the server sends, as it is; a typed array copied into a plain one. */
export function coerceArray<T>(value: T[] | null | undefined): T[] {
  if (Array.isArray(value)) {
    return value;
  }
  if (ArrayBuffer.isView(value)) {
    return Array.from(value as unknown as ArrayLike<T>);
  }
  return [];
}

/** One object the server sends, as it is; null for anything else. */
export function coerceJson<T extends object>(value: T | null | undefined): T | null {
  return value !== null && typeof value === 'object' ? value : null;
}

/** A map the server sends, as it is; empty for anything else. */
export function coerceObject<V>(value: Record<string, V> | null | undefined): Record<string, V> {
  return value !== null && typeof value === 'object' ? value : {};
}

/**
 * A set of what the server sends as a list: asked about one value at a time,
 * as hidden rows are inside loops over rows.
 */
export function coerceSet<T>(value: T[] | null | undefined): ReadonlySet<T> {
  return new Set(coerceArray(value));
}

/** The sizes the server sends: a plain array, or a typed one. */
export type NumberList = number[] | Int32Array | Float32Array;

/** A list of numbers, as it is, typed arrays included; empty for anything else. */
export function coerceNumberArray(value: NumberList | null | undefined): NumberList {
  if (Array.isArray(value) || value instanceof Int32Array || value instanceof Float32Array) {
    return value;
  }
  return [];
}

/** The most sizes runs may spell out: far more than a sheet has rows. */
const MAX_SIZES = 1 << 24;

/**
 * The size of each row or column. The server sends them as runs of equal
 * sizes - as one size per row they came to 5.6 MB for the largest sheet - and
 * a plain list is taken as it is. Malformed runs read as no sizes at all.
 */
export function coerceSizes(value: NumberList | SizeRuns | null | undefined): NumberList {
  if (Array.isArray(value) || value instanceof Int32Array || value instanceof Float32Array) {
    return value;
  }
  if (value !== null && typeof value === 'object' && Array.isArray((value as SizeRuns).runs)) {
    return spellOut((value as SizeRuns).runs);
  }
  return [];
}

function spellOut(runs: unknown[]): number[] {
  let total = 0;
  for (let i = 0; i < runs.length; i += 2) {
    const count = runs[i + 1];
    // A size that is no number, or a count missing, not whole or below zero.
    if (!Number.isFinite(runs[i]) || !Number.isInteger(count) || (count as number) < 0) {
      return [];
    }
    total += count as number;
    if (total > MAX_SIZES) {
      return [];
    }
  }
  const sizes = new Array<number>(total);
  let at = 0;
  for (let i = 0; i < runs.length; i += 2) {
    const count = runs[i + 1] as number;
    sizes.fill(runs[i] as number, at, at + count);
    at += count;
  }
  return sizes;
}
