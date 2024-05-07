package org.aksw.rml.jena.service;

import org.aksw.rml.v2.common.vocab.Rml2Terms;
import org.aksw.rml.v2.common.vocab.RmlIoTerms;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.Symbol;

public class RmlSymbols {
    /**
     * Symbol to provide eventual resolution of the constant RDF term {@code rml:MappingDirectory} to
     * a {@link java.nio.Path} object.
     *
     * The following values for the symbol are valid and should be supported by implementations:
     * <ul>
     *   <li>A {@link java.nio.Path} object.</li>
     *   <li>A {@link java.lang.String} object. Will be passed to {@link java.nio.Path#of(String, String...)}.</li>
     * </ul>
     */
    public static final Symbol symMappingDirectory = Symbol.create(RmlIoTerms.MappingDirectory);

    /**
     * Symbol to provide a {@link RmlSourceResolver}. The source resolver is a callback that
     * can modify the state of a {@link Resource} that describes the source.
     * For example, can be used to alter the values for port or host name of a
     * {@link org.aksw.jenax.model.d2rq.domain.api.D2rqDatabase}.
     */
    public static final Symbol symD2rqDatabaseResolver = Symbol.create(Rml2Terms.uri + "d2rqDatabaseResolver");
}
