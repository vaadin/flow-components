/**
 * @license
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
import type Collection from 'ol/Collection';
import type { Coordinate } from 'ol/coordinate';
import type { Size } from 'ol/size';
import type { MapSyncContext } from '../vaadin-map-types.js';
import type { MapCoordinate, MapSize } from './synchronization-types.js';

/**
 * Helper to convert a coordinate object with the shape { x: number, y: number}
 * into a coordinate array used by OpenLayers
 */
export function convertToCoordinateArray(coordinate: MapCoordinate): Coordinate {
  return [coordinate.x, coordinate.y];
}

/**
 * Helper to convert a coordinate object from the server with
 * the shape [[{ x: number, y: number}]]
 * to the structure of a GeoJSON coordinate array for polygons.
 */
export function convertToGeoJSONCoordinateArray(coordinates: MapCoordinate[][]): Coordinate[][] {
  return (
    coordinates
      // The first linear ring of the array defines the outer-boundary or surface of the polygon
      // Each subsequent linear ring defines a hole in the surface of the polygon
      .map((linearRing) => linearRing.map((coordinate) => convertToCoordinateArray(coordinate)))
  );
}

/**
 * Helper to convert a size object with the shape { width: number, height: number}
 * into a size array used by OpenLayers
 */
export function convertToSizeArray(size: MapSize): Size {
  return [size.width, size.height];
}

/**
 * Convert from snake-case Java enum value like `BOTTOM_LEFT` to kebab-case OL
 * enum value like `bottom-left`
 */
export function convertEnumValue(enumValue: string): string {
  return enumValue.toLowerCase().replace(/_/, '-');
}

/**
 * Synchronizes an OpenLayers collection with data from a Javascript array
 */
export function synchronizeCollection<T extends object>(
  collection: Collection<T>,
  updatedIds: string[],
  options: MapSyncContext
): void {
  // Check if we have changes
  const hasChanges =
    updatedIds.length !== collection.getLength() ||
    collection.getArray().some((existingItem, index) => existingItem !== options.lookup.get(updatedIds[index]));
  // Skip if there aren't any changes
  if (!hasChanges) return;
  // Get instance references from ids, these must have been synchronized earlier
  const updatedItems: T[] = updatedIds
    .map((id) => options.lookup.get(id))
    // This shouldn't be necessary, but having this safe-guard allows us to
    // at least continue the sync in case a reference is missing
    .filter((item) => !!item);
  // Rebuild the collection
  // It shouldn't matter whether we just add/move/remove specific items, or rebuild
  // the whole thing, this will result in several change events anyway
  collection.clear();
  updatedItems.forEach((item) => collection.push(item));
}

/** The type of an options object created from a configuration object of type T */
export type MapOptions<T> = {
  [K in keyof T]: Exclude<T[K], null>;
};

/**
 * Creates an options object from a configuration object.
 * This clones the configuration object and removes any properties that have the
 * value `null`, as OpenLayers requires the use of `undefined` for properties
 * that should not be set.
 */
export function createOptions<T extends object>(configurationObject: T): MapOptions<T> {
  const options = { ...configurationObject } as Record<string, unknown>;
  Object.keys(configurationObject).forEach((key) => {
    const value = (configurationObject as Record<string, unknown>)[key];

    if (value === null) {
      delete options[key];
    }
  });
  return options as MapOptions<T>;
}
