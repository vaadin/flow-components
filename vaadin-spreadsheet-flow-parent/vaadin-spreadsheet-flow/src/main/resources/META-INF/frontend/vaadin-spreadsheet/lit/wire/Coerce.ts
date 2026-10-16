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
 * Values as the server sends them, read into what the client works with:
 * Flow passes collections either as they are or as JSON text.
 */

/**
 * Coerce a value that may be either an array or a JSON-encoded
 * string into an array. The Flow runtime serializes server-side
 * property values to JSON strings when they reach the
 * {@code <vaadin-spreadsheet>} element's attribute lane, and they
 * arrive at the connector's setters as strings, to be parsed on the way in.
 */
export function coerceArray<T>(value: T[] | string | null | undefined): T[] {
  if (value == null) {
    return [];
  }
  if (Array.isArray(value)) {
    return value;
  }
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }
  // Typed arrays (Int32Array, Float32Array) — already array-like;
  // convert to a real array for downstream `.map` etc. compatibility.
  if (typeof (value as any).length === 'number') {
    return Array.from(value as any);
  }
  return [];
}

/**
 * One object the server sends, as it is or as the JSON string Flow may send
 * it as; null for anything else.
 */
export function coerceJson<T extends object>(value: T | string | null | undefined): T | null {
  if (value == null) {
    return null;
  }
  if (typeof value !== 'string') {
    return value;
  }
  try {
    const parsed: unknown = JSON.parse(value);
    return parsed !== null && typeof parsed === 'object' ? (parsed as T) : null;
  } catch {
    return null;
  }
}

/**
 * Object-shaped variant of {@link coerceArray}. The same JSON-string-
 * coming-from-Flow-attribute path applies to record/map setters.
 */
export function coerceObject<V>(value: Record<string, V> | string | null | undefined): Record<string, V> {
  if (value == null) {
    return {};
  }
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value);
      return parsed && typeof parsed === 'object' ? parsed : {};
    } catch {
      return {};
    }
  }
  return value;
}

/** The sizes the server sends: a plain array, or a typed one. */
export type NumberList = number[] | Int32Array | Float32Array;

/**
 * Numeric-array variant. Typed arrays (Int32Array, Float32Array)
 * arrive intact when pushed via Flow's typed-array support; JSON
 * strings get parsed.
 */
export function coerceNumberArray(value: NumberList | string | null | undefined): NumberList {
  if (value == null) {
    return [];
  }
  if (Array.isArray(value) || value instanceof Int32Array || value instanceof Float32Array) {
    return value;
  }
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
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
export function coerceSizes(value: NumberList | SizeRuns | string | null | undefined): NumberList {
  const parsed = typeof value === 'string' ? coerceJson<NumberList | SizeRuns>(value) : value;
  if (Array.isArray(parsed) || parsed instanceof Int32Array || parsed instanceof Float32Array) {
    return parsed;
  }
  if (parsed != null && Array.isArray((parsed as SizeRuns).runs)) {
    return spellOut((parsed as SizeRuns).runs);
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
