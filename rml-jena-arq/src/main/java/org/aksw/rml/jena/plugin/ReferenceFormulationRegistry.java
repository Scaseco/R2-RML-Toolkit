package org.aksw.rml.jena.plugin;

import java.util.Map;

import org.aksw.rml.jena.impl.ReferenceFormulation;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class ReferenceFormulationRegistry {
    public static final Symbol symRegistry = SystemARQ.allocSymbol("referenceFormulationRegistry");

    public static void set(Context cxt, ReferenceFormulationRegistry registry) {
        cxt.set(symRegistry, registry);
    }

    public static ReferenceFormulationRegistry get(Context cxt) {
        return cxt.get(symRegistry);
        // FunctionRegistry.get()
    }

//    public static ReferenceFormulationRegistry getOrDefault(Context cxt) {
//    	ReferenceFormulationRegistry result =
//    }

    protected Map<String, ReferenceFormulation> registry;


    public void put(String key, ReferenceFormulation ref) {
        registry.put(key, ref);
    }

    public Map<String, ReferenceFormulation> getMap() {
        return registry;
    }
}
