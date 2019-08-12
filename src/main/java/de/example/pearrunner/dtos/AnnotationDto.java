/**
 *
 */
package de.example.pearrunner.dtos;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author phil
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class AnnotationDto {

	private int begin;
	private int end;
	private String coveredText;
	private Map<String, Object> additionalFeatures = new HashMap<>();


	public void addAdditionalParameter(String featureName, Object featureValueObject) {

		this.additionalFeatures.put(featureName, featureValueObject);
	}

}
