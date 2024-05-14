package org.aksw.rml.jena.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.rml.jena.impl.ReferenceFormulation;
import org.aksw.rml.jena.ref.impl.ReferenceFormulationCsvViaService;
import org.aksw.rml.jena.ref.impl.ReferenceFormulationJsonViaService;
import org.aksw.rml.jena.ref.impl.ReferenceFormulationRdbViaService;
import org.aksw.rml.jena.ref.impl.ReferenceFormulationXmlViaService;
import org.aksw.rml.model.QlTerms;
import org.aksw.rml.v2.common.vocab.RmlIoTerms;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class ReferenceFormulationRegistry
    implements ReferenceFormulationService
{
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
        // Json works also for CSV because CSV rows are represented as JSON rows
        registry.put(QlTerms.CSV, new ReferenceFormulationCsvViaService());
        registry.put(QlTerms.JSONPath, new ReferenceFormulationJsonViaService());
        registry.put(QlTerms.XPath, new ReferenceFormulationXmlViaService());
        registry.put(QlTerms.RDB, ReferenceFormulationRdbViaService.getInstance());

        registry.put(RmlIoTerms.SQL2008Query, ReferenceFormulationRdbViaService.getInstance());
        registry.put(RmlIoTerms.SQL2008Table, ReferenceFormulationRdbViaService.getInstance());
        registry.put(RmlIoTerms.JSONPath, new ReferenceFormulationJsonViaService());
        registry.put(RmlIoTerms.XPath, new ReferenceFormulationXmlViaService());
        registry.put(RmlIoTerms.CSV, new ReferenceFormulationCsvViaService());
    }

//    public static ReferenceFormulationRegistry getOrDefault(Context cxt) {
//    	ReferenceFormulationRegistry result =
//    }

    protected Map<String, ReferenceFormulation> registry = new LinkedHashMap<>();


    public void put(String key, ReferenceFormulation ref) {
        registry.put(key, ref);
    }

    @Override
    public ReferenceFormulation get(String iri) {
        return registry.get(iri);
    }

    public Map<String, ReferenceFormulation> getMap() {
        return registry;
    }
}
