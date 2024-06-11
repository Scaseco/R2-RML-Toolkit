package org.aksw.rml.cli.cmd;

import picocli.CommandLine;

@CommandLine.Command(name = "xml", description = "XML Conversions", subcommands = {
        CmdRmlTkXmlToParent.class
})
public class CmdRmlTkXmlParent {
}
