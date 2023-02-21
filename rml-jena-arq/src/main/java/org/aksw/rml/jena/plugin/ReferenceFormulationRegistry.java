package org.aksw.rml.jena.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.rml.jena.impl.ReferenceFormulation;
import org.aksw.rml.jena.impl.ReferenceFormulationCsv;
import org.aksw.rml.jena.impl.ReferenceFormulationJson;
import org.aksw.rml.model.QlTerms;
import org.apache.jena.query.ARQ;
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

    public static ReferenceFormulationRegistry get() {
        Context cxt = ARQ.getContext();
        ReferenceFormulationRegistry result = get(cxt);
        if (result == null) {
            result = new ReferenceFormulationRegistry();
            registryDefaults(result);
            set(cxt, result);
        }
        return result;
    }

    public static void registryDefaults(ReferenceFormulationRegistry registry) {
        registry.put(QlTerms.CSV, new ReferenceFormulationCsv());
        registry.put(QlTerms.JSONPath, new ReferenceFormulationJson());
        // registry.put(QlTerms.XPath, new ReferenceFormulatio());
    }

//    public static ReferenceFormulationRegistry getOrDefault(Context cxt) {
//    	ReferenceFormulationRegistry result =
//    }

    protected Map<String, ReferenceFormulation> registry = new LinkedHashMap<>();


    public void put(String key, ReferenceFormulation ref) {
        registry.put(key, ref);
    }

    public ReferenceFormulation get(String iri) {
        return registry.get(iri);
    }

    public ReferenceFormulation getOrThrow(String iri) {
        ReferenceFormulation result = get(iri);
        if (result == null) {
            throw new IllegalArgumentException("No reference formulation found for " + iri);
        }
        return result;
    }

    public Map<String, ReferenceFormulation> getMap() {
        return registry;
    }
}
