/*
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uhh.lt.webanno.mm.io;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.admin.CASAdminException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.uhh.lt.webanno.mm.io.HiatTeiReaderExt;
import de.uhh.lt.webanno.mm.io.HiatTeiReaderReorderSegments;
import de.uhh.lt.webanno.mm.io.TestUtils.TeiExpectation;

public class HiatTeiReaderExtTest{
    
    @BeforeClass
    public static void setup(){
        try {
            TestUtils.setupTest();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
		
    @Test
    public void testReading() throws Exception {
    	testReading(TestUtils._tei_expectations);
    } 
    
    @Test
    public void testExpectations() throws ResourceInitializationException, CollectionException, CASAdminException, IOException, ClassNotFoundException, NoSuchElementException, IllegalArgumentException, CASException{
        for(TeiExpectation expect : TestUtils._tei_expectations){
            if(expect == null)
                continue;
            JCas cas = TestUtils.getCas(HiatTeiReaderExt.class, expect.filename);
            expect.testCas(cas);
        }
    }
    
    public static void testReading(List<TeiExpectation> tei_expectations) throws Exception {
        tei_expectations.stream().filter(x -> x != null).map(x -> x.filename).forEach(fname -> {
            URL fullname = ClassLoader.getSystemClassLoader().getResource(fname);
            if(fullname == null)
                try{ fullname = new URL(fname); }catch(Exception e){throw new RuntimeException(e);};
            File f = new File(fullname.toString());
            String dname = f.getParent();
            try {
                testReading(f.getName(), dname);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    } 
    
    public static void testExpectations(List<TeiExpectation> tei_expectations) throws Exception{
        for(TeiExpectation expect : tei_expectations){
            if(expect == null)
                continue;
            JCas cas = TestUtils.getCas(HiatTeiReaderExt.class, expect.filename);
            expect.testCas(cas);
        }
    }
    
    public static void testReading(String fname, String dname) throws Exception{
        testReading(HiatTeiReaderExt.class, fname, dname);
        testReading(HiatTeiReaderReorderSegments.class, fname, dname);
    }
    
    public static void testReading(Class<? extends JCasResourceCollectionReader_ImplBase> reader_clazz, String fname, String dname) throws Exception{
        System.err.format("testing '%s'.%n", fname);
                
        File dir = new File(TestUtils._temp_folder, HiatTeiReaderExtTest.class.getSimpleName());
        dir.mkdirs();
        String dump_out = new File(dir, fname + "-" + reader_clazz.getSimpleName() + ".txt").getAbsolutePath();
        
        CollectionReaderDescription reader = createReaderDescription(
                reader_clazz, 
                JCasResourceCollectionReader_ImplBase.PARAM_SOURCE_LOCATION, dname,
                JCasResourceCollectionReader_ImplBase.PARAM_PATTERNS, fname);
        
        AnalysisEngineDescription dumper = createEngineDescription(
                CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE,  dump_out); //dump_out);  //
        
        AnalysisEngineDescription printer = createEngineDescription(TestUtils.SegPrint.class);

        runPipeline(reader, printer, dumper);
        
        System.err.format("Dumped CAS to '%s'.%n", dump_out);
    }
    
    
    @Before
    public void setupLogging()
    {
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");
    }
}
