package org.aksw.rml.cli.cmd;

import picocli.CommandLine.Command;

@Command(name="to", versionProvider = VersionProviderRmlTk.class, description = "RML Conversion Targets", subcommands = {
        CmdRmlTkRmlToSparql.class,
        CmdRmlTkRmlToTarql.class
})
public class CmdRmlTkRmlToParent {

}
