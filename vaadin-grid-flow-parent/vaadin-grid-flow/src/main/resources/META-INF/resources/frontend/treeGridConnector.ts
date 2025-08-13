// @ts-nocheck
import './gridConnector.ts';

window.Vaadin.Flow.treeGridConnector = {};
window.Vaadin.Flow.treeGridConnector.initLazy = function (grid) {
  if (grid.$connector) {
    return;
  }

  window.Vaadin.Flow.gridConnector.initLazy(grid);

  function getViewportRange() {
    const renderedRows = grid._getRenderedRows();
    return [renderedRows[0]?.index ?? 0, renderedRows[renderedRows.length - 1]?.index ?? 0];
  }

  grid._dataProviderController.__loadCachePage = function (cache, page) {
    if (grid.__pendingScrollToIndexes) {
      return;
    }

    Object.getPrototypeOf(this).__loadCachePage.call(this, cache, page);
  };

  grid.scrollToIndex = async function (...indexes) {
    grid.__pendingScrollToIndexes = indexes;

    if (!grid.clientHeight || !grid._columnTree || grid._dataProviderController.isLoading()) {
      return;
    }

    const [start, end] = getViewportRange();
    const padding = Math.floor((end - start) * 1.5);
    const flatIndex = await grid.$server.setViewportRangeByIndexPath(indexes, padding);
    grid._scrollToFlatIndex(flatIndex);

    delete grid.__pendingScrollToIndexes;
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
};
