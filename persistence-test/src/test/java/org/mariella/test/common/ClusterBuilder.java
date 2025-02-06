package org.mariella.test.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mariella.persistence.mapping.SchemaMapping;
import org.mariella.persistence.persistor.ClusterDescription;

public class ClusterBuilder {
	private Class<?> root;
	private Map<String, List<ClusterBuilder>> clusterBuilders = new HashMap<>();
	private List<String> pathExpressions = new ArrayList<String>();

public ClusterBuilder(Class<?> root) {
	this.root = root;
}

public List<String> getPathExpressions() {
	return pathExpressions;
}

public ClusterBuilder addPathExpressions(String...pathExpressions) {
	for(String pathExpression : pathExpressions) {
		this.pathExpressions.add(pathExpression);
	}
	return this;
}

public ClusterBuilder addPathExpressions(String pathExpression, ClusterBuilder clusterBuilder) {
	if(!pathExpressions.contains(pathExpression)) {
		pathExpressions.add(pathExpression);
	}
	List<ClusterBuilder> list = clusterBuilders.get(pathExpression);
	if(list == null) {
		list = new ArrayList<>();
		clusterBuilders.put(pathExpression, list);
	}
	list.add(clusterBuilder);
	return this;
}

public ClusterBuilder addPathExpressions(ClusterBuilder clusterBuilder) {
	addPathExpressions("", clusterBuilder);
	return this;
}

public ClusterDescription getClusterDescription(SchemaMapping schemaMapping) {
	List<String> pes = getClusterDescriptionPathExpressions();
	return new ClusterDescription(
		schemaMapping.getSchemaDescription().getClassDescription(root.getName()),
		pes.toArray(new String[pes.size()])
	);

}

public List<String> getClusterDescriptionPathExpressions() {
	List<String> pes = new ArrayList<String>();
	addPathExpression(pes, "");
	for(String pathExpression : pathExpressions) {
		addPathExpression(pes, pathExpression);
	}
	return pes;
}

private void addPathExpression(List<String> pes, String pathExpression) {
	if(!pathExpression.isEmpty() && !pes.contains(pathExpression)) {
		pes.add(pathExpression);
	}
	List<ClusterBuilder> list = clusterBuilders.get(pathExpression);
	if(list != null) {
		for(ClusterBuilder cb : list) {
			for(String pe : cb.getClusterDescriptionPathExpressions()) {
				if(!pathExpression.isEmpty()) {
					pe = pathExpression + "." + pe;
				}
				if(!pes.contains(pe)) {
					pes.add(pe);
				}
			}
		}
	}
}

}
