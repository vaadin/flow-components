// @ts-nocheck
import './gridConnector.ts';

window.Vaadin.Flow.treeGridConnector = {};
window.Vaadin.Flow.treeGridConnector.initLazy = function (grid) {
  // Check whether the connector was already initialized for the grid
  if (grid.$connector) {
    return;
  }

  window.Vaadin.Flow.gridConnector.initLazy(grid);

  const dataProviderController = grid._dataProviderController;
  dataProviderController.__loadCachePage = function (cache, page) {
    if (grid.__pendingScrollToIndexes) {
      return;
    }

    Object.getPrototypeOf(this).__loadCachePage.call(this, cache, page);
  };

  function getViewportRange() {
    const renderedRows = grid._getRenderedRows();
    return [renderedRows[0]?.index ?? 0, renderedRows[renderedRows.length - 1]?.index ?? 0];
  }

  function getViewportSize() {
    const viewportRange = getViewportRange();
    return viewportRange[1] - viewportRange[0];
  }

  grid.scrollToIndex = async function (...indexes) {
    grid.__pendingScrollToIndexes = indexes;

    if (!grid.clientHeight || !grid._columnTree || dataProviderController.isLoading()) {
      return;
    }

    const buffer = Math.floor(getViewportSize() * 1.5);
    const flatIndex = await grid.$server.setViewportRangeByIndexPath(indexes, buffer);

    grid._scrollToFlatIndex(flatIndex);

    delete grid.__pendingScrollToIndexes;
  };

  grid._isExpanded = function (item) {
    return item?.expanded;
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
};
