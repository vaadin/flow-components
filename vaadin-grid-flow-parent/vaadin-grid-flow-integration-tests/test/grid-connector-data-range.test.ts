import { expect } from 'chai';
import { aTimeout, fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import {
  init,
  GRID_CONNECTOR_ROOT_REQUEST_DELAY
} from './shared.js';
import type { FlowGrid } from './shared.js';

const PAGE_SIZE = 50;

describe('grid connector - data range', () => {
  let grid: FlowGrid;
  let lastRequestedRange: [number, number] | null;
  let rootSize: number;

  function setRootItemsRange(start: number, count: number) {
    const items = Array.from({ length: rootSize }, (_, i) => ({ key: `${i}`, name: `Item-${i}` }));

    grid.$connector.updateSize(rootSize);

    count = Math.min(count, rootSize - start);
    if (count === 0) {
      return;
    }

    if (lastRequestedRange) {
      grid.$connector.clear(lastRequestedRange[0], lastRequestedRange[1]);
    }

    grid.$connector.set(start, items.slice(start, start + count));
    grid.$connector.confirm(-1);
    lastRequestedRange = [start, count];
  }

  function expectRangeRequest([start, count]: [number, number]) {
    expect(grid.$server.setViewportRange).to.be.calledOnce;
    expect(grid.$server.setViewportRange.args[0]).to.eql([start, count]);
  }

  function resolveRangeRequest([start, count]: [number, number]) {
    setRootItemsRange(start, count);
    grid.$server.setViewportRange.promise?.resolve(null);
    grid.$server.setViewportRange.resetHistory();
    return Promise.resolve();
  }

  beforeEach(async () => {
    lastRequestedRange = null;

    rootSize = 200;

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

    init(grid);
    await nextFrame();

    grid.pageSize = PAGE_SIZE;
    grid.$connector.updateSize(rootSize);

    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expectRangeRequest([0, PAGE_SIZE]);

    await resolveRangeRequest([0, PAGE_SIZE]);
  });

  it('should request correct ranges when scrolling (start -> end -> start)', async () => {
    grid.scrollToIndex(rootSize - 1);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expectRangeRequest([rootSize - PAGE_SIZE, PAGE_SIZE * 2]);

    await resolveRangeRequest([rootSize - PAGE_SIZE, PAGE_SIZE * 2]);

    grid.scrollToIndex(0);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expectRangeRequest([0, PAGE_SIZE]);
  });

  it('should request correct range when size decreases after scrolling (start -> end)', async () => {
    grid.scrollToIndex(rootSize - 1);
    rootSize /= 2;
    grid.$connector.updateSize(rootSize);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expectRangeRequest([rootSize - PAGE_SIZE, PAGE_SIZE * 2]);
  });

  it('should request correct ranges when scrolling (start -> middle -> end)', async () => {
    grid.scrollToIndex(rootSize / 2);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expectRangeRequest([rootSize / 2 - PAGE_SIZE, PAGE_SIZE * 2]);

    await resolveRangeRequest([rootSize / 2 - PAGE_SIZE, PAGE_SIZE * 2]);

    grid.scrollToIndex(rootSize - 1);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expectRangeRequest([rootSize - PAGE_SIZE, PAGE_SIZE * 2]);
  });

  it('should request correct ranges when scrolling (end -> middle -> start)', async () => {
    grid.scrollToIndex(rootSize - 1);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expectRangeRequest([rootSize - PAGE_SIZE, PAGE_SIZE * 2]);

    await resolveRangeRequest([rootSize - PAGE_SIZE, PAGE_SIZE * 2]);

    grid.scrollToIndex(rootSize / 2);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expectRangeRequest([rootSize / 2 - PAGE_SIZE, PAGE_SIZE * 2]);

    await resolveRangeRequest([rootSize / 2 - PAGE_SIZE, PAGE_SIZE * 2]);

    grid.scrollToIndex(0);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expectRangeRequest([0, PAGE_SIZE]);
  });

  it('should debounce range requests when scrolling fast', async () => {
    grid.scrollToIndex(rootSize / 2);
    grid.scrollToIndex(rootSize - 1);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expectRangeRequest([rootSize - PAGE_SIZE, PAGE_SIZE * 2]);
  });

  it('should request correct ranges when scrolling fast (start -> end -> start)', async () => {
    grid.scrollToIndex(rootSize - 1);
    grid.scrollToIndex(0);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expectRangeRequest([0, PAGE_SIZE]);
  });

  it('should resolve pending requests after scrolling fast (start -> end -> start)', async () => {
    grid.scrollToIndex(rootSize - 1);
    grid.scrollToIndex(0);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    await resolveRangeRequest([0, 0]);
    expect(grid.loading).to.be.false;
  });
});
