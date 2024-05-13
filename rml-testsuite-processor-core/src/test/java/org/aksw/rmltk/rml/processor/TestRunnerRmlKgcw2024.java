package org.aksw.rmltk.rml.processor;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.aksw.commons.util.lifecycle.ResourceMgr;
import org.aksw.jenax.arq.util.quad.DatasetCmp.Report;
import org.apache.jena.datatypes.xsd.impl.XSDDouble;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@RunWith(Parameterized.class)
public class TestRunnerRmlKgcw2024 {
    private static final Logger logger = LoggerFactory.getLogger(TestRunnerRmlKgcw2024.class);

    protected static ResourceMgr resourceMgr = new ResourceMgr();

//    @BeforeClass
//    public static void beforeClass() {
//        resourceMgr = new ResourceMgr();
//    }

    @AfterClass
    public static void afterClass() {
        resourceMgr.close();
    }

    @Parameters(name = "RML TestCase {index}: {0}")
    public static Collection<Object[]> data()
            throws Exception
    {
        Path basePath = RmlTestCaseLister.toPath(resourceMgr, TestRunnerRmlKgcw2024.class.getResource("/kgcw/2024/track1").toURI());
        List<RmlTestCase> testCases = RmlTestCaseLister.list(basePath, resourceMgr);

        List<Object[]> params = testCases.stream()
                .map(testCase -> new Object[] {testCase.getName(), testCase})
                .toList();

        return params;
    }

    protected RmlTestCase testCase;

    public TestRunnerRmlKgcw2024(String name, RmlTestCase testCase) {
        this.testCase = testCase;
    }

    @Test
    public void run() {
        // System.out.println("GOT: " + XSDDouble.XSDdouble.unparse(Double.valueOf(123)));

        Report report;
        try {
            report = testCase.call();

            // Were we expected to fail?
            Assert.assertFalse(testCase.isExpectedFailure());

            boolean isIsomorphic = report.isIsomorphic();
            if (!isIsomorphic) {
                logger.error(report.toString());
            }
            Assert.assertTrue(isIsomorphic);

        } catch (Exception e) {
            if (testCase.isExpectedFailure()) {
                // Ignore
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}
