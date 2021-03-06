package com.kumiq.identity.scim.resource.misc

import com.fasterxml.jackson.annotation.JsonProperty
import com.kumiq.identity.scim.path.Tokenizer
import com.kumiq.identity.scim.path.Tokenizer.NoMoreSequenceException
import com.kumiq.identity.scim.resource.core.Meta
import com.kumiq.identity.scim.resource.core.Resource
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils

import java.util.jar.Attributes

import static com.kumiq.identity.scim.resource.constant.ScimConstants.RESOURCE_TYPE_SCHEMA
import static com.kumiq.identity.scim.resource.constant.ScimConstants.URN_SCHEMA

/**
 * Schema resource
 *
 * @author Weinan Qiu
 * @since 1.0.0
 */
@ToString
@EqualsAndHashCode
final class Schema extends Resource {

    public static final String HINT_USER_SCHEMA_ID = 'HintUser'
    public static final String HINT_GROUP_SCHEMA_ID = 'HintGroup'

    Schema() {
        this.schemas = [URN_SCHEMA]
        this.meta = new Meta(resourceType: RESOURCE_TYPE_SCHEMA)
    }

    @JsonProperty('name')
    String name

    @JsonProperty('description')
    String description

    @JsonProperty('attributes')
    List<Attribute> attributes = []

    @ToString
    @EqualsAndHashCode
    static class Attribute {
        @JsonProperty('name') String name
        @JsonProperty('description') String description
        @JsonProperty('type') String type
        @JsonProperty('mutability') String mutability
        @JsonProperty('returned') String returned
        @JsonProperty('uniqueness') String uniqueness
        @JsonProperty('required') boolean required
        @JsonProperty('multiValued') boolean multiValued
        @JsonProperty('caseExact') boolean caseExact
        @JsonProperty('canonicalValues') List<String> canonicalValues = []
        @JsonProperty('referenceTypes') List<String> referenceTypes = []
        @JsonProperty('subAttributes') List<Attribute> subAttributes = []
        @JsonProperty('class') Class clazz
        @JsonProperty('elementClass') Class elementClazz
        @JsonProperty('property') String property
    }

    public Attribute findAttributeByPath(String path) {
        Tokenizer tokenizer = new Tokenizer.PathTokenizer(path)
        List<String> paths = []
        while (true) {
            try {
                paths.add(tokenizer.nextSequence().toString())
            } catch (NoMoreSequenceException ex) {
                break;
            }
        }
        Attribute attribute = this.attributes.find { it.name == paths[0] }
        if (!attribute)
            return null

        if (paths.size() > 1) {
            for (int i = 1; i < paths.size(); i++) {
                attribute = attribute.subAttributes.find { it.name == paths[i] }
            }
        }
        return attribute
    }

    public List<String> findAllPaths() {
        List<String> results = []
        findAllPathsInternal(results, '', this.attributes)
        return results
    }

    private void findAllPathsInternal(List<String> results, String currentBase, List<Attribute> attributesToScan) {
        for (Attribute eachAttribute : attributesToScan) {
            String pathToAdd = appendToCurrentBase(currentBase, eachAttribute.name)
            results.add(pathToAdd)
            if (eachAttribute.subAttributes?.size() > 0) {
                findAllPathsInternal(results, pathToAdd, eachAttribute.subAttributes)
            }
        }
    }

    private static String appendToCurrentBase(String currentBase, String path) {
        StringUtils.hasLength(currentBase) ? currentBase + '.' + path : path
    }
}
