package org.palladiosimulator.retriever.core.cli;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.palladiosimulator.retriever.core.configuration.RuleEngineConfigurationImpl;
import org.palladiosimulator.retriever.core.service.RuleCollection;
import org.palladiosimulator.retriever.core.workflow.RuleEngineJob;
import org.palladiosimulator.retriever.extraction.discoverers.Discoverer;
import org.palladiosimulator.retriever.extraction.discoverers.DiscovererCollection;
import org.palladiosimulator.retriever.extraction.engine.Rule;
import org.palladiosimulator.retriever.extraction.engine.RuleEngineConfiguration;
import org.palladiosimulator.retriever.extraction.engine.ServiceConfiguration;

public class RuleEngineApplication implements IApplication {

    private static Options createOptions(Set<String> availableRuleIDs) {
        final Options options = new Options();
        options
            .addRequiredOption("i", "input-directory", true,
                    "Path to the root directory of the project to be reverse engineered.")
            .addRequiredOption("o", "output-directory", true, "Path to the output directory for the generated models.")
            .addRequiredOption("r", "rules", true,
                    "Supported rules for reverse engineering: " + String.join(", ", availableRuleIDs));

        options.addOption("h", "help", false, "Print this help message.");

        return options;
    }

    private static void printHelp(final Options options) {
        String cmdSyntax = "./eclipse";
        final String operatingSystem = System.getProperty("os.name", "generic")
            .toLowerCase(Locale.ENGLISH);
        if ((operatingSystem.indexOf("mac") >= 0) || (operatingSystem.indexOf("darwin") >= 0)) {
            cmdSyntax = "open -a eclipse.app";
        } else if (operatingSystem.indexOf("win") >= 0) {
            cmdSyntax = "start eclipse.exe";
        }
        new HelpFormatter().printHelp(cmdSyntax, options);
    }

    @Override
    public Object start(IApplicationContext context) throws Exception {

        Set<Rule> availableRules = new RuleCollection().getServices();
        Set<String> availableRuleIDs = availableRules.stream()
            .map(Rule::getID)
            .collect(Collectors.toSet());

        final Options options = createOptions(availableRuleIDs);
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd;
        try {
            cmd = parser.parse(options, (String[]) context.getArguments()
                .get(IApplicationContext.APPLICATION_ARGS));
        } catch (final Exception e) {
            System.err.println(e.getMessage());
            printHelp(options);
            return -1;
        }

        if (cmd.hasOption("help")) {
            printHelp(options);
        }

        RuleEngineConfiguration configuration = new RuleEngineConfigurationImpl();

        try {
            configuration.setInputFolder(URI.createFileURI(URI.decode(Paths.get(cmd.getOptionValue("i"))
                .toAbsolutePath()
                .normalize()
                .toString())));
        } catch (final InvalidPathException e) {
            System.err.println("Invalid input path: " + e.getMessage());
            return -1;
        }

        try {
            configuration.setOutputFolder(URI.createFileURI(URI.decode(Paths.get(cmd.getOptionValue("o"))
                .toAbsolutePath()
                .normalize()
                .toString())));
        } catch (final InvalidPathException e) {
            System.err.println("Invalid output path: " + e.getMessage());
            return -1;
        }

        // Enable all discoverers, in case a selected rule depends on them.
        // TODO: This is unnecessary once rule dependencies are in place
        ServiceConfiguration<Discoverer> discovererConfig = configuration.getConfig(Discoverer.class);
        for (Discoverer discoverer : new DiscovererCollection().getServices()) {
            discovererConfig.select(discoverer);
        }

        ServiceConfiguration<Rule> ruleConfig = configuration.getConfig(Rule.class);
        // Extract and check rules
        Set<String> requestedRuleIDs = Arrays.stream(cmd.getOptionValue("r")
            .split(","))
            .map(String::strip)
            .collect(Collectors.toSet());
        Set<Rule> rules = availableRules.stream()
            .filter(x -> requestedRuleIDs.contains(x.getID()))
            .collect(Collectors.toSet());
        if (rules.isEmpty()) {
            System.err.println("Invalid rules: " + cmd.getOptionValue("r"));
            return -1;
        }
        for (Rule rule : rules) {
            ruleConfig.select(rule);
        }

        new RuleEngineJob(configuration).execute(new NullProgressMonitor());

        return 0;
    }

    @Override
    public void stop() {
    }
}
