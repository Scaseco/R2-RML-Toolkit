package org.aksw.rml.jena.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.aksw.commons.util.lifecycle.ResourceMgr;
import org.aksw.jenax.model.d2rq.domain.api.D2rqDatabase;
import org.aksw.jenax.reprogen.core.MapperProxyUtils;
import org.apache.jena.rdf.model.Resource;

public class DataSourceFactoryImpl
    implements DataSourceFactoryFromD2rqDatabase
{
    private Map<Resource, DataSource> resourceToDatasource = new ConcurrentHashMap<>();
    private ResourceMgr resourceMgr;

    @Override
    public DataSource getOrCreate(D2rqDatabase settings) {
        // XXX Using the hash id is not ideal - we'd need a proper "equals" implementation based on HashId
        String hashId = MapperProxyUtils.getHashId(settings).getHashAsString(settings);

        DataSource result = null;

        return result;
    }

}
