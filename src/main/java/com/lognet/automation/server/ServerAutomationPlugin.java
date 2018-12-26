package com.lognet.automation.server;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ServerAutomationPlugin implements Plugin<Project> {

    public void apply(Project project) {
        project.getTasks().create("serverManagerTask", ServerAutomationTask.class, (task) -> {
            task.setProtocol("");
            task.setHost("");
            task.setPort("");
            task.setUsername("");
            task.setPassword("");
            task.setTargets("");
            task.setApplicationName("");
            task.setFileSource("");
            task.setAction("");
            task.setModuleType("");
            task.setServerType("");
        });
    }
}
