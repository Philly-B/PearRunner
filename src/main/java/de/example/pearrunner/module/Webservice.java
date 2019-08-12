/**
 *
 */
package de.example.pearrunner.module;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
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


	@GetMapping(path = "/rest/health", produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> healthCheck() {

		if (this.analyzeService.healthy()) {
			return new ResponseEntity<String>("Service up and running", HttpStatus.OK);
		}
		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);

	}


	@PostMapping(path = "/rest/analyze/analyzeText", consumes = "text/plain", produces = "application/json")
	@ResponseBody
	public WebServiceResponse analyseText(@RequestBody(required = true) String toAnalyse) {

		List<AnnotationDto> result = this.analyzeService.analyseText(toAnalyse);

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
