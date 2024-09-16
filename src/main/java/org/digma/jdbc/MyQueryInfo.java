package org.digma.jdbc;

import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

import java.util.ArrayList;
import java.util.List;

class MyQueryInfo {
    private String query;
    private final List<ParameterSetOperation> parametersList = new ArrayList<>();

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<ParameterSetOperation> getParametersList() {
        return parametersList;
    }

}
