package org.digma.jdbc;

import net.ttddyy.dsproxy.listener.MethodExecutionContext;
import net.ttddyy.dsproxy.listener.MethodExecutionListener;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import net.ttddyy.dsproxy.proxy.StatementMethodNames;
import org.digma.Log;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//this listener is installed only for PreparedStatement , no need to consider methods of CallableStatement
public class PreparedStatementMethodExecutionListener implements MethodExecutionListener {

    //parameters
    private final MyQueryInfo parameters = new MyQueryInfo();
    private final List<MyQueryInfo> batches = new ArrayList<>();
    private final String query;

    public PreparedStatementMethodExecutionListener(String query) {
        this.query = query;
        parameters.setQuery(query);
    }


    @Override
    public void beforeMethod(MethodExecutionContext executionContext) {

        String methodName = executionContext.getMethod().getName();

        Log.debug("PreparedStatementMethodExecutionListener.beforeMethod " + methodName);

        if (StatementMethodNames.METHODS_TO_OPERATE_PARAMETER.contains(methodName)) {
            if (methodName.equals("clearParameters")) {
                clearParameters();
            } else if (StatementMethodNames.BATCH_PARAM_METHODS.contains(methodName)) {
                if (methodName.equals("addBatch")) {
                    addBatch();
                } else if (methodName.equals("clearBatch")) {
                    clearBatch();
                }
            } else if (StatementMethodNames.PARAMETER_METHODS.contains(executionContext.getMethod().getName())) {
                if (executionContext.getMethodArgs().length >= 2 &&
                        executionContext.getMethodArgs()[0] instanceof Integer) {
                    addParameter(executionContext.getMethod(), executionContext.getMethodArgs());
                }
            }
        }
    }

    @Override
    public void afterMethod(MethodExecutionContext executionContext) {

    }

    private void clearBatch() {
        batches.clear();
    }

    private void clearParameters() {
        parameters.getParametersList().clear();
    }


    private void addBatch() {
        MyQueryInfo batch = new MyQueryInfo();
        batch.setQuery(query);
        batch.getParametersList().addAll(parameters.getParametersList());
        batches.add(batch);
        //clean the main parameters after adding a batch, after adding a batch user should call executeBatch and not executeQuery
        parameters.getParametersList().clear();
    }

    private void addParameter(Method method, Object[] args) {
        ParameterSetOperation parameterSetOperation = new ParameterSetOperation(method, args);
        parameters.getParametersList().add(parameterSetOperation);
    }


    //if this method can't build the query or there are no parameters and no batches then it should return null,
    // when null is returned our otel extension will not override the original db.statement span attribute.
    @Nullable
    public String buildQueryWithParameters(String methodName) {
        if (StatementMethodNames.BATCH_EXEC_METHODS.contains(methodName) && !batches.isEmpty()) {
            return buildBatchQueriesWithParameters();
        } else if (StatementMethodNames.QUERY_EXEC_METHODS.contains(methodName) && !parameters.getParametersList().isEmpty()) {
            return buildQueryWithParameters(parameters.getQuery(), parameters.getParametersList());
        } else {
            return null;
        }
    }


    //if this method can't build the query with parameters or there are no batches then return null.
    @Nullable
    private String buildBatchQueriesWithParameters() {

        if (batches.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (MyQueryInfo batch : batches) {
            if (!batch.getParametersList().isEmpty()) {

                String queryWithParams = buildQueryWithParameters(batch.getQuery(), batch.getParametersList());
                if (queryWithParams != null) {
                    sb.append("[");
                    sb.append(queryWithParams);
                    sb.append("]");
                    if (index + 1 < batches.size()) {
                        sb.append(", ");
                    }
                }

                index++;
            }
        }

        if (sb.length() == 0) {
            return null;
        }

        return sb.toString();
    }


    //if this method can't build the query with parameters or there are no parameters then return null.
    @Nullable
    private String buildQueryWithParameters(String query, List<ParameterSetOperation> queryParameters) {
        List<ParameterSetOperation> paramsSortedByIndex = sortParametersByIndex(queryParameters);
        long questionMarks = IntStream.range(0, query.length()).filter(i -> query.charAt(i) == '?').count();
        if (questionMarks != paramsSortedByIndex.size()) {
            return null;
        }

        if (paramsSortedByIndex.isEmpty()) {
            return null;
        }

        String queryWithParams = query;
        for (ParameterSetOperation param : paramsSortedByIndex) {
            if (param.getArgs()[0] instanceof Integer) {
                Object value = param.getArgs()[1];
                String valueAsSqlString = valueToSqlString(value);
                queryWithParams = queryWithParams.replaceFirst("\\?", valueAsSqlString);
            }
        }

        return queryWithParams;
    }

    private String valueToSqlString(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        return "'" + value + "'";
    }


    @Nullable
    public String buildQueryParameters(String methodName) {

        if (StatementMethodNames.BATCH_EXEC_METHODS.contains(methodName) && !batches.isEmpty()) {
            return buildBatchesQueriesParameters();
        } else if (StatementMethodNames.QUERY_EXEC_METHODS.contains(methodName) && !parameters.getParametersList().isEmpty()) {
            return buildQueryParameters(parameters.getParametersList());
        } else {
            return null;
        }
    }

    @Nullable
    private String buildBatchesQueriesParameters() {

        if (batches.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (MyQueryInfo batch : batches) {

            String queryParams = buildQueryParameters(batch.getParametersList());
            if (queryParams != null && !queryParams.isEmpty()) {
                sb.append("[");
                sb.append(queryParams);
                sb.append("]");
                if (index + 1 < batches.size()) {
                    sb.append(", ");
                }
            }

            index++;
        }

        if (sb.length() == 0) {
            return null;
        }

        return sb.toString();
    }


    @Nullable
    private String buildQueryParameters(List<ParameterSetOperation> queryParameters) {

        List<ParameterSetOperation> paramsSortedByIndex = sortParametersByIndex(queryParameters);

        if (paramsSortedByIndex.isEmpty()) {
            return null;
        }


        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (ParameterSetOperation param : paramsSortedByIndex) {
            if (param.getArgs()[0] instanceof Integer) {
                int paramIndex = (int) param.getArgs()[0];
                Object value = param.getArgs()[1];
                sb.append(paramIndex).append("=").append(value.toString());
                if (index + 1 < paramsSortedByIndex.size()) {
                    sb.append(",");
                }
                index++;
            }
        }
        return sb.toString();
    }


    private List<ParameterSetOperation> sortParametersByIndex(List<ParameterSetOperation> params) {
        return params.stream().sorted((o1, o2) -> {
            if (o1.getArgs()[0] instanceof Integer && o2.getArgs()[0] instanceof Integer) {
                return ((Integer) o1.getArgs()[0]).compareTo((Integer) o2.getArgs()[0]);
            }
            return 0;
        }).collect(Collectors.toList());
    }


}

