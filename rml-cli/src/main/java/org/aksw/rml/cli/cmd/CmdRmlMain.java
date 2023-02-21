package org.aksw.rml.cli.cmd;

import org.aksw.rml.cli.main.VersionProviderRml;

import picocli.CommandLine.Command;

@Command(name="rml", versionProvider = VersionProviderRml.class, description = "AKSW RML Toolkit", subcommands = {
        CmdRmlToSparql.class,
})
public class CmdRmlMain {
}
