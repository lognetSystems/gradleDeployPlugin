package com.lognet.automation.server;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class ServerAutomationTask extends DefaultTask {

    private String protocol;
    private String host;
    private String port;
    private String username;
    private String password;
    private String targets;
    private String applicationName;
    private String fileSource;
    private String moduleType;
    private String action;
    private ServerType serverType;

    public String getProtocol(){return protocol;}
    public void setProtocol(String protocol){this.protocol = protocol;}

    public String getHost(){return host;}
    public void setHost(String host){this.host = host;}

    public String getPort(){return port;}
    public void setPort(String port){this.port = port;}

    public String getUsername(){return username;}
    public void setUsername(String username){this.username = username;}

    public String getPassword(){return password;}
    public void setPassword(String password){this.password = password;}

    public String getTargets(){return targets;}
    public void setTargets(String targets){this.targets = targets;}

    public String getApplicationName(){return applicationName;}
    public void setApplicationName(String applicationName){this.applicationName = applicationName;}

    public String getFileSource(){return fileSource;}
    public void setFileSource(String fileSource){this.fileSource = fileSource;}

    public String getAction(){return action;}
    public void setAction(String action){this.action = action;}

    public String getModuleType(){return moduleType;}
    public void setModuleType(String moduleType){this.moduleType = moduleType;}

    public String getServerType(){return serverType.getServerType();}
    public void setServerType(String serverType){
        if(serverType.compareToIgnoreCase(ServerType.WEBLOGIC.toString()) == 0){
            this.serverType = ServerType.WEBLOGIC;
        }
    }

    /*
     * main action that is called from gradle
     * depending on which action was chosen, it run the appropriate function
     * */
    @TaskAction
    public void serverManagerTask() throws Exception {

        BaseServerManager serverManager = (BaseServerManager) BaseServerManager.getClass(serverType.getServerType());
        serverManager.setAll(protocol,host,port,username,password,targets,applicationName,fileSource,moduleType,action);
        switch (action){
            case "productionRedeployment":
                serverManager.productionRedeployment();
                break;
            case "completeDeployment":
                serverManager.completeDeployment();
                break;
            case "undeploy":
                serverManager.undeploy();
                break;
            case "deploy":
                serverManager.deploy();
                break;
            case "undeployRetired":
                serverManager.undeployRetired();
                break;
            case "restartServers":
                serverManager.restartServers();
                break;
        }
    }
}
