package org.aksw.rml.cli.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.aksw.commons.picocli.VersionProviderFromClasspathProperties;

public class VersionProviderRml extends VersionProviderFromClasspathProperties
{
    @Override public String getResourceName() { return "rml-jena-arq.properties"; }
    @Override public Collection<String> getStrings(Properties p) { return Arrays.asList(
            p.get("rml-jena-arq.version") + " built at " + p.get("rml-jena-arq.build.timestamp")
    ); }
}
