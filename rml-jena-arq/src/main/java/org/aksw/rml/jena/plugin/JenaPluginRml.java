package org.aksw.rml.jena.plugin;

import org.aksw.fno.model.Function;
import org.aksw.fno.model.Param;
import org.aksw.fnox.model.JavaFunction;
import org.aksw.fnox.model.JavaMethodReference;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginRml
    implements JenaSubsystemLifecycle
{
    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        JenaPluginUtils.registerResourceClasses(
            Function.class,
            JavaMethodReference.class,
            Param.class
        );

        JenaPluginUtils.registerResourceClasses(
            JavaFunction.class,
            JavaMethodReference.class
        );
    }
}
