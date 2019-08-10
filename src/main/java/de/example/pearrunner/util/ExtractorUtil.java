/**
 *
 */
package de.example.pearrunner.util;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.springframework.stereotype.Component;

import de.example.pearrunner.dtos.AnnotationDto;

/**
 * @author phil
 *
 */
@Component
public class ExtractorUtil {

    public List<AnnotationDto> extractAnnotations(JCas cas) {

	return JCasUtil.selectAll(cas)
		       .stream()
		       .filter(fs -> fs instanceof Annotation)
		       .map(top -> (Annotation) top)
		       .map(this::createAnnotationDto)
		       .collect(Collectors.toList());

    }

    private AnnotationDto createAnnotationDto(Annotation annotation) {

	AnnotationDto annotationDto = new AnnotationDto();
	annotationDto.setBegin(annotation.getBegin());
	annotationDto.setEnd(annotation.getEnd());
	annotationDto.setCoveredText(annotation.getCoveredText());
	return annotationDto;
    }

}
