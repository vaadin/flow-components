import './gridConnector.ts';
import type { FlowTreeGrid } from './vaadin-grid-types.js';

/**
 * treeGridConnector is a communication layer between TreeGrid's flow component
 * (server-side) and web component (client-side).
 *
 * TreeGrid does not rely on the web component's built-in features for handling
 * hierarchical data. Instead, the hierarchy is fully managed on the server side
 * and sent to the client as a flattened structure. This approach simplifies the
 * client-side implementation and improves performance by avoiding recursive
 * requests to the data provider.
 *
 * While the data is transferred as a flat list, the connector makes it appear as
 * a hierarchy by overriding the web component's methods to add indentation based
 * on information from server-provided fields `item.level`, `item.expanded`, etc.
 *
 * The connector overrides the web component's default `scrollToIndex(...indexes)`
 * implementation, as it by default assumes that the hierarchy is managed on the
 * client side. Instead, it uses the server-side method to resolve the hierarchical
 * path and preload the viewport range, all in a single round-trip. As a result,
 * required data is already loaded on the client-side by the time the scrolling
 * begins, which allows the scrollToIndex operation to be executed faster.
 *
 * The server estimates the viewport range for `scrollToIndex` based on the `padding`
 * parameter of $server.setViewportRangeByIndexPath, which defines how many items to
 * include above and below the target item in the range.
 */
function initLazy(grid: FlowTreeGrid) {
  if (grid.$connector) {
    return;
  }

  window.Vaadin.Flow.gridConnector.initLazy(grid);

  grid.scrollToIndex = async function (...indexes) {
    if (!grid.clientHeight || !grid._columnTree || grid._dataProviderController.isLoading()) {
      // Not ready yet. The web component retries from __scrollToPendingIndexes
      // once a page has loaded or the grid has become visible.
      grid.__pendingScrollToIndexes = indexes;
      return;
    }

    const [start, end] = grid.$connector.getRenderedRange();
    const padding = Math.floor((end - start) * 1.5);
    const flatIndex = await grid.$server.setViewportRangeByIndexPath(indexes, padding);
    grid._scrollToFlatIndex(flatIndex);

    // While the preloaded range was being applied, the rows of the old range
    // were still rendered and requested their pages again. Now that the grid
    // has scrolled, those requests are stale. Resolve them so that the
    // rendered rows are re-checked against the cache and the debounced
    // server request gets cancelled.
    grid.$connector.resolvePendingCallbacks();

    return flatIndex;
  };

  grid.__getRowLevel = function (row) {
    return row._item?.level ?? 0;
  };

  grid._isExpanded = function (item) {
    return !!item?.expanded;
  };

  grid.expandItem = function (item) {
    if (item !== undefined) {
      grid.$server.updateExpandedState(grid.getItemId(item), true);
    }
  };

  grid.collapseItem = function (item) {
    if (item !== undefined) {
      grid.$server.updateExpandedState(grid.getItemId(item), false);
    }
  };
}

window.Vaadin.Flow.treeGridConnector = { initLazy };
