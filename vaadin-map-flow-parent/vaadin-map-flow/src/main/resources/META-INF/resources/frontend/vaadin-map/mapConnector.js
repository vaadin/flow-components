import { synchronize } from "./synchronization";

(function () {
  function init(mapElement) {
    mapElement.$connector = {
      /**
       * Contains image assets that were passed from the server
       */
      imageAssets: {},
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

        const context = { synchronize, imageAssets: this.imageAssets };

        synchronize(target, configuration, context);
        // TODO: layers don't render on initialization in some cases, needs investigation
        mapElement.configuration.updateSize();
      },
      /**
       * Adds an image asset, which should be an <img> element that was added as a virtual child.
       * This allows to use server-side images, for example from JAR resources, in OL constructs such as `ol/style/Icon`
       * @param assetName Unique id associated with the server-side asset
       * @param image <img> element
       */
      addImageAsset(assetName, image) {
        this.imageAssets[assetName] = image;
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
  }

  window.Vaadin.Flow.mapConnector = {
    init,
  };
})();
