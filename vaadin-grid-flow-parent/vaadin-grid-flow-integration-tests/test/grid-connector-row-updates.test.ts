import { expect } from 'chai';
import { aTimeout, fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { init, setRootItems, GRID_CONNECTOR_ROOT_REQUEST_DELAY } from './shared.js';
import type { FlowGrid, Item } from './shared.js';
import sinon from 'sinon';

// These tests guard the connector optimization that suppresses redundant
// `__updateRow` calls (via `preventRowUpdates`) while it applies selection,
// row details, and resolves pending callbacks. Each suppressed update would
// otherwise re-run the column renderer for the affected rows, so the renderer
// call count is used to measure how many times rows are rendered.

function addRenderer(grid: FlowGrid): sinon.SinonSpy {
  const column = grid.querySelector('vaadin-grid-column')!;
  const renderer = sinon.spy((root: HTMLElement, _column: unknown, model: { item: Item }) => {
    root.textContent = model.item.name ?? '';
  });
  column.renderer = renderer;
  return renderer;
}

describe('grid connector - row updates', () => {
  describe('local data', () => {
    const items: Item[] = Array.from({ length: 5 }, (_, i) => ({ key: `${i}`, name: `name-${i}` }));

    let grid: FlowGrid;
    let renderer: sinon.SinonSpy;

    beforeEach(async () => {
      grid = fixtureSync(`
        <vaadin-grid>
          <vaadin-grid-column></vaadin-grid-column>
        </vaadin-grid>
      `);
      renderer = addRenderer(grid);
      init(grid);
      grid.$connector.setSelectionMode('MULTI');
      setRootItems(grid.$connector, items);
      await nextFrame();
    });

    it('should render each visible row once when applying selection from data', async () => {
      renderer.resetHistory();
      setRootItems(
        grid.$connector,
        items.map((item) => ({ ...item, selected: true }))
      );
      await nextFrame();
      // One render per visible row.
      expect(renderer.callCount).to.equal(items.length);
    });

    it('should not render rows when clearing them', async () => {
      setRootItems(
        grid.$connector,
        items.map((item) => ({ ...item, selected: true }))
      );
      await nextFrame();

      renderer.resetHistory();
      grid.$connector.clear(0, items.length);
      await nextFrame();
      // Cleared rows have no data to render.
      expect(renderer.callCount).to.equal(0);
    });

    it('should keep suppressing row updates across nested connector updates', async () => {
      // Start with every item selected.
      setRootItems(
        grid.$connector,
        items.map((item) => ({ ...item, selected: true }))
      );
      await nextFrame();

      // While the next connector update is applying selection (with row updates
      // suppressed), a synchronous selection listener triggers a second
      // connector update. Suppression must stay active until the outermost
      // update finishes, so the rows are still rendered only once.
      const onSelection = () => {
        grid.$connector.updateFlatData([{ ...items[0], name: 'updated' }]);
      };
      grid.addEventListener('selected-items-changed', onSelection, { once: true });

      renderer.resetHistory();
      // Deselect the last three items. Applying the change fires the selection
      // listener, which re-enters the connector via updateFlatData.
      setRootItems(grid.$connector, [
        { ...items[0], selected: true },
        { ...items[1], selected: true },
        { ...items[2] },
        { ...items[3] },
        { ...items[4] }
      ]);
      await nextFrame();

      expect(renderer.callCount).to.equal(items.length);
    });
  });

  describe('lazy data', () => {
    const PAGE_SIZE = 50;
    const SIZE = 200;

    let grid: FlowGrid;
    let renderer: sinon.SinonSpy;

    function setRootItemsRange(start: number, count: number) {
      const items = Array.from({ length: SIZE }, (_, i) => ({ key: `${i}`, name: `Item-${i}` }));
      grid.$connector.updateSize(SIZE);
      grid.$connector.set(start, items.slice(start, start + count));
      grid.$connector.confirm(-1);
    }

    function resolveRangeRequest(start: number, count: number) {
      setRootItemsRange(start, count);
      grid.$server.setViewportRange.promise?.resolve(null);
      grid.$server.setViewportRange.resetHistory();
    }

    beforeEach(async () => {
      grid = fixtureSync(`
        <vaadin-grid>
          <vaadin-grid-column path="name"></vaadin-grid-column>
        </vaadin-grid>
        <style>
          vaadin-grid::part(cell) {
            min-height: 36px;
          }
        </style>
      `);
      renderer = addRenderer(grid);
      init(grid);
      await nextFrame();

      grid.pageSize = PAGE_SIZE;
      grid.$connector.updateSize(SIZE);
      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
      resolveRangeRequest(0, PAGE_SIZE);
      await nextFrame();
    });

    it('should not render cached rows when a range request resolves with no new data', async () => {
      // Scroll to the end and back so the grid issues a new range request while
      // the start page is still cached and rendered.
      grid.scrollToIndex(SIZE - 1);
      grid.scrollToIndex(0);
      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);

      renderer.resetHistory();
      // The server resolves the range request without sending new data, because
      // the requested rows are already on the client. The connector then
      // resolves its pending callbacks, which must not re-render the cached
      // rows.
      grid.$server.setViewportRange.promise?.resolve(null);
      await nextFrame();
      expect(renderer.callCount).to.equal(0);
    });
  });
});
