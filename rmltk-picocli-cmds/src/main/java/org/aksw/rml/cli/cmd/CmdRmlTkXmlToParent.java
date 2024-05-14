package org.aksw.rml.cli.cmd;

import picocli.CommandLine;

@CommandLine.Command(name = "xml", subcommands = {
        CmdRmlTkRmlXmlToJson.class
})
public class CmdRmlTkXmlToParent {
}
