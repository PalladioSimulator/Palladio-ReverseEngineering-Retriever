package org.palladiosimulator.retriever.extraction.commonalities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RESTName implements InterfaceName, OperationName {
    private final String host;
    private final List<String> path;
    private final Set<HTTPMethod> httpMethods;

    public RESTName(final String host, final String path, final Set<HTTPMethod> httpMethods)
            throws IllegalArgumentException {
        this.host = host;
        this.httpMethods = httpMethods;
        final Optional<List<String>> parsedPath = parsePath(host + path);
        if (parsedPath.isEmpty()) {
            throw new IllegalArgumentException("Could not parse path due to illegal format: \"" + path + "\"");
        }
        this.path = parsedPath.get();

        // Keep host name separate
        this.path.remove(0);
    }

    public RESTName(final String host, final String path, final HTTPMethod... httpMethods)
            throws IllegalArgumentException {
        this(host, path, Set.of(httpMethods));
    }

    @Override
    public String getName() {
        return this.getInterface();
    }

    @Override
    public String getInterface() {
        return this.toString();
    }

    @Override
    public Optional<String> forInterface(final String baseInterface) {
        if (!this.isPartOf(baseInterface)) {
            return Optional.empty();
        }

        return Optional.of(this.getInterface());
    }

    @Override
    public List<String> getInterfaces() {
        final Stack<List<String>> prefixes = new Stack<>();

        if (this.path.size() > 0) {
            prefixes.push(List.of(this.path.get(0)));
            for (int i = 1; i < this.path.size(); i++) {
                final List<String> prefix = new ArrayList<>(prefixes.peek());
                prefix.add(this.path.get(i));
                prefixes.push(prefix);
            }
        }

        final List<String> interfaces = new ArrayList<>(prefixes.size());

        if (!this.httpMethods.isEmpty()) {
            interfaces.add(this.getInterface());
        }

        // Insert the prefixes in reverse since the most specific element is at index 0 there.
        while (!prefixes.empty()) {
            interfaces.add(this.toName(prefixes.pop()));
        }

        // Always add root interface
        interfaces.add(this.toName(List.of()));

        return interfaces;
    }

    @Override
    public InterfaceName createInterface(final String name) {
        return parse(name).orElseThrow();
    }

    private String toName(final List<String> path) {
        final StringBuilder name = new StringBuilder();
        name.append("/");
        for (int i = 0; i < path.size(); i++) {
            name.append(path.get(i));
            if (i + 1 < path.size()) {
                name.append("/");
            }
        }
        return this.host + name.toString();
    }

    @Override
    public String toString() {
        final String pathString = this.toName(this.path);

        String methodString = "";
        if (!this.httpMethods.isEmpty()) {
            methodString = "[";
            if (HTTPMethod.areAllPresent(this.httpMethods)) {
                methodString += "*";
            } else {
                List<String> httpMethodNames = this.httpMethods.stream()
                    .map(HTTPMethod::toString)
                    .sorted()
                    .collect(Collectors.toList());
                methodString += String.join(",", httpMethodNames);
            }
            methodString += "]";
        }

        return pathString + methodString;
    }

    private static Optional<List<String>> parsePath(final String string) {
        final String[] segments = string.split("/");

        // Require at least a "/" after the host name
        if (segments.length == 1 && !string.endsWith("/")) {
            return Optional.empty();
        }

        // Remove empty segments.
        return Optional.of(List.of(segments)
            .stream()
            .filter(x -> !x.isEmpty())
            .collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.host, this.path, this.httpMethods);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final RESTName other = (RESTName) obj;
        return Objects.equals(this.host, other.host) && Objects.equals(this.path, other.path)
                && Objects.equals(this.httpMethods, other.httpMethods);
    }

    @Override
    public Optional<String> getCommonInterface(final Name other) {
        if (other instanceof RESTName otherREST) {
            if (!this.host.equals(otherREST.host)) {
                return Optional.empty();
            }

            int commonSegments = 0;
            while (commonSegments < Math.min(this.path.size(), otherREST.path.size()) && this.path.get(commonSegments)
                .equals(otherREST.path.get(commonSegments))) {
                commonSegments++;
            }
            if (commonSegments == 0) {
                return Optional.empty();
            }
            String commonPath = toName(this.path.subList(0, commonSegments));
            Set<HTTPMethod> httpMethodUnion = new HashSet<>();
            httpMethodUnion.addAll(this.httpMethods);
            httpMethodUnion.addAll(otherREST.httpMethods);
            return Optional.of(new RESTName(this.host, commonPath, httpMethodUnion).toString());
        } else {
            // Implementation from Name
            final Set<String> interfaces = new HashSet<>(this.getInterfaces());
            for (final String iface : other.getInterfaces()) {
                if (interfaces.contains(iface)) {
                    return Optional.of(iface);
                }
            }
            return Optional.empty();
        }
    }

    @Override
    public boolean isPartOf(final String iface) {
        Optional<RESTName> parsedIface = parse(iface);
        if (parsedIface.isEmpty()) {
            return false;
        }

        RESTName restIface = parsedIface.get();

        if (!restIface.host.equals(this.host)) {
            return false;
        }
        if (restIface.path.size() > this.path.size()) {
            return false;
        }
        for (int i = 0; i < restIface.path.size(); i++) {
            if (!this.path.get(i)
                .equals(restIface.path.get(i))) {
                return false;
            }
        }
        if (restIface.path.size() == this.path.size() && !restIface.httpMethods.containsAll(this.httpMethods)) {
            return false;
        }
        return true;
    }

    private static Optional<RESTName> parse(final String iface) {
        final String[] parts = iface.split("\\[");
        final Optional<List<String>> interfacePathOption = parsePath(parts[0]);
        if (interfacePathOption.isEmpty()) {
            return Optional.empty();
        }
        final List<String> path = interfacePathOption.get();
        final String host = path.remove(0);

        // If no HTTP method is specified, handle it like a wildcard.
        Set<HTTPMethod> httpMethods = HTTPMethod.all();
        if (parts.length > 1) {
            // Assume that a '[' implies a ']'.
            final int end = parts[1].lastIndexOf(']');
            final String httpMethodNames = parts[1].substring(0, end);

            if (!httpMethodNames.equals("*")) {
                // Narrow the scope.
                httpMethods = Stream.of(httpMethodNames.split(","))
                    .map(HTTPMethod::valueOf)
                    .collect(Collectors.toSet());
            }
        }

        return Optional.of(new RESTName(host, "/" + String.join("/", path), httpMethods));
    }
}