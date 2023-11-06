package org.aksw.rml.cli.cmd;

import picocli.CommandLine.Command;

@Command(name="rmltk", versionProvider = VersionProviderRmlTk.class, description = "R(2R)ML Toolkit", subcommands = {
        CmdRmlTkRmlParent.class,
        CmdRmlTkRmlOptimizeParent.class,
        CmdRmlTkRmlXmlToJson.class
})
public class CmdRmlTkParent {
}
