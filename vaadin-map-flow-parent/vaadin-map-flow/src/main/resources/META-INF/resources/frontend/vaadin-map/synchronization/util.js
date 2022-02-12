/**
 * Helper to convert a coordinate object with the shape { x: number, y: number}
 * into a coordinate array used by OpenLayers
 * @param coordinate
 * @returns {*[]}
 */
export function convertToCoordinateArray(coordinate) {
  return [coordinate.x, coordinate.y];
}

/**
 * Helper to convert a size object with the shape { width: number, height: number}
 * into a size array used by OpenLayers
 * @param size
 * @returns {*[]}
 */
export function convertToSizeArray(size) {
  return [size.width, size.height];
}

/**
 * Synchronizes an OpenLayers collection with data from a Javascript array
 */
export function synchronizeCollection(collection, updatedIds, options) {
  const updatedItems = updatedIds.map((id) => options.lookup.get(id));

  // Iterate updated items and update collection at indexes where reference doesn't match
  updatedItems.forEach((updatedItem, i) => {
    const existingItem = collection.item(i);
    const isUnchanged = existingItem && existingItem.id === updatedItem.id;
    if (isUnchanged) return;
    collection.setAt(i, updatedItem);
  });
  // Remove leftover collection elements
  while (collection.getLength() > updatedItems.length) {
    collection.pop();
  }
}

/**
 * Creates an options object from a configuration object.
 * This clones the configuration object and removes any properties that have the
 * value `null`, as OpenLayers requires the use of `undefined` for properties
 * that should not be set.
 * @param configurationObject
 * @returns {*}
 */
export function createOptions(configurationObject) {
  const options = { ...configurationObject };
  Object.keys(configurationObject).forEach((key) => {
    const value = configurationObject[key];

    if (value === null) {
      delete options[key];
    }
  });
  return options;
}
