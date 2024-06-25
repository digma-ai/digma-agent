package org.digma.configuration;

import java.util.Map;

public abstract class AbstractConfigurationTest {


    protected Configuration withProperties(Map<String,String> props) {
        return new Configuration(new ConfigurationReader(){
            @Override
            public String getEnvOrSystemProperty(String key) {
                if (props.containsKey(key)) {
                    return props.get(key);
                }

                return super.getEnvOrSystemProperty(key);
            }
        });
    }


}
