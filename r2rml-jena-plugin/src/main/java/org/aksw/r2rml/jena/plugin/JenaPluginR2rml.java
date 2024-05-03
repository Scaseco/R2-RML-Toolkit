package org.aksw.r2rml.jena.plugin;

import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.r2rmlx.domain.api.Constraint;
import org.aksw.r2rmlx.domain.api.PrefixConstraint;
import org.aksw.r2rmlx.domain.api.RangeConstraint;
import org.aksw.r2rmlx.domain.api.TermMapX;
import org.aksw.rmltk.model.r2rml.BaseTableOrView;
import org.aksw.rmltk.model.r2rml.GraphMap;
import org.aksw.rmltk.model.r2rml.JoinCondition;
import org.aksw.rmltk.model.r2rml.LogicalTable;
import org.aksw.rmltk.model.r2rml.ObjectMap;
import org.aksw.rmltk.model.r2rml.ObjectMapType;
import org.aksw.rmltk.model.r2rml.PredicateMap;
import org.aksw.rmltk.model.r2rml.PredicateObjectMap;
import org.aksw.rmltk.model.r2rml.R2rmlView;
import org.aksw.rmltk.model.r2rml.RefObjectMap;
import org.aksw.rmltk.model.r2rml.SubjectMap;
import org.aksw.rmltk.model.r2rml.TermMap;
import org.aksw.rmltk.model.r2rml.TriplesMap;
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
