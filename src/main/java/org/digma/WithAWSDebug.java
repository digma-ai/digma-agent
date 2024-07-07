package org.digma;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class WithAWSDebug {

    public static void install(Instrumentation inst) {

        Log.debug("installing WithAWSDebug");

        new AgentBuilder.Default()
                .type(named("software.amazon.awssdk.core.interceptor.ClasspathInterceptorChainFactory"))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {

                    try {

                        Log.debug("transforming " + typeDescription.getCanonicalName() + " in class loader " + classLoader);

                        return builder
                                .method(named("createExecutionInterceptorFromResource"))
                                .intercept(MethodDelegation.to(DigmaInterceptor.class));
                    } catch (Throwable e) {
                        Log.error("got exception in bytebuddy transformer", e);
                        return builder;
                    }

                }).installOn(inst);

    }


    public static class DigmaInterceptor {
        @RuntimeType
        public static Object intercept(@Origin Method method,
                                       @AllArguments Object[] allArguments,
                                       @SuperCall Callable<?> callable) throws Exception {

            List<String> log = new ArrayList<>();
            String url = allArguments.length > 0 ? (String) allArguments[0].toString() : null;
            log.add("************************************");
            Throwable exception = null;
            log.add("in Digma interceptor for method " + method.getName() + ", for url "+url);
            for (Object arg : allArguments) {
                log.add("argument " + arg);
            }


            try {
                return callable.call();
            } catch (Throwable e) {
                exception = e;
                e.printStackTrace();
                log.add("got exception in Digma interceptor for method " + method.getName() + ": " + e +", for url "+url);
                throw e;
            } finally {
                log.add("exit Digma interceptor for method " + method.getName());
                try {
                    log.add("************************************");
                    File logFile = new File(System.getProperty("java.io.tmpdir") + "/digma.log");
                    Files.write(logFile.toPath(), log, StandardOpenOption.CREATE,StandardOpenOption.WRITE,StandardOpenOption.APPEND);
                    for (String s : log) {
                        System.out.println(s);
                    }
                    if (exception != null){
                        PrintWriter writer = new PrintWriter(new FileOutputStream(logFile, true));
                        writer.println("exception stack trace for url " + url);
                        exception.printStackTrace(writer);
                        writer.close();
                    }

                }catch (Throwable e){
                    System.out.println("error in interceptor code");
                    e.printStackTrace();
                }
            }
        }
    }
}
