package org.mariella.persistence.jdbc;

import org.mariella.persistence.loader.*;
import org.mariella.persistence.mapping.SchemaMapping;
import org.mariella.persistence.persistor.ClusterDescription;
import org.mariella.persistence.persistor.DatabaseAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class JdbcClusterLoader extends AbstractClusterLoader {

    private static final Logger logger = LoggerFactory.getLogger(JdbcClusterLoader.class);


    public JdbcClusterLoader(SchemaMapping schemaMapping, ClusterDescription clusterDescription) {
        super(schemaMapping, clusterDescription);
    }

    private void setParameters(PreparedStatement ps) {
        JdbcParameterValues pv = new JdbcParameterValues(ps);
        for (ClusterLoaderQueryParameter parameter : queryParameters) {
            parameter.setParameter(pv);
        }
    }

    public List<?> load(DatabaseAccess databaseAccess, final LoaderContext loaderContext,
                        final ClusterLoaderConditionProvider conditionProvider) {
        try {
            return (List<?>) databaseAccess.doInConnection(
                    connection -> {
                        if (logger.isDebugEnabled())
                            logger.debug("loading cluster");
                        long ms = System.currentTimeMillis();
                        loaderContext.startLoading();
                        try {
                            List<Object> result = new ArrayList<>();

                            for (LoadingPolicy lp : loadingPolicies) {
                                clearParameters();
                                LoadingPolicyStatementBuilder statementBuilder = new LoadingPolicyStatementBuilder(lp,
                                        loaderContext, conditionProvider);
                                String sql = statementBuilder.createSelectStatement();
                                long psms = System.currentTimeMillis();
                                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                                    setParameters(ps);
                                    if (logger.isDebugEnabled())
                                        logger.debug(sql);
                                    try (ResultSet rs = ps.executeQuery()) {
                                        LoadingPolicyObjectBuilder lpb = new LoadingPolicyObjectBuilder(lp, loaderContext,
                                                new JdbcResultSetReader(rs));
                                        for (Object m : lpb.createObjects()) {
                                            if (!result.contains(m)) {
                                                result.add(m);
                                            }
                                        }
                                    }
                                } finally {
                                    if (logger.isDebugEnabled())
                                        logger.debug("executed statement in " + (System.currentTimeMillis() - psms) + " ms");
                                }
                            }
                            return result;
                        } finally {
                            loaderContext.finishedLoading();
                            if (logger.isDebugEnabled())
                                logger.debug("loaded cluster in " + (System.currentTimeMillis() - ms) + " ms");
                        }
                    });
        } catch (SQLException e) {
            logger.error("Failed to load cluster", e);
            throw new RuntimeException("Failed to load cluster");
        }
    }


}
