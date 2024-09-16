package org.digma.jdbc;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.ttddyy.dsproxy.ConnectionInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.proxy.ProxyConfig;
import net.ttddyy.dsproxy.proxy.ProxyJdbcObject;
import org.digma.Log;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.concurrent.Callable;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class JDBCTrackingTransformer {

    public static void install(Instrumentation inst) {

        Log.debug("installing jdbc tracking transformer.");

        //instrument DataSource
        new AgentBuilder.Default()
                .type(not(isAbstract()).and(implementsInterface(named("javax.sql.DataSource"))))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {

                    try {

                        Log.debug("transforming " + typeDescription.getCanonicalName() + " in class loader " + classLoader);

                        return builder
                                .method(named("getConnection"))
                                .intercept(MethodDelegation.to(DigmaJdbcConnectionInterceptor.class));
                    } catch (Throwable e) {
                        Log.error("got exception in bytebuddy transformer", e);
                        return builder;
                    }

                }).installOn(inst);

        //instrument Driver
        new AgentBuilder.Default()
                .type(not(isAbstract()).and(implementsInterface(named("java.sql.Driver"))))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {

                    try {

                        Log.debug("transforming " + typeDescription.getCanonicalName() + " in class loader " + classLoader);

                        return builder
                                .method(named("connect"))
                                .intercept(MethodDelegation.to(DigmaJdbcConnectionInterceptor.class));
                    } catch (Throwable e) {
                        Log.error("got exception in bytebuddy transformer", e);
                        return builder;
                    }

                }).installOn(inst);

    }


    public static class DigmaJdbcConnectionInterceptor {
        @RuntimeType
        public static Object intercept(@Origin Method method,
                                       @Super Object target,
                                       @AllArguments Object[] allArguments,
                                       @SuperCall Callable<?> callable) throws Exception {


            Log.debug("intercepting "+method.getName()+" for " + target.getClass().getName());

            //NOTE: it's not good to wrap the top most getConnection because the wrapper is a jdk proxy and will not get an otel advice.
            // wrap the bottom most getConnection

            //don't catch exceptions from the original call
            Connection connection = (Connection) callable.call();

            if (connection == null) {
                Log.debug("connection is null");
                return null;
            }

            Log.debug(method.getName()+" for " + target.getClass().getName() + " returned " + connection.getClass().getName());

            try {

                //already wrapped
                if (connection.isWrapperFor(ProxyJdbcObject.class)) {
                    Log.debug("connection " + connection.getClass().getName() + " is already a wrapper for " + ProxyJdbcObject.class.getName());
                    return connection;
                }

                if (ProxyJdbcObject.class.isAssignableFrom(connection.getClass())) {
                    Log.debug("connection " + connection.getClass().getName() + " is already " + ProxyJdbcObject.class.getName());
                    return connection;
                }

                try {
                    //maybe some implementations will not implement isWrapperFor correctly but will implement unwrap correctly.
                    //so try to unwrap
                    ProxyJdbcObject proxyJdbcObject = connection.unwrap(ProxyJdbcObject.class);
                    if (proxyJdbcObject != null) {
                        Log.debug("connection  " + connection.getClass().getName() + " is already a wrapper for " + ProxyJdbcObject.class.getName());
                        return connection;
                    }
                } catch (Throwable e) {
                    //ignore: some implementations will throw an exception if connection is not a wrapper for ProxyJdbcObject
                }


                Log.debug("wrapping connection " + connection.getClass().getName() + " with proxy");
                ConnectionInfo connectionInfo = new ConnectionInfo();
                connectionInfo.setDataSourceName("MyDs");
                connectionInfo.setIsolationLevel(connection.getTransactionIsolation());

                ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                        .queryListener(QueryExecutionListener.DEFAULT)
                        .jdbcProxyFactory(new DigmaJdkJdbcProxyFactory())
                        .build();

                return new DigmaJdkJdbcProxyFactory().createConnection(connection, connectionInfo, proxyConfig);

            } catch (Throwable e) {
                Log.error("error in DigmaJdbcConnectionInterceptor", e);
                return connection;
            }
        }
    }
}
