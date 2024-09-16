package org.digma.jdbc;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

//todo: not used , keep as example
public class PreparedStatementQueryListener implements QueryExecutionListener {

    private ExecutionInfo beforeExecInfo;
    private ExecutionInfo afterExecInfo;
    private List<QueryInfo> beforeQueries = new ArrayList<>();
    private List<QueryInfo> afterQueries = new ArrayList<>();


    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        StringBuilder sb = new StringBuilder();
        for (QueryInfo queryInfo : queryInfoList) {
            sb.append(queryInfo.getQuery());
            sb.append("[");
            for (List<ParameterSetOperation> parameter : queryInfo.getParametersList()) {
                sb.append("[");
                for(ParameterSetOperation param : parameter){
                    sb.append(param.getArgs()[1]);
                    sb.append(",");
                }
                sb.append("]");
            }
            sb.append("]");
        }

        System.out.println("in PreparedStatementQueryListener.beforeQuery "+"queryInfoList="+ sb);
        beforeExecInfo = execInfo;
        beforeQueries = queryInfoList;
    }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        StringBuilder sb = new StringBuilder();
        for (QueryInfo queryInfo : queryInfoList) {
            sb.append(queryInfo.getQuery());
            sb.append("[");
            for (List<ParameterSetOperation> parameter : queryInfo.getParametersList()) {
                sb.append("[");
                for(ParameterSetOperation param : parameter){
                    sb.append(param.getArgs()[1]);
                    sb.append(",");
                }
                sb.append("]");
            }
            sb.append("]");
        }
        System.out.println("in PreparedStatementQueryListener.afterQuery "+"queryInfoList="+ sb);
        afterExecInfo = execInfo;
        afterQueries = queryInfoList;
    }

    public ExecutionInfo getBeforeExecInfo() {
        return beforeExecInfo;
    }

    public ExecutionInfo getAfterExecInfo() {
        return afterExecInfo;
    }

    public List<QueryInfo> getBeforeQueries() {
        return beforeQueries;
    }

    public List<QueryInfo> getAfterQueries() {
        return afterQueries;
    }


    @Nullable
    public String buildQueryWithParameters() {

        if (!beforeQueries.isEmpty() && !beforeQueries.get(0).getParametersList().isEmpty()) {
            QueryInfo queryInfo = beforeQueries.get(0);
            List<ParameterSetOperation> parameters = queryInfo.getParametersList().get(0);
            final StringBuilder sb = new StringBuilder();
            final String statementQuery = queryInfo.getQuery();

            // iterate over the characters in the query replacing the parameter placeholders
            // with the actual values
            int currentParameter = 0;
            for (int pos = 0; pos < statementQuery.length(); pos++) {
                char character = statementQuery.charAt(pos);
                if (character == '?' && currentParameter < parameters.size()) {
                    // replace with parameter value
                    ParameterSetOperation param = parameters.get(currentParameter);
                    sb.append(valueToSqlString(param));
                    currentParameter++;
                } else {
                    sb.append(character);
                }
            }
            return sb.toString();
        }
        return null;
    }

    private String valueToSqlString(ParameterSetOperation param) {
        //todo: convert to sql
        Object value = param.getArgs()[1];
        return value == null ? "NULL" : value.toString();
    }


    public String getQueryParameters() {

        final StringBuilder sb = new StringBuilder();

        for (QueryInfo queryInfo : beforeQueries) {
            sb.append("(");
            for (List<ParameterSetOperation> parameters : queryInfo.getParametersList()) {
                sb.append("[");
                for (ParameterSetOperation parameter : parameters) {
                    sb.append(parameter.getArgs()[1]);
                    sb.append(",");
                }
                sb.append("]");
            }
            sb.append(")");
        }
        return sb.toString();
    }


}
