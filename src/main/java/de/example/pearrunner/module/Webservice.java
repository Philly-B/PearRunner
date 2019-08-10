/**
 *
 */
package de.example.pearrunner.module;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.example.pearrunner.dtos.AnnotationDto;
import de.example.pearrunner.dtos.ErrorDto;
import de.example.pearrunner.dtos.WebServiceResponse;
import de.example.pearrunner.service.AnalyzeService;

/**
 * @author phil
 *
 */
@RestController
public class Webservice {

    @Autowired
    private AnalyzeService analyzeService;

    @PostMapping(path = "/rest/analyze/analyzeText", consumes = "text/plain", produces = "application/json")
    @ResponseBody
    public WebServiceResponse analyseText(@RequestBody(required = true) String toAnalyse) {

	List<AnnotationDto> result = analyzeService.analyseText(toAnalyse);

	WebServiceResponse response = new WebServiceResponse();
	response.setAnnotations(result);

	return response;
    }

    @ExceptionHandler(RuntimeException.class)
    public WebServiceResponse handlExceptions(RuntimeException e) {

	WebServiceResponse response = new WebServiceResponse();

	ErrorDto err = new ErrorDto();
	err.setErrorMessage(e.getMessage());
	response.setErrors(Arrays.asList(err));

	return response;
    }

}
