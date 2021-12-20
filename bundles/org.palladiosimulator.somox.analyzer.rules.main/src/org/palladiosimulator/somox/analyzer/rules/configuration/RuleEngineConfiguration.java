package org.palladiosimulator.somox.analyzer.rules.configuration;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.somox.configuration.AbstractMoxConfiguration;
import org.somox.configuration.FileLocationConfiguration;

import de.uka.ipd.sdq.workflow.extension.ExtendableJobConfiguration;

public class RuleEngineConfiguration extends AbstractMoxConfiguration implements ExtendableJobConfiguration {

    public static final String RULE_ENGINE_INPUT_PATH = "org.palladiosimulator.somox.analyzer.rules.configuration.input.path";
    public static final String RULE_ENGINE_OUTPUT_PATH = "org.palladiosimulator.somox.analyzer.rules.configuration.output.path";
    public static final String RULE_ENGINE_SELECTED_RULES = "org.palladiosimulator.somox.analyzer.rules.configuration.rules";
    public static final String RULE_LIST_SEPARATOR = ";";

    private URI inputFolder;
    private URI outputFolder;
    private FileLocationConfiguration fileLocations;
    private Set<DefaultRule> rules;

    private final Map<String, Object> attributes;

    public RuleEngineConfiguration() {
        this(new HashMap<>());
    }

    public RuleEngineConfiguration(Map<String, Object> attributes) {
        this.attributes = Objects.requireNonNull(attributes);
        this.fileLocations = new FileLocationConfiguration();
        applyAttributeMap(attributes);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void applyAttributeMap(final Map<String, Object> attributeMap) {
        if ((attributeMap == null)) {
            return;
        }
        super.applyAttributeMap(attributeMap);

        if (attributeMap.get(RULE_ENGINE_INPUT_PATH) != null) {
            setInputFolder(URI.createURI((String) attributeMap.get(RULE_ENGINE_INPUT_PATH)));
        }
        if (attributeMap.get(RULE_ENGINE_OUTPUT_PATH) != null) {
            setOutputFolder(URI.createURI((String) attributeMap.get(RULE_ENGINE_OUTPUT_PATH)));
        }
        if (attributeMap.get(RULE_ENGINE_SELECTED_RULES) != null) {
            setSelectedRules(parseRules((Set<String>) attributeMap.get(RULE_ENGINE_SELECTED_RULES)));
        }
    }

    private void setSelectedRules(Set<DefaultRule> rules) {
        this.rules = rules;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public URI getInputFolder() {
        return inputFolder;
    }

    public URI getOutputFolder() {
        return outputFolder;
    }

    public void setInputFolder(URI inputFolder) {
        this.inputFolder = inputFolder;
        fileLocations.setAnalyserInputFile(inputFolder.toString());
    }

    public void setOutputFolder(URI outputFolder) {
        this.outputFolder = outputFolder;
        fileLocations.setOutputFolder(outputFolder.toString());
    }

    @Override
    public Map<String, Object> toMap() {
        final Map<String, Object> result = super.toMap();

        result.put(RULE_ENGINE_INPUT_PATH, inputFolder);
        result.put(RULE_ENGINE_OUTPUT_PATH, outputFolder);
        result.put(RULE_ENGINE_SELECTED_RULES, serializeRules(rules));

        return result;
    }
    
    @Override
    public FileLocationConfiguration getFileLocations() {
        return fileLocations;
    }

    public Set<DefaultRule> getSelectedRules() {
        return rules;
    }

    public static Set<DefaultRule> parseRules(Set<String> strRules) {
        Set<DefaultRule> rules = new HashSet<>();
        for (String rule : strRules){
            rules.add(DefaultRule.valueOf(rule));
        }
        return rules;
    }

    public static Set<String> serializeRules(Set<DefaultRule> rules) {
        Set<String> strRules = new HashSet<>();
        for (DefaultRule rule : rules) {
            strRules.add(rule.toString());
        }
        return strRules;
    }
}
