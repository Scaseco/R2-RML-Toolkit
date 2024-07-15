package org.aksw.rml.cli.main;

import org.aksw.commons.picocli.CmdUtils;
import org.aksw.commons.util.derby.DerbyUtils;
import org.aksw.rml.cli.cmd.CmdRmlTkParent;

public class MainCliRml {
    static { DerbyUtils.disableDerbyLog(); } // Seems to come from Jena GeoSPARQL

    public static void main(String[] args) {
        CmdUtils.callCmdObject(new CmdRmlTkParent(), args);
    }
}
