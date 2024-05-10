package org.aksw.rml.jena.service;

import javax.sql.DataSource;

import org.aksw.jenax.model.d2rq.domain.api.D2rqDatabase;

public interface DataSourceFactoryFromD2rqDatabase {
    public DataSource getOrCreate(D2rqDatabase settings);
}
