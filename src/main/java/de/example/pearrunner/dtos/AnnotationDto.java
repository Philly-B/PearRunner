/**
 *
 */
package de.example.pearrunner.dtos;

import lombok.Data;

/**
 * @author phil
 *
 */

@Data
public class AnnotationDto {

	private int begin;
	private int end;
	private String coveredText;
	private String additionalFeatures;

}
