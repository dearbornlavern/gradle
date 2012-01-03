/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.launcher.daemon.client;

import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.Factory;
import org.gradle.launcher.daemon.context.DaemonContext;
import org.gradle.launcher.daemon.context.DaemonContextBuilder;
import org.gradle.launcher.daemon.registry.DaemonDir;
import org.gradle.launcher.daemon.registry.DaemonRegistry;
import org.gradle.launcher.daemon.registry.EmbeddedDaemonRegistry;
import org.gradle.launcher.daemon.server.Daemon;
import org.gradle.launcher.daemon.server.DaemonParameters;
import org.gradle.launcher.daemon.server.DaemonServerConnector;
import org.gradle.launcher.daemon.server.DaemonTcpServerConnector;
import org.gradle.launcher.daemon.server.exec.DaemonCommandExecuter;
import org.gradle.launcher.daemon.server.exec.DefaultDaemonCommandExecuter;
import org.gradle.logging.LoggingServiceRegistry;
import org.gradle.logging.internal.OutputEvent;
import org.gradle.logging.internal.OutputEventListener;
import org.gradle.messaging.concurrent.DefaultExecutorFactory;
import org.gradle.messaging.concurrent.ExecutorFactory;
import org.gradle.os.ProcessEnvironment;

/**
 * Wires together the embedded daemon client.
 */
public class EmbeddedDaemonClientServices extends DaemonClientServicesSupport {

    private final boolean displayOutput;

    public EmbeddedDaemonClientServices() {
        this(LoggingServiceRegistry.newCommandLineProcessLogging(), false);
    }

    private class EmbeddedDaemonFactory implements Factory<Daemon> {
        public Daemon create() {
            return new Daemon(
                get(DaemonServerConnector.class),
                get(DaemonRegistry.class),
                get(DaemonContext.class),
                "password",
                get(DaemonCommandExecuter.class),
                get(ExecutorFactory.class)
            );
        }
    }

    protected DaemonCommandExecuter createDaemonCommandExecuter() {
         return new DefaultDaemonCommandExecuter(getLoggingServices(), get(ExecutorFactory.class), get(ProcessEnvironment.class));
    }

    public EmbeddedDaemonClientServices(ServiceRegistry loggingServices, boolean displayOutput) {
        super(loggingServices, System.in);
        this.displayOutput = displayOutput;
        add(EmbeddedDaemonFactory.class, new EmbeddedDaemonFactory());
    }

    protected DaemonRegistry createDaemonRegistry() {
        return new EmbeddedDaemonRegistry();
    }

    protected OutputEventListener createOutputEventListener() {
        if (displayOutput) {
            return super.createOutputEventListener();
        } else {
            return new OutputEventListener() { public void onOutput(OutputEvent event) {} };
        }
    }

    @Override
    protected void configureDaemonContextBuilder(DaemonContextBuilder builder) {
        builder.setDaemonRegistryDir(new DaemonDir(new DaemonParameters().getBaseDir()).getRegistry());
    }

    protected ExecutorFactory createExecutorFactory() {
        return new DefaultExecutorFactory();
    }

    protected DaemonServerConnector createDaemonServerConnector() {
        return new DaemonTcpServerConnector();
    }

    protected Runnable makeDaemonStarter() {
        return new EmbeddedDaemonStarter((EmbeddedDaemonRegistry)get(DaemonRegistry.class), getFactory(Daemon.class));
    }
}
