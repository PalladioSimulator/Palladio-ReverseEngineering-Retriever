package org.palladiosimulator.retriever.core.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.retriever.core.service.Analyst;
import org.palladiosimulator.retriever.core.service.AnalystCollection;
import org.palladiosimulator.retriever.core.service.DiscovererCollection;
import org.palladiosimulator.retriever.core.service.EmptyCollection;
import org.palladiosimulator.retriever.core.service.RuleCollection;
import org.palladiosimulator.retriever.extraction.engine.Discoverer;
import org.palladiosimulator.retriever.extraction.engine.RetrieverConfiguration;
import org.palladiosimulator.retriever.extraction.engine.Rule;
import org.palladiosimulator.retriever.extraction.engine.Service;
import org.palladiosimulator.retriever.extraction.engine.ServiceCollection;
import org.palladiosimulator.retriever.extraction.engine.ServiceConfiguration;

import de.uka.ipd.sdq.workflow.configuration.AbstractComposedJobConfiguration;

public class RetrieverConfigurationImpl extends AbstractComposedJobConfiguration implements RetrieverConfiguration {
    private static final Logger LOG = Logger.getLogger(RetrieverConfigurationImpl.class);

    private static final String CONFIG_PREFIX = "org.palladiosimulator.retriever.core.configuration.";
    public static final String RULE_ENGINE_INPUT_PATH = "input.path";
    public static final String RULE_ENGINE_OUTPUT_PATH = CONFIG_PREFIX + "output.path";
    public static final String RULE_ENGINE_SELECTED_RULES = CONFIG_PREFIX + "rules";
    public static final String RULE_ENGINE_SELECTED_ANALYSTS = CONFIG_PREFIX + "analysts";
    public static final String RULE_ENGINE_SELECTED_DISCOVERERS = CONFIG_PREFIX + "discoverers";
    public static final String RULE_ENGINE_RULE_CONFIG_PREFIX = CONFIG_PREFIX + "ruleconfig.";
    public static final String RULE_ENGINE_ANALYST_CONFIG_PREFIX = CONFIG_PREFIX + "analystconfig.";
    public static final String RULE_ENGINE_DISCOVERER_CONFIG_PREFIX = CONFIG_PREFIX + "discovererconfig.";

    private /* not final */ URI inputFolder;
    private /* not final */ URI outputFolder;

    private final Map<Class<? extends Service>, ServiceConfiguration<? extends Service>> serviceConfigs;

    private final Map<String, Object> attributes;

    public RetrieverConfigurationImpl() {
        this(new HashMap<>());
    }

    public RetrieverConfigurationImpl(final Map<String, Object> attributes) {
        this.attributes = Objects.requireNonNull(attributes);
        this.serviceConfigs = new HashMap<>();

        ServiceCollection<Discoverer> discovererCollection = null;
        try {
            discovererCollection = new DiscovererCollection();
        } catch (final CoreException e) {
            LOG.error("Exception occurred while discovering discoverers!");
            discovererCollection = new EmptyCollection<>();
        }
        final ServiceConfiguration<Discoverer> discovererConfig = new ServiceConfiguration<>(discovererCollection,
                RULE_ENGINE_SELECTED_DISCOVERERS, RULE_ENGINE_DISCOVERER_CONFIG_PREFIX);
        this.serviceConfigs.put(Discoverer.class, discovererConfig);

        ServiceCollection<Rule> ruleCollection = null;
        try {
            ruleCollection = new RuleCollection();
        } catch (final CoreException e) {
            LOG.error("Exception occurred while discovering rules!");
            ruleCollection = new EmptyCollection<>();
        }
        final ServiceConfiguration<Rule> ruleConfig = new ServiceConfiguration<>(ruleCollection,
                RULE_ENGINE_SELECTED_RULES, RULE_ENGINE_RULE_CONFIG_PREFIX);
        ruleConfig.addDependencyProvider(discovererConfig);
        this.serviceConfigs.put(Rule.class, ruleConfig);

        ServiceCollection<Analyst> analystCollection = null;
        try {
            analystCollection = new AnalystCollection();
        } catch (final CoreException e) {
            LOG.error("Exception occurred while discovering analysts!");
            analystCollection = new EmptyCollection<>();
        }
        final ServiceConfiguration<Analyst> analystConfig = new ServiceConfiguration<>(analystCollection,
                RULE_ENGINE_SELECTED_ANALYSTS, RULE_ENGINE_ANALYST_CONFIG_PREFIX);
        analystConfig.addDependencyProvider(discovererConfig);
        analystConfig.addDependencyProvider(ruleConfig);
        this.serviceConfigs.put(Analyst.class, analystConfig);

        this.applyAttributeMap(attributes);
    }

    public void applyAttributeMap(final Map<String, Object> attributeMap) {
        if (attributeMap == null) {
            return;
        }

        if (attributeMap.get(RULE_ENGINE_INPUT_PATH) != null) {
            this.setInputFolder(URI.createURI((String) attributeMap.get(RULE_ENGINE_INPUT_PATH)));
        }
        if (attributeMap.get(RULE_ENGINE_OUTPUT_PATH) != null) {
            this.setOutputFolder(URI.createURI((String) attributeMap.get(RULE_ENGINE_OUTPUT_PATH)));
        }

        for (final ServiceConfiguration<? extends Service> serviceConfig : this.serviceConfigs.values()) {
            serviceConfig.applyAttributeMap(attributeMap);
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public URI getInputFolder() {
        return this.inputFolder;
    }

    @Override
    public URI getOutputFolder() {
        return this.outputFolder;
    }

    @Override
    public void setInputFolder(final URI inputFolder) {
        this.inputFolder = inputFolder;
    }

    @Override
    public void setOutputFolder(final URI outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Override
    public <T extends Service> ServiceConfiguration<T> getConfig(final Class<T> serviceClass) {
        // serviceConfig only contains legal mappings
        @SuppressWarnings("unchecked")
        final ServiceConfiguration<T> serviceConfig = (ServiceConfiguration<T>) this.serviceConfigs.get(serviceClass);
        return serviceConfig;
    }

    public Map<String, Object> toMap() {
        final Map<String, Object> result = new HashMap<>();

        result.put(RULE_ENGINE_INPUT_PATH, this.getInputFolder());
        result.put(RULE_ENGINE_OUTPUT_PATH, this.getOutputFolder());

        for (final ServiceConfiguration<? extends Service> serviceConfig : this.serviceConfigs.values()) {
            result.putAll(serviceConfig.toMap());
        }

        return result;
    }
}
