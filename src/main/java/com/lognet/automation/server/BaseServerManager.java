package com.lognet.automation.server;

import javax.enterprise.deploy.shared.ModuleType;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class BaseServerManager implements IServerManager {

    protected String protocol;
    protected String host;
    protected String port;
    protected String username;
    protected String password;
    protected String targets;
    protected String applicationName;
    protected String fileSource;
    protected ModuleType moduleType;
    protected String action;

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

    public ModuleType getModuleType(){return moduleType;}
    public void setModuleType(String moduleType){
        if(moduleType.compareTo(ModuleType.WAR.toString()) == 0){
            this.moduleType = ModuleType.WAR;
        }
        else if(moduleType.compareTo(ModuleType.EAR.toString()) == 0){
            this.moduleType = ModuleType.EAR;
        }
        else if(moduleType.compareTo(ModuleType.RAR.toString()) == 0){
            this.moduleType = ModuleType.RAR;
        }
        else if(moduleType.compareTo(ModuleType.CAR.toString()) == 0){
            this.moduleType = ModuleType.CAR;
        }
        else if(moduleType.compareTo(ModuleType.EJB.toString()) == 0){
            this.moduleType = ModuleType.EJB;
        }
    }

    public void setAll(String protocol, String host, String port, String username, String password, String targets,
                       String applicationName, String fileSource, String moduleType, String action){

        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.targets = targets;
        this.applicationName = applicationName;
        this.fileSource = fileSource;
        this.action = action;
        setModuleType(moduleType);

    }

    public BaseServerManager(){
        try {
            service = new ObjectName("com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");
        }catch (MalformedObjectNameException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    public abstract void deploy() throws Exception;

    public abstract void undeploy() throws Exception;

    public abstract void undeployRetired() throws Exception;

    public abstract void completeDeployment() throws Exception;

    public abstract void productionRedeployment() throws Exception;

    public abstract void restartServers() throws Exception;

    public abstract void startServers() throws Exception;

    public abstract void stopServers() throws Exception;

    public static Object getClass(String classPath) {
        Class<?> aClass;
        if("".compareTo(classPath) != 0) {
            try {
                aClass = Class.forName(classPath);
                Constructor<?> constructor= aClass.getDeclaredConstructor();
                return constructor.newInstance();
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ignored) {
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected final ObjectName service;

    public String[] getTargetsAsArray(){
        return targets.split(",");
    }
}
