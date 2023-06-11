package org.palladiosimulator.somox.analyzer.rules.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.net4j.util.collection.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.generator.fluent.shared.util.ModelLoader;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;
import org.palladiosimulator.somox.analyzer.rules.mocore.workflow.MoCoReJob;
import org.palladiosimulator.somox.analyzer.rules.mocore.workflow.PersistenceJob;
import org.palladiosimulator.somox.analyzer.rules.workflow.utility.MethodDeclarationVisitor;
import org.palladiosimulator.somox.ast2seff.jobs.Ast2SeffJob;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;

public class PostAnalysisJobTest {
    private static final String DIRECTORY_CASESTUDY = "src/org/palladiosimulator/somox/analyzer/rules/workflow/casestudy";
    private static final String DIRECTORY_OUTPUT = "./tmp";
    private static final String KEY_ANALYSIS_REPOSITORY = "input.repository.analysis";
    private static final String KEY_AST2SEFF_REPOSITORY = "intermediate.repository.ast2Seff";
    private static final String KEY_MOCORE_REPOSITORY = "output.repository";
    private static final String KEY_MOCORE_SYSTEM = "output.system";
    private static final String KEY_MOCORE_ALLOCATION = "output.allocation";
    private static final String KEY_MOCORE_RESOURCE_ENVIRONMENT = "output.resource_environment";
    private static final String KEY_SEFF_ASSOCIATIONS = "input.ast2seff";

    private Blackboard<Object> blackboard;
    private Path temporaryOutputDirectory;

    @BeforeEach
    public void setupEnvironment() throws IOException {
        Repository repository = RepositoryFactory.eINSTANCE.createRepository();
        Map<ASTNode, ServiceEffectSpecification> ast2seffMap = new HashMap<>();

        Path directoryPath = Path.of(DIRECTORY_CASESTUDY);
        Map<String, CompilationUnit> compilationUnits = parseCasestudyPackage(directoryPath);

        // For each comp. unit fill repository & ast2seff map with correct elements
        for (CompilationUnit compilationUnit : compilationUnits.values()) {
            List<MethodDeclaration> methodDeclarations = MethodDeclarationVisitor.perform(compilationUnit);

            // Associate method declarations with class names (-> usually single class per comp. unit)
            Multimap<String, MethodDeclaration> classDeclarations = HashMultimap.create();
            for (MethodDeclaration methodDeclaration : methodDeclarations) {
                TypeDeclaration typeDeclaration = (TypeDeclaration) methodDeclaration.getParent();
                String className = typeDeclaration.getName().toString();
                classDeclarations.put(className, methodDeclaration);
            }

            // For each class create one component, one interface, associated signatures, & seffs
            for (String className : classDeclarations.keySet()) {
                // Create component
                BasicComponent component = RepositoryFactory.eINSTANCE.createBasicComponent();
                component.setEntityName(className);
                component.setRepository__RepositoryComponent(repository);

                // Create operation interface
                OperationInterface operationInterface = RepositoryFactory.eINSTANCE.createOperationInterface();
                operationInterface.setEntityName(className + "able");
                operationInterface.setRepository__Interface(repository);

                // Add interface to component via provided role
                OperationProvidedRole providedRole = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
                providedRole.setProvidedInterface__OperationProvidedRole(operationInterface);
                component.getProvidedRoles_InterfaceProvidingEntity().add(providedRole);

                // Create signatures & Create seffs
                for (MethodDeclaration methodDeclaration : classDeclarations.get(className)) {
                    String methodName = methodDeclaration.getName().toString();
                    OperationSignature operationSignature = RepositoryFactory.eINSTANCE.createOperationSignature();
                    operationSignature.setEntityName(methodName);

                    // Add signature to interface
                    operationInterface.getSignatures__OperationInterface().add(operationSignature);

                    // Create seff for signature & add to component
                    ResourceDemandingSEFF seff = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
                    seff.setBasicComponent_ServiceEffectSpecification(component);
                    seff.setDescribedService__SEFF(operationSignature);

                    // Add AST seff association to map
                    ast2seffMap.put(methodDeclaration, seff);
                }
            }
        }

        // Add required roles manually
        List<Pair<String, String>> requiredRelations = List.of(Pair.create("EntityService", "EntityRepository"),
                Pair.create("EntityService", "Entity"), Pair.create("EntityRepository", "Entity"));
        for (Pair<String, String> requiredRelation : requiredRelations) {
            BasicComponent requirerer = (BasicComponent) repository.getComponents__Repository().stream()
                    .filter(component -> component.getEntityName().equals(requiredRelation.getElement1()))
                    .findFirst().orElseThrow();
            BasicComponent provider = (BasicComponent) repository.getComponents__Repository().stream()
                    .filter(component -> component.getEntityName().equals(requiredRelation.getElement2()))
                    .findFirst().orElseThrow();
            OperationInterface providerInterface = ((OperationProvidedRole) provider
                    .getProvidedRoles_InterfaceProvidingEntity().get(0))
                    .getProvidedInterface__OperationProvidedRole();
            OperationRequiredRole requiredRole = RepositoryFactory.eINSTANCE.createOperationRequiredRole();
            requiredRole.setRequiredInterface__OperationRequiredRole(providerInterface);
            requirerer.getRequiredRoles_InterfaceRequiringEntity().add(requiredRole);
        }

        // Initialize blackboard for tests
        this.blackboard = new Blackboard<>();
        this.blackboard.addPartition(KEY_ANALYSIS_REPOSITORY, repository);
        this.blackboard.addPartition(KEY_SEFF_ASSOCIATIONS, ast2seffMap);

        // Initialize temporary output directory
        this.temporaryOutputDirectory = Files.createTempDirectory(Paths.get(DIRECTORY_OUTPUT), null);
        this.temporaryOutputDirectory.toFile().deleteOnExit();
    }

    @AfterEach
    public void cleanupTemporaryDirectory() throws IOException {
        Files.walk(this.temporaryOutputDirectory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @BeforeAll
    public static void setupOutputDirectory() throws IOException {
        Files.createDirectories(Paths.get(DIRECTORY_OUTPUT));
    }

    @Test
    public void processAnalysedCasestudyWithAst2SeffJob() throws Exception {
        // Construct & execute job
        Ast2SeffJob ast2SeffJob = new Ast2SeffJob(this.getBlackboard(),
                KEY_SEFF_ASSOCIATIONS, KEY_AST2SEFF_REPOSITORY);
        ast2SeffJob.execute(new NullProgressMonitor());

        // Get output repo from blackboard & extract all seff elements
        Repository outputRepository = (Repository) this.getBlackboard().getPartition(KEY_AST2SEFF_REPOSITORY);
        List<ServiceEffectSpecification> seffs = outputRepository.getComponents__Repository().stream()
                .flatMap(component -> ((BasicComponent) component)
                        .getServiceEffectSpecifications__BasicComponent().stream())
                .collect(Collectors.toList());

        // Assert all seffs have at least start & stop action
        for (ServiceEffectSpecification seff : seffs) {
            EList<AbstractAction> actions = ((ResourceDemandingSEFF) seff).getSteps_Behaviour();
            assertTrue(actions.size() >= 2);
            assertTrue(actions.stream().anyMatch(action -> action instanceof StartAction));
            assertTrue(actions.stream().anyMatch(action -> action instanceof StopAction));
        }
    }

    @Test
    public void mergeAst2SeffOutputWithRepository() throws Exception {
        // Construct jobs
        Ast2SeffJob ast2SeffJob = new Ast2SeffJob(this.getBlackboard(),
                KEY_SEFF_ASSOCIATIONS, KEY_AST2SEFF_REPOSITORY);
        SeffMergerJob seffMergerJob = new SeffMergerJob(this.getBlackboard(),
                KEY_AST2SEFF_REPOSITORY, KEY_ANALYSIS_REPOSITORY);

        // Assert empty seffs in analysis repository
        Repository analysisRepository = (Repository) this.getBlackboard().getPartition(KEY_ANALYSIS_REPOSITORY);
        List<ServiceEffectSpecification> analysisRepositorySeffs = analysisRepository.getComponents__Repository()
                .stream()
                .flatMap(component -> ((BasicComponent) component)
                        .getServiceEffectSpecifications__BasicComponent().stream())
                .collect(Collectors.toList());
        for (ServiceEffectSpecification seff : analysisRepositorySeffs) {
            EList<AbstractAction> actions = ((ResourceDemandingSEFF) seff).getSteps_Behaviour();
            assertTrue(actions.isEmpty());
        }

        // Execute jobs
        ast2SeffJob.execute(new NullProgressMonitor());
        seffMergerJob.execute(new NullProgressMonitor());

        // Assert all seffs in analysis repository have at least start & stop action
        analysisRepositorySeffs = analysisRepository.getComponents__Repository()
                .stream()
                .flatMap(component -> ((BasicComponent) component)
                        .getServiceEffectSpecifications__BasicComponent().stream())
                .collect(Collectors.toList());
        for (ServiceEffectSpecification seff : analysisRepositorySeffs) {
            EList<AbstractAction> actions = ((ResourceDemandingSEFF) seff).getSteps_Behaviour();
            assertTrue(actions.size() >= 2);
            assertTrue(actions.stream().anyMatch(action -> action instanceof StartAction));
            assertTrue(actions.stream().anyMatch(action -> action instanceof StopAction));
        }
    }

    @Test
    public void refineIntermediateRepository() throws Exception {
        // Construct jobs
        Ast2SeffJob ast2SeffJob = new Ast2SeffJob(this.getBlackboard(),
                KEY_SEFF_ASSOCIATIONS, KEY_AST2SEFF_REPOSITORY);
        SeffMergerJob seffMergerJob = new SeffMergerJob(this.getBlackboard(),
                KEY_AST2SEFF_REPOSITORY, KEY_ANALYSIS_REPOSITORY);
        MoCoReJob mocoreJob = new MoCoReJob(this.getBlackboard(), KEY_ANALYSIS_REPOSITORY, KEY_MOCORE_REPOSITORY,
                KEY_MOCORE_SYSTEM, KEY_MOCORE_ALLOCATION, KEY_MOCORE_RESOURCE_ENVIRONMENT);

        // Execute jobs
        ast2SeffJob.execute(new NullProgressMonitor());
        seffMergerJob.execute(new NullProgressMonitor());
        mocoreJob.execute(new NullProgressMonitor());

        // Fetch output models from blackboard
        Repository repository = (Repository) this.getBlackboard().getPartition(KEY_MOCORE_REPOSITORY);
        org.palladiosimulator.pcm.system.System system = (org.palladiosimulator.pcm.system.System) this.getBlackboard()
                .getPartition(KEY_MOCORE_SYSTEM);
        Allocation allocation = (Allocation) this.getBlackboard().getPartition(KEY_MOCORE_ALLOCATION);
        ResourceEnvironment resourceEnvironment = (ResourceEnvironment) this.getBlackboard()
                .getPartition(KEY_MOCORE_RESOURCE_ENVIRONMENT);

        // Check repository validity
        assertEquals(3, repository.getComponents__Repository().size());
        assertEquals(3, repository.getInterfaces__Repository().size());
        repository.getInterfaces__Repository().forEach(interFace -> {
            OperationInterface operationInterface = (OperationInterface) interFace;
            assertFalse(operationInterface.getSignatures__OperationInterface().isEmpty());
        });
        repository.getComponents__Repository().forEach(component -> {
            BasicComponent basicComponent = (BasicComponent) component;
            assertFalse(basicComponent.getServiceEffectSpecifications__BasicComponent().isEmpty());
            assertFalse(basicComponent.getProvidedRoles_InterfaceProvidingEntity().isEmpty());
        });

        // Check system validity
        assertEquals(3, system.getAssemblyContexts__ComposedStructure().size());
        assertEquals(3, system.getConnectors__ComposedStructure().size());

        // Check resource environment validity
        assertEquals(3, resourceEnvironment.getResourceContainer_ResourceEnvironment().size());
        assertEquals(3, resourceEnvironment.getLinkingResources__ResourceEnvironment().size());

        // Check allocation validity
        assertEquals(system, allocation.getSystem_Allocation());
        assertEquals(resourceEnvironment, allocation.getTargetResourceEnvironment_Allocation());
        assertEquals(3, allocation.getAllocationContexts_Allocation().size());
    }

    @Test
    public void persistModels() throws Exception {
        // Construct jobs
        Ast2SeffJob ast2SeffJob = new Ast2SeffJob(this.getBlackboard(),
                KEY_SEFF_ASSOCIATIONS, KEY_AST2SEFF_REPOSITORY);
        SeffMergerJob seffMergerJob = new SeffMergerJob(this.getBlackboard(),
                KEY_AST2SEFF_REPOSITORY, KEY_ANALYSIS_REPOSITORY);
        MoCoReJob mocoreJob = new MoCoReJob(this.getBlackboard(), KEY_ANALYSIS_REPOSITORY, KEY_MOCORE_REPOSITORY,
                KEY_MOCORE_SYSTEM, KEY_MOCORE_ALLOCATION, KEY_MOCORE_RESOURCE_ENVIRONMENT);
        URI inputDirectory = URI.createURI(DIRECTORY_CASESTUDY);
        URI outputDirectory = URI.createURI(this.temporaryOutputDirectory.toString());
        PersistenceJob persistenceJob = new PersistenceJob(this.getBlackboard(), inputDirectory, outputDirectory,
                KEY_MOCORE_REPOSITORY, KEY_MOCORE_SYSTEM, KEY_MOCORE_ALLOCATION, KEY_MOCORE_RESOURCE_ENVIRONMENT);

        // Execute jobs
        ast2SeffJob.execute(new NullProgressMonitor());
        seffMergerJob.execute(new NullProgressMonitor());
        mocoreJob.execute(new NullProgressMonitor());
        persistenceJob.execute(new NullProgressMonitor());

        // Check if model files were created successfully
        List<Path> paths = Files.walk(this.temporaryOutputDirectory).toList();
        Path repositoryPath = paths.stream().filter(path -> com.google.common.io.Files.getFileExtension(path.toString())
                .equals("repository")).findFirst().orElseThrow();
        Path systemPath = paths.stream().filter(path -> com.google.common.io.Files.getFileExtension(path.toString())
                .equals("system")).findFirst().orElseThrow();
        Path resourceEnvironmentPath = paths.stream()
                .filter(path -> com.google.common.io.Files.getFileExtension(path.toString())
                        .equals("resourceenvironment"))
                .findFirst().orElseThrow();
        Path allocationPath = paths.stream().filter(path -> com.google.common.io.Files.getFileExtension(path.toString())
                .equals("allocation")).findFirst().orElseThrow();

        // Load model files saved to disk
        Repository persistedRepository = ModelLoader.loadRepository(repositoryPath.toString());
        org.palladiosimulator.pcm.system.System persistedSystem = ModelLoader.loadSystem(systemPath.toString());
        ResourceEnvironment persistedResourceEnvironment = ModelLoader
                .loadResourceEnvironment(resourceEnvironmentPath.toString());
        Allocation persistedAllocation = ModelLoader.loadAllocation(allocationPath.toString());

        // Check loaded repository validity
        assertEquals(3, persistedRepository.getComponents__Repository().size());
        assertEquals(3, persistedRepository.getInterfaces__Repository().size());
        persistedRepository.getInterfaces__Repository().forEach(interFace -> {
            OperationInterface operationInterface = (OperationInterface) interFace;
            assertFalse(operationInterface.getSignatures__OperationInterface().isEmpty());
        });
        persistedRepository.getComponents__Repository().forEach(component -> {
            BasicComponent basicComponent = (BasicComponent) component;
            assertFalse(basicComponent.getServiceEffectSpecifications__BasicComponent().isEmpty());
            assertFalse(basicComponent.getProvidedRoles_InterfaceProvidingEntity().isEmpty());
        });

        // Check loaded system validity
        assertEquals(3, persistedSystem.getAssemblyContexts__ComposedStructure().size());
        assertEquals(3, persistedSystem.getConnectors__ComposedStructure().size());
        // Check repository information via assembly context to verify that references were resolved correctly
        persistedSystem.getAssemblyContexts__ComposedStructure().forEach(assemblyContext -> {
            BasicComponent basicComponent = (BasicComponent) assemblyContext
                    .getEncapsulatedComponent__AssemblyContext();
            assertFalse(basicComponent.getServiceEffectSpecifications__BasicComponent().isEmpty());
            assertFalse(basicComponent.getProvidedRoles_InterfaceProvidingEntity().isEmpty());
        });

        // Check loaded resource environment validity
        assertEquals(3, persistedResourceEnvironment.getResourceContainer_ResourceEnvironment().size());
        assertEquals(3, persistedResourceEnvironment.getLinkingResources__ResourceEnvironment().size());

        // Check loaded allocation validity
        assertEquals(persistedSystem.getId(), persistedAllocation.getSystem_Allocation().getId());
        assertEquals(persistedResourceEnvironment.getEntityName(),
                persistedAllocation.getTargetResourceEnvironment_Allocation().getEntityName());
        assertEquals(3, persistedAllocation.getAllocationContexts_Allocation().size());
        // Check repository information via allocation context to verify that references were resolved correctly
        persistedAllocation.getAllocationContexts_Allocation().forEach(allocationContext -> {
            BasicComponent basicComponent = (BasicComponent) allocationContext.getAssemblyContext_AllocationContext()
                    .getEncapsulatedComponent__AssemblyContext();
            assertFalse(basicComponent.getServiceEffectSpecifications__BasicComponent().isEmpty());
            assertFalse(basicComponent.getProvidedRoles_InterfaceProvidingEntity().isEmpty());
        });
    }

    protected Blackboard<Object> getBlackboard() {
        return this.blackboard;
    }

    private Map<String, CompilationUnit> parseCasestudyPackage(Path directory) {
        ASTParser parser = getASTParser();
        String[] classpathEntries = getEntries(directory, ".jar");
        final String[] sources = getEntries(directory, ".java");
        final String[] encodings = new String[sources.length];
        Arrays.fill(encodings, StandardCharsets.UTF_8.toString());
        final Map<String, CompilationUnit> compilationUnits = new HashMap<>();
        try {
            parser.setEnvironment(classpathEntries, new String[0], new String[0], true);
            parser.createASTs(sources, encodings, new String[0], new FileASTRequestor() {
                @Override
                public void acceptAST(final String sourceFilePath, final CompilationUnit ast) {
                    compilationUnits.put(sourceFilePath, ast);
                }
            }, new NullProgressMonitor());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            exception.printStackTrace();
        }
        return compilationUnits;
    }

    private ASTParser getASTParser() {
        String javaCoreVersion = JavaCore.latestSupportedJavaVersion();
        final ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);
        parser.setCompilerOptions(Map.of(JavaCore.COMPILER_SOURCE, javaCoreVersion, JavaCore.COMPILER_COMPLIANCE,
                javaCoreVersion, JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, javaCoreVersion));
        return parser;
    }

    private String[] getEntries(Path directory, String suffix) {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                    .filter(path -> Files.isRegularFile(path)
                            && path.getFileName().toString().toLowerCase().endsWith(suffix))
                    .map(Path::toAbsolutePath).map(Path::normalize).map(Path::toString).toArray(i -> new String[i]);
        } catch (final IOException exception) {
            exception.printStackTrace();
            return new String[0];
        }
    }
}
