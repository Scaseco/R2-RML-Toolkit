package org.aksw.rml.cli.cmd;

import picocli.CommandLine.Command;

@Command(name="rml", versionProvider = VersionProviderRmlTk.class, description = "RML Conversions", subcommands = {
		CmdRmlTkRmlToParent.class
})
public class CmdRmlTkRmlParent {
}
