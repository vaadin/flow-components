import { setUserProjection as openLayersSetUserProjection } from 'ol/proj';
import { synchronize } from './synchronization';
import { createLookup, getLayerForFeature } from './util';

// By default, use EPSG:4326 projection for all coordinates passed to, and return from the public API.
// Internally coordinates will be converted to the projection used by the map's view.
openLayersSetUserProjection('EPSG:4326');

(function () {
  function init(mapElement) {
    mapElement.$connector = {
      /**
       * Lookup for storing and retrieving every OL instance used in the map's configuration
       * by its unique ID
       */
      lookup: createLookup(),
      /**
       * Synchronizes an array of Javascript objects into OL instances.
       * It is expected that objects that are lower in the configuration hierarchy occur
       * earlier in the array than objects that are higher in the hierarchy. That ensures
       * that lower-level objects are synchronized first, before higher-level objects that
       * reference them.
       * @param changedObjects array of Javascript objects to be synchronized into OL instances
       */
      synchronize(changedObjects) {
        // Provide synchronization function and the OL instance lookup through context object
        const context = { synchronize, lookup: this.lookup, mapElement, connector: mapElement.$connector };

        changedObjects.forEach((change) => {
          // The OL map instance already exists and should not be created by the
          // synchronization mechanism. So we put it into the lookup manually.
          if (change.type === 'ol/Map') {
            this.lookup.put(change.id, mapElement.configuration);
          }

          synchronize(change, context);
        });
      },
      /**
       * Forces a render of the OpenLayers map. Some objects in OpenLayers are not observable
       * and do not trigger change events, for example Style objects or any of their children.
       * In these cases this method can be called from the synchronization functions of these
       * objects.
       * This method will trigger a debounced render of the map by firing a change event from
       * each layer. We simply render all layers as a sync. function does not know which layer
       * its synced object is in. Even if the change event is fired from multiple layers, this
       * only results in a single render of the map.
       */
      forceRender() {
        if (this._forceRenderTimeout) {
          return;
        }
        this._forceRenderTimeout = setTimeout(() => {
          this._forceRenderTimeout = null;
          mapElement.configuration
            .getLayers()
            .getArray()
            .forEach((layer) => layer.changed());
        });
      }
    };

    mapElement.configuration.on('moveend', (_event) => {
      const view = mapElement.configuration.getView();
      const center = view.getCenter();
      const rotation = view.getRotation();
      const zoom = view.getZoom();
      const extent = view.calculateExtent();

      const customEvent = new CustomEvent('map-view-moveend', {
        detail: {
          center,
          rotation,
          zoom,
          extent
        }
      });

      mapElement.dispatchEvent(customEvent);
    });

    mapElement.configuration.on('singleclick', (event) => {
      const coordinate = event.coordinate;
      // Get the features at the clicked pixel position
      // In case multiple features exist at that position, OpenLayers
      // returns the features sorted in the order that they are displayed,
      // with the front-most feature as the first result, and the
      // back-most feature as the last result
      const pixelCoordinate = event.pixel;
      const featuresAtPixel = mapElement.configuration.getFeaturesAtPixel(pixelCoordinate);
      // Create tuples of features and the layer that they are in
      const featuresAndLayers = featuresAtPixel.map((feature) => {
        const layer = getLayerForFeature(mapElement.configuration.getLayers().getArray(), feature);
        return {
          feature,
          layer
        };
      });

      // Map click event
      const mapClickEvent = new CustomEvent('map-click', {
        detail: {
          coordinate,
          features: featuresAndLayers,
          originalEvent: event.originalEvent
        }
      });

      mapElement.dispatchEvent(mapClickEvent);

      // Feature click event
      if (featuresAndLayers.length > 0) {
        // Send a feature click event for the top-level feature
        const featureAndLayer = featuresAndLayers[0];
        const featureClickEvent = new CustomEvent('map-feature-click', {
          detail: {
            feature: featureAndLayer.feature,
            layer: featureAndLayer.layer,
            originalEvent: event.originalEvent
          }
        });

        mapElement.dispatchEvent(featureClickEvent);
      }
    });
  }

  /**
   * Set a custom user projection for all coordinates passing through the public API.
   * Internally coordinates will be converted to the projection used by the map's view.
   * @param projection
   */
  function setUserProjection(projection) {
    openLayersSetUserProjection(projection);
  }

  window.Vaadin.Flow.mapConnector = {
    init,
    setUserProjection,
  };
})();
