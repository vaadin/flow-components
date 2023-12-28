import { aTimeout, expect, fixtureSync, nextFrame } from '@open-wc/testing';
import {
  init,
  GRID_CONNECTOR_ROOT_REQUEST_DELAY
} from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector - data range', () => {
  let grid: FlowGrid;
  let size = 500;

  let lastRange: [number, number] | null = null;

  function setItemsRange(start: number, count: number) {
    const items = Array.from({ length: size }, (_, i) => ({ key: `${i}`, name: `Item ${i}` }));

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

    grid.$connector.updateSize(size);
    await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
  });

  it('should request initial data range', () => {
    expect(grid.$server.setRequestedRange).to.be.calledOnce;
    expect(grid.$server.setRequestedRange.args[0]).to.eql([0, 50]);
  });

  describe('initial data range is loaded', () => {
    beforeEach(async () => {
      setItemsRange(0, 50);
      grid.$server.setRequestedRange.resetHistory();
    });

    it('should request data range after scrolling to middle', async () => {
      grid.scrollToIndex(250);
      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
      expect(grid.$server.setRequestedRange).to.be.calledOnce;
      expect(grid.$server.setRequestedRange.args[0]).to.eql([200, 100]);
    });

    it('should request data range after scrolling to end', async () => {
      grid.scrollToIndex(size - 1);
      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
      expect(grid.$server.setRequestedRange).to.be.calledOnce;
      expect(grid.$server.setRequestedRange.args[0]).to.eql([450, 100]);
    });

    it('should request data range after scrolling to end and back to start', async () => {
      grid.scrollToIndex(size - 1);
      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
      setItemsRange(450, 100);
      grid.$server.setRequestedRange.resetHistory();

      grid.scrollToIndex(0);
      await aTimeout(GRID_CONNECTOR_ROOT_REQUEST_DELAY);
      expect(grid.$server.setRequestedRange).to.be.calledOnce;
      expect(grid.$server.setRequestedRange.args[0]).to.eql([0, 50]);
    });
  });
});
