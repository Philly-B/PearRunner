/**
 *
 */
package de.example.pearrunner.dtos;

import java.util.List;

import lombok.Data;

/**
 * @author phil
 *
 */
@Data
public class WebServiceResponse {

    private List<AnnotationDto> annotations;
    private List<ErrorDto> errors;

}
