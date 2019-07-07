package utfbox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import trabalhofinalsd.telaLogin;
import trabalhofinalsd.telaRegister;
import trabalhofinalsd.telaServidor;
import trabalhofinalsd.telaUsuario;

public class ClientSide {

    static Set<Integer> commandIDs = new HashSet<>();
    static int requestID = 0;
    
    //creating object of socket class which requires IP address of Server and port number 
    //String IpAdd = "192.168.0.181";
    public static String IpAdd = "192.168.0.181";
    public static int portNum = 9999;
    public static int tport = 9998;
    public static Socket soc;
    public static DataInputStream dis;
    public static DataOutputStream dos;
    
    public static void main(String[] args) throws Exception {

        soc = new Socket(IpAdd, portNum);
        dis = new DataInputStream(soc.getInputStream());
        dos = new DataOutputStream(soc.getOutputStream());
        
        String terminal = "Comando> ";
        boolean quit = false;
        
        System.out.println("Connected to server");
        
//        while (!quit) {
//            
//            System.out.print(terminal);
//            Scanner input = new Scanner(System.in);
//            String command[] = input.nextLine().split(" ");
//
//            switch (command[0].toLowerCase()) {
//
//                case "get":
//                    if (command.length > 3) {
//                        if (command[2].endsWith("&")) {
//                            System.out.println("in get case");
//                            new Thread(new clientHandler(IpAdd, portNum, command, "get")).start();
//                        }
//                    } else {
//                        dos.writeUTF("get");
//                        dos.writeUTF(command[1]);
//                        get(dis, soc, command[1], command[2]);
//                        dos.flush();
//                    }
//                    break;
//                case "put":
//                    if (command.length > 3) {
//                        if (command[2].endsWith("&")) {
//                            System.out.println("in put case");
//                            new Thread(new clientHandler(IpAdd, portNum, command, "put")).start();
//                        }
//                    } else {
//                        dos.writeUTF("put");
//                        String path = command[1];
//                        String name = path.substring(path.lastIndexOf('/') + 1);
//                        String pathDownload = command[2];
//                        dos.writeUTF(name);
//                        dos.writeUTF(pathDownload);
//                        put(dis, soc, command[1]);
//                    }
//                    break;
//                case "delete":
//                    dos.writeUTF("delete");
//                    dos.writeUTF(command[1]);
//                    System.out.println("Deleted:" + command[1]);
//                    break;
//
//            }
//        }

        //System.out.println("Connection closed");
    }
    //GET command

    public static void get(DataInputStream dis, Socket soc, String dir) throws Exception {
        //String dir = System.getProperty("user.dir");
        //String newDir = path.substring(path.lastIndexOf('/')+1);
        String stat = dis.readUTF();
        if (stat.equalsIgnoreCase("wait")) {
            System.out.println("The file is being updated currently. Try again once available");
            return;
        }
        int commandID = dis.readInt();
        commandIDs.add(commandID);
        boolean terminated = false;
        long fileSize = dis.readLong();
        System.out.println(dir);
        File getFile = new File(dir);
        FileOutputStream fos = new FileOutputStream(getFile);
        byte[] bytes = new byte[10000000];

        int count = 0;
        int readSum = 0;
        int remaining = (int) fileSize;
        System.out.println("Reading File");
        while ((count = dis.read(bytes, 0, Math.min(bytes.length, remaining))) > 0) {
            readSum += count;
            remaining -= count;
            if (!commandIDs.contains(commandID)) {
                terminated = true;
                break;
            } else {
                fos.write(bytes, 0, count);
            }
        }
        System.out.println("done");
        if (terminated) {
            getFile.delete();
        } else {
            System.out.println("C: File received");
        }
        fos.close();

    }

    //put command
    public static void put(DataInputStream dis, Socket soc, String path) throws Exception {
        //Read from text file 
        System.out.println("C: Writing File");
        boolean terminated = false;
        int commandID = dis.readInt();
        commandIDs.add(commandID);
        String status = dis.readUTF();
        if (status.equalsIgnoreCase("Wait")) {
            System.out.println("File is waitlisted");
        }
        String stat = dis.readUTF();
        System.out.println("stat" + stat);

        File putFile = new File(path);
        DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
        FileInputStream fis = new FileInputStream(putFile);
        long fileSize = putFile.length();
        dos.writeLong(fileSize);
        int asciiValOfChar = 0;
        byte[] bytes = new byte[1000];

        while ((asciiValOfChar = fis.read(bytes)) > 0) {
            if (!commandIDs.contains(commandID)) {
                terminated = true;
            } else {
                dos.write(bytes, 0, asciiValOfChar);
            }
        }
        dos.flush();
        if (terminated) {
            System.out.println("C: Process terminated");
        } else {
            System.out.println("C: File written in Server");
        }
    }
}

class clientHandler extends ClientSide implements Runnable {

    Socket soc = null;
    String ipAdd;
    String currentCommand;
    int portNum;
    String[] command;
    int commandID;

    public clientHandler(String ipAdd, int portNum, String[] command, String currentCommand) {
        this.ipAdd = ipAdd;
        this.portNum = portNum;
        this.command = command;
        this.currentCommand = currentCommand;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        if (currentCommand.equalsIgnoreCase("get")) {
            try {
                get();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (currentCommand.equalsIgnoreCase("put")) {
            try {
                put();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void put() throws UnknownHostException, IOException {
        // TODO Auto-generated method stub
        soc = new Socket(ipAdd, portNum);
        DataInputStream dis = new DataInputStream(soc.getInputStream());
        DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
        dos.writeUTF("put");
        dos.writeUTF(command[1]);

        System.out.println("C: Writing File");
        boolean terminated = false;
        int commandID = dis.readInt();
        commandIDs.add(commandID);
        System.out.println("Command id is " + commandID);
        String status = dis.readUTF();
        if (status.equalsIgnoreCase("Wait")) {
            System.out.println("File is waitlisted");
        }
        String stat = dis.readUTF();
        System.out.println("stat" + stat);
        File putFile = new File(command[1]);
        FileInputStream fis = new FileInputStream(putFile);
        long fileSize = putFile.length();
        dos.writeLong(fileSize);
        int asciiValOfChar = 0;
        byte[] bytes = new byte[1000];

        while ((asciiValOfChar = fis.read(bytes)) > 0) {
            if (!commandIDs.contains(commandID)) {
                terminated = true;
            } else {
                dos.write(bytes, 0, asciiValOfChar);
            }
        }
        //dos.close();
        if (terminated) {
            System.out.println("Process terminated");
        } else {
            System.out.println("File written in Server");
        }

    }

    private void get() throws Exception {
        soc = new Socket(ipAdd, portNum);
        DataInputStream dis = new DataInputStream(soc.getInputStream());
        DataOutputStream dos = new DataOutputStream(soc.getOutputStream());
        dos.writeUTF("get");
        dos.writeUTF(command[1]);
        String stat = dis.readUTF();
        if (stat.equalsIgnoreCase("wait")) {
            System.out.println("The file is being updated currently. Try again once available");
            return;
        }
        int commandID = dis.readInt();
        System.out.println("command ID is: " + commandID);
        commandIDs.add(commandID);
        boolean terminated = false;
        long fileSize = dis.readLong();
        String dir = System.getProperty("user.dir");
        //String newDir = path.substring(path.lastIndexOf('/')+1);
        File getFile = new File(command[1]);
        FileOutputStream fos = new FileOutputStream(getFile);
        byte[] bytes = new byte[1000];

        int count;
        int readSum = 0;
        int remaining = (int) fileSize;
        while ((count = dis.read(bytes, 0, Math.min(bytes.length, remaining))) > 0) {
            readSum += count;
            remaining -= count;
            if (!commandIDs.contains(commandID)) {
                terminated = true;
                break;
            } else {
                fos.write(bytes, 0, count);
            }
        }
        if (terminated) {
            getFile.delete();
        } else {
            System.out.println("C: File received via Thread");
        }
        dos.writeUTF("quit");
        fos.close();
    }
}
