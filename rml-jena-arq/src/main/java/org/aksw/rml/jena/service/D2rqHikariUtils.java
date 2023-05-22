package org.aksw.rml.jena.service;

import org.aksw.jenax.model.d2rq.domain.api.D2rqDatabase;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class D2rqHikariUtils {
    public static HikariDataSource configureDataSource(D2rqDatabase model) {
        HikariConfig conf = new HikariConfig();
        configure(conf, model);
        HikariDataSource result = new HikariDataSource(conf);
        return result;
    }

    /** Configure a hikari config from a d2rq model */
    public static void configure(HikariConfig target, D2rqDatabase source) {
        String value;
        if ((value = source.getJdbcDriver()) != null) {
            target.setDriverClassName(value);
        }

        if ((value = source.getJdbcDSN()) != null) {
            target.setJdbcUrl(value);
        }

        if ((value = source.getUsername()) != null) {
            target.setUsername(value);
        }

        if ((value = source.getPassword()) != null) {
            target.setPassword(value);
        }
    }
}
