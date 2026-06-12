import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { init } from './shared.js';
import type { FlowGrid } from './shared.js';

describe('grid connector - aria attributes', () => {
  describe('aria-multiselectable', () => {
    it('should apply aria-multiselectable on attach for a selection mode set while detached', async () => {
      const container = fixtureSync('<div></div>');
      const detachedGrid = document.createElement('vaadin-grid') as FlowGrid;
      init(detachedGrid);

      // Setting the selection mode before the grid is rendered should not throw
      detachedGrid.$connector.setSelectionMode('SINGLE');

      container.appendChild(detachedGrid);
      await nextFrame();

      const table = detachedGrid.shadowRoot!.querySelector('#table')!;
      expect(table.getAttribute('aria-multiselectable')).to.equal('false');
    });
  });
});
