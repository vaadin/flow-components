import { aTimeout, expect, fixtureSync, nextFrame } from '@open-wc/testing';
import {
  init,
  setRootItems,
  expandItems,
  GRID_CONNECTOR_PARENT_REQUEST_DELAY
} from './shared.js';
import type { FlowGrid, Item } from './shared.js';

const PAGE_SIZE = 50;

describe('grid connector - tree data range', () => {
  let grid: FlowGrid;
  let lastRequestedRangeMap: Map<string, [number, number]>;
  let childSize;

  const rootItems = [
    { key: 'Item-0', name: 'Item-0' },
    { key: 'Item-1', name: 'Item-1' },
  ];

  function setChildItemsRange(parentKey: string, start: number, count: number) {
    const items = Array.from({ length: childSize }, (_, i) => {
      return { key: `${parentKey}-${i}`, name: `${parentKey}-${i}` };
    });

    const lastRequestedRange = lastRequestedRangeMap.get(parentKey);
    if (lastRequestedRange) {
      grid.$connector.clear(lastRequestedRange[0], lastRequestedRange[1], parentKey);
    }

    grid.$connector.set(start, items.slice(start, start + count), parentKey);
    grid.$connector.confirmParent(-1, parentKey, childSize);
    lastRequestedRangeMap.set(parentKey, [start, count]);
  }

  function processParentRequestedRanges() {
    grid.$server.setParentRequestedRanges.args[0][0].forEach(
      ({ parentKey, firstIndex, size }) => setChildItemsRange(parentKey, firstIndex, size)
    );
    grid.$server.setParentRequestedRanges.resetHistory();
  }

  function expectParentRequestedRanges(ranges: Parameters<typeof grid.$server.setParentRequestedRanges>[0]) {
    expect(grid.$server.setParentRequestedRanges).to.be.calledOnce;
    expect(grid.$server.setParentRequestedRanges.args[0][0]).to.eql(ranges);
  }

  beforeEach(async () => {
    lastRequestedRangeMap = new Map();

    childSize = 400;

    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-tree-column path="name"></vaadin-grid-tree-column>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);
    await nextFrame();

    setRootItems(grid.$connector, rootItems);
    expandItems(grid.$connector, [rootItems[0]]);

    await nextFrame();
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    expectParentRequestedRanges([
      { parentKey: rootItems[0].key, firstIndex: 0, size: PAGE_SIZE }
    ]);
    processParentRequestedRanges();
  });

  it('should request correct range when scrolling gradually (start <-> end)', async () => {
    grid.scrollToIndex(0, childSize - 1);
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    expectParentRequestedRanges([
      { parentKey: rootItems[0].key, firstIndex: childSize - PAGE_SIZE, size: PAGE_SIZE }
    ]);

    processParentRequestedRanges();

    grid.scrollToIndex(0, 0);
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    expectParentRequestedRanges([
      { parentKey: rootItems[0].key, firstIndex: 0, size: PAGE_SIZE }
    ]);
  });

  it('should request correct range when scrolling gradually (start -> middle -> end)', async () => {
    grid.scrollToIndex(0, childSize / 2);
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    expectParentRequestedRanges([
      { parentKey: rootItems[0].key, firstIndex: childSize / 2 - PAGE_SIZE, size: PAGE_SIZE * 2 }
    ]);

    processParentRequestedRanges();

    grid.scrollToIndex(0, childSize);
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    expectParentRequestedRanges([
      { parentKey: rootItems[0].key, firstIndex: childSize - PAGE_SIZE, size: PAGE_SIZE }
    ]);
  });

  it('should request correct range when scrolling gradually (end -> middle -> start)', async () => {
    grid.scrollToIndex(0, childSize - 1);
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    processParentRequestedRanges();

    grid.scrollToIndex(0, childSize / 2);
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    expectParentRequestedRanges([
      { parentKey: rootItems[0].key, firstIndex: childSize / 2 - PAGE_SIZE, size: PAGE_SIZE * 2 }
    ]);

    processParentRequestedRanges();

    grid.scrollToIndex(0, 0);
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    expectParentRequestedRanges([
      { parentKey: rootItems[0].key, firstIndex: 0, size: PAGE_SIZE }
    ]);
  });

  it('should request correct ranges when scrolling instantly (start <-> end)', async () => {
    grid.scrollToIndex(0, childSize - 1);
    grid.scrollToIndex(0, 0);
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    expectParentRequestedRanges([
      { parentKey: rootItems[0].key, firstIndex: childSize - PAGE_SIZE, size: PAGE_SIZE }
    ]);

    processParentRequestedRanges();

    await nextFrame();
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    expectParentRequestedRanges([
      { parentKey: rootItems[0].key, firstIndex: 0, size: PAGE_SIZE }
    ]);
  });

  it('should debounce range requests when scrolling', async () => {
    grid.scrollToIndex(0, childSize / 2);
    grid.scrollToIndex(0, childSize - 1);
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    expectParentRequestedRanges([
      { parentKey: rootItems[0].key, firstIndex: childSize - PAGE_SIZE, size: PAGE_SIZE }
    ]);
  });

  it('should batch range requests from different levels when scrolling', async () => {
    grid.$connector.expandItems([rootItems[1]]);
    await nextFrame();
    grid.scrollToIndex(1);
    await nextFrame();
    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    expectParentRequestedRanges([
      { parentKey: rootItems[0].key, firstIndex: childSize - PAGE_SIZE, size: PAGE_SIZE },
      { parentKey: rootItems[1].key, firstIndex: 0, size: PAGE_SIZE }
    ]);
  });
});
