package placement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import placement.core.Service;

public class GenerateServices {

    private List<Service> services;
    private Logger logger = LoggerFactory.getLogger(App.class);
    private Random rnd = new Random();

    private enum ServiceType {
        URLLC("1"), 
        EMBB("2"), 
        MMTC("3");
        
        private final String text;

        ServiceType(final String text){
            this.text = text;
        }

        @Override
        public String toString(){
            return text;
        }
    }
    
    public GenerateServices() {
        this.services = new ArrayList<Service>();
        long millis = Instant.now().toEpochMilli();
        this.rnd.setSeed(millis);
        
        // Service svc1 = new Service(1, Service.ServiceType.URLLC, "URLLC_RO1", 4.0);
        // Service svc2 = new Service(2, Service.ServiceType.URLLC, "URLLC_RO2", 25.0);
        // Service svc3 = new Service(3, Service.ServiceType.URLLC, "URLLC_AW1", 100.0);
        Service svc4 = new Service(4, Service.ServiceType.URLLC, "URLLC_AW2", 1000.0);
        Service svc5 = new Service(5, Service.ServiceType.EMBB, "eMBB1", 10000.0);
        // Service svc6 = new Service(6, Service.ServiceType.EMBB, "eMBB2", 20000.0);
        // Service svc7 = new Service(7, Service.ServiceType.MMTC, "mMTC1", 1.0);
        Service svc8 = new Service(8, Service.ServiceType.MMTC, "mMTC2", 5.0);
        // this.services.add(svc1);
        // this.services.add(svc2);
        // this.services.add(svc3);
        this.services.add(svc4);
        this.services.add(svc5);
        // this.services.add(svc6);
        // this.services.add(svc7);
        this.services.add(svc8);
    }

    public List<Service> generateServices(int nServices) {
        List<Service> randomServices = new ArrayList<Service>();
        for (int i = 0; i < nServices; i++) {
            logger.info("Generating service {}.", String.valueOf(i + 1));
            randomServices.add(this.services.get(this.rnd.nextInt(this.services.size())));
        }
        return randomServices;
    }

    public List<Service> generateServicesByLoad(int load, String serviceType) {
        int generatedLoad = 0;
        int serviceCounter = 0;
        List<Service> randomServices = new ArrayList<Service>();
        ArrayList<Service> filteredServices = new ArrayList<Service>();
        Service.ServiceType srvTypeToGenerate = null;
        switch (serviceType) {
            case "URLLC":
                srvTypeToGenerate = Service.ServiceType.URLLC;
                break;

            case "eMBB":
                srvTypeToGenerate = Service.ServiceType.EMBB;
                break;

            case "mMTC":
                srvTypeToGenerate = Service.ServiceType.EMBB;
                break;

            default:
                filteredServices.addAll(this.services);
        }

        if (srvTypeToGenerate != null){
            for (Service service : this.services) {
                if(service.getDmClassServiceType().equals(srvTypeToGenerate)){
                    filteredServices.add(service);
                }
            }
        }
        while (generatedLoad < load) {
            logger.info("Generating service {}. The current network load is {}", String.valueOf(serviceCounter + 1), String.valueOf(generatedLoad));
            Service svc = filteredServices.get(this.rnd.nextInt(filteredServices.size()));
            randomServices.add(svc);
            generatedLoad = generatedLoad + svc.getDmBandwidth(true).intValue();
            serviceCounter++;
        }
        return randomServices;
    }

    public List<Service> generateServices(int nServices, int factor) {
        List<Service> randomServices = new ArrayList<Service>();
        for (int i = 0; i < nServices; i++) {
            logger.info("Generating service {}.", String.valueOf(i + 1));
            Service generatedService = this.services.get(this.rnd.nextInt(this.services.size()));
            generatedService.setDmBandwidth((Double) generatedService.getDmBandwidth(true) * Double.valueOf(factor) / 100.0);
            randomServices.add(generatedService);
        }
        return randomServices;
    }

    public void execute(){
        this.execute(100, 10);
    }

    public void execute(int nDemands, int maxRep){
        String folderName = "./dem/";
        String fileName = new String();
        // String strSlices;
        List<String> stubContent = this.readStubFile();
        List<String> demandsStrArr = new ArrayList<String>();
        List<Service> origServices = this.generateServices(nDemands);
        List<String> srcNodesList = new ArrayList<String>();
        List<String> dstNodesList = new ArrayList<String>();
        List<String> availableDstNodes = new ArrayList<String>();
        availableDstNodes.add("1");
        availableDstNodes.add("2");
        availableDstNodes.add("3");
        
        // clean demands folder
        this.cleanOldDemands(folderName);
        for (int rep = 1; rep <= maxRep; rep++) {
            // generate list of source nodes
            srcNodesList.clear();
            for (int i = 0; i < nDemands; i++) {
                srcNodesList.add(String.valueOf(this.rnd.nextInt(8) + 9));
                // Destination nodes
                dstNodesList.add(availableDstNodes.get(this.rnd.nextInt(availableDstNodes.size())));
            }
            demandsStrArr.clear();
            List<String> generatedStrSvc = servicesToStrArr(origServices, srcNodesList, dstNodesList);
            demandsStrArr.addAll(generatedStrSvc);
            fileName = folderName + "icc_" + nDemands + "-" + String.format("%02d", rep) + ".yml";
            System.out.println("# Generating file" + fileName + ".");
            saveToFile(stubContent, demandsStrArr, fileName);
        }
    }

    public void executeScaling(int nDemands, int maxRep){
        String folderName = "./dem/";
        String fileName = new String();
        // String strSlices;
        String strFactor;
        Integer factor;
        List<String> stubContent = this.readStubFile();
        List<Integer> sliceScaleFactor = Arrays.asList(100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 350, 375, 400);
        List<String> demandsStrArr = new ArrayList<String>();
        List<Service> origServices = this.generateServices(nDemands);
        List<String> srcNodesList = new ArrayList<String>();
        List<String> dstNodesList = new ArrayList<String>();
        List<String> availableDstNodes = new ArrayList<String>();
        // clean demands folder
        availableDstNodes.add("1");
        availableDstNodes.add("2");

        this.cleanOldDemands(folderName);
        for (int rep = 1; rep <= maxRep; rep++) {
            // generate list of source nodes
            srcNodesList.clear();
            for (int i = 0; i < nDemands; i++) {
                //  First Access Node = 25
                // Number of Access Nodes = 39 (39 + 1 bc bound is not included) = 40
                srcNodesList.add(String.valueOf(this.rnd.nextInt(8) + 9));
                // Destination nodes
                dstNodesList.add(availableDstNodes.get(this.rnd.nextInt(availableDstNodes.size())));
            }
            for (int i = 0; i < sliceScaleFactor.size(); i++) {
                List<Service> generatedSvc = new ArrayList<Service>();
                demandsStrArr.clear();
                factor = sliceScaleFactor.get(i);
                for (int j = 0; j < origServices.size(); j++) {
                    Service svc2 = origServices.get(j).clone();
                    // Only scale up demands of type URLLC and eMBB
                    if (svc2.getDmClass().equals(ServiceType.URLLC.toString()) || svc2.getDmClass().equals(ServiceType.EMBB.toString())) {
                        svc2.setDmBandwidth(Math.ceil(origServices.get(j).getDmBandwidth(true) * factor / 100));
                    }
                    generatedSvc.add(svc2);
                }
                List<String> generatedStrSvc = servicesToStrArr(generatedSvc, srcNodesList, dstNodesList);
                demandsStrArr.addAll(generatedStrSvc);
                strFactor = String.format("%03d", factor);
                fileName = folderName + "icc_" + strFactor + "-" + String.format("%02d", rep) + ".yml";
                System.out.println("# Generating file" + fileName + ".");
                saveToFile(stubContent, demandsStrArr, fileName);
            }
        }
    }

    public void executeLoad(int load, int maxRep, String serviceType){
        String folderName = "./dem/";
        String fileName = new String();
        // String strSlices;
        List<String> stubContent = this.readStubFile();
        List<String> demandsStrArr = new ArrayList<String>();
        List<Service> origServices = this.generateServicesByLoad(load, serviceType);
        List<String> srcNodesList = new ArrayList<String>();
        List<String> dstNodesList = new ArrayList<String>();
        List<String> availableDstNodes = new ArrayList<String>();
        availableDstNodes.add("1");
        availableDstNodes.add("2");
        availableDstNodes.add("3");
        
        // clean demands folder
        // this.cleanOldDemands(folderName);
        for (int rep = 1; rep <= maxRep; rep++) {
            // generate list of source nodes
            srcNodesList.clear();
            for (int i = 0; i < origServices.size(); i++) {
                srcNodesList.add(String.valueOf(this.rnd.nextInt(8) + 9));
                // Destination nodes
                dstNodesList.add(availableDstNodes.get(this.rnd.nextInt(availableDstNodes.size())));
            }
            demandsStrArr.clear();
            List<String> generatedStrSvc = servicesToStrArr(origServices, srcNodesList, dstNodesList);
            demandsStrArr.addAll(generatedStrSvc);
            serviceType = !serviceType.isEmpty() ? serviceType : "MIX";
            fileName = folderName + "icc_l" + String.format("%d", load/1000) + "-" + serviceType + String.format("%02d", rep) + ".yml";
            System.out.println("# Generating file" + fileName + ".");
            saveToFile(stubContent, demandsStrArr, fileName);
        }
    }

    private void cleanOldDemands(String folderName) {
        File dir = new File(folderName);
        logger.info("Purging files in directory: {}", folderName);
        try{
            for (File file: dir.listFiles()){
                if (!file.isDirectory()){
                    logger.debug("Erasing file {}", file.getName());
                    file.delete();
                }
            }
        } catch (NullPointerException e){
            logger.info("The folder {} does not exists. Creating the folder", folderName);
            dir.mkdir();
        }
    }

    private void saveToFile(List<String> stubContent, List<String> servicesToStrArr, String fileName) {
        File file = new File(fileName);
        file.setWritable(true);
        file.setReadable(true);
        try {
            FileWriter fileWritter = new FileWriter(file);
            for (String string : stubContent) {
                fileWritter.write(string);
            }
            for (String string : servicesToStrArr) {
                fileWritter.write(string);
            }
            fileWritter.close();
            logger.info("Demands written to file {}.", file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Impossible to write to file.", e);
            e.printStackTrace();
        }

    }

    private List<String> readStubFile() {
        List<String> content = new ArrayList<String>();
        String fileName = "icc_stub.yml";
        File file = new File(fileName);
        String line;
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(file));
            while ((line = buffer.readLine()) != null) {
                content.add(line + System.lineSeparator());
            }
            buffer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return content;
    }

    private void printDemands(List<String> servicesStr) {
        for (String line : servicesStr) {
            System.out.print(line);
        }
    }

    public List<String> servicesToStrArr(List<Service> services) {
        List<String> output = new ArrayList<String>();
        String srcNode;
        for (Service service : services) {
            srcNode = String.valueOf(this.rnd.nextInt(30) + 21);
            output.add("  - demands: [" + service.getDmBandwidth() + "]" + System.lineSeparator());
            output.add("    services: [" + service.getDmClass() + "]" + System.lineSeparator());
            output.add("    service_length: [0]" + System.lineSeparator());
            output.add("    src: '" + srcNode + "'" + System.lineSeparator());
            output.add("    dst: '99'" + System.lineSeparator());
        }
        return output;
    }

    public List<String> servicesToStrArr(List<Service> services, List<String> srcNodeList, List<String> dstNodeList) {
        List<String> output = new ArrayList<String>();
        for (int i=0; i < services.size(); i++) {
            Service service = services.get(i);
            String srcNode = srcNodeList.get(i);
            String dstNode = dstNodeList.get(i);
            output.add("  - demands: [" + service.getDmBandwidth() + "]" + System.lineSeparator());
            output.add("    services: [" + service.getDmClass() + "]" + System.lineSeparator());
            output.add("    service_length: [0]" + System.lineSeparator());
            output.add("    src: '" + srcNode + "'" + System.lineSeparator());
            output.add("    dst: '" + dstNode + "'" + System.lineSeparator());
        }
        return output;
    }
}
