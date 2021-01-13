package org.aksw.r2rml.jena.plugin;

import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.r2rml.jena.domain.api.BaseTableOrView;
import org.aksw.r2rml.jena.domain.api.GraphMap;
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

public class JenaPluginR2rml implements JenaSubsystemLifecycle {
	//private static final Logger logger = LoggerFactory.getLogger(InitJenaPluginR2rml.class);
	
	public void start() {
		init();
	}

	@Override
	public void stop() {
	}


	public static void init() {
	    
//			JenaPluginR2rml.init(BuiltinPersonalities.model);		

		JenaPluginUtils.registerResourceClasses(
			TriplesMap.class,
			LogicalTable.class,
			BaseTableOrView.class,
			R2rmlView.class,
			PredicateObjectMap.class,
			GraphMap.class,
			SubjectMap.class,
			PredicateMap.class,
			ObjectMapType.class,
			ObjectMap.class,
			RefObjectMap.class,
			TermMap.class
		);
		
		JenaPluginUtils.registerResourceClasses(
			TermMapX.class,
			Constraint.class,
			PrefixConstraint.class,
			RangeConstraint.class
		);
	}	
}
