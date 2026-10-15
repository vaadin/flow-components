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
