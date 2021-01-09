package org.aksw.r2rml.jena.plugin;

import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.r2rml.jena.domain.api.GraphMap;
import org.aksw.r2rml.jena.domain.api.LogicalTable;
import org.aksw.r2rml.jena.domain.api.ObjectMap;
import org.aksw.r2rml.jena.domain.api.PredicateMap;
import org.aksw.r2rml.jena.domain.api.PredicateObjectMap;
import org.aksw.r2rml.jena.domain.api.SubjectMap;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
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
			PredicateObjectMap.class,
			GraphMap.class,
			SubjectMap.class,
			PredicateMap.class,
			ObjectMap.class,
			TermMap.class
		);
	}	
}
