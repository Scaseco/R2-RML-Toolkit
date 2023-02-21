package org.aksw.rml.cli.cmd;

import picocli.CommandLine.Command;

@Command(name="optimize", description = "Optimization tasks", subcommands = {
        CmdRmlOptimizeWorkload.class
})
public class CmdRmlOptimizeParent {

}
