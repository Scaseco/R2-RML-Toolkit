package org.aksw.rml.cli.cmd;

import picocli.CommandLine;

@CommandLine.Command(name = "to", description = "XML Conversion Targets", subcommands = {
        CmdRmlTkRmlXmlToJson.class
})
public class CmdRmlTkXmlToParent {
}
