package com.naveen.test;

import com.sun.tools.attach.VirtualMachine;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.lang.management.MemoryUsage;
import java.util.Set;


/**
 * Created by IntelliJ IDEA.
 * User: naveen
 * Date: 25/2/11
 * Time: 12:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class JMXTest {
    private static final String CONNECTOR_ADDRESS =
            "com.sun.management.jmxremote.localConnectorAddress";

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage JMXTest -pid process-id");
        }
        JMXServiceURL urlForPid = new JMXTest().getURLForPid(args[1]);
        System.out.println("urlForPid = " + urlForPid);
        final JMXConnector connector = JMXConnectorFactory.connect(urlForPid);
        final MBeanServerConnection remote = connector.getMBeanServerConnection();
        Set<ObjectName> objectNames = remote.queryNames(new ObjectName("java.lang:name=PS Eden Space,type=MemoryPool"), null);
        for (ObjectName objectName : objectNames) {
            System.out.println("objectName = " + objectName);
            System.out.println("=================================================================");
            MBeanInfo mBeanInfo = remote.getMBeanInfo(objectName);
            MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
            for (MBeanAttributeInfo attribute : attributes) {
                try
                {
                    Object attributeValue = remote.getAttribute(objectName, attribute.getName());

                    if(attributeValue instanceof CompositeDataSupport)
                    {
                        System.out.println("remote.getAttribute(objectName, " + attribute.getName() +") = " + attributeValue);
                        CompositeDataSupport compositeDataSupport = (CompositeDataSupport) attributeValue;
                        CompositeType compositeType = compositeDataSupport.getCompositeType();
                        Set<String> strings = compositeType.keySet();
                        for (String key : strings) {
                            System.out.println("compositeDataSupport.get(" + key + ") = " + compositeDataSupport.get(key));
                        }
                    }
                }
                catch (Exception e)
                {

                }
            }
            System.out.println("=================================================================");
        }

    }

    private JMXServiceURL getURLForPid(String pid) throws Exception {

        // attach to the target application
        final VirtualMachine vm = VirtualMachine.attach(pid);

        // get the connector address
        String connectorAddress =
                vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);

        // no connector address, so we start the JMX agent
        if (connectorAddress == null) {
            String agent = vm.getSystemProperties().getProperty("java.home") +
                    File.separator + "lib" + File.separator + "management-agent.jar";
            vm.loadAgent(agent);

            // agent is started, get the connector address
            connectorAddress =
                    vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
            assert connectorAddress != null;
        }
        return new JMXServiceURL(connectorAddress);
    }

}
