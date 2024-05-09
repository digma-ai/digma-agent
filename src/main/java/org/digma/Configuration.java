package org.digma;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Configuration {

    private static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY = "digma.autoinstrument.packages";
    private static final String DIGMA_AUTO_INSTRUMENT_PACKAGES_ENV_VAR = "DIGMA_AUTOINSTRUMENT_PACKAGES";

    public List<String> getExtendedObservabilityPackages() {

        String pNames = getEnvOrSystemProperty(DIGMA_AUTO_INSTRUMENT_PACKAGES_SYSTEM_PROPERTY);
        if (pNames == null){
            pNames = getEnvOrSystemProperty(DIGMA_AUTO_INSTRUMENT_PACKAGES_ENV_VAR);
        }
        if (pNames != null){
            return Arrays.asList(pNames.split(";"));
        }else{
            return Collections.emptyList();
        }
    }

    public static String getEnvOrSystemProperty(String entryName) {
        String envVal = System.getenv(entryName);
        if (envVal != null) {
            return envVal;
        }
        return System.getProperty(entryName);
    }


}
