package org.aksw.rml.jena.impl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.aksw.commons.util.lifecycle.ResourceMgr;
import org.aksw.jenax.arq.util.exec.query.JenaXSymbols;
import org.aksw.jenax.arq.util.io.StreamRDFEmitter;
import org.aksw.jenax.arq.util.security.ArqSecurity;
import org.aksw.jenax.model.d2rq.domain.api.D2rqDatabase;
import org.aksw.rml.jena.service.RmlSymbols;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionDatasetBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmlExec
    implements StreamRDFEmitter
{
    private static final Logger logger = LoggerFactory.getLogger(RmlExec.class);

    protected List<Entry<Query, String>> labeledQueries;
    protected Path rmlMappingDirectory;
    protected Consumer<D2rqDatabase> d2rqResolver;

    protected RmlExec(List<Entry<Query, String>> labeledQueries, Path rmlMappingDirectory, Consumer<D2rqDatabase> d2rqResolver) {
        super();
        this.labeledQueries = labeledQueries;
        this.rmlMappingDirectory = rmlMappingDirectory;
        this.d2rqResolver = d2rqResolver;
    }

    @Override
    public void emit(StreamRDF streamRDF) {
        Model emptyModel = ModelFactory.createDefaultModel();

        for (Entry<Query, String> e : labeledQueries) {
            Query query = e.getKey();
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Executing SPARQL Query [%s]: %s", e.getValue(), query));
            }
            try (ResourceMgr qExecResMgr = new ResourceMgr();
                 QueryExecution qe = QueryExecutionDatasetBuilder.create()
                     .model(emptyModel)
                     .query(query)
                     .set(ArqSecurity.symAllowFileAccess, true)
                     .set(RmlSymbols.symMappingDirectory, rmlMappingDirectory)
                     .set(RmlSymbols.symD2rqDatabaseResolver, d2rqResolver)
                     .set(JenaXSymbols.symResourceMgr, qExecResMgr)
                     .build()) {

                if (logger.isDebugEnabled()) {
                    logger.debug("Begin of RDF data Contribution:");
                }

                Iterator<Quad> it = qe.execConstructQuads();
                while (it.hasNext()) {
                    Quad quad = it.next();
                    streamRDF.quad(quad);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("End of RDF data contribution");
                }
            }
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        protected List<Entry<Query, String>> labeledQueries = new ArrayList<>();
        protected Path rmlMappingDirectory;
        protected Consumer<D2rqDatabase> d2rqResolver;

        public List<Entry<Query, String>> getLabeledQueries() {
            return labeledQueries;
        }

        public Builder setLabeledQueries(List<Entry<Query, String>> labeledQueries) {
            this.labeledQueries.clear();
            addLabeledQueries(labeledQueries);
            return this;
        }

        public Builder addLabeledQueries(List<Entry<Query, String>> labeledQueries) {
            this.labeledQueries.addAll(labeledQueries);
            return this;
        }

        public Builder addQuery(Query query) {
            this.labeledQueries.add(Map.entry(query, "Query #"+ labeledQueries.size()));
            return this;
        }

        public Builder addQueries(Collection<Query> queries) {
            for(Query query : queries) {
                addQuery(query);
            }
            return this;
        }

        public Path getRmlMappingDirectory() {
            return rmlMappingDirectory;
        }

        public Builder setRmlMappingDirectory(Path rmlMappingDirectory) {
            this.rmlMappingDirectory = rmlMappingDirectory;
            return this;
        }

        public Consumer<D2rqDatabase> getD2rqResolver() {
            return d2rqResolver;
        }

        public Builder setD2rqResolver(Consumer<D2rqDatabase> d2rqResolver) {
            this.d2rqResolver = d2rqResolver;
            return this;
        }

        public RmlExec build() {
            Path finalRmlMappingDirectory = rmlMappingDirectory != null
                    ? rmlMappingDirectory
                    : Path.of("");

            return new RmlExec(new ArrayList<>(labeledQueries), finalRmlMappingDirectory, d2rqResolver);
        }
    }
}
