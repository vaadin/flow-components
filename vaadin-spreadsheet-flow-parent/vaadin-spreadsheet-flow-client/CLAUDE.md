# Spreadsheet client (GWT)

The module README, imported below, explains how to build the client and how to debug it with SuperDevMode (SDM).

@README.md

## Notes for agents

- Check client changes in the browser with SDM, following the README, instead of rebuilding with Maven and restarting Jetty after every edit.
- Start the code server and Jetty as background tasks. Wait for `The code server is ready` and `Frontend compiled successfully` in their output.
- Recompile with the `curl` command, not the bookmark.
- Before reporting a client change as done, follow "Before committing" in the README: revert the patch, build with `clean`, and run the tests against that bundle.
