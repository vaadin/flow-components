import { expect } from 'chai';
import { fixtureSync, nextFrame } from '@vaadin/testing-helpers';
import { init, setRootItems, getHeaderCellContent, getFooterCellContent } from './shared.js';
import type { FlowGrid } from './shared.js';
import type { GridColumn } from '@vaadin/grid/vaadin-grid-column.js';

describe('grid connector - header and footer renderers', () => {
  let grid: FlowGrid;
  let column: GridColumn;

  beforeEach(async () => {
    grid = fixtureSync(`
      <vaadin-grid>
        <vaadin-grid-column path="name"></vaadin-grid-column>
      </vaadin-grid>
    `);

    init(grid);

    column = grid.querySelector('vaadin-grid-column')!;

    setRootItems(grid.$connector, [{ key: '0', name: 'foo' }]);
    await nextFrame();
  });

  describe('header', () => {
    it('should render text content', async () => {
      grid.$connector.setHeaderRenderer(column, { content: 'Header text' });
      await nextFrame();
      expect(getHeaderCellContent(column).textContent).to.equal('Header text');
    });

    it('should render node content', async () => {
      const span = document.createElement('span');
      span.textContent = 'Header node';
      grid.$connector.setHeaderRenderer(column, { content: span });
      await nextFrame();
      expect(getHeaderCellContent(column).contains(span)).to.be.true;
    });

    it('should clear previous content when re-rendering', async () => {
      const first = document.createElement('span');
      first.textContent = 'first';
      grid.$connector.setHeaderRenderer(column, { content: first });
      await nextFrame();

      const second = document.createElement('span');
      second.textContent = 'second';
      grid.$connector.setHeaderRenderer(column, { content: second });
      await nextFrame();

      expect(getHeaderCellContent(column).textContent).to.equal('second');
    });

    it('should restore the default header when content is null', async () => {
      grid.$connector.setHeaderRenderer(column, { content: 'Custom' });
      await nextFrame();

      grid.$connector.setHeaderRenderer(column, { content: null });
      await nextFrame();

      // With no header renderer, the column falls back to the default
      // path-based header
      expect(getHeaderCellContent(column).textContent).to.equal('Name');
      expect(column.headerRenderer).to.be.null;
    });

    describe('sorter', () => {
      it('should render sorter', async () => {
        grid.$connector.setHeaderRenderer(column, { content: 'Name', showSorter: true, sorterPath: 'name' });
        await nextFrame();

        const sorter = getHeaderCellContent(column).querySelector('vaadin-grid-sorter')!;
        expect(sorter).to.exist;
        expect(sorter.path).to.equal('name');
      });

      it('should render text content inside sorter', async () => {
        grid.$connector.setHeaderRenderer(column, { content: 'Name', showSorter: true, sorterPath: 'name' });
        await nextFrame();

        const sorter = getHeaderCellContent(column).querySelector('vaadin-grid-sorter')!;
        expect(sorter.textContent).to.equal('Name');
      });

      it('should render node content inside sorter', async () => {
        const span = document.createElement('span');
        span.textContent = 'Name';
        grid.$connector.setHeaderRenderer(column, { content: span, showSorter: true, sorterPath: 'name' });
        await nextFrame();

        const sorter = getHeaderCellContent(column).querySelector('vaadin-grid-sorter')!;
        expect(sorter.contains(span)).to.be.true;
      });

      it('should reuse sorter element when renderer runs again', async () => {
        grid.$connector.setHeaderRenderer(column, { content: 'Name', showSorter: true, sorterPath: 'name' });
        await nextFrame();

        const oldSorter = getHeaderCellContent(column).querySelector('vaadin-grid-sorter')!;
        grid.requestContentUpdate();
        const newSorter = getHeaderCellContent(column).querySelector('vaadin-grid-sorter')!;

        expect(newSorter).to.equal(oldSorter);
      });
    });
  });

  describe('footer', () => {
    it('should render text content', async () => {
      grid.$connector.setFooterRenderer(column, { content: 'Footer text' });
      await nextFrame();
      expect(getFooterCellContent(column).textContent).to.equal('Footer text');
    });

    it('should render node content', async () => {
      const span = document.createElement('span');
      span.textContent = 'Footer node';
      grid.$connector.setFooterRenderer(column, { content: span });
      await nextFrame();
      expect(getFooterCellContent(column).contains(span)).to.be.true;
    });

    it('should clear previous content when re-rendering', async () => {
      const first = document.createElement('span');
      first.textContent = 'first';
      grid.$connector.setFooterRenderer(column, { content: first });
      await nextFrame();

      const second = document.createElement('span');
      second.textContent = 'second';
      grid.$connector.setFooterRenderer(column, { content: second });
      await nextFrame();

      expect(getFooterCellContent(column).textContent).to.equal('second');
    });

    it('should remove the footer renderer when content is null', async () => {
      grid.$connector.setFooterRenderer(column, { content: 'Custom' });
      await nextFrame();

      grid.$connector.setFooterRenderer(column, { content: null });
      await nextFrame();

      expect(getFooterCellContent(column).textContent).to.equal('');
      expect(column.footerRenderer).to.be.null;
    });
  });
});
