import { aTimeout, expect, fixtureSync, nextFrame } from '@open-wc/testing';
import {
  init,
  GRID_CONNECTOR_ROOT_REQUEST_DELAY,
  setRootItems,
  expandItems,
  GRID_CONNECTOR_PARENT_REQUEST_DELAY
} from './shared.js';
import type { FlowGrid, Item } from './shared.js';

const SIZE = 200;
const PAGE_SIZE = 50;

describe('grid connector - tree data range', () => {
  let grid: FlowGrid;
  let lastRangeMap: Map<string, [number, number]>;

  function setChildItemsRange(parentItem: Item, size: number, start: number, count: number) {
    const items = Array.from({ length: size }, (_, i) => {
      return { key: `${i}`, name: `${parentItem.name}-${i}` };
    });

    const lastRange = lastRangeMap.has(parentItem.key);
    if (lastRange) {
      grid.$connector.clear(lastRange[0], lastRange[1], parentItem.key);
    }

    grid.$connector.set(start, items.slice(start, start + count), parentItem.key);
    grid.$connector.confirmParent(-1, parentItem.key, size);

    lastRangeMap.set(parentItem.key, [start, count]);
  }

  beforeEach(async () => {
    lastRangeMap = new Map();

    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-tree-column path="name"></vaadin-grid-tree-column>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);
    await nextFrame();

    setRootItems(grid.$connector, [
      { key: '0', name: 'Item 0' },
      { key: '1', name: 'Item 1' },
    ]);
    expandItems(grid.$connector, [
      { key: '0', name: 'Item 0' },
      { key: '1', name: 'Item 1' }
    ]);

    await aTimeout(GRID_CONNECTOR_PARENT_REQUEST_DELAY);
    expect(grid.$server.setParentRequestedRanges).to.be.calledOnce;
    expect(grid.$server.setParentRequestedRanges.args[0]).to.eql([
      { start: 0, size: PAGE_SIZE, parentKey: '0' },
      { start: 0, size: PAGE_SIZE, parentKey: '1' }
    ]);
  });

  // it('should request correct data range when scrolling to end', async () => {
  //   grid.scrollToIndex(SIZE - 1);
  //   await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //   expect(grid.$server.setRequestedRange).to.be.calledOnce;
  //   expect(grid.$server.setRequestedRange.args[0]).to.eql([SIZE - PAGE_SIZE, PAGE_SIZE * 2]);
  // });

  // it('should request correct data range when size decreases after scrolling to end', async () => {
  //   const newSize = SIZE / 2;
  //   grid.scrollToIndex(SIZE - 1);
  //   grid.$connector.updateSize(newSize);
  //   await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //   expect(grid.$server.setRequestedRange).to.be.calledOnce;
  //   expect(grid.$server.setRequestedRange.args[0]).to.eql([newSize - PAGE_SIZE, PAGE_SIZE * 2]);
  // });

  // it('should request correct data range when scrolling from end to start', async () => {
  //   grid.scrollToIndex(SIZE - 1);
  //   await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //   setItemsRange(SIZE - PAGE_SIZE, PAGE_SIZE * 2);
  //   grid.$server.setRequestedRange.resetHistory();

  //   grid.scrollToIndex(0);
  //   await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //   expect(grid.$server.setRequestedRange).to.be.calledOnce;
  //   expect(grid.$server.setRequestedRange.args[0]).to.eql([0, PAGE_SIZE]);
  // });

  // it('should request correct data ranges when gradually scrolling to end', async () => {
  //   for (let i = PAGE_SIZE; i < SIZE; i += PAGE_SIZE) {
  //     grid.scrollToIndex(i - 1);
  //     await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //     expect(grid.$server.setRequestedRange).to.be.calledOnce;
  //     expect(grid.$server.setRequestedRange.args[0]).to.eql([i - PAGE_SIZE, PAGE_SIZE * 2]);
  //     grid.$server.setRequestedRange.resetHistory();
  //   }
  // });

  // it('should request correct data ranges when gradually scrolling from end to start', async () => {
  //   grid.scrollToIndex(SIZE - 1);
  //   await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //   setItemsRange(SIZE - PAGE_SIZE, PAGE_SIZE);
  //   grid.$server.setRequestedRange.resetHistory();

  //   for (let i = SIZE - PAGE_SIZE; i > 0; i -= PAGE_SIZE) {
  //     grid.scrollToIndex(i - 1);
  //     await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //     expect(grid.$server.setRequestedRange).to.be.calledOnce;
  //     expect(grid.$server.setRequestedRange.args[0]).to.eql([i - PAGE_SIZE, PAGE_SIZE * 2]);
  //     grid.$server.setRequestedRange.resetHistory();
  //   }
  // });

  // it('should debounce data range requests when scrolling', async () => {
  //   grid.scrollToIndex(SIZE / 2);
  //   grid.scrollToIndex(SIZE - 1);
  //   await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //   expect(grid.$server.setRequestedRange).to.be.calledOnce;
  //   expect(grid.$server.setRequestedRange.args[0]).to.eql([SIZE - PAGE_SIZE, PAGE_SIZE * 2]);
  // });

  // describe('scrolling to end and immediately back to start', () => {
  //   beforeEach(() => {
  //     grid.scrollToIndex(SIZE - 1);
  //     grid.scrollToIndex(0);
  //   });

  //   it('should request data range first for end and then for start', async () => {
  //     await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //     expect(grid.$server.setRequestedRange).to.be.calledOnce;
  //     expect(grid.$server.setRequestedRange.args[0]).to.eql([SIZE - PAGE_SIZE, PAGE_SIZE]);

  //     grid.$server.setRequestedRange.resetHistory();

  //     setItemsRange(SIZE - PAGE_SIZE, PAGE_SIZE);
  //     await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //     expect(grid.$server.setRequestedRange).to.be.calledOnce;
  //     expect(grid.$server.setRequestedRange.args[0]).to.eql([0, PAGE_SIZE]);
  //   });

  //   it('should request correct data range when size decreases after scrolling', async () => {
  //     const newSize = SIZE / 2;
  //     grid.$connector.updateSize(newSize);
  //     await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //     expect(grid.$server.setRequestedRange).to.be.calledOnce;
  //     expect(grid.$server.setRequestedRange.args[0]).to.eql([newSize - PAGE_SIZE, PAGE_SIZE]);
  //   });
  // });
});
