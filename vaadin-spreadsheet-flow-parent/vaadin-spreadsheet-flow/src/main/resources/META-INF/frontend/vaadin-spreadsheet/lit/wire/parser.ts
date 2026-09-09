/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */

import type {
  CellData,
  GroupingData,
  MergedRegion,
  OverlayInfo,
  PopupButtonState,
  SpreadsheetActionDetails
} from '../types/wire.js';

/**
 * Wire-format JSON parser. Consumes the raw strings the server emits via
 * `Serializer.serialize(...)` and returns typed TypeScript objects.
 *
 * Replaces the GWT-side `com.vaadin.component.spreadsheet.client.js.
 * Parser` class (185 LOC of generic JSON-to-Java-bean copying). The TS
 * version is much smaller because `JSON.parse` produces objects whose
 * shape already matches our interfaces — no `copyJsToJava` reflection
 * needed.
 *
 * Empty / null / "null" inputs return an empty array OR `null`, matching
 * the Java-side behavior pinned by the IT suite (`BigExcelFileIT`,
 * `GenericIT`, etc.) — the server emits `""` for some absent properties
 * and `"null"` for others.
 */

/**
 * `true` iff the raw string is one of the empty-input sentinels the
 * server emits (`null`, `""`, or the literal string `"null"`). Mirrors
 * the Java Parser's early-return condition.
 */
function isEmpty(raw: string | null | undefined): boolean {
  return raw === null || raw === undefined || raw === '' || raw === 'null';
}

/** Parse an array of `CellData`. */
export function parseArrayOfCellData(raw: string | null | undefined): CellData[] {
  if (isEmpty(raw)) {
    return [];
  }
  return JSON.parse(raw as string) as CellData[];
}

/** Parse an array of `MergedRegion`. */
export function parseArrayOfMergedRegion(raw: string | null | undefined): MergedRegion[] {
  if (isEmpty(raw)) {
    return [];
  }
  return JSON.parse(raw as string) as MergedRegion[];
}

/** Parse an array of `SpreadsheetActionDetails`. */
export function parseArrayOfSpreadsheetActionDetails(raw: string | null | undefined): SpreadsheetActionDetails[] {
  if (isEmpty(raw)) {
    return [];
  }
  return JSON.parse(raw as string) as SpreadsheetActionDetails[];
}

/** Parse an array of `PopupButtonState`. */
export function parseArrayOfPopupButtonState(raw: string | null | undefined): PopupButtonState[] {
  if (isEmpty(raw)) {
    return [];
  }
  return JSON.parse(raw as string) as PopupButtonState[];
}

/** Parse a single `PopupButtonState`. */
export function parsePopupButtonState(raw: string | null | undefined): PopupButtonState | null {
  if (isEmpty(raw)) {
    return null;
  }
  return JSON.parse(raw as string) as PopupButtonState;
}

/** Parse an array of `GroupingData`. */
export function parseArrayOfGroupingData(raw: string | null | undefined): GroupingData[] {
  if (isEmpty(raw)) {
    return [];
  }
  return JSON.parse(raw as string) as GroupingData[];
}

/** Parse a `Record<string, OverlayInfo>`. */
export function parseMapStringOverlayInfo(raw: string | null | undefined): Record<string, OverlayInfo> | null {
  if (isEmpty(raw)) {
    return null;
  }
  return JSON.parse(raw as string) as Record<string, OverlayInfo>;
}

/** Parse an array of strings. */
export function parseArrayOfStrings(raw: string | null | undefined): string[] {
  if (isEmpty(raw)) {
    return [];
  }
  return JSON.parse(raw as string) as string[];
}

/** Parse an array of integers. */
export function parseArrayOfIntegers(raw: string | null | undefined): number[] {
  if (isEmpty(raw)) {
    return [];
  }
  return JSON.parse(raw as string) as number[];
}

/**
 * Parse a JSON array of numbers as a typed `Int32Array`. Matches Java's
 * `Parser.parseArrayInt` shape — the consumers wanted a primitive int
 * array, not a boxed Integer list.
 */
export function parseInt32Array(raw: string | null | undefined): Int32Array {
  if (isEmpty(raw)) {
    return new Int32Array(0);
  }
  return Int32Array.from(JSON.parse(raw as string) as number[]);
}

/**
 * Parse a JSON array of numbers as a typed `Float32Array`. Matches
 * Java's `Parser.parseArrayFloat`.
 */
export function parseFloat32Array(raw: string | null | undefined): Float32Array {
  if (isEmpty(raw)) {
    return new Float32Array(0);
  }
  return Float32Array.from(JSON.parse(raw as string) as number[]);
}

/**
 * Parse a `Record<number, string>`. Mirrors Java's
 * `parseMapIntegerString`. Map keys are JSON strings (JSON spec); we
 * coerce to numbers on read.
 */
export function parseMapNumberString(raw: string | null | undefined): Map<number, string> | null {
  if (isEmpty(raw)) {
    return null;
  }
  const obj = JSON.parse(raw as string) as Record<string, string>;
  const map = new Map<number, string>();
  for (const key of Object.keys(obj)) {
    map.set(Number(key), obj[key]);
  }
  return map;
}

/** Parse a `Record<number, number>`. Mirrors Java's `parseMapIntegerInteger`. */
export function parseMapNumberNumber(raw: string | null | undefined): Map<number, number> | null {
  if (isEmpty(raw)) {
    return null;
  }
  const obj = JSON.parse(raw as string) as Record<string, number>;
  const map = new Map<number, number>();
  for (const key of Object.keys(obj)) {
    map.set(Number(key), obj[key]);
  }
  return map;
}

/** Parse a `Record<string, string>`. */
export function parseMapStringString(raw: string | null | undefined): Map<string, string> | null {
  if (isEmpty(raw)) {
    return null;
  }
  const obj = JSON.parse(raw as string) as Record<string, string>;
  return new Map(Object.entries(obj));
}

/** Parse a `Set<number>`. */
export function parseSetNumber(raw: string | null | undefined): Set<number> {
  if (isEmpty(raw)) {
    return new Set();
  }
  return new Set(JSON.parse(raw as string) as number[]);
}

/** Parse a `Set<string>`. */
export function parseSetString(raw: string | null | undefined): Set<string> {
  if (isEmpty(raw)) {
    return new Set();
  }
  return new Set(JSON.parse(raw as string) as string[]);
}
