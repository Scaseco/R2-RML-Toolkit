package org.aksw.rml.jena.plugin;

import org.aksw.fnml.model.FunctionMap;
import org.aksw.fno.model.Function;
import org.aksw.fno.model.Param;
import org.aksw.fnox.model.JavaFunction;
import org.aksw.fnox.model.JavaMethodReference;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.rml.model.HasLogicalSource;
import org.aksw.rml.model.LogicalSource;
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
        // Function Ontology (fno)
        JenaPluginUtils.registerResourceClasses(
            Function.class,
            Param.class
        );

        // Java Method References (presently this ontology doesn't seem to have a specific name)
        JenaPluginUtils.registerResourceClasses(
            JavaFunction.class,
            JavaMethodReference.class
        );

        // Function Mapping Language (fnml)
        JenaPluginUtils.registerResourceClasses(
            FunctionMap.class
        );

        // Rml
        JenaPluginUtils.registerResourceClasses(
            HasLogicalSource.class,
            LogicalSource.class
        );
    }
}
