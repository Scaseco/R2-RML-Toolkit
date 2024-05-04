# Track 1: Conformance

Track 1 focusses on conformance of engines with the [new RML modules](http://w3id.org/rml/portal).
The conformance is measured through test-cases which each of the modules
provide.

In the first phase, the test-cases are still considered 'Beta' which means
that issues still may arise in terms of mappings, output, etc. If you find
them, please report them as an issue to the corresponding RML module on GitHub.
You can find them all on [https://github.com/kg-construct/](https://github.com/kg-construct).

## How to participate?

1. Execute your engine against each of test-cases from each module.
2. Check if the output of your engine matches the provided output in the test-case
3. List all successfull and failed test-cases in a spreadsheet. 
We provide already a [template for this spreadsheet](https://docs.google.com/spreadsheets/d/1hBkGj2NZgHyfNqVTm79ioirqGdsk0zusU14Mt8WsRm4),
please make a copy and fill it in for your engine.

[Our tool](https://github.com/kg-construct/challenge-tool) for executing Track 2: Performance has been extended to perform
steps 1 and 2 for you. However, you may need to adjust the
name of your tool inside the `metadata.json` file in each dataset.
This is currently `RMLMapper`, but has to be replaced by the name of your
engine. This name is the same name as the Python class you created for your
engine in the tool.

Tip: if you want to execute the test-cases faster and only once, you can use
the following arguments for the tool: `--wait-time=0 --runs=1`

## Questions?

If you encounter problems or having questions, feel free to contact us
on Slack #kgc_challenge or send us an e-mail at dylan.vanassche@ugent.be
