package org.aksw.rml.cli.cmd;

import picocli.CommandLine.Command;

@Command(name="rml", versionProvider = VersionProviderRml.class, description = "R(2R)ML Toolkit", subcommands = {
        CmdRmlToSparql.class,
        CmdRmlToTarql.class,
        CmdRmlOptimizeParent.class
})
public class CmdRmlParent {
}
