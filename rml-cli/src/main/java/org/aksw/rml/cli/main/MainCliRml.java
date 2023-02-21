package org.aksw.rml.cli.main;

import org.aksw.commons.picocli.CmdUtils;
import org.aksw.rml.cli.cmd.CmdRmlParent;

public class MainCliRml {
    public static void main(String[] args) {
        CmdUtils.callCmd(new CmdRmlParent(), args);
    }
}
