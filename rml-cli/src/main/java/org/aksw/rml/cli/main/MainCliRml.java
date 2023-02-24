package org.aksw.rml.cli.main;

import org.aksw.commons.picocli.CmdUtils;
import org.aksw.commons.util.derby.DerbyUtils;
import org.aksw.rml.cli.cmd.CmdRmlParent;

public class MainCliRml {
    static { DerbyUtils.disableDerbyLog(); } // Seems to come from Jena GeoSPARQL

    public static void main(String[] args) {
        CmdUtils.callCmd(new CmdRmlParent(), args);
    }
}
