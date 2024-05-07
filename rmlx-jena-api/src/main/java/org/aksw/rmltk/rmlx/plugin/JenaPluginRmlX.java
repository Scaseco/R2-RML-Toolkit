package org.aksw.rmltk.rmlx.plugin;

import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.rml.rso.model.SourceOutput;
import org.aksw.rmlx.model.RmlAlias;
import org.aksw.rmlx.model.RmlDefinitionBlock;
import org.aksw.rmlx.model.RmlQualifiedBind;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginRmlX
    implements JenaSubsystemLifecycle
{
    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        // Rml Extensions
        JenaPluginUtils.registerResourceClasses(
            RmlAlias.class,
            RmlQualifiedBind.class,
            RmlDefinitionBlock.class
        );

        // JenaX
        JenaPluginUtils.registerResourceClasses(
            SourceOutput.class
        );
    }
}
