package org.aksw.rml.cli.cmd;

import picocli.CommandLine.Command;

@Command(name="optimize", description = "Optimization tasks", subcommands = {
        CmdRmlTkRmlOptimizeWorkload.class
})
public class CmdRmlTkRmlOptimizeParent {

}
