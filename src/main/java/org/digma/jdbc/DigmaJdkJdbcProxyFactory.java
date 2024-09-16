package org.digma.jdbc;

import net.ttddyy.dsproxy.ConnectionInfo;
import net.ttddyy.dsproxy.proxy.ProxyConfig;
import net.ttddyy.dsproxy.proxy.jdk.JdkJdbcProxyFactory;
import org.digma.Log;
import org.digma.configuration.Configuration;

import javax.sql.DataSource;
import java.sql.*;

public class DigmaJdkJdbcProxyFactory extends JdkJdbcProxyFactory {


    @Override
    public PreparedStatement createPreparedStatement(PreparedStatement preparedStatement, String query, ConnectionInfo connectionInfo, Connection proxyConnection, ProxyConfig proxyConfig, boolean generateKey) {

        Log.debug("DigmaJdkJdbcProxyFactory.createPreparedStatement for " + query);
        Log.debug("DigmaJdkJdbcProxyFactory.createPreparedStatement wrapping " + preparedStatement.getClass().getName());

        if (Configuration.getInstance().isExposePreparedStatementsParametersEnabled()) {
            proxyConfig = ProxyConfig.Builder.create()
                    .methodListener(new PreparedStatementMethodExecutionListener(query))
                    .jdbcProxyFactory(new DigmaJdkJdbcProxyFactory())
                    .build();
        }

        return super.createPreparedStatement(preparedStatement, query, connectionInfo, proxyConnection, proxyConfig, generateKey);
    }


    @Override
    public DataSource createDataSource(DataSource dataSource, ProxyConfig proxyConfig) {
        return super.createDataSource(dataSource, proxyConfig);
    }

    @Override
    public Connection createConnection(Connection connection, ConnectionInfo connectionInfo, ProxyConfig proxyConfig) {
        return super.createConnection(connection, connectionInfo, proxyConfig);
    }

    @Override
    public Statement createStatement(Statement statement, ConnectionInfo connectionInfo, Connection proxyConnection, ProxyConfig proxyConfig) {
        return super.createStatement(statement, connectionInfo, proxyConnection, proxyConfig);
    }

    @Override
    public CallableStatement createCallableStatement(CallableStatement callableStatement, String query, ConnectionInfo connectionInfo, Connection proxyConnection, ProxyConfig proxyConfig) {
        return super.createCallableStatement(callableStatement, query, connectionInfo, proxyConnection, proxyConfig);
    }

    @Override
    public ResultSet createResultSet(ResultSet resultSet, ConnectionInfo connectionInfo, ProxyConfig proxyConfig) {
        return super.createResultSet(resultSet, connectionInfo, proxyConfig);
    }

    @Override
    public ResultSet createGeneratedKeys(ResultSet resultSet, ConnectionInfo connectionInfo, ProxyConfig proxyConfig) {
        return super.createGeneratedKeys(resultSet, connectionInfo, proxyConfig);
    }
}
