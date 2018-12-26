package com.lognet.automation.server;

public enum ServerType {
    WEBLOGIC("com.lognet.automation.server.WeblogicServerManager");

    private String serverType;

    ServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getServerType(){
        return this.serverType;
    }
}
