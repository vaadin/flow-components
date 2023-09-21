import { expect, fixtureSync } from '@open-wc/testing';
import { init } from './shared.js';
import type { FlowGrid, GridConnector } from './shared.js';

describe('grid connector unit tests', () => {
  let connector: GridConnector;

  beforeEach(() => {
    const grid = fixtureSync<FlowGrid>(`<vaadin-grid></vaadin-grid>`);
    init(grid);
    connector = grid.$connector;
  });

  describe('removeFromArray', () => {
    it('should clear the array', () => {
      const array = [1, 2, 3];
      connector.removeFromArray(array, () => true);
      expect(array).to.deep.equal([]);
    });

    it('should remove even numbers from the array', () => {
      const array = [1, 2, 3, 4, 5];
      connector.removeFromArray(array, (item) => item % 2 === 0);
      expect(array).to.deep.equal([1, 3, 5]);
    });

    it('should remove odd numbers from the array', () => {
      const array = [1, 2, 3, 4, 5];
      connector.removeFromArray(array, (item) => item % 2 !== 0);
      expect(array).to.deep.equal([2, 4]);
    });

    it('should remove the first item from the array', () => {
      const array = [1, 2, 3];
      connector.removeFromArray(array, (item) => item === 1);
      expect(array).to.deep.equal([2, 3]);
    });

    it('should remove the last item from the array', () => {
      const array = [1, 2, 3];
      connector.removeFromArray(array, (item) => item === 3);
      expect(array).to.deep.equal([1, 2]);
    });

    it('should not remove anything from the array', () => {
      const array = [1, 2, 3];
      connector.removeFromArray(array, (item) => item === 4);
      expect(array).to.deep.equal([1, 2, 3]);
    });
  });
});
