package com.lognet.automation.server;

public interface IServerManager {

    void deploy() throws Exception;

    void undeploy() throws Exception;

    void completeDeployment() throws Exception;

    void productionRedeployment() throws Exception;

    void startServers() throws Exception;

    void stopServers() throws Exception;
}