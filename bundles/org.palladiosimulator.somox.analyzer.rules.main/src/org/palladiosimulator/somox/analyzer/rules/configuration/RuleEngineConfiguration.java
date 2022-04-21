package org.palladiosimulator.somox.analyzer.rules.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.workflow.Analyst;
import org.palladiosimulator.somox.analyzer.rules.workflow.AnalystCollection;
import org.somox.configuration.AbstractMoxConfiguration;
import org.somox.configuration.FileLocationConfiguration;

import de.uka.ipd.sdq.workflow.extension.ExtendableJobConfiguration;

public class RuleEngineConfiguration extends AbstractMoxConfiguration implements ExtendableJobConfiguration {

    public static final String RULE_ENGINE_INPUT_PATH = "org.palladiosimulator.somox.analyzer.rules.configuration.input.path";
    public static final String RULE_ENGINE_OUTPUT_PATH = "org.palladiosimulator.somox.analyzer.rules.configuration.output.path";
    public static final String RULE_ENGINE_SELECTED_RULES = "org.palladiosimulator.somox.analyzer.rules.configuration.rules";
    public static final String RULE_ENGINE_SELECTED_ANALYSTS = "org.palladiosimulator.somox.analyzer.rules.configuration.analysts";
    public static final String RULE_LIST_SEPARATOR = ";";
    public static final String RULE_ENGINE_ANALYST_CONFIG_PREFIX = "org.palladiosimulator.somox.analyzer.rules.configuration.analystconfig.";

    private FileLocationConfiguration fileLocations;
    private Set<DefaultRule> rules;
    private Map<String, Map<String, String>> analystConfigs;
    private List<Analyst> analysts;
    private Set<Analyst> selectedAnalysts;

    private final Map<String, Object> attributes;

    public RuleEngineConfiguration() {
        this(new HashMap<>());
    }

    public RuleEngineConfiguration(Map<String, Object> attributes) {
        this.attributes = Objects.requireNonNull(attributes);
        this.fileLocations = new FileLocationConfiguration();
        this.analystConfigs = new HashMap<>();
        try {
            this.analysts = new ArrayList<>(new AnalystCollection().getAnalysts());
        } catch (CoreException e) {
            Logger.getLogger(RuleEngineConfiguration.class)
                .error("An exception occurred while collecting analysts");
            this.analysts = new ArrayList<>();
        }
        this.selectedAnalysts = new HashSet<>();
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
        Set<String> analystIds = (Set<String>) attributeMap.get(RULE_ENGINE_SELECTED_ANALYSTS);
        for (Analyst analyst : analysts) {
            String analystId = analyst.getID();
            if (attributeMap.get(RULE_ENGINE_ANALYST_CONFIG_PREFIX + analystId) != null) {
                analystConfigs.put(analystId,
                        (Map<String, String>) attributeMap.get(RULE_ENGINE_ANALYST_CONFIG_PREFIX + analystId));
            }
            if (analystIds != null && analystIds.contains(analyst.getID())) {
                selectedAnalysts.add(analyst);
            }
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
        return URI.createURI(fileLocations.getAnalyserInputFile());
    }

    public URI getOutputFolder() {
        return URI.createURI(fileLocations.getOutputFolder());
    }

    public String getAnalystConfig(String analystId, String key) {
        Map<String, String> analystConfig = analystConfigs.get(analystId);
        if (analystConfig == null) {
            return null;
        }
        return analystConfig.get(key);
    }

    public Map<String, String> getWholeAnalystConfig(String analystId) {
        return Collections.unmodifiableMap(analystConfigs.get(analystId));
    }

    public void setInputFolder(URI inputFolder) {
        fileLocations.setAnalyserInputFile(inputFolder.toString());
    }

    public void setOutputFolder(URI outputFolder) {
        fileLocations.setOutputFolder(outputFolder.toString());
    }

    public void setAnalystConfig(String analystId, String key, String value) {
        Map<String, String> analystConfig = analystConfigs.get(analystId);
        if (analystConfig == null) {
            analystConfig = new HashMap<String, String>();
            analystConfigs.put(analystId, analystConfig);
        }
        analystConfig.put(key, value);
    }

    public void setAnalystSelected(Analyst analyst, boolean selected) {
        if (selected) {
            selectedAnalysts.add(analyst);
        } else {
            selectedAnalysts.remove(analyst);
        }
    }

    public boolean getAnalystSelected(Analyst analyst) {
        return selectedAnalysts.contains(analyst);
    }

    public Set<Analyst> getSelectedAnalysts() {
        return Collections.unmodifiableSet(selectedAnalysts);
    }

    @Override
    public Map<String, Object> toMap() {
        final Map<String, Object> result = super.toMap();

        result.put(RULE_ENGINE_INPUT_PATH, getInputFolder());
        result.put(RULE_ENGINE_OUTPUT_PATH, getOutputFolder());
        result.put(RULE_ENGINE_SELECTED_RULES, serializeRules(rules));
        for (String analystId : analystConfigs.keySet()) {
            result.put(RULE_ENGINE_ANALYST_CONFIG_PREFIX + analystId, analystConfigs.get(analystId));
        }
        result.put(RULE_ENGINE_SELECTED_ANALYSTS, serializeAnalysts(selectedAnalysts));

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
        for (String rule : strRules) {
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

    public static Set<String> serializeAnalysts(Set<Analyst> analysts) {
        Set<String> analystIds = new HashSet<>();
        for (Analyst analyst : analysts) {
            analystIds.add(analyst.getID());
        }
        return analystIds;
    }
}
