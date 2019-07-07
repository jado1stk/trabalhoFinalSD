package utfbox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import senha.Senhas;
import usuarios.Users;

public class ServerSide {

    static int nport = 9999;
    static int tport = 9998;
    static int waitListId = 0;

    static Map<Integer, String> reqMap = new HashMap<>();
    static Map<Integer, String> reqType = new HashMap<>();

    ServerSide(int nport, int tport) {
        new Thread(new Terminator()).start();
        new Thread(new ServerThread()).start();
    }
    static Set<Integer> commandIDs = new HashSet<>();
    Map<String, Deque<Integer>> queueMap = new HashMap<>();

    static int counter = 0;

    public static void main(String[] args) throws Exception {
        new ServerSide(nport, tport);
    }

    //Inner class starts
    class Terminator implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                ServerSocket socket = new ServerSocket(tport);
                while (true) {
                    Socket terminationSocket = socket.accept();
                    DataInputStream dis = new DataInputStream(terminationSocket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(terminationSocket.getOutputStream());
                    String command = dis.readUTF();
                    int id = dis.readInt();
                    commandIDs.remove(id);
                    System.out.println("Command id removed " + id);
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    //Inner class starts
    class ServerThread implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            System.out.println("S: Server has started");
            // Creating a server Socket
            ServerSocket servSoc;
            try {
                servSoc = new ServerSocket(nport);

                Socket clientSoc;
                while (true) {
                    System.out.println("S: Waiting for client");
                    //For every request we create a socket
                    clientSoc = servSoc.accept();
                    System.out.println("connection accepted");

                    new Thread(new serverHandler(clientSoc)).start();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println(e);
            }
        }
    }

    //Inner class starts
    class serverHandler implements Runnable {

        Socket clientSoc = null;

        serverHandler(Socket socket) {
            this.clientSoc = socket;
        }
        DataInputStream dis = null;
        DataOutputStream dos = null;

        @Override
        public void run() {
            System.out.println("S: client Thread connected");
            Boolean verificado;
            try {
                dis = new DataInputStream(clientSoc.getInputStream());

                dos = new DataOutputStream(clientSoc.getOutputStream());
                boolean quit = false;

                while (!quit) {
                    //read command from client
                    System.out.println("Waiting for command");
                    String command = dis.readUTF();
                    System.out.println(command);

                    //execute corresponding output
                    switch (command.toLowerCase()) {

                        case "get":
                            String path = dis.readUTF();
                            get(clientSoc, path);
                            break;
                            
                        case "delete":
                            String deletePath = dis.readUTF();
                            delete(deletePath);
                            break;
                        case "put":
                            String name = dis.readUTF();
                            put(dis, clientSoc, name);
                            dos.flush();
                            break;

                        case "mkdir":
                            String pathNewDirectory = dis.readUTF();
                            new File(pathNewDirectory).mkdir();
                            break;

                        case "ls":
                            String dir = dis.readUTF();
                            System.out.println("" + dir);
                            String lsout = ls(dir);
                            dos.writeUTF(lsout);
                            break;
                        case "isdir":
                            String diretorio = dis.readUTF();
                            File f = new File(diretorio);
                            String isDir = Boolean.toString(f.isDirectory());
                            dos.writeUTF(isDir);
                            break;
                        case "verifica":
                            Senhas verificaSenha = new Senhas();
                            String user = dis.readUTF();
                            String password = dis.readUTF();
                            verificado = verificaSenha.verificaUsers(user, password);
                            String resultVerifica = Boolean.toString(verificado);
                            dos.writeUTF(resultVerifica);
                            break;
                        case "criauser":
                            Senhas criaUser = new Senhas();
                            String newUser = dis.readUTF();
                            String newPassword = dis.readUTF();
                            verificado = criaUser.verificaUsers(newUser, newPassword);
                            String pathUser = "false";
                            if(!verificado)
                            {
                                pathUser = criaUser.criaNewUser(newUser, newPassword);
                            }
                            dos.writeUTF(pathUser);
                            break;
                        case "quit":
                            quit(clientSoc, dis, dos);
                            System.out.println("Server Connection closed");
                            quit = true;
                            break;
                        case "compartilhar":
                            String comquem = dis.readUTF();
                            String oq = dis.readUTF();
                            Senhas.compartilhar(comquem, oq);
                            break;
                        case "arquivoscompartilhados":
                            String quem = dis.readUTF();
                            String arquivos = Senhas.getCompartilhados(quem);
                            if(arquivos.equals("")){
                                arquivos = "false";
                            }
                            dos.writeUTF(arquivos);
                            break;
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        //get command

        void get(Socket clientSoc, String path) throws Exception {
            //Read from text file
            System.out.println("S: Writing File");
            File getFile = new File(path);
            DataOutputStream dos = new DataOutputStream(clientSoc.getOutputStream());
            boolean terminated = false;
            int commandID = counter++;
            waitListId++;
            int readId = waitListId;
            boolean proceed = true;
            System.out.println("curr get file " + getFile.toString());
            for (Map.Entry<Integer, String> req : reqMap.entrySet()) {
                if (!getFile.toString().equals(req.getValue())) {
                } else {
                    System.out.println("Here " + reqType.get(req.getKey()));
                    if (reqType.get(req.getKey()).equals("PUT")) {
                        proceed = false;
                    }
                }
            }
            if (proceed) {
                dos.writeUTF("proceed");
                System.out.println(getFile.toString());

                reqMap.put(readId, getFile.toString());
                reqType.put(readId, "GET");
                if (queueMap.get(getFile.toString()) == null) {
                    Deque<Integer> curDeq = new LinkedList<>();
                    curDeq.push(readId);
                    queueMap.put(getFile.toString(), curDeq);
                } else {
                    Deque<Integer> curDeq = queueMap.get(getFile.toString());
                    curDeq.push(readId);
                    queueMap.put(getFile.toString(), curDeq);
                }
            } else {
                dos.writeUTF("wait");
                return;
            }

            dos.writeInt(commandID);
            System.out.println("The command id sent is " + commandID);
            commandIDs.add(commandID);
            dos.writeLong(getFile.length());
            FileInputStream fis = new FileInputStream(getFile);

            int asciiValOfChar = 0;
            byte[] bytes = new byte[10000000];

            while ((asciiValOfChar = fis.read(bytes)) > 0) {
                if (!commandIDs.contains(commandID)) {
                    terminated = true;
                } else {
                    dos.write(bytes, 0, asciiValOfChar);
                }
            }
            dos.flush();
            System.out.println("Outside file send");

            if (terminated) {
                System.out.println("Action terminated");
                reqMap.remove(readId);
                reqType.remove(readId);
                Deque<Integer> currDeque = queueMap.get(getFile.toString());
                currDeque.removeLast();
                queueMap.put(getFile.toString(), currDeque);
            } else {
                System.out.println("S: File written in Client");
                reqMap.remove(readId);
                reqType.remove(readId);
                Deque<Integer> currDeque = queueMap.get(getFile.toString());
                currDeque.removeLast();
                queueMap.put(getFile.toString(), currDeque);
            }

        }

        //put command
        void put(DataInputStream dis, Socket clientSoc, String name) throws Exception {
            File putFile = new File(name);
            boolean terminated = false;
            int commandID = counter++;
            dos.writeInt(commandID);
            commandIDs.add(commandID);
            //int commandID = counter++;
            waitListId++;
            int readId = waitListId;
            boolean proceed = true;
            for (Map.Entry<Integer, String> req : reqMap.entrySet()) {
                if (putFile.toString().equals(req.getValue())) {
                    proceed = false;

                    break;
                }
            }

            if (proceed) {

                dos.writeUTF("proceed");
                System.out.println("curr put file " + putFile.toString());
                reqMap.put(readId, putFile.toString());
                reqType.put(readId, "PUT");
                if (queueMap.get(putFile.toString()) == null) {
                    Deque<Integer> curDeq = new LinkedList<>();
                    curDeq.push(readId);
                    queueMap.put(putFile.toString(), curDeq);
                } else {
                    Deque<Integer> curDeq = queueMap.get(putFile.toString());
                    curDeq.push(readId);
                    queueMap.put(putFile.toString(), curDeq);
                }
            } else {
                reqMap.put(readId, putFile.toString());
                reqType.put(readId, "PUT");
                if (queueMap.get(putFile.toString()) == null) {
                    Deque<Integer> curDeq = new LinkedList<>();
                    curDeq.push(readId);
                    queueMap.put(putFile.toString(), curDeq);
                } else {
                    Deque<Integer> curDeq = queueMap.get(putFile.toString());
                    curDeq.push(readId);
                    queueMap.put(putFile.toString(), curDeq);
                }
                dos.writeUTF("Wait");
                while (!proceed) {
                    Thread.currentThread().sleep(100);
                    Deque<Integer> currDeq = queueMap.get(putFile.toString());
                    if (currDeq.getLast() == readId) {
                        break;
                    } else {
                        System.out.println("Waiting");
                    }
                }
            }

            dos.writeUTF("send");
            long fileSize = dis.readLong();

            try (FileOutputStream fos = new FileOutputStream(putFile)) {
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
            }
            if (terminated) {
                reqMap.remove(readId);
                reqType.remove(readId);
                Deque<Integer> currDeque = queueMap.get(putFile.toString());
                currDeque.removeLast();
                queueMap.put(putFile.toString(), currDeque);
                putFile.delete();
                System.out.println("File deleted in put method");
            } else {
                reqMap.remove(readId);
                reqType.remove(readId);
                Deque<Integer> currDeque = queueMap.get(putFile.toString());
                currDeque.removeLast();
                queueMap.put(putFile.toString(), currDeque);
                System.out.println("S: File received");
            }
        }

        String pwd() throws Exception {

            String dir = System.getProperty("user.dir");
            return Users.getPwd();
        }

        String ls(String dir) throws Exception {
            String lsout = "";
            String[] strings = new File(dir).list();
            for (String string : strings) {
                lsout += string + "\n";
            }
            return lsout;

        }

        void mkdir(String newDir) throws Exception {
            File f;
            boolean bool = false;
            f = new File(pwd() + File.separator + newDir);
            bool = f.mkdir();
            System.out.println("created directory: " + newDir);
        }

        boolean delete(String deletePath) throws Exception {
            File file = new File(deletePath);
            if (file.exists()) {
                if(file.isDirectory())
                {
                    String[]entries = file.list();
                    for(String s: entries){
                        File currentFile = new File(file.getPath(),s);
                        currentFile.delete();
                    }
                }
                file.delete();
                return true;
            } else {
                return false;
            }
        }

        void quit(Socket clientSoc, DataInputStream dis, DataOutputStream dos) {
            try {
                dis.close();
                dos.close();
                clientSoc.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
