/**
 *
 */
package de.example.pearrunner.initializers;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.pear.tools.PackageBrowser;
import org.apache.uima.pear.tools.PackageInstaller;
import org.apache.uima.resource.PearSpecifier;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.XMLInputSource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author phil
 *
 */
@Component
@Slf4j
public class PearHandler {

	public static final String POOLISIOUS = "poolisious";
	private File pearPath = null;
	private File installDir;

	private AnalysisEngine pearAsPipeline;
	private boolean initSuccessful;


	public void setPearPath(String pearFilePath, String installDir) {

		this.pearPath = new File(pearFilePath);
		this.installDir = new File(installDir);
		this.initSuccessful = false;

		this.setUpToRunNoCheckedExceptionShit();

	}


	public void processCas(JCas cas) {

		if (this.initSuccessful) {
			this.processCasInPipeline(cas);
		} else {
			throw new RuntimeException("Cas is not initialized yet");
		}
	}


	private void processCasInPipeline(JCas cas) {

		try {
			this.pearAsPipeline.process(cas);
		} catch (AnalysisEngineProcessException e) {
			PearHandler.log.error("Could not process cas", e);
			throw new RuntimeException(e);
		}
	}


	private void setUpToRunNoCheckedExceptionShit() {

		try {
			this.setUpToRun();
		} catch (Exception e) {
			PearHandler.log.error("Can not start the pear", e);
			throw new RuntimeException(e);
		}
	}


	private void setUpToRun() throws Exception {

		String descriptorPath = this.installPear();
		XMLInputSource xmlInputSource = new XMLInputSource(descriptorPath);
		PearSpecifier parsePearSpecifier = UIMAFramework.getXMLParser().parsePearSpecifier(xmlInputSource);
		AnalysisEngine pipeline = this.runDescriptor(parsePearSpecifier);

		this.pearAsPipeline = pipeline;
		this.initSuccessful = true;
	}


	private String installPear() throws IOException {

		PackageBrowser installPackage = PackageInstaller.installPackage(this.installDir, this.pearPath, false);
		return installPackage.getComponentPearDescPath();

	}


	private AnalysisEngine runDescriptor(ResourceSpecifier resourceSpecifier) throws ResourceInitializationException {

		AnalysisEngine analysisEngine = UIMAFramework.produceAnalysisEngine(resourceSpecifier, null, null);
		analysisEngine.getResourceManager().getCasManager().defineCasPool(PearHandler.POOLISIOUS, 2, new Properties());
		return analysisEngine;
	}


	public AnalysisEngine getAE() {

		return this.pearAsPipeline;
	}

}
