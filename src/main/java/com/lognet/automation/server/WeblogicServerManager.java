package com.lognet.automation.server;


import java.io.*;
import java.util.*;
import java.util.List;

import weblogic.deploy.api.tools.*;  //SesionHelper
import weblogic.deploy.api.spi .*;  //WebLogicDeploymentManager
import weblogic.deploy.api.spi.DeploymentOptions;
import weblogic.management.jmx.MBeanServerInvocationHandler;
import weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean;
import weblogic.management.runtime.DomainRuntimeMBean;
import weblogic.management.runtime.ServerLifeCycleRuntimeMBean;
import weblogic.management.runtime.ServerLifeCycleTaskRuntimeMBean;
import weblogic.server.ServerLifecycleException;

import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.Target;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;

public class WeblogicServerManager extends BaseServerManager {

    /*
    * deploys a given application onto the given targets of a domain
    * */
    @Override
    public void deploy() throws Exception {

        File toDeploy = new File(fileSource);
        System.out.println("------------------------------------------------------------");
        DeploymentOptions options = new DeploymentOptions();
        WebLogicDeploymentManager deployManager = SessionHelper.getRemoteDeploymentManager(protocol,host,port,username,password);
        System.out.println("WebLogicDeploymentManager: " + deployManager);
        System.out.println("Is Connected To Admin: " + deployManager.isConnected());
        System.out.println("Deployment Options: "+options);
        System.out.println("file to deploy: " + toDeploy.getAbsolutePath());
        System.out.println("file exists: " + toDeploy.exists());

        System.out.println("Deploying Application On Targets:" + this.targets);
        Target[] targets = deployManager.getTargets();
        Target[] deployTargets = new Target[this.targets.split(",").length];
        int i = 0, j = 0;
        for(i = 0; i < targets.length; i++){
            if(this.targets.contains(targets[i].getName())){
                deployTargets[j] = targets[i];
                j++;
            }
        }

        options.setName(applicationName);
        //deployManager.distribute(deployTargets, new File("C:/Users/alexandra/Desktop/v1/wg-rest-api-0.0.1-SNAPSHOT.war"), null,options);
        deployManager.enableFileUploads();
        ProgressObject processStatus = deployManager.deploy(deployTargets, toDeploy, null,options);
        DeploymentStatus deploymentStatus = processStatus.getDeploymentStatus();

        while(!deploymentStatus.isCompleted() && !deploymentStatus.isFailed()){
            try {
                System.out.println("    Deployment For " + applicationName + " Is " + deploymentStatus.getState());
                deploymentStatus = processStatus.getDeploymentStatus();
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(deploymentStatus.getState().toString().toUpperCase().compareTo("FAILED") == 0){
            throw new Exception(deploymentStatus.getMessage());
        }
        System.out.println("    Deployment For " + applicationName + " Is " + deploymentStatus.getState().toString().toUpperCase());
        deployManager.release();
    }

    /*
    * undeploys retired application that exists on the server with the given name
    * leaving the other application to run
    * */
    @Override
    public void undeployRetired() throws Exception {

        System.out.println("------------------------------------------------------------");
        DeploymentOptions options = new DeploymentOptions();
        WebLogicDeploymentManager deployManager = SessionHelper.getRemoteDeploymentManager(protocol,host,port,username,password);
        System.out.println("WebLogicDeploymentManager: " + deployManager);
        System.out.println("Is Connected To Admin: " + deployManager.isConnected());
        System.out.println("Deployment Options: "+options);

        System.out.println("Undeployin Application On Targets:" + this.targets);
        Target[] targets = deployManager.getTargets();
        Target[] undeployTargets = new Target[this.targets.split(",").length];
        int i = 0, j = 0;
        for(i = 0; i < targets.length; i++){
            if(this.targets.contains(targets[i].getName())){
                undeployTargets[j] = targets[i];
                j++;
            }
        }
        TargetModuleID[] moduleIDS = deployManager.getNonRunningModules(moduleType, undeployTargets);
        TargetModuleID[] deployModules = new TargetModuleID[undeployTargets.length];
        i = 0;
        if(moduleIDS != null) {
            for(TargetModuleID targetModuleID : moduleIDS){
                if(targetModuleID.getModuleID().contains(applicationName) && i < deployModules.length){
                    deployModules[i] = targetModuleID;
                    i++;
                }
            }
            ProgressObject processStatus = deployManager.undeploy(deployModules, options);
            DeploymentStatus deploymentStatus = processStatus.getDeploymentStatus();

            while(!deploymentStatus.isCompleted()){
                try {
                    System.out.println("    Undeployment For " + applicationName + " Is " + deploymentStatus.getState());
                    deploymentStatus = processStatus.getDeploymentStatus();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(deploymentStatus.getState().toString().toUpperCase().compareTo("FAILED") == 0)
                throw new Exception(deploymentStatus.getMessage());
            System.out.println("    Undeployment For " + applicationName + " Is " + deploymentStatus.getState().toString().toUpperCase());
        }
        deployManager.release();
    }

    /*
    * undeploys RUNNING application with the given name from the targets
    * */
    @Override
    public void undeploy() throws Exception {

        System.out.println("------------------------------------------------------------");
        DeploymentOptions options = new DeploymentOptions();
        try {
            WebLogicDeploymentManager deployManager = SessionHelper.getRemoteDeploymentManager(protocol, host, port, username, password);
            System.out.println("WebLogicDeploymentManager: " + deployManager);
            System.out.println("Is Connected To Admin: " + deployManager.isConnected());
            System.out.println("Deployment Options: " + options);

            System.out.println("Undeployin Application On Targets:" + this.targets);
            Target[] targets = deployManager.getTargets();
            Target[] undeployTargets = new Target[this.targets.split(",").length];
            int i = 0, j = 0;
            for (i = 0; i < targets.length; i++) {
                if (this.targets.contains(targets[i].getName())) {
                    undeployTargets[j] = targets[i];
                    j++;
                }
            }
            TargetModuleID[] moduleIDS = deployManager.getRunningModules(moduleType, undeployTargets);
            if (moduleIDS == null) {
                moduleIDS = deployManager.getNonRunningModules(moduleType, undeployTargets);
            }
            TargetModuleID[] deployModules = new TargetModuleID[undeployTargets.length];
            i = 0;
            if (moduleIDS != null) {
                for (TargetModuleID targetModuleID : moduleIDS) {
                    if (targetModuleID.getModuleID().contains(applicationName) && i < deployModules.length) {
                        deployModules[i] = targetModuleID;
                        i++;
                    }
                }
                ProgressObject processStatus = deployManager.undeploy(deployModules, options);
                DeploymentStatus deploymentStatus = processStatus.getDeploymentStatus();

                while (!deploymentStatus.isCompleted()) {
                    try {
                        System.out.println("    Undeployment For " + applicationName + " Is " + deploymentStatus.getState());
                        deploymentStatus = processStatus.getDeploymentStatus();
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (deploymentStatus.getState().toString().toUpperCase().compareTo("FAILED") == 0)
                    throw new Exception(deploymentStatus.getMessage());
                System.out.println("    Undeployment For " + applicationName + " Is " + deploymentStatus.getState().toString().toUpperCase());
            }
            deployManager.release();
        } catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }


    /*
    * does undeployment, deployment, starts, and stops servers.
    * */
    @Override
    public void completeDeployment() throws Exception {
        initConnection();
        undeploy();
        deploy();
        stopServers();
        startServers();
        closeConnection();
    }

    /*
    * does undeployRetired, deploy, starts, and stops the servers
    * */
    @Override
    public void productionRedeployment() throws Exception {
        initConnection();
        undeployRetired();
        deploy();
        //stopServers();
        //startServers();
        closeConnection();
    }

    @Override
    public void restartServers() throws Exception{
        initConnection();
        stopServers();
        startServers();
        closeConnection();
    }



    private static MBeanServerConnection connection;
    private static JMXConnector connector;

    public void initConnection() throws IOException {
        Integer portInteger = Integer.valueOf(port);
        int portInt = portInteger.intValue();
        Hashtable h = new Hashtable();
        h.put(Context.SECURITY_PRINCIPAL,username);
        h.put(Context.SECURITY_CREDENTIALS,password);
        h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES,"weblogic.management.remote");
        JMXServiceURL serviceURL = new JMXServiceURL(protocol,host,portInt, "/jndi/weblogic.management.mbeanservers.domainruntime");
        connector = JMXConnectorFactory.connect(serviceURL,h);
        connection = connector.getMBeanServerConnection();
    }

    public static void closeConnection(){
        try {
            connector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObjectName[] getServerRuntimes() throws Exception {
        return (ObjectName[]) connection.getAttribute(service,"ServerRuntimes");
    }

    public void startServers() throws Exception {

        List<ServerLifeCycleTaskRuntimeMBean> tasks = new ArrayList<>();
        String serverState="UNKNOWN";
        ObjectName domain = (ObjectName) connection.getAttribute(service,"DomainConfiguration");
        //System.out.println("Domain: " + domain.toString());
        System.out.println("------------------------------------------------------------");
        ObjectName[] domainClusters = (ObjectName[]) connection.getAttribute(domain, "Clusters");
        ObjectName[] domainServers = (ObjectName[]) connection.getAttribute(domain, "Servers");
        for(ObjectName domainCluster : domainClusters){
            String cName = (String) connection.getAttribute(domainCluster, "Name");
            if(Arrays.asList(getTargetsAsArray()).contains(cName)){
                ObjectName[] servers = (ObjectName[]) connection.getAttribute(domainCluster, "Servers");
                for (ObjectName server : servers) {
                    String aName = (String) connection.getAttribute(server, "Name");
                    if (!"AdminServer".equals(aName)) {
                        DomainRuntimeServiceMBean domainRuntimeService = (DomainRuntimeServiceMBean) MBeanServerInvocationHandler.newProxyInstance(connection, new ObjectName(DomainRuntimeServiceMBean.OBJECT_NAME));
                        DomainRuntimeMBean domainRuntime = domainRuntimeService.getDomainRuntime();
                        ServerLifeCycleRuntimeMBean serverLifeCycleRuntimeMBean = domainRuntime.lookupServerLifeCycleRuntime(aName);
                        System.out.println("Server: " + aName + " In LifeCycleState : " + serverLifeCycleRuntimeMBean.getState());
                        System.out.println("Starting Servers");
                        try {
                            tasks.add(serverLifeCycleRuntimeMBean.start());

                            Thread.sleep(1500);
                            System.out.println("Server " + aName + " is " + serverLifeCycleRuntimeMBean.getState());
                        } catch (ServerLifecycleException e) {
                            e.printStackTrace();
                            throw e;
                        }
                    }
                }
            }
        }
        for(ObjectName domainServer : domainServers){
            String sName = (String) connection.getAttribute(domainServer, "Name");
            if(Arrays.asList(getTargetsAsArray()).contains(sName)){
                DomainRuntimeServiceMBean domainRuntimeService = (DomainRuntimeServiceMBean) MBeanServerInvocationHandler.newProxyInstance(connection, new ObjectName(DomainRuntimeServiceMBean.OBJECT_NAME));
                DomainRuntimeMBean domainRuntime = domainRuntimeService.getDomainRuntime();
                ServerLifeCycleRuntimeMBean serverLifeCycleRuntimeMBean = domainRuntime.lookupServerLifeCycleRuntime(sName);
                System.out.println("Server: " + sName + " In LifeCycleState : " + serverLifeCycleRuntimeMBean.getState());
                System.out.println("Starting Servers");
                try {
                    tasks.add(serverLifeCycleRuntimeMBean.start());

                    Thread.sleep(1500);
                    System.out.println("Server " + sName + " is " + serverLifeCycleRuntimeMBean.getState());
                } catch (ServerLifecycleException e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        try{
            Thread.sleep(1500);
            waitForProcessToFinish(tasks, ServerLifeCycleRuntimeMBean.RUNNING);
            System.out.println("    Servers Are Running");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void stopServers() throws Exception {
        List<ServerLifeCycleTaskRuntimeMBean> tasks = new ArrayList<>();
        String serverState="UNKNOWN";
        ObjectName arr[]=getServerRuntimes();
        ObjectName domain = (ObjectName) connection.getAttribute(service,"DomainConfiguration");
        //System.out.println("Domain: " + domain.toString());
        System.out.println("------------------------------------------------------------");
        ObjectName[] domainClusters = (ObjectName[]) connection.getAttribute(domain, "Clusters");
        ObjectName[] domainServers = (ObjectName[]) connection.getAttribute(domain, "Servers");
        for(ObjectName domainCluster : domainClusters){
            String cName = (String) connection.getAttribute(domainCluster, "Name");
            if (Arrays.asList(getTargetsAsArray()).contains(cName)) {
                ObjectName[] servers = (ObjectName[]) connection.getAttribute(domainCluster, "Servers");
                for (ObjectName server : servers) {
                    String aName = (String) connection.getAttribute(server, "Name");
                    if (!"AdminServer".equals(aName)) {
                        DomainRuntimeServiceMBean domainRuntimeService = (DomainRuntimeServiceMBean) MBeanServerInvocationHandler.newProxyInstance(connection, new ObjectName(DomainRuntimeServiceMBean.OBJECT_NAME));
                        DomainRuntimeMBean domainRuntime = domainRuntimeService.getDomainRuntime();
                        ServerLifeCycleRuntimeMBean serverLifeCycleRuntimeMBean = domainRuntime.lookupServerLifeCycleRuntime(aName);
                        System.out.println("Server: " + aName + " In LifeCycleState : " + serverLifeCycleRuntimeMBean.getState());
                        System.out.println("Shuting Down Server");
                        tasks.add(serverLifeCycleRuntimeMBean.forceShutdown());

                        Thread.sleep(1500);
                        System.out.println("Server " + aName + " is " + serverLifeCycleRuntimeMBean.getState());
                    }

                }
            }
        }
        for(ObjectName domainServer : domainServers){
            String sName = (String) connection.getAttribute(domainServer, "Name");
            if(Arrays.asList(getTargetsAsArray()).contains(sName)){
                DomainRuntimeServiceMBean domainRuntimeService = (DomainRuntimeServiceMBean) MBeanServerInvocationHandler.newProxyInstance(connection, new ObjectName(DomainRuntimeServiceMBean.OBJECT_NAME));
                DomainRuntimeMBean domainRuntime = domainRuntimeService.getDomainRuntime();
                ServerLifeCycleRuntimeMBean serverLifeCycleRuntimeMBean = domainRuntime.lookupServerLifeCycleRuntime(sName);
                System.out.println("Server: " + sName + " In LifeCycleState : " + serverLifeCycleRuntimeMBean.getState());
                System.out.println("Shuting Down Server");
                tasks.add(serverLifeCycleRuntimeMBean.forceShutdown());

                Thread.sleep(1500);
                System.out.println("Server " + sName + " is " + serverLifeCycleRuntimeMBean.getState());
            }
        }
        try {
            Thread.sleep(1500);
            waitForProcessToFinish(tasks, ServerLifeCycleRuntimeMBean.SHUTDOWN);
            System.out.println("    Servers Are Shutdown");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void waitForTasksToFinish(List<ServerLifeCycleTaskRuntimeMBean> tasks, String desiredOutcome) throws Exception {

        StringBuilder errors = null;
        for(ServerLifeCycleTaskRuntimeMBean task : tasks){
            if(task.getStatus().compareTo("FAILED") == 0){
                if(errors == null)
                    errors = new StringBuilder();
                errors.append("\n" + task.getOperation() + " Failed For Server " + task.getServerName() + "\n");
                continue;
            }
            else{
                while(task.isRunning()){
                    System.out.println("    Task Is Running");
                    try{
                        Thread.sleep(8000);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    continue;
                }
            }
        }
        if(errors != null)
            throw new Exception(errors.toString());
    }

    public void waitForProcessToFinish(List<ServerLifeCycleTaskRuntimeMBean> tasks, String desiredOutcome) throws Exception {

        long start = System.currentTimeMillis();
        long end = start + 360*1000; //give 10 minutes for process to finish
        int finished = tasks.size();//every time a server has finished its task the counter will decrease by one

        if(desiredOutcome.compareTo("SHUTDOWN") == 0){
            waitForTasksToFinish(tasks, "SHUTDOWN");
        }
        else if(desiredOutcome.compareTo("RUNNING") == 0){
            waitForTasksToFinish(tasks, "START");
        }

        /*while(finished != 0 && System.currentTimeMillis() < end){
            System.out.println("    Waiting For Process To Finish");
            for(int i = 0; i < servers.size(); i++){
                ServerLifeCycleTaskRuntimeMBean[] tasks = servers.get(i).getTasks();
                System.out.println("####################################################");
                for(int j = 0; j < tasks.length; j++){
                    System.out.println(tasks[j].getServerName() + " " + tasks[j].getOperation() + " " + tasks[j].getStatus());
                }
                System.out.println("####################################################");
                String state = servers.get(i).getState();
                if(state.startsWith(ServerLifeCycleRuntimeMBean.FAILED) || state.compareTo(ServerLifeCycleRuntimeMBean.UNKNOWN) == 0){
                    System.out.println("-------- Process Failed --------");
                    throw new Exception("Process Failed When Doing " + desiredOutcome);
                }
                else if(state.compareTo(desiredOutcome) == 0){
                    finished--;
                    servers.remove(servers.get(i));
                }
            }
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }
}
