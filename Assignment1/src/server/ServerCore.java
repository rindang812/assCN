package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import data.Peer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import tags.Decode;
import tags.Tags;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class ServerCore {

    private ArrayList<Peer> dataPeer = null;
    private ServerSocket server;
    private Socket connection;
    private ObjectOutputStream obOutputClient;
    private String option = "";
    private String getListFriend = "";
    private ObjectInputStream obInputStream;
    private DataOutputStream outData;
    private DataInputStream inData;
    public boolean isStop = false, isExit = false;

    //Intial server socket
    public ServerCore(int port) throws Exception {
        server = new ServerSocket(port);
        dataPeer = new ArrayList<Peer>();
        (new WaitForConnect()).start();
    }

    //	show status of state
    private String sendSessionAccept() throws Exception {
        String msg = Tags.SESSION_ACCEPT_OPEN_TAG;
        int size = dataPeer.size();
        for (int i = 0; i < size; i++) {
            Peer peer = dataPeer.get(i);
            msg += Tags.PEER_OPEN_TAG;
            msg += Tags.PEER_NAME_OPEN_TAG;
            msg += peer.getName();
            msg += Tags.PEER_NAME_CLOSE_TAG;
            msg += Tags.IP_OPEN_TAG;
            msg += peer.getHost();
            msg += Tags.IP_CLOSE_TAG;
            msg += Tags.PORT_OPEN_TAG;
            msg += peer.getPort();
            msg += Tags.PORT_CLOSE_TAG;
            msg += Tags.PEER_CLOSE_TAG;
        }
        msg += Tags.SESSION_ACCEPT_CLOSE_TAG;
        return msg;
    }

    public void stopserver() throws Exception {
        isStop = true;
        server.close();
        connection.close();
    }

    //client connect to server
    public class WaitForConnect extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                while (!isStop) {
                    if (waitForConnection()) {
                        if (isExit) {
                            isExit = false;
                        } else {
                            if (option.equals("loginsuccess")) {
                                obOutputClient = new ObjectOutputStream(connection.getOutputStream());
                                obOutputClient.writeObject(sendSessionAccept());
                                obOutputClient.flush();
                                obOutputClient.close();
                            } else if (option.equals("sendlistfriend")) {
                                obOutputClient = new ObjectOutputStream(connection.getOutputStream());
                                obOutputClient.writeObject(sendSessionAccept() + "," + getListFriend);
                                obOutputClient.flush();
                                obOutputClient.close();
                            } else {
                                obOutputClient = new ObjectOutputStream(connection.getOutputStream());
                                obOutputClient.writeObject(option);
                                obOutputClient.flush();
                                obOutputClient.close();
                            }

                        }
                    } else {
                        if (option.equals("logined")) {
                            obOutputClient = new ObjectOutputStream(connection.getOutputStream());
                            obOutputClient.writeObject(option);
                            obOutputClient.flush();
                            obOutputClient.close();
                        } else {
                            obOutputClient = new ObjectOutputStream(connection.getOutputStream());
                            obOutputClient.writeObject(Tags.SESSION_DENY_TAG);
                            obOutputClient.flush();
                            obOutputClient.close();
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean waitForConnection() throws Exception {
        connection = server.accept();
        obInputStream = new ObjectInputStream(connection.getInputStream());
        ArrayList<String> lstMsg = (ArrayList<String>) obInputStream.readObject();
        ArrayList<String> getData = Decode.getUser(lstMsg.get(0));
        if (!lstMsg.get(1).equals("exit")) {
            if (lstMsg.get(1).equals("addFriendSucceed")) {
                File f = new File("dataRegister.xml");
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder b = factory.newDocumentBuilder();
                Document doc = b.parse(f);
                Element registers = doc.getDocumentElement();
                NodeList lstRegister = registers.getElementsByTagName("REGISTER");
                for (int i = 0; i < lstRegister.getLength(); i++) {
                    Node info = lstRegister.item(i);
                    Element getInfo = (Element) info;
                    String nameInData = getInfo.getElementsByTagName("USER_NAME").item(0).getTextContent();
                    if (nameInData.equals(lstMsg.get(0))) {
                        String txt = getInfo.getElementsByTagName("FRIEND").item(0).getTextContent();
                        if (!txt.equals("")) {
                            getInfo.getElementsByTagName("FRIEND").item(0).setTextContent(txt + "," + lstMsg.get(2));
                        } else {
                            getInfo.getElementsByTagName("FRIEND").item(0).setTextContent(lstMsg.get(2));
                        }
                        break;
                    }
                }
                for (int i = 0; i < lstRegister.getLength(); i++) {
                    Node info = lstRegister.item(i);
                    Element getInfo = (Element) info;
                    String nameInData = getInfo.getElementsByTagName("USER_NAME").item(0).getTextContent();
                    if (nameInData.equals(lstMsg.get(2))) {
                        String txt = getInfo.getElementsByTagName("FRIEND").item(0).getTextContent();
                        if (!txt.equals("")) {
                            getInfo.getElementsByTagName("FRIEND").item(0).setTextContent(txt + "," + lstMsg.get(0));
                        } else {
                            getInfo.getElementsByTagName("FRIEND").item(0).setTextContent(lstMsg.get(0));
                        }
                        break;
                    }
                }
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(f);
                transformer.transform(source, result);
                isExit = true;
            }
            if (lstMsg.get(1).equals("getfriend")) {
                File f = new File("dataRegister.xml");
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder b = factory.newDocumentBuilder();
                Document doc = b.parse(f);
                Element registers = doc.getDocumentElement();
                NodeList lstRegister = registers.getElementsByTagName("REGISTER");
                for (int i = 0; i < lstRegister.getLength(); i++) {
                    Node info = lstRegister.item(i);
                    Element getInfo = (Element) info;
                    String nameInData = getInfo.getElementsByTagName("USER_NAME").item(0).getTextContent();
                    if (nameInData.equals(lstMsg.get(2))) {
                        getListFriend = getInfo.getElementsByTagName("FRIEND").item(0).getTextContent();
                        break;
                    }
                }
                option = "sendlistfriend";
                return true;
            } else {
                if (lstMsg.size() == 4) {
                    String name = lstMsg.get(1);
                    String password = lstMsg.get(2);
                    String flag = lstMsg.get(3);
                    if (getData != null) {
                        if (flag.equals("login")) {
                            if (isExistName(name)) {
                                option = "logined";
                                return false;
                            } else if (isExistLogin(name, password)) {
                                saveNewPeer(getData.get(0), connection.getInetAddress()
                                        .toString(), Integer.parseInt(getData.get(1)));
                                ServerGui.updateNumberClient();
                                option = "loginsuccess";
                            } else
                                return false;
                        } else if (flag.equals("register")) {
                            if (name.equals("") || password.equals("")) {
                                option = "empty";
                            } else if (isExistRegister(name, password)) {
                                File f = new File("dataRegister.xml");
                                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                DocumentBuilder b = factory.newDocumentBuilder();
                                Document doc = b.parse(f);
                                Element registers = doc.getDocumentElement();
                                NodeList lstRegister = registers.getElementsByTagName("REGISTER");
                                Node data = doc.getFirstChild();

                                Element reg = doc.createElement("REGISTER");

                                Element user = doc.createElement("USER_NAME");
                                user.setTextContent(name);
                                Element passWord = doc.createElement("PASSWORD");
                                passWord.setTextContent(password);
                                Element friend = doc.createElement("FRIEND");
                                friend.setTextContent("");

                                reg.appendChild(user);
                                reg.appendChild(passWord);
                                reg.appendChild(friend);
                                data.appendChild(reg);


                                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                                Transformer transformer = transformerFactory.newTransformer();
                                DOMSource source = new DOMSource(doc);
                                StreamResult result = new StreamResult(f);
                                transformer.transform(source, result);
                                option = "registersuccess";
                            } else {
                                option = "reregister";

                            }
                        }

                    } else {
                        int size = dataPeer.size();

                        Decode.updatePeerOnline(dataPeer, lstMsg.get(0));
                        if (size != dataPeer.size()) {
                            isExit = true;
                            ServerGui.decreaseNumberClient();
                        }
                    }
                }

            }

        } else {
            int size = dataPeer.size();

            Decode.updatePeerOnline(dataPeer, lstMsg.get(0));
            if (size != dataPeer.size()) {
                isExit = true;
                ServerGui.decreaseNumberClient();
            }
        }

        return true;
    }


    private void saveNewPeer(String user, String ip, int port) throws Exception {
        Peer newPeer = new Peer();
        if (dataPeer.size() == 0)
            dataPeer = new ArrayList<Peer>();
        newPeer.setPeer(user, ip, port);
        dataPeer.add(newPeer);
    }

    private boolean isExistName(String name) throws Exception {
        if (dataPeer == null)
            return false;
        int size = dataPeer.size();
        for (int i = 0; i < size; i++) {
            Peer peer = dataPeer.get(i);
            if (peer.getName().equals(name))
                return true;
        }
        return false;
    }

    private boolean isExistRegister(String name, String password) throws Exception {
        File f = new File("dataRegister.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder b = factory.newDocumentBuilder();
        Document doc = b.parse(f);
        Element registers = doc.getDocumentElement();
        NodeList lstRegister = registers.getElementsByTagName("REGISTER");
        for (int i = 0; i < lstRegister.getLength(); i++) {
            Node info = lstRegister.item(i);
            Element getInfo = (Element) info;
            String nameInData = getInfo.getElementsByTagName("USER_NAME").item(0).getTextContent();
            if (name.equals(nameInData)) {
                return false;
            }
        }

        return true;
    }

    private boolean isExistLogin(String name, String password) throws Exception {
        File f = new File("dataRegister.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder b = factory.newDocumentBuilder();
        Document doc = b.parse(f);
        Element registers = doc.getDocumentElement();
        NodeList lstRegister = registers.getElementsByTagName("REGISTER");
        for (int i = 0; i < lstRegister.getLength(); i++) {
            Node info = lstRegister.item(i);
            Element getInfo = (Element) info;
            String nameInData = getInfo.getElementsByTagName("USER_NAME").item(0).getTextContent();
            String passInData = getInfo.getElementsByTagName("PASSWORD").item(0).getTextContent();
            if (name.equals(nameInData) && password.equals(passInData)) {
                return true;
            }
        }
        return false;
    }
}


