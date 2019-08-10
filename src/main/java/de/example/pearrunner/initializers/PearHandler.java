/**
 *
 */
package de.example.pearrunner.initializers;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.analysis_engine.metadata.AnalysisEngineMetaData;
import org.apache.uima.analysis_engine.metadata.FixedFlow;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.tools.PackageBrowser;
import org.apache.uima.pear.tools.PackageInstaller;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.Import;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author phil
 *
 */
@Component
@Slf4j
public class PearHandler {

    private File pearPath = null;
    private File installDir;

    private AnalysisEngine pearAsPipeline;
    private boolean initSuccessful;

    public void setPearPath(String pearFilePath, String installDir) {
	this.pearPath = new File(pearFilePath);
	this.installDir = new File(installDir);
	initSuccessful = false;

	setUpToRunNoCheckedExceptionShit();

    }

    public void processCas(JCas cas) {
	if (initSuccessful) {
	    processCasInPipeline(cas);
	} else {
	    throw new RuntimeException("Cas is not initialized yet");
	}
    }

    private void processCasInPipeline(JCas cas) {
	try {
	    pearAsPipeline.process(cas);
	} catch (AnalysisEngineProcessException e) {
	    log.error("Could not process cas", e);
	    throw new RuntimeException(e);
	}
    }

    private void setUpToRunNoCheckedExceptionShit() {
	try {
	    setUpToRun();
	} catch (Exception e) {
	    log.error("Can not start the pear", e);
	    throw new RuntimeException(e);
	}
    }

    private void setUpToRun() throws Exception {

	Import installedPear = installPear();
	AnalysisEngineDescription desc = buildDescriptor(installedPear);
	AnalysisEngine pipeline = runDescriptor(desc);

	pearAsPipeline = pipeline;
	initSuccessful = true;
    }

    private Import installPear() throws IOException {

	PackageBrowser installPackage = PackageInstaller.installPackage(installDir, pearPath, false);

	Import impPear = UIMAFramework.getResourceSpecifierFactory().createImport();
	File import1 = new File(installPackage.getComponentPearDescPath());
	impPear.setLocation(import1.toURI().getPath());

	System.out.println(import1);

	return impPear;

    }

    private AnalysisEngineDescription buildDescriptor(Import pearImport) {

	AnalysisEngineDescription desc = UIMAFramework.getResourceSpecifierFactory().createAnalysisEngineDescription();
	desc.setPrimitive(false);

	String pearNameInFlow = "pear";
	desc.getDelegateAnalysisEngineSpecifiersWithImports().put(pearNameInFlow, pearImport);

	FixedFlow fixedFlow = UIMAFramework.getResourceSpecifierFactory().createFixedFlow();
	fixedFlow.setFixedFlow(new String[] { pearNameInFlow });

	// add analysis engine meta data
	AnalysisEngineMetaData md = desc.getAnalysisEngineMetaData();
	md.setName("PEAR Runner");
	md.setDescription("please just run this time");
	md.setVersion("0.0.1");
	md.setFlowConstraints(fixedFlow);

	return desc;
    }

    private AnalysisEngine runDescriptor(AnalysisEngineDescription desc) throws ResourceInitializationException {

	return UIMAFramework.produceAnalysisEngine(desc, null, null);
    }

}
