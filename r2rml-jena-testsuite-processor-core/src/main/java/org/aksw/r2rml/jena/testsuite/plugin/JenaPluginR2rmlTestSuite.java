package org.aksw.r2rml.jena.testsuite.plugin;

import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.r2rml.jena.testsuite.domain.Database;
import org.aksw.r2rml.jena.testsuite.domain.R2rmlTestCase;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginR2rmlTestSuite
    implements JenaSubsystemLifecycle
{
    @Override public void start() { init(); }
    @Override public void stop() { }

    public static void init() {
        JenaPluginUtils.registerResourceClasses(
            R2rmlTestCase.class,
            Database.class
        );
    }
}
