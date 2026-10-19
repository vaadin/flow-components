// Types for the configuration objects the server sends to be synchronized
// into OpenLayers instances. Each interface declares the properties the
// corresponding synchronization function reads; references to other OL
// instances are their unique ids in the instance lookup.
import type { MapSyncContext } from '../vaadin-map-types.js';

/** A coordinate sent by the server, converted to an OL coordinate array */
export interface MapCoordinate {
  x: number;
  y: number;
}

/** A size sent by the server, converted to an OL size array */
export interface MapSize {
  width: number;
  height: number;
}

/**
 * A function synchronizing a configuration object of a specific type into an
 * OL instance, creating the instance when no existing one is passed. The
 * synchronizers are dispatched dynamically by the configuration object's
 * type, so the target, source and result are only known at runtime.
 */
export type MapSynchronizer = (target: any, source: any, context: MapSyncContext) => any;

/** Properties shared by all configuration objects */
export interface MapChangeBase {
  type: string;
  id: string;
}

export interface MapChange extends MapChangeBase {
  layers: string[];
  visibleControls: string[];
  view: string;
}

export interface ViewChange extends MapChangeBase {
  projection?: string;
  center?: MapCoordinate | null;
  rotation?: number | null;
  minZoom: number;
  maxZoom: number;
  zoom?: number | null;
}

export interface LineStringChange extends MapChangeBase {
  coordinates: MapCoordinate[];
}

export interface PointChange extends MapChangeBase {
  coordinates: MapCoordinate;
}

export interface PolygonChange extends MapChangeBase {
  coordinates: MapCoordinate[][];
}

export interface FeatureChange extends MapChangeBase {
  geometry: string;
  style: string;
  text?: string | null;
  draggable?: boolean;
}

export interface LayerChange extends MapChangeBase {
  opacity: number;
  visible: boolean;
  zIndex?: number | null;
  minZoom?: number | null;
  maxZoom?: number | null;
  background?: string | null;
}

export interface TileLayerChange extends LayerChange {
  source: string;
}

export interface VectorLayerChange extends LayerChange {
  source: string;
}

export interface FeatureLayerChange extends VectorLayerChange {
  clusterStyle?: string | null;
}

export interface ImageLayerChange extends LayerChange {
  source: string;
}

export interface SourceChange extends MapChangeBase {
  attributions?: string | string[] | null;
}

export interface UrlTileSourceChange extends SourceChange {
  url?: string | null;
}

export interface TileWMSSourceChange extends UrlTileSourceChange {
  params: Record<string, unknown>;
}

export type XYZSourceChange = UrlTileSourceChange;

export type OSMSourceChange = XYZSourceChange;

export interface ImageWMSSourceChange extends SourceChange {
  url?: string | null;
  params: Record<string, unknown>;
}

export interface VectorSourceChange extends SourceChange {
  features: string[];
}

export interface ClusterSourceChange extends VectorSourceChange {
  distance: number;
  minDistance: number;
}

export interface FillChange extends MapChangeBase {
  color?: string | null;
}

export interface StrokeChange extends MapChangeBase {
  color?: string | null;
  width?: number | null;
}

export interface ImageStyleChange extends MapChangeBase {
  opacity: number;
  rotateWithView: boolean;
  rotation: number;
  scale: number;
}

export interface IconChange extends ImageStyleChange {
  img?: string | null;
  src?: string | null;
  imgSize?: MapSize | null;
  anchor?: MapCoordinate | null;
  anchorOrigin?: string | null;
}

export interface TextStyleChange extends MapChangeBase {
  font?: string | null;
  offset?: MapCoordinate | null;
  scale: number;
  rotation: number;
  rotateWithView: boolean;
  textAlign?: string | null;
  textBaseline?: string | null;
  fill?: string | null;
  stroke?: string | null;
  backgroundFill?: string | null;
  backgroundStroke?: string | null;
  padding: number;
}

export interface StyleChange extends MapChangeBase {
  image?: string | null;
  fill?: string | null;
  stroke?: string | null;
  text?: string | null;
}

export type AttributionControlChange = MapChangeBase;

export interface ScaleLineChange extends MapChangeBase {
  minWidth?: number | null;
  maxWidth?: number | null;
  units: string;
  displayMode?: string | null;
  scaleBarSteps?: number | null;
  scaleBarRatioVisible?: boolean | null;
}

export type ZoomControlChange = MapChangeBase;
