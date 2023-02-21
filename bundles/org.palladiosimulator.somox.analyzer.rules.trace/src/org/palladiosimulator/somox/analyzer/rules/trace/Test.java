package org.palladiosimulator.somox.analyzer.rules.trace;

public class Test {

	public static void main(String[] args) {
		final ArtifactTraceCreator trace = new ArtifactTraceCreator();
		trace.addTrace("X", "Y");
		trace.addTrace("A", "B");
		trace.addTrace("B", "X");
		trace.addTrace("B", "X");
		trace.addTrace("Y", "Y");
		trace.addTrace("X", "A", "B");
		trace.save();
	}

}
