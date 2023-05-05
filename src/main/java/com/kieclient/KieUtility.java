package com.kieclient;

import java.util.List;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ServiceResponse;

public class KieUtility {

    private static final Logger logger = LogManager.getLogger(KieUtility.class);
    private static final String URL = "http://localhost:8080/kie-server/services/rest/server";
    private static final String USER = "rhpamAdmin";
    private static final String PASSWORD = "Surendhar3298$";

    private static final MarshallingFormat FORMAT = MarshallingFormat.JSON;

    public KieServicesClient getKieServicesClient() {
        KieServicesConfiguration conf = KieServicesFactory.newRestConfiguration(URL, USER, PASSWORD);
        conf.setMarshallingFormat(FORMAT);
        return KieServicesFactory.newKieServicesClient(conf);
    }

    public static void main(String[] args) {
        KieUtility kieUtility = new KieUtility();
        KieServicesClient kieServicesClient = kieUtility.getKieServicesClient();
        List<KieContainerResource> kieContainers = kieServicesClient.listContainers().getResult().getContainers();
        if (kieContainers.size() == 0) {
            System.out.println("No containers available...");
            return;
        }
        for (KieContainerResource kieContainerResource : kieContainers) {
            System.out.println(kieContainerResource.getContainerId());
        }
        SLATrackingCommand slatCommand = new SLATrackingCommand();
        slatCommand.executeMethod();
    }
}
