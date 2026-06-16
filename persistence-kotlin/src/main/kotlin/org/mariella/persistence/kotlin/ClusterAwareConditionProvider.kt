package org.mariella.persistence.kotlin

import org.mariella.persistence.loader.ClusterLoader
import org.mariella.persistence.loader.ClusterLoaderConditionProviderImpl

abstract class ClusterAwareConditionProvider : ClusterLoaderConditionProviderImpl() {
    protected lateinit var clusterLoader: ClusterLoader

    override fun initialize(clusterLoader: ClusterLoader) {
        this.clusterLoader = clusterLoader
    }
}