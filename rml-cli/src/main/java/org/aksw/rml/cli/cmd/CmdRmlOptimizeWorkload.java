package org.aksw.rml.cli.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jena_sparql_api.rx.script.SparqlScriptProcessor;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.apache.hadoop.thirdparty.com.google.common.collect.Sets;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementLateral;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "workload", description = "Optimize a workload of sparql queries by merging queries having the same RML source")
public class CmdRmlOptimizeWorkload
    implements Callable<Integer>
{
    @Parameters(arity = "1..n", description = "Input RML file(s)")
    public List<String> inputFiles = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        SparqlScriptProcessor processor = SparqlScriptProcessor.createPlain(null, null);
        processor.process(inputFiles);
        List<SparqlStmt> stmts = new ArrayList<>(processor.getPlainSparqlStmts());

        ListMultimap<ElementService, Query> sourceToQueries = ArrayListMultimap.create();

        Iterator<SparqlStmt> it = stmts.iterator();
        while (it.hasNext()) {
            SparqlStmt stmt = it.next();
            // System.out.println(stmt);

            boolean grouped = false;
            if (stmt.isParsed() && stmt.isQuery()) {
                Query query = stmt.getQuery();

                // TODO Only allow grouping if there is a single source!
                ElementGroup grp = ObjectUtils.castAsOrNull(ElementGroup.class, query.getQueryPattern());
                if (grp != null && !grp.isEmpty()) {
                    ElementService svc = ObjectUtils.castAsOrNull(ElementService.class, grp.get(0));
                    if (svc != null) {
                        grp.getElements().remove(0); // Changes query!
                        sourceToQueries.put(svc, query);
                        it.remove();
                    }
                }
            }
        }

        for (Entry<ElementService, Collection<Query>> e : sourceToQueries.asMap().entrySet()) {
            ElementService sourceElt = e.getKey();

            QuadAcc newQuads = new QuadAcc();
            List<Element> lateralUnionMembers = new ArrayList<>();

            // The variable(s) mentioned in the source must not be renamed
            Set<Var> staticVars = SetUtils.asSet(PatternVars.vars(sourceElt));

            int memberId = 0;
            for (Query query : e.getValue()) {
                Element memberElt = query.getQueryPattern();
                Set<Var> mentionedVars = SetUtils.asSet(PatternVars.vars(memberElt));
                Set<Var> toRename = Sets.difference(mentionedVars, staticVars);
                int finalMemberId = memberId;
                Map<Var, Var> remap = toRename.stream()
                        .collect(Collectors.toMap(v -> v, v -> Var.alloc("m" + finalMemberId + "_" + v.getVarName())));
                Query mergeQuery = QueryUtils.applyNodeTransform(query, new NodeTransformSubst(remap));
                //Query mergeQuery = QueryTransformOps.transform(query, remap);

                mergeQuery.getConstructTemplate().getQuads().forEach(newQuads::addQuad);
                lateralUnionMembers.add(mergeQuery.getQueryPattern());
                ++memberId;
            }

            Element union = ElementUtils.unionIfNeeded(lateralUnionMembers);

            Query q = new Query();
            q.setQueryConstructType();
            q.setConstructTemplate(new Template(newQuads));
            ElementGroup elt = new ElementGroup();
            elt.addElement(sourceElt);
            elt.addElement(new ElementLateral(union));
            q.setQueryPattern(elt);
            stmts.add(new SparqlStmtQuery(q));
        }

        for (SparqlStmt stmt : stmts) {
            System.out.println(stmt);
        }


        return 0;
    }
}
