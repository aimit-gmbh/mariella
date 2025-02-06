package org.mariella.test.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mariella.persistence.persistor.ClusterDescription;
import org.mariella.persistence.schema.ClassDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Cluster<T, I> {
	public static final Logger logger = LoggerFactory.getLogger(Cluster.class);
	
	public static class ExistingCluster<T, I> implements Cluster<T, I> {
		private MariellaUtil mariella;
		private ClassDescription rootDescription;
		private I id;

		private Set<String> loadedPathExpressions = new HashSet<>();
		private List<String> requiredPathExpressions = new ArrayList<>();

		protected T root;

		public ExistingCluster(MariellaUtil mariella, Class<?> rootClass, I id) {
			this.mariella = mariella;
			rootDescription = mariella.getClassDescription(rootClass);
			this.id = id;
			requiredPathExpressions.add("root");
		}
		@Override
		public I getId() {
			return id;
		}
		@Override
		public Cluster<T, I> required(ClusterBuilder cb) {
			for(String pathExpression : cb.getClusterDescriptionPathExpressions()) {
				required(pathExpression);
			}
			return this;
		}
		@Override
		public Cluster<T, I> required(String...pathExpressions) {
			for(String pathExpression : pathExpressions) {
				String pe = "root." + pathExpression;
				if(!requiredPathExpressions.contains(pe) && !loadedPathExpressions.contains(pe)) {
					requiredPathExpressions.add(pe);
				}
			}
			return this;
		}
		@Override
		@SuppressWarnings("unchecked")
		public T get() {
			if(!requiredPathExpressions.isEmpty()) {
				logger.atTrace()
					.addArgument(rootDescription.getClassName())
					.addArgument(id)
					.addArgument(requiredPathExpressions)
					.log("loading cluster {} ({}). pathExpressions: {}");
				Collections.sort(requiredPathExpressions);
				String[] pes = requiredPathExpressions.toArray(new String[requiredPathExpressions.size()]);
				this.root = (T)mariella.loadById(
					new ClusterDescription(rootDescription, pes),
					id,
					false
				);
				loadedPathExpressions.addAll(requiredPathExpressions);
				requiredPathExpressions = new ArrayList<>();
			}
			return root;
		}
	}
	
	public static class LoadedCluster<T, I> implements Cluster<T, I> {
		protected T root;
		protected I id;
		
		public LoadedCluster(I id, T root) {
			this.id = id;
			this.root = root;
		}
		@Override
		public Cluster<T, I> required(ClusterBuilder clusterBuilder) {
			return this;
		}
		@Override
		public Cluster<T, I> required(String... pathExpressions) {
			return this;
		}
		@Override
		public T get() {
			return root;
		}
		@Override
		public I getId() {
			return id;
		}
	}
	
	public static class DelegatingCluster<T, I> implements Cluster<T, I> {
		private Cluster<T, I> delegate;
		public DelegatingCluster(Cluster<T, I> delegate) {
			this.delegate = delegate;
		}
		@Override
		public Cluster<T, I> required(ClusterBuilder clusterBuilder) {
			delegate.required(clusterBuilder);
			return this;
		}
		@Override
		public Cluster<T, I> required(String... pathExpressions) {
			delegate.required(pathExpressions);
			return this;
		}
		@Override
		public T get() {
			return delegate.get();
		}
		@Override
		public I getId() {
			return delegate.getId();
		}
	}
	
public Cluster<T, I> required(String...pathExpressions);
public Cluster<T, I> required(ClusterBuilder clusterBuilder);
public T get();
public I getId();
}
