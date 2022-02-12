import { synchronize } from "./synchronization";
import { createLookup, getLayerForFeature } from "./util";

const debug = true;

(function () {
  function init(mapElement) {
    mapElement.$connector = {
      lookup: createLookup(),
      /**
       * Synchronize a configuration object into the internal OpenLayers map instance.
       *
       * The target parameter can be left unspecified, in which case the OpenLayers
       * map instance itself is used as synchronization target. This results in a
       * full sync of the map configuration hierarchy.
       * Specifying a target instance allows synchronizing only a part of the
       * configuration. In that case the configuration parameter needs to be for the
       * specific part that should be synchronized. For example, when passing an
       * `ol/View` instance as target, then the configuration parameter should
       * contain a configuration object for a view as well.
       *
       * @param configuration the configuration object to synchronize from
       * @param target the OpenLayers configuration instance to synchronize into, or undefined if the root map instance should be used
       */
      synchronize(configuration, target) {
        if (!configuration || typeof configuration !== "object") {
          throw new Error("Configuration must be an object");
        }
        // Assume we want to sync the full map by default
        if (!target) {
          target = mapElement.configuration;
        }

        // We don't want the synchronization to return a new instance (we have no
        // idea where to store it afterwards), so we adopt the ID from the
        // configuration object and assume that the types will be the same
        target.id = configuration.id;

        const context = { synchronize };

        synchronize(target, configuration, context);
        // TODO: layers don't render on initialization in some cases, needs investigation
        mapElement.configuration.updateSize();
      },
      synchronizeChanges(changes) {
        const context = { synchronize, lookup: this.lookup };

        if (debug) {
          console.debug("Changeset", JSON.stringify(changes, null, 2));
        }

        changes.forEach((change) => {
          // TODO improve sync of map ID
          if (change.type === "ol/Map") {
            this.lookup.put(change.id, mapElement.configuration);
          }
          // TODO improve sync of view ID
          if (change.type === "ol/View") {
            this.lookup.put(change.id, mapElement.configuration.getView());
          }

          synchronize(change, context);
        });
      },
    };

    mapElement.configuration.on("moveend", (_event) => {
      const view = mapElement.configuration.getView();
      const center = view.getCenter();
      const rotation = view.getRotation();
      const zoom = view.getZoom();
      const extent = view.calculateExtent();

      const customEvent = new CustomEvent("map-view-moveend", {
        detail: {
          center,
          rotation,
          zoom,
          extent,
        },
      });

      mapElement.dispatchEvent(customEvent);
    });

    mapElement.configuration.on("singleclick", (event) => {
      const coordinate = event.coordinate;
      // Get the features at the clicked pixel position
      // In case multiple features exist at that position, OpenLayers
      // returns the features sorted in the order that they are displayed,
      // with the front-most feature as the first result, and the
      // back-most feature as the last result
      const pixelCoordinate = event.pixel;
      const featuresAtPixel =
        mapElement.configuration.getFeaturesAtPixel(pixelCoordinate);
      // Create tuples of features and the layer that they are in
      const featuresAndLayers = featuresAtPixel.map((feature) => {
        const layer = getLayerForFeature(
          mapElement.configuration.getLayers().getArray(),
          feature
        );
        return {
          feature,
          layer,
        };
      });

      // Map click event
      const mapClickEvent = new CustomEvent("map-click", {
        detail: {
          coordinate,
          features: featuresAndLayers,
          originalEvent: event.originalEvent,
        },
      });

      mapElement.dispatchEvent(mapClickEvent);

      // Feature click event
      if (featuresAndLayers.length > 0) {
        // Send a feature click event for the top-level feature
        const featureAndLayer = featuresAndLayers[0];
        const featureClickEvent = new CustomEvent("map-feature-click", {
          detail: {
            feature: featureAndLayer.feature,
            layer: featureAndLayer.layer,
            originalEvent: event.originalEvent,
          },
        });

        mapElement.dispatchEvent(featureClickEvent);
      }
    });
  }

  window.Vaadin.Flow.mapConnector = {
    init,
  };
})();
