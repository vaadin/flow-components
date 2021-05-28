const { expect } = require('chai')
const { JSDOM } = require('jsdom');
const mock = require('mock-fs')

const jsdomExporter = require('../jsdom-exporter.js')

/**
 *
 * @param {string} svgString
 * @returns Document
 */
function parseSVG(svgString) {
  const dom = new JSDOM(`<body>${svgString}</body>`);
  return dom.window.document
}

describe('jsdom-exporter', () => {

  beforeEach(() => mock());

  afterEach(() => mock.restore());

  it('should render based on Highchart options', async () => {
    const result = await jsdomExporter({ options: { title: { text: 'TITLE' } } });
    const document = parseSVG(result.svgString);

    expect(document.querySelector('svg')).to.be.not.null;
    expect(document.querySelector('.highcharts-title').textContent).to.be.equal('TITLE');
  });

  it('should use default filename to write svg file', async () => {
    const result = await jsdomExporter({ options: { } });

    expect(result.outfile).to.contain('chart.svg');
  });

  it('should accept outfile name to write svg file', async () => {
    const result = await jsdomExporter({ options: { }, outfile: 'custom-file.svg' });

    expect(result.outfile).to.contain('custom-file.svg');
  });

  it('should accept width/height as exporting options', async () => {
    const configuration = { options: {}, exportOptions: { width: 100, height: 100 } };
    const result = await jsdomExporter(configuration);

    const document = parseSVG(result.svgString);
    const svgElement = document.querySelector('svg');

    expect(svgElement.getAttribute('width')).to.be.equal('100');
    expect(svgElement.getAttribute('height')).to.be.equal('100');
  });

  it('should accept theme as exporting options', async () => {
    const configuration = {
      options: {}, exportOptions: {
        theme: {
          chart: {
            backgroundColor: "red"
          }
        }
      }
    };
    const result = await jsdomExporter(configuration);
    const document = parseSVG(result.svgString);

    const backgroundColor = document.querySelector('.highcharts-background').getAttribute('fill');
    expect(backgroundColor).to.be.equal('red');
  });

  it('should accept lang as exporting options', async () => {
    const configuration = { options: {}, exportOptions: { lang: { noData: 'custom message' } } };
    const result = await jsdomExporter(configuration);
    const document = parseSVG(result.svgString);

    expect(document.querySelector('.highcharts-no-data').textContent).to.be.equal('custom message');
  });
});

describe('timeline', () => {
  beforeEach(() => mock());

  afterEach(() => mock.restore());

  it('should render stock chart if isTimeline is set to `true`', async () => {
    const result = await jsdomExporter({ options: { }, isTimeline: true });
    const document = parseSVG(result.svgString);

    expect(document.querySelector('.highcharts-navigator')).to.be.not.null;
  });
});