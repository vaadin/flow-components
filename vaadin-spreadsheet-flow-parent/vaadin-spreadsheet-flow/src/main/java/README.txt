
Apache POI classes are replicated here in order to work around missing
features that have not (yet) been submitted to the main POI project.
These classes are to be considered temporary and shall be removed once
the main POI project supports the features replicated here.

The cell formatting requires special considerations and the logic there
is interconnected, so for custom custom formatting we've modified the
CellFormatPart class and renamed it VCellFormatPart and stored it in an
overloaded org.apache.poi.ss.format namespace. In order to support that
change, all classes that depend on the CellFormatPart class also need
to be replicated, renamed and refactored to point to VCellFormatPart
instead of CellFormatPart.

This is hairy and undesirable, but necessary in order to make Excel-
compatible indexed colors and custom cell formatting work.
