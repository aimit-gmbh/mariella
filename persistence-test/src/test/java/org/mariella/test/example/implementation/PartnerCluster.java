package org.mariella.test.example.implementation;

import java.util.UUID;

import org.mariella.test.common.Cluster;
import org.mariella.test.common.ClusterBuilder;
import org.mariella.test.common.Cluster.DelegatingCluster;
import org.mariella.test.model.Partner;

public abstract class PartnerCluster<T extends Partner> extends DelegatingCluster<T, UUID> {
	public static ClusterBuilder cbWithPhoneNumbers(Class<?> cls) {
		return new ClusterBuilder(cls)
			.addPathExpressions(
				"phoneNumbers"
			);
	}

	public static ClusterBuilder cbWithMailAddresses(Class<?> cls) {
		return new ClusterBuilder(cls)
			.addPathExpressions(
				"mailAddresses"
			);
	}
	public static ClusterBuilder cbFull(Class<?> cls) {
		return new ClusterBuilder(cls)
			.addPathExpressions(cbWithMailAddresses(cls))
			.addPathExpressions(cbWithPhoneNumbers(cls));
	}
	

	public PartnerCluster(Cluster<T, UUID> delegate) {
		super(delegate);
	}

	protected abstract Class<?> getEntityClass();
	
	public PartnerCluster<T> withPhoneNumbers() {
		required(cbWithPhoneNumbers(getEntityClass()));
		return this;
	}

	public PartnerCluster<T> withMailAddresses() {
		required(cbWithMailAddresses(getEntityClass()));
		return this;
	}

}
