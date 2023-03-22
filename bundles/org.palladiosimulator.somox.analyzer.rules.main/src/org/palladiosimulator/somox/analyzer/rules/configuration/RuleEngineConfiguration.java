package org.palladiosimulator.somox.analyzer.rules.configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.somox.analyzer.rules.all.DefaultRule;
import org.palladiosimulator.somox.analyzer.rules.blackboard.RuleEngineBlackboard;
import org.palladiosimulator.somox.analyzer.rules.service.Analyst;
import org.palladiosimulator.somox.analyzer.rules.service.AnalystCollection;
import org.palladiosimulator.somox.analyzer.rules.service.EmptyCollection;
import org.palladiosimulator.somox.analyzer.rules.service.ServiceCollection;
import org.palladiosimulator.somox.analyzer.rules.service.ServiceConfiguration;
import org.palladiosimulator.somox.discoverer.Discoverer;
import org.palladiosimulator.somox.discoverer.DiscovererCollection;

import de.uka.ipd.sdq.workflow.configuration.AbstractComposedJobConfiguration;
import de.uka.ipd.sdq.workflow.extension.ExtendableJobConfiguration;

public class RuleEngineConfiguration extends AbstractComposedJobConfiguration implements ExtendableJobConfiguration {
    private static final Logger LOG = Logger.getLogger(RuleEngineConfiguration.class);

    private static final String CONFIG_PREFIX = "org.palladiosimulator.somox.analyzer.rules.configuration.";
    public static final String RULE_ENGINE_INPUT_PATH = "input.path";
    public static final String RULE_ENGINE_OUTPUT_PATH = CONFIG_PREFIX + "output.path";
    public static final String RULE_ENGINE_SELECTED_RULES = CONFIG_PREFIX + "rules";
    public static final String RULE_ENGINE_SELECTED_ANALYSTS = CONFIG_PREFIX + "analysts";
    public static final String RULE_ENGINE_SELECTED_DISCOVERERS = CONFIG_PREFIX + "discoverers";
    public static final String RULE_ENGINE_ANALYST_CONFIG_PREFIX = CONFIG_PREFIX + "analystconfig.";
    public static final String RULE_ENGINE_DISCOVERER_CONFIG_PREFIX = CONFIG_PREFIX + "discovererconfig.";
    public static final String RULE_ENGINE_USE_EMFTEXT_PARSER = CONFIG_PREFIX + "use_emftext_parser";
    public static final String RULE_ENGINE_BLACKBOARD_KEY_REPOSITORY = RuleEngineBlackboard.KEY_REPOSITORY;
    public static final String RULE_ENGINE_BLACKBOARD_KEY_SEFF_ASSOCIATIONS = RuleEngineBlackboard.KEY_SEFF_ASSOCIATIONS;
    public static final String RULE_LIST_SEPARATOR = ";";

    private /* not final */ URI inputFolder;
    private /* not final */ URI outputFolder;
    private /* not final */ boolean useEMFTextParser;
    private final Set<DefaultRule> rules;
    private final ServiceConfiguration<Analyst> analystConfig;
    private final ServiceConfiguration<Discoverer> discovererConfig;

    private final Map<String, Object> attributes;

    public RuleEngineConfiguration() {
        this(new HashMap<>());
    }

    public RuleEngineConfiguration(Map<String, Object> attributes) {
        rules = new HashSet<>();
        this.attributes = Objects.requireNonNull(attributes);
        ServiceCollection<Analyst> analystCollection = null;
        try {
            analystCollection = new AnalystCollection();
        } catch (CoreException e) {
            LOG.error("Exception occurred while discovering analysts!");
            analystCollection = new EmptyCollection<>();
        }
        analystConfig = new ServiceConfiguration<>(analystCollection, RULE_ENGINE_SELECTED_ANALYSTS,
                RULE_ENGINE_ANALYST_CONFIG_PREFIX);

        ServiceCollection<Discoverer> discovererCollection = null;
        try {
            discovererCollection = new DiscovererCollection();
        } catch (CoreException e) {
            LOG.error("Exception occurred while discovering discoverers!");
            discovererCollection = new EmptyCollection<>();
        }
        discovererConfig = new ServiceConfiguration<>(discovererCollection, RULE_ENGINE_SELECTED_DISCOVERERS,
                RULE_ENGINE_DISCOVERER_CONFIG_PREFIX);
        applyAttributeMap(attributes);
    }

    @SuppressWarnings("unchecked")
    public void applyAttributeMap(final Map<String, Object> attributeMap) {
        if (attributeMap == null) {
            return;
        }

        if (attributeMap.get(RULE_ENGINE_INPUT_PATH) != null) {
            setInputFolder(URI.createURI((String) attributeMap.get(RULE_ENGINE_INPUT_PATH)));
        }
        if (attributeMap.get(RULE_ENGINE_OUTPUT_PATH) != null) {
            setOutputFolder(URI.createURI((String) attributeMap.get(RULE_ENGINE_OUTPUT_PATH)));
        }
        if (attributeMap.get(RULE_ENGINE_USE_EMFTEXT_PARSER) != null) {
            setUseEMFTextParser((boolean) attributeMap.get(RULE_ENGINE_USE_EMFTEXT_PARSER));
        }
        if (attributeMap.get(RULE_ENGINE_SELECTED_RULES) != null) {
            setSelectedRules(parseRules((Set<String>) attributeMap.get(RULE_ENGINE_SELECTED_RULES)));
        }

        analystConfig.applyAttributeMap(attributeMap);
        discovererConfig.applyAttributeMap(attributeMap);
    }

    public void setSelectedRules(Set<DefaultRule> rules) {
        this.rules.clear();
        this.rules.addAll(rules);
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

    public boolean useEmfTextParser() {
        return useEMFTextParser;
    }

    public ServiceConfiguration<Analyst> getAnalystConfig() {
        return analystConfig;
    }

    public ServiceConfiguration<Discoverer> getDiscovererConfig() {
        return discovererConfig;
    }

    public void setInputFolder(URI inputFolder) {
        this.inputFolder = inputFolder;
    }

    public void setOutputFolder(URI outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void setUseEMFTextParser(boolean value) {
        useEMFTextParser = value;
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> result = new HashMap<>();

        result.put(RULE_ENGINE_INPUT_PATH, getInputFolder());
        result.put(RULE_ENGINE_OUTPUT_PATH, getOutputFolder());
        result.put(RULE_ENGINE_USE_EMFTEXT_PARSER, useEmfTextParser());
        result.put(RULE_ENGINE_SELECTED_RULES, serializeRules(rules));
        result.putAll(analystConfig.toMap());
        result.putAll(discovererConfig.toMap());

        return result;
    }

    public Set<DefaultRule> getSelectedRules() {
        return rules;
    }

    public static Set<DefaultRule> parseRules(Iterable<String> strRules) {
        Set<DefaultRule> rules = new HashSet<>();
        for (String rule : strRules) {
            rules.add(DefaultRule.valueOf(rule));
        }
        return rules;
    }

    public static Set<String> serializeRules(Iterable<DefaultRule> rules) {
        Set<String> strRules = new HashSet<>();
        for (DefaultRule rule : rules) {
            strRules.add(rule.toString());
        }
        return strRules;
    }
}
