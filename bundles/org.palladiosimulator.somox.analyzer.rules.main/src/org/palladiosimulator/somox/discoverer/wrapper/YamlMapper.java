package org.palladiosimulator.somox.discoverer.wrapper;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class YamlMapper implements Function<String, Optional<Object>> {
	private final Iterable<Object> subfiles;
	
	public YamlMapper(Iterable<Object> content) {
		this.subfiles = content;
	}
	
	@Override
	public Optional<Object> apply(String fullKey) {
		String[] segments = fullKey.split("\\.");

		for (Object subfile : subfiles) {

			boolean failed = false;
			Object currentNode = subfile;
			for (String segment : segments) {
				Optional<Object> nextNode = load(segment, currentNode);
				if (nextNode.isEmpty()) {
					failed = true;
					break;
				}
				currentNode = nextNode.get();
			}

			if (!failed) {
				return Optional.of(currentNode);
			}
		}
		
		return Optional.empty();
	}
	
	private Optional<Object> load(String key, Object yamlObject) {
		if (yamlObject instanceof Map map) {
			Object value = map.get(key);
			return Optional.ofNullable(value);
		}
		return Optional.empty();
	}
}