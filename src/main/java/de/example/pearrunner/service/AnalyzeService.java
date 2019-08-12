/**
 *
 */
package de.example.pearrunner.service;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.example.pearrunner.dtos.AnnotationDto;
import de.example.pearrunner.initializers.PearHandler;
import de.example.pearrunner.util.CasPool;
import de.example.pearrunner.util.ExtractorUtil;

/**
 * @author phil
 *
 */
@Component
public class AnalyzeService {

	@Autowired
	private CasPool casPool;

	@Autowired
	private ExtractorUtil extractorUtil;

	@Autowired
	private PearHandler pearHandler;


	public boolean healthy() {

		return this.pearHandler.isInitCompleted();
	}


	public List<AnnotationDto> analyseText(String text) {

		JCas cas = this.casPool.getCas();

		try {
			cas.setDocumentText(text);

			this.pearHandler.processCas(cas);

			return this.extractorUtil.extractAnnotations(cas);

		} finally {
			this.casPool.returnCas(cas);
		}

	}

}
