import { aTimeout, expect, fixtureSync, nextFrame } from '@open-wc/testing';
import {
  init,
  GRID_CONNECTOR_ROOT_REQUEST_DELAY
} from './shared.js';
import type { FlowGrid } from './shared.js';

const SIZE = 200;
const PAGE_SIZE = 50;

describe('grid connector - data range', () => {
  let grid: FlowGrid;
  let lastRange: [number, number] | null = null;

  function setItemsRange(start: number, count: number) {
    const items = Array.from({ length: SIZE }, (_, i) => ({ key: `${i}`, name: `Item ${i}` }));

    if (lastRange) {
      grid.$connector.clear(lastRange[0], lastRange[1]);
    }

    grid.$connector.set(start, items.slice(start, start + count));
    grid.$connector.confirm(-1);

    lastRange = [start, count];
  }

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);
    await nextFrame();

    grid.pageSize = PAGE_SIZE;
    grid.$connector.updateSize(SIZE);

    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expect(grid.$server.setRequestedRange).to.be.calledOnce;
    expect(grid.$server.setRequestedRange.args[0]).to.eql([0, PAGE_SIZE]);

    setItemsRange(0, PAGE_SIZE);
    grid.$server.setRequestedRange.resetHistory();
  });

  it('should request correct data range after scrolling to end', async () => {
    grid.scrollToIndex(SIZE - 1);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expect(grid.$server.setRequestedRange).to.be.calledOnce;
    expect(grid.$server.setRequestedRange.args[0]).to.eql([SIZE - PAGE_SIZE, PAGE_SIZE * 2]);
  });

  it('should request correct data range after scrolling from end to start', async () => {
    grid.scrollToIndex(SIZE - 1);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    setItemsRange(SIZE - PAGE_SIZE, PAGE_SIZE * 2);
    grid.$server.setRequestedRange.resetHistory();

    grid.scrollToIndex(0);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expect(grid.$server.setRequestedRange).to.be.calledOnce;
    expect(grid.$server.setRequestedRange.args[0]).to.eql([0, PAGE_SIZE]);
  });

  it('should request correct data range after size decreases after scrolling to end', async () => {
    const newSize = SIZE / 2;
    grid.scrollToIndex(SIZE - 1);
    grid.$connector.updateSize(newSize);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expect(grid.$server.setRequestedRange).to.be.calledOnce;
    expect(grid.$server.setRequestedRange.args[0]).to.eql([newSize - PAGE_SIZE, PAGE_SIZE * 2]);
  });

  it('should request correct data ranges when gradually scrolling to end', async () => {
    for (let i = PAGE_SIZE; i < SIZE; i += PAGE_SIZE) {
      grid.scrollToIndex(i - 1);
      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
      expect(grid.$server.setRequestedRange).to.be.calledOnce;
      expect(grid.$server.setRequestedRange.args[0]).to.eql([i - PAGE_SIZE, PAGE_SIZE * 2]);
      grid.$server.setRequestedRange.resetHistory();
    }
  });

  it('should request correct data ranges when gradually scrolling from end to start', async () => {
    grid.scrollToIndex(SIZE - 1);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    setItemsRange(SIZE - PAGE_SIZE, PAGE_SIZE);
    grid.$server.setRequestedRange.resetHistory();

    for (let i = SIZE - PAGE_SIZE; i > 0; i -= PAGE_SIZE) {
      grid.scrollToIndex(i - 1);
      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
      expect(grid.$server.setRequestedRange).to.be.calledOnce;
      expect(grid.$server.setRequestedRange.args[0]).to.eql([i - PAGE_SIZE, PAGE_SIZE * 2]);
      grid.$server.setRequestedRange.resetHistory();
    }
  });

  it('should debounce data range requests when scrolling', async () => {
    grid.scrollToIndex(SIZE / 2);
    grid.scrollToIndex(SIZE - 1);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
    expect(grid.$server.setRequestedRange).to.be.calledOnce;
    expect(grid.$server.setRequestedRange.args[0]).to.eql([SIZE - PAGE_SIZE, PAGE_SIZE * 2]);
  });

  // describe('scrolling to end and immediately back to start', () => {
  //   beforeEach(() => {
  //     grid.scrollToIndex(SIZE - 1);
  //     grid.scrollToIndex(0);
  //   });

  //   it('should request data range first for end and then start', async () => {
  //     await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //     expect(grid.$server.setRequestedRange).to.be.calledOnce;
  //     expect(grid.$server.setRequestedRange.args[0]).to.eql([SIZE - PAGE_SIZE, PAGE_SIZE]);

  //     grid.$server.setRequestedRange.resetHistory();

  //     setItemsRange(SIZE - PAGE_SIZE, PAGE_SIZE);
  //     await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //     expect(grid.$server.setRequestedRange).to.be.calledOnce;
  //     expect(grid.$server.setRequestedRange.args[0]).to.eql([0, PAGE_SIZE]);
  //   });

  //   it('should request correct data range after size decreases after scrolling', async () => {
  //     const newSize = SIZE / 2;
  //     grid.$connector.updateSize(newSize);
  //     await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  //     expect(grid.$server.setRequestedRange).to.be.calledOnce;
  //     expect(grid.$server.setRequestedRange.args[0]).to.eql([newSize - PAGE_SIZE, PAGE_SIZE]);
  //   });
  // });
});
