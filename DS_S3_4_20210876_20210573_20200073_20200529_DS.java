/*
 *==========================================================*
 *                                                          *
 *             Operating Systems - Assignment-2             *
 *                      Router System                       *
 *                                                          *
 * ----------------------------------------------------------
 *                                                          *
 *          Author1.Name() = Ali Mohammed Abduljabbar;      *
 *                  Author1.ID() = 20210876;                *
 *          Author2.Name() = Yasmen Amr Abdelhady;          *
 *                  Author2.ID() = 20210573;                *
 *          Author3.Name() = Esraa Hamdy Ali;               *
 *                  Author3.ID() = 20200073;                *
 *          Author4.Name() = Mariam Mohammed AbdulGhani;    *
 *                  Author4.ID() = 20200529                 *
 *                                                          *
 * ---------------------------------------------------------*
 *                                                          *
 *                   Start Date: 14-11-2023                 *
 *                   Last Modified: 24-11-2023              *
 *                   Version: 1.0V                          *
 *                                                          *
 * =========================================================*
 */

 //Change the file name into " Network " to make sure it's run :D


import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.io.PrintStream;

class CustomSemaphore {
    private int permits; // number of avaible Connections in router
    private final Object lock = new Object(); // when connection is finished, it's occupped another

    public CustomSemaphore(int permits) {
        this.permits = permits;// set value for it
    }

    public void acquire() throws InterruptedException {// for connect a new device
        synchronized (lock){
            while (permits == 0) {// if there are no connection avaible, let the user wait
                lock.wait();
            }
            permits--; // if there is a space
        }
    } 
    // if there is any device logged out, increasing the premits
    public void release() {
        synchronized (lock) {
            permits++;
            lock.notify();
        }
    }
}

class Router {
     // max number of connection in each device
    private final int MaxNumberOfDeviceConnect;
    private final CustomSemaphore semaphore;
    private final Map<Integer, List<String>> CurrentConnections;
    private final Object connectionsLock = new Object();
    private int nextConnectionID = 1;
    private final PrintStream out;

    public Router(int MaxNumberOfDeviceConnect, PrintStream out) {
        this.MaxNumberOfDeviceConnect = MaxNumberOfDeviceConnect;
        this.semaphore = new CustomSemaphore(MaxNumberOfDeviceConnect);
        this.out = out;
        this.CurrentConnections = new HashMap<>();
    }

    public void connect(String DeviceName) throws InterruptedException {
        semaphore.acquire(); // to add a device if there is a space avaible
        int ConnectionID = assignConnection(); // for example, conenction 1, 2, 3....
       // synchronized (connectionsLock) {
            // list for save attributes for all devices
            List<String> connectionDevices = CurrentConnections.getOrDefault(ConnectionID, new ArrayList<>());
            connectionDevices.add(DeviceName);
            CurrentConnections.put(ConnectionID, connectionDevices);
       // }
        out.println("- Connection " + ConnectionID + ": " + DeviceName + " Occupied");
        out.println("- Connection " + ConnectionID + ": " + DeviceName + " log in");
    }

    public void disconnect(String DeviceName) {
        int ConnectionID = findConnectionID(DeviceName);
        //check if the ID more than 0, that is mean there is still a device in connect so it's can be disconnected
        if (ConnectionID > 0) {
            //synchronized (connectionsLock) {
                List<String> connectionDevices = CurrentConnections.get(ConnectionID);
                connectionDevices.remove(DeviceName);// remove it from the list
                if (connectionDevices.isEmpty()) { 
                    CurrentConnections.remove(ConnectionID);
                }
           // }
            out.println("- Connection " + ConnectionID + ": " + DeviceName + " Logged out");
            semaphore.release(); // release it using the semaphore release
        }
    }
    public void performOnlineActivity(String DeviceName) throws InterruptedException {
        int ConnectionID = findConnectionID(DeviceName);
        //check if the ID more than 0, that is mean there is still a device in connect so it's can be disconnected
        if (ConnectionID > 0) {
            out.println("- Connection " + ConnectionID + ": " + DeviceName + " performs online activity");
            Thread.sleep(new Random().nextInt(5000) + 1000);
        }
    }

    private int assignConnection() {
        synchronized (connectionsLock) {
            return (nextConnectionID++ % MaxNumberOfDeviceConnect) + 1;
        }
    }



    private int findConnectionID(String DeviceName) {
        synchronized (connectionsLock) {
            for (Map.Entry<Integer, List<String>> entry : CurrentConnections.entrySet()) {
                if (entry.getValue().contains(DeviceName)) {
                    return entry.getKey();
                }
            }
        }
        return -1;
    }
}

class Device extends Thread {
    private final String name;
    private final String type;
    private final Router router;
    private final PrintStream out;

    public Device(String name, String type, Router router, PrintStream out) {
        this.name = name;
        this.type = type;
        this.router = router;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            out.println("- " + name + " (" + type + ") arrived");
            router.connect(name);
            router.performOnlineActivity(name);
            router.disconnect(name);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


// Network Class
public class DS_S3_4_20210876_20210573_20200073_20200529_DS {
    public static void main(String[] args) throws IOException {
        PrintStream OutputFile = new PrintStream("output.txt");

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the max number that WI-FI could connect: ");
        int MaxNumberOfDeviceConnect = scanner.nextInt();

        System.out.println("Enter the number of devices that client want to connect: ");
        int totalDevices = scanner.nextInt();

        Router router = new Router(MaxNumberOfDeviceConnect, OutputFile);
        List<Device> devices = new ArrayList<>();

        for (int i = 1; i <= totalDevices; i++) {
            System.out.println("Name of device " + i + ":");
            String DeviceName = scanner.next();

            System.out.println("Type of device " + i + ":");
            String deviceType = scanner.next();
            System.out.println("");
            Device device = new Device(DeviceName, deviceType, router, OutputFile);
            devices.add(device);
        }
        for (Device device : devices) {
            device.start();
        }
        for (Device device : devices) {
            try {
                device.join();// wait for each device to complete before moving on
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Bye :D");
        scanner.close();
        OutputFile.close();
    }
}
