package org.aksw.rml.cli.cmd;

import org.aksw.rml.cli.main.VersionProviderRml;

import picocli.CommandLine.Command;

@Command(name="rml", versionProvider = VersionProviderRml.class, description = "R(2R)ML Toolkit", subcommands = {
        CmdRmlToSparql.class,
        CmdRmlToTarql.class,
        CmdRmlOptimizeParent.class
})
public class CmdRmlParent {
}
