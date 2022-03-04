package org.aksw.r2rml.jena.plugin;

import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.r2rml.jena.domain.api.BaseTableOrView;
import org.aksw.r2rml.jena.domain.api.GraphMap;
import org.aksw.r2rml.jena.domain.api.JoinCondition;
import org.aksw.r2rml.jena.domain.api.LogicalTable;
import org.aksw.r2rml.jena.domain.api.ObjectMap;
import org.aksw.r2rml.jena.domain.api.ObjectMapType;
import org.aksw.r2rml.jena.domain.api.PredicateMap;
import org.aksw.r2rml.jena.domain.api.PredicateObjectMap;
import org.aksw.r2rml.jena.domain.api.R2rmlView;
import org.aksw.r2rml.jena.domain.api.RefObjectMap;
import org.aksw.r2rml.jena.domain.api.SubjectMap;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rmlx.domain.api.Constraint;
import org.aksw.r2rmlx.domain.api.PrefixConstraint;
import org.aksw.r2rmlx.domain.api.RangeConstraint;
import org.aksw.r2rmlx.domain.api.TermMapX;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginR2rml
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
            TriplesMap.class,
            LogicalTable.class,
            BaseTableOrView.class,
            R2rmlView.class,
            PredicateObjectMap.class,
            TermMap.class,
            GraphMap.class,
            SubjectMap.class,
            PredicateMap.class,
            ObjectMapType.class,
            ObjectMap.class,
            RefObjectMap.class,
            JoinCondition.class
        );

        JenaPluginUtils.registerResourceClasses(
            TermMapX.class,
            Constraint.class,
            PrefixConstraint.class,
            RangeConstraint.class
        );
    }
}
