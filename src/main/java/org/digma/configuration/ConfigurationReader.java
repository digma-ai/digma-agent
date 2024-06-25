package org.digma.configuration;

import org.jetbrains.annotations.Nullable;

public class ConfigurationReader {

    @Nullable
    public String getEnvOrSystemProperty(String key) {
        String envVal = System.getenv(key);
        if (envVal != null) {
            return envVal;
        }
        return System.getProperty(key);
    }


}
