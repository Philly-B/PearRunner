/**
 *
 */
package de.example.pearrunner.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.BooleanArray;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.FloatArray;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.LongArray;
import org.apache.uima.jcas.cas.ShortArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.springframework.stereotype.Component;

import de.example.pearrunner.dtos.AnnotationDto;
import lombok.extern.slf4j.Slf4j;

/**
 * @author phil
 *
 */
@Component
@Slf4j
public class ExtractorUtil {

    private static final String VALUE_OF_THIS_FEATURE_TYPE_IS_NOT_EXTRACTED = "Value of this feature type is not extracted.";
    private static final List<String> FEATURE_NAMES_NOT_TO_BE_EXTRACTED = Arrays.asList("begin", "end", "sofa");

    public List<AnnotationDto> extractAnnotations(JCas cas) {

	List<AnnotationDto> annotationDtos = new ArrayList<>();

	List<Type> annotationTypes = this.getAllMatchingTypes(cas);

	for (Type annotationType : annotationTypes) {

	    AnnotationIndex<Annotation> annotationIndex = cas.getAnnotationIndex(annotationType);

	    for (Annotation annotation : annotationIndex) {
		// filter subtypes to avoid duplicate return of subtypes (i.e. do not return
		// DiagnosisConcept if annotationType is Concept)
		if (annotation.getType().equals(annotationType)) {
		    Set<Integer> parentAdresses = new HashSet<>();
		    AnnotationDto annotationDto = this.buildAnnotationDtoRecursive(cas, annotation, parentAdresses);
		    if (annotationDto != null) {
			annotationDtos.add(annotationDto);
		    }
		}
	    }
	}

	return annotationDtos;

    }

    private AnnotationDto buildAnnotationDtoRecursive(JCas jCas, Annotation annotation, Set<Integer> parentAdresses) {

	if (!parentAdresses.add(Integer.valueOf(annotation.getAddress()))) {
	    // recursion termination: this annotation is detected to be a parent
	    return null;
	}

	if (this.areOffsetsValid(jCas, annotation)) {

	    AnnotationDto annotationDto = this.buildAnnotation(annotation);

	    for (Feature feature : annotation.getType().getFeatures()) {
		String featureName = feature.getShortName();
		if (!ExtractorUtil.FEATURE_NAMES_NOT_TO_BE_EXTRACTED.contains(featureName)) {
		    Object featureValueObject = this.buildFeatureValueObject(jCas, annotation, feature, parentAdresses);
		    annotationDto.addAdditionalParameter(featureName, featureValueObject);
		}
	    }
	    parentAdresses.remove(Integer.valueOf(annotation.getAddress()));
	    return annotationDto;
	}

	ExtractorUtil.log.error(
		"Ignoring annotation of type [{}] with illegal offsets. Begin: [{}], end: [{}], document text length: [{}]",
		annotation.getType(), String.valueOf(annotation.getBegin()), String.valueOf(annotation.getEnd()),
		String.valueOf(jCas.getDocumentText().length()));

	return null;
    }

    private boolean areOffsetsValid(JCas jCas, Annotation annotation) {

	return annotation.getBegin() >= 0 && annotation.getBegin() <= annotation.getEnd()
		&& annotation.getEnd() <= jCas.getDocumentText().length();
    }

    private Object buildFeatureValueObject(JCas jCas, Annotation annotation, Feature feature,
	    Set<Integer> parentAdresses) {

	if (feature.getRange().isPrimitive()) {
	    return this.buildFeatureValueFromPrimitive(annotation, feature);
	} else if (feature.getRange().isArray()) {
	    return this.buildFeatureValueFromArray(jCas, annotation, feature, parentAdresses);
	} else {
	    return this.buildFeatureValueFromComplexType(jCas, annotation, feature, parentAdresses);
	}
    }

    private Object buildFeatureValueFromPrimitive(Annotation annotation, Feature feature) {

	switch (feature.getRange().getName()) {
	case CAS.TYPE_NAME_BOOLEAN:
	    return Boolean.valueOf(annotation.getBooleanValue(feature));

	case CAS.TYPE_NAME_SHORT:
	    return Short.valueOf(annotation.getShortValue(feature));

	case CAS.TYPE_NAME_INTEGER:
	    return Integer.valueOf(annotation.getIntValue(feature));

	case CAS.TYPE_NAME_LONG:
	    return Long.valueOf(annotation.getLongValue(feature));

	case CAS.TYPE_NAME_FLOAT:
	    return Float.valueOf(annotation.getFloatValue(feature));

	case CAS.TYPE_NAME_DOUBLE:
	    return Double.valueOf(annotation.getDoubleValue(feature));
	default:
	    return annotation.getFeatureValueAsString(feature);
	}
    }

    private Object buildFeatureValueFromArray(JCas jCas, Annotation annotation, Feature feature,
	    Set<Integer> parentAdresses) {

	FeatureStructure featureValue = annotation.getFeatureValue(feature);
	if (featureValue == null) {
	    // feature value not set
	    return null;
	}
	Type componentType = feature.getRange().getComponentType();
	if (componentType.isPrimitive()) {
	    return this.buildFeatureValueFromPrimitiveArray(featureValue, componentType);
	} else if (componentType.isArray()) {
	    // not implemented
	    return this.getFeatureNotImplementedValue();
	} else {
	    return this.buildFeatureValueFromComplexArray(jCas, parentAdresses, featureValue);
	}
    }

    private Object buildFeatureValueFromComplexArray(JCas jCas, Set<Integer> parentAdresses,
	    FeatureStructure featureValue) {

	List<AnnotationDto> annotationList = new ArrayList<>();
	FSArray fsArray = (FSArray) featureValue;
	if (fsArray.size() == 0) {
	    return annotationList;
	}
	if (fsArray.get(0) instanceof Annotation) {

	    for (FeatureStructure featureStructure : fsArray) {
		AnnotationDto featureAnnotationDto = this.buildAnnotationDtoRecursive(jCas,
			(Annotation) featureStructure, parentAdresses);
		if (featureAnnotationDto != null) {
		    annotationList.add(featureAnnotationDto);
		}
	    }
	    return annotationList;
	}

	return this.getFeatureNotImplementedValue();
    }

    private Object buildFeatureValueFromPrimitiveArray(FeatureStructure featureValue, Type componentType) {

	List<Object> primitiveList = new ArrayList<>();

	switch (componentType.getName()) {

	case CAS.TYPE_NAME_BOOLEAN:
	    BooleanArray booleanArray = (BooleanArray) featureValue;
	    for (Boolean booleanElem : booleanArray) {
		primitiveList.add(booleanElem);
	    }
	    return primitiveList;

	case CAS.TYPE_NAME_SHORT:
	    ShortArray shortArray = (ShortArray) featureValue;
	    for (Short shortElem : shortArray) {
		primitiveList.add(shortElem);
	    }
	    return primitiveList;

	case CAS.TYPE_NAME_INTEGER:
	    IntegerArray intArray = (IntegerArray) featureValue;
	    for (Integer intElem : intArray) {
		primitiveList.add(intElem);
	    }
	    return primitiveList;

	case CAS.TYPE_NAME_LONG:
	    LongArray longArray = (LongArray) featureValue;
	    for (Long longElem : longArray) {
		primitiveList.add(longElem);
	    }
	    return primitiveList;

	case CAS.TYPE_NAME_FLOAT:
	    FloatArray floatArray = (FloatArray) featureValue;
	    for (int i = 0; i < floatArray.size(); i++) {
		primitiveList.add(Float.valueOf(floatArray.get(i)));
	    }
	    return primitiveList;

	case CAS.TYPE_NAME_DOUBLE:
	    DoubleArray doubleArray = (DoubleArray) featureValue;
	    for (Double doubleElem : doubleArray) {
		primitiveList.add(doubleElem);
	    }
	    return primitiveList;

	case CAS.TYPE_NAME_STRING:
	    StringArray stringArray = (StringArray) featureValue;
	    for (String stringElem : stringArray) {
		primitiveList.add(stringElem);
	    }
	    return primitiveList;

	default:
	    return this.getFeatureNotImplementedValue();
	}
    }

    private List<String> getFeatureNotImplementedValue() {

	return Arrays.asList(ExtractorUtil.VALUE_OF_THIS_FEATURE_TYPE_IS_NOT_EXTRACTED);
    }

    private Object buildFeatureValueFromComplexType(JCas jCas, Annotation annotation, Feature feature,
	    Set<Integer> parentAdresses) {

	FeatureStructure featureValue = annotation.getFeatureValue(feature);
	if (featureValue == null) {
	    // feature value not set
	    return null;
	} else if (featureValue instanceof Annotation) {
	    Annotation featureAnnotation = (Annotation) featureValue;
	    return this.buildAnnotationDtoRecursive(jCas, featureAnnotation, parentAdresses);
	} else {
	    return ExtractorUtil.VALUE_OF_THIS_FEATURE_TYPE_IS_NOT_EXTRACTED;
	}
    }

    private List<Type> getAllMatchingTypes(JCas jCas) {

	TypeSystem typeSystem = jCas.getTypeSystem();
	Type annotationType = typeSystem.getType("uima.tcas.Annotation");
	return typeSystem.getProperlySubsumedTypes(annotationType);
    }

    private AnnotationDto buildAnnotation(Annotation a) {

	AnnotationDto annotationDto = new AnnotationDto();
	annotationDto.setBegin(a.getBegin());
	annotationDto.setEnd(a.getEnd());
	annotationDto.setCoveredText(a.getCoveredText());
	return annotationDto;
    }

}
