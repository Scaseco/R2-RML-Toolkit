package org.aksw.rml.jena.plugin;

import org.aksw.fnml.model.FunctionMap;
import org.aksw.fno.model.Function;
import org.aksw.fno.model.Param;
import org.aksw.fnox.model.JavaFunction;
import org.aksw.fnox.model.JavaMethodReference;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.rml.jena.service.InitRmlService;
import org.aksw.rml.model.LogicalSourceRml1;
import org.aksw.rml.model.PredicateObjectMapRml1;
import org.aksw.rml.model.TermMapRml1;
import org.aksw.rml.model.TriplesMapRml1;
import org.aksw.rml.rso.model.SourceOutput;
import org.aksw.rmlx.model.RmlAlias;
import org.aksw.rmlx.model.RmlDefinitionBlock;
import org.aksw.rmlx.model.RmlQualifiedBind;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginRml1
    implements JenaSubsystemLifecycle
{
    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

    public static void init() {

        // RML1 builds on top of the R2RML model
        JenaPluginRml2.init();

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
            TriplesMapRml1.class,
            LogicalSourceRml1.class,
            PredicateObjectMapRml1.class,
            TermMapRml1.class,
//            GraphMapRml1.class,
//            SubjectMapRml1.class,
//            PredicateMapRml1.class,
//            ObjectMapRml1.class,
            LogicalSourceRml1.class

            // BaseTableOrView.class,
            // R2rmlView.class,
//            SubjectMap.class,
//            PredicateMap.class,
//            ObjectMapType.class,
//            ObjectMap.class,
//            RefObjectMap.class,
//            JoinCondition.class
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
