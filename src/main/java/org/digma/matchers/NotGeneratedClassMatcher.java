package org.digma.matchers;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.utility.JavaModule;

import java.security.ProtectionDomain;

public class NotGeneratedClassMatcher implements AgentBuilder.RawMatcher {

    @Override
    public boolean matches(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, Class<?> classBeingRedefined, ProtectionDomain protectionDomain) {
        if (protectionDomain != null && protectionDomain.getCodeSource() != null){
            return protectionDomain.getCodeSource().getLocation() != null;
        }
        return true;
    }
}
