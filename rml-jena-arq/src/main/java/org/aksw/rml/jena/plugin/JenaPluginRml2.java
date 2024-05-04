package org.aksw.rml.jena.plugin;

import org.aksw.fnml.model.FunctionMap;
import org.aksw.fno.model.Function;
import org.aksw.fno.model.Param;
import org.aksw.fnox.model.JavaFunction;
import org.aksw.fnox.model.JavaMethodReference;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.rml.jena.service.InitRmlService;
import org.aksw.rml.rso.model.SourceOutput;
import org.aksw.rml.v2.jena.domain.api.GraphMapRml2;
import org.aksw.rml.v2.jena.domain.api.LogicalSourceRml2;
import org.aksw.rml.v2.jena.domain.api.ObjectMapRml2;
import org.aksw.rml.v2.jena.domain.api.PredicateMapRml2;
import org.aksw.rml.v2.jena.domain.api.RefObjectMapRml2;
import org.aksw.rml.v2.jena.domain.api.SubjectMapRml2;
import org.aksw.rml.v2.jena.domain.api.TermMapRml2;
import org.aksw.rml.v2.jena.domain.api.TriplesMapRml2;
import org.aksw.rmlx.model.RmlAlias;
import org.aksw.rmlx.model.RmlDefinitionBlock;
import org.aksw.rmlx.model.RmlQualifiedBind;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginRml2
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

        // Standard Rml
        JenaPluginUtils.registerResourceClasses(
            TriplesMapRml2.class,
            LogicalSourceRml2.class,
            TermMapRml2.class,
            GraphMapRml2.class,
            SubjectMapRml2.class,
            PredicateMapRml2.class,
            ObjectMapRml2.class,
            RefObjectMapRml2.class
        );

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

        InitRmlService.registerServiceRmlSource(ServiceExecutorRegistry.get());
    }
}
