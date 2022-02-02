/**
 * @license
 * Copyright (c) 2022 - 2022 Vaadin Ltd.
 * This program is available under Commercial Vaadin Developer License 4.0, available at https://vaadin.com/license/cvdl-4.0.
 */
import Feature from "ol/Feature";
import Point from "ol/geom/Point";
import View from "ol/View";
import TileGrid from "ol/tilegrid/TileGrid";
import { synchronizeTileLayer, synchronizeVectorLayer } from "./layers.js";
import {
  synchronizeOSMSource,
  synchronizeTileWMSSource,
  synchronizeVectorSource,
  synchronizeXYZSource,
} from "./sources.js";
import {
  synchronizeIcon,
  synchronizeFill,
  synchronizeStroke,
  synchronizeStyle,
} from "./styles.js";
import {
  convertToCoordinateArray,
  convertToExtentArray,
  convertToSizeArray,
  synchronizeCollection,
} from "./util.js";

function synchronizeMap(target, source, context) {
  if (!target) {
    throw new Error("Existing map instance must be provided");
  }

  // Layers
  synchronizeCollection(target.getLayers(), source.layers, context);

  // View
  if (source.view) {
    synchronizeView(target.getView(), source.view, context);
  }

  return target;
}

function synchronizeView(target, source, _context) {
  if (!target) {
    target = new View({
      projection: source.projection,
    });
  }

  target.setCenter(
    source.center ? convertToCoordinateArray(source.center) : [0, 0]
  );
  target.setRotation(source.rotation || 0);
  target.setZoom(source.zoom || 0);

  return target;
}

function synchronizePoint(target, source, _context) {
  if (!target) {
    target = new Point(convertToCoordinateArray(source.coordinates));
  }

  target.setCoordinates(convertToCoordinateArray(source.coordinates));

  return target;
}

function synchronizeFeature(target, source, context) {
  if (!target) {
    target = new Feature();
  }

  target.setGeometry(
    context.synchronize(target.getGeometry(), source.geometry, context)
  );
  target.setStyle(
    context.synchronize(target.getStyle(), source.style, context)
  );

  return target;
}

function synchronizeTileGrid(target, source, _context) {
  if (!target) {
    target = new TileGrid({
      extent: convertToExtentArray(source.extent),
      size: convertToSizeArray(source.size),
      resolutions: source.resolutions,
    });
  }

  return target;
}

const synchronizerLookup = {
  "ol/Feature": synchronizeFeature,
  "ol/Map": synchronizeMap,
  "ol/View": synchronizeView,
  // Layers
  "ol/layer/Tile": synchronizeTileLayer,
  "ol/layer/Vector": synchronizeVectorLayer,
  // Sources
  "ol/source/OSM": synchronizeOSMSource,
  "ol/source/TileWMS": synchronizeTileWMSSource,
  "ol/source/Vector": synchronizeVectorSource,
  "ol/source/XYZ": synchronizeXYZSource,
  // Geometry
  "ol/geom/Point": synchronizePoint,
  // Styles
  "ol/style/Icon": synchronizeIcon,
  "ol/style/Fill": synchronizeFill,
  "ol/style/Stroke": synchronizeStroke,
  "ol/style/Style": synchronizeStyle,
  // Tile grids
  "ol/tilegrid/TileGrid": synchronizeTileGrid,
};

/**
 * Synchronizes a configuration object into a corresponding OpenLayers class
 * instance. All objects are expected to have:
 * - a type property to specify which OpenLayers class / type to use
 * - an ID property to identify the instance in future syncs
 *
 * If the target instance is null, or if its ID does not match with the source
 * configuration object, then a new target instance will be created.
 *
 * Only specific OpenLayers classes are supported for synchronization.
 *
 * @param target The OpenLayers instance into which to synchronize, or null if a new instance should be created
 * @param source The configuration object to synchronize from
 * @param context The context object providing global context for the synchronization
 * @returns {*}
 */
export function synchronize(target, source, context) {
  const type = source.type;

  if (!type) {
    throw new Error("Configuration object must have a type");
  }
  if (!source.id) {
    throw new Error("Configuration object must have an ID");
  }

  const synchronizer = synchronizerLookup[type];
  if (!synchronizer) {
    throw new Error(`Unsupported configuration object type: ${type}`);
  }

  // If IDs do not match, then we have a new configuration object, and we want
  // a new matching OpenLayers instance
  if (target && target.id !== source.id) {
    target = null;
  }

  // Call the type-specific synchronizer function to either create a new
  // OpenLayers instance, or update the existing one
  const result = synchronizer(target, source, context);

  // Store ID on the sync result for future updates
  result.id = source.id;
  // Store type name on sync result for type checks in tests
  result.typeName = type;

  return result;
}
