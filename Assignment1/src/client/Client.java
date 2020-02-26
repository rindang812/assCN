package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

import com.sun.tools.javac.Main;
import data.Peer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tags.Decode;
import tags.Encode;
import tags.Tags;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Client {

    public static ArrayList<Peer> clientarray = null;
    private ClientServer server;
    private InetAddress IPserver;
    private int portServer = 8080;
    private String nameUser = "";
    private boolean isStop = false;
    private static int portClient = 10000;
    private int timeOut = 1000;  //time to each request is 1 seconds.
    private Socket socketClient;
    private ObjectInputStream serverInputStream;
    private ObjectOutputStream serverOutputStream;
    private String[] listFriend = new String[10];

    public Client(String arg, int arg1, String name, String dataUser) throws Exception {
        IPserver = InetAddress.getByName(arg);
        portClient = arg1;
        nameUser = name;
        clientarray = Decode.getAllUser(dataUser);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //getListFriend();
                    updateActive();
                    updateFriend();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        server = new ClientServer(nameUser);
        (new Request()).start();
    }

    public static int getPort() {
        return portClient;
    }

    public void request() throws Exception {
        socketClient = new Socket();
        SocketAddress addressServer = new InetSocketAddress(IPserver, portServer);
        socketClient.connect(addressServer);
        String msg = Encode.sendRequest(nameUser);

        ArrayList<String> lstMsg = new ArrayList<String>();
        lstMsg.add(msg);
        lstMsg.add("exit");
        serverOutputStream = new ObjectOutputStream(socketClient.getOutputStream());
        serverOutputStream.writeObject(lstMsg);
        serverOutputStream.flush();
        serverInputStream = new ObjectInputStream(socketClient.getInputStream());
        msg = (String) serverInputStream.readObject();
        serverInputStream.close();
        String[] listCheck = msg.split(",");
        clientarray = Decode.getAllUser(listCheck[0]);
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    //getListFriend();
                    updateActive();
//                    updateFriend();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public class Request extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isStop) {
                try {

                    Thread.sleep(timeOut);
                    request();
                    getListFriend();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void intialNewChat(String IP, int host, String guest) throws Exception {
        final Socket connclient = new Socket(InetAddress.getByName(IP), host);
        ObjectOutputStream sendrequestChat = new ObjectOutputStream(connclient.getOutputStream());
        sendrequestChat.writeObject(Encode.sendRequestChat(nameUser));
        sendrequestChat.flush();
        ObjectInputStream receivedChat = new ObjectInputStream(connclient.getInputStream());
        String msg = (String) receivedChat.readObject();
        if (msg.equals(Tags.CHAT_DENY_TAG)) {
            MainGui.request("Your friend denied connect with you!", false);
            connclient.close();
            return;
        }
        new ChatGui(nameUser, guest, connclient, portClient);
    }

    public void intialNewAddFriend(String IP, int host, String guest) throws Exception {
        final Socket connclient = new Socket(InetAddress.getByName(IP), host);
        ObjectOutputStream sendrequestChat = new ObjectOutputStream(connclient.getOutputStream());
        sendrequestChat.writeObject(Encode.sendRequestChat(nameUser) + "," + "flagAdd");
        sendrequestChat.flush();
        ObjectInputStream receivedChat = new ObjectInputStream(connclient.getInputStream());
        String msg = (String) receivedChat.readObject();
        if (msg.equals(Tags.CHAT_DENY_TAG)) {
            MainGui.request("Your friend denied add friend with you!", false);
            connclient.close();
            return;
        }

        Socket socketClient = new Socket(IPserver, portServer);
        ArrayList<String> lstMsg = new ArrayList<String>();
        lstMsg.add(nameUser);
        lstMsg.add("addFriendSucceed");
        lstMsg.add(guest);
        ObjectOutputStream serverOutputStream = new ObjectOutputStream(socketClient.getOutputStream());
        serverOutputStream.writeObject(lstMsg);
        serverOutputStream.flush();
        socketClient.close();
    }

    public void exit() throws IOException, ClassNotFoundException {
        isStop = true;
        socketClient = new Socket();
        SocketAddress addressServer = new InetSocketAddress(IPserver, portServer);
        socketClient.connect(addressServer);
        String msg = Encode.exit(nameUser);
        serverOutputStream = new ObjectOutputStream(socketClient.getOutputStream());
        ArrayList<String> lstMsg = new ArrayList<String>();
        lstMsg.add(msg);
        lstMsg.add("exit");
        serverOutputStream.writeObject(lstMsg);
        serverOutputStream.flush();
        serverOutputStream.close();
        server.exit();
    }

    public void getListFriend() throws IOException, ClassNotFoundException {
        Socket socketClient = new Socket(IPserver, portServer);
        ArrayList<String> lstMsg = new ArrayList<String>();
        lstMsg.add(nameUser);
        lstMsg.add("getfriend");
        lstMsg.add(nameUser);
        ObjectOutputStream serverOutputStream = new ObjectOutputStream(socketClient.getOutputStream());
        serverOutputStream.writeObject(lstMsg);
        serverOutputStream.flush();
        ObjectInputStream serverInputStream = new ObjectInputStream(socketClient.getInputStream());
        String msg = (String) serverInputStream.readObject();
        socketClient.close();
        String[] listCheck = msg.split(",");
        if (listCheck.length != 1) {
            int j = 0;
            for (int i = 1; i < listCheck.length; i++) {
                listFriend[j] = listCheck[i];
                j++;
            }
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    updateFriend();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void updateActive() throws IOException, SAXException, ParserConfigurationException, ClassNotFoundException {
        int size = clientarray.size();
        MainGui.resetListActive();

        int i = 0;
        while (i < size) {
            if (!clientarray.get(i).getName().equals(nameUser) && checkFriend(clientarray.get(i).getName(), listFriend))
                MainGui.updateActiveMainGui(clientarray.get(i).getName());
            i++;
        }
    }

    public void updateFriend() throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException {
        MainGui.resetListFriend();
        int size = clientarray.size();
        for (int i = 0; i < listFriend.length; i++) {
            if (listFriend[i] == null) {
                return;
            }
            for (int j = 0; j < size; j++) {
                if (listFriend[i].equals(clientarray.get(j).getName())) {
                    MainGui.updateFriendMainGui(listFriend[i]);
                    break;
                }

            }
        }
    }

    public boolean checkFriend(String name, String[] listFriend) {
        for (int i = 0; i < listFriend.length; i++) {
            if (listFriend[i] == null) {
                return true;
            }
            if (name.equals(listFriend[i])) {
                return false;
            }
        }
        return true;
    }
}