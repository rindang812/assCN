package login;

import java.awt.EventQueue;
import java.io.*;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.*;

import client.MainGui;
import login.Register;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import tags.Encode;
import tags.Tags;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.awt.Font;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Login {

    private JFrame frameLoginForm;
    private JTextField txtPort;
    private JLabel lblError;
    private String name = "", pass = "", IP = "";
    private JTextField txtIP;
    private JTextField txtUsername;
    private JPasswordField txtPassWord;
    private JButton btnLogin;
    private JButton btnRegister;
    File f = new File("/data/dataRegister.xml");

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Login window = new Login();
                    window.frameLoginForm.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Login() {
        initialize();
    }

    private void initialize() {
        frameLoginForm = new JFrame();
        frameLoginForm.setTitle("Login Form");
        frameLoginForm.setResizable(false);
        frameLoginForm.setBounds(100, 100, 517, 343);
        frameLoginForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameLoginForm.getContentPane().setLayout(null);

        JLabel lblWelcome = new JLabel("Connect With Server\r\n");
        lblWelcome.setForeground(UIManager.getColor("RadioButtonMenuItem.selectionBackground"));
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblWelcome.setBounds(27, 13, 312, 48);
        frameLoginForm.getContentPane().add(lblWelcome);

        JLabel lblHostServer = new JLabel("IP Server");
        lblHostServer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHostServer.setBounds(47, 74, 86, 20);
        frameLoginForm.getContentPane().add(lblHostServer);

        JLabel lblPortServer = new JLabel("Port Server");
        lblPortServer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPortServer.setBounds(349, 77, 79, 14);
        frameLoginForm.getContentPane().add(lblPortServer);

        txtPort = new JTextField();
        txtPort.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPort.setText("8080");
        txtPort.setEditable(false);
        txtPort.setColumns(10);
        txtPort.setBounds(429, 70, 65, 28);
        frameLoginForm.getContentPane().add(txtPort);

        JLabel lblUserName = new JLabel("User Name");
        lblUserName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUserName.setBounds(10, 134, 106, 38);
        frameLoginForm.getContentPane().add(lblUserName);
        lblUserName.setIcon(new javax.swing.ImageIcon(Login.class.getResource("/image/user.png")));

        JLabel lblPassWord = new JLabel("Pass Word");
        lblPassWord.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPassWord.setBounds(10, 190, 106, 38);
        frameLoginForm.getContentPane().add(lblPassWord);
        lblPassWord.setIcon(new javax.swing.ImageIcon(Login.class.getResource("/image/password.png")));

        lblError = new JLabel("");
        lblError.setBounds(66, 287, 399, 20);
        frameLoginForm.getContentPane().add(lblError);

        txtIP = new JTextField();
        txtIP.setBounds(128, 70, 185, 28);
        frameLoginForm.getContentPane().add(txtIP);
        txtIP.setColumns(10);

        txtIP.setText("192.168.137.3");
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsername.setColumns(10);
        txtUsername.setBounds(128, 138, 366, 30);
        frameLoginForm.getContentPane().add(txtUsername);

        txtPassWord = new JPasswordField();
        txtPassWord.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassWord.setColumns(10);
        txtPassWord.setBounds(128, 198, 366, 30);
        frameLoginForm.getContentPane().add(txtPassWord);

        btnRegister = new JButton("Register");
        btnRegister.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRegister.setIcon(new javax.swing.ImageIcon(Login.class.getResource("/image/register.png")));
        btnRegister.addActionListener(new ActionListener() {
            // Event Register
            public void actionPerformed(ActionEvent arg0) {

                IP = txtIP.getText();
                int portServer = Integer.parseInt("8080");
                new Register(IP, portServer);
            }

        });

        btnRegister.setBounds(90, 250, 150, 40);
        frameLoginForm.getContentPane().

                add(btnRegister);
        lblError.setVisible(false);


        btnLogin = new

                JButton("Login");
        btnLogin.setFont(new

                Font("Segoe UI", Font.PLAIN, 13));
        btnLogin.setIcon(new javax.swing.ImageIcon(Login.class.

                getResource("/image/login.png")));
        btnLogin.addActionListener(new
                                           ActionListener() {
                                               //Login
                                               public void actionPerformed(ActionEvent arg0) {
                                                   name = txtUsername.getText();
                                                   pass = txtPassWord.getText();
                                                   lblError.setVisible(false);
                                                   IP = txtIP.getText();

                                                   if (!name.equals("") && !pass.equals("")) {
                                                       try {
                                                           Random rd = new Random();
                                                           int portPeer = 10000 + rd.nextInt() % 1000;
                                                           InetAddress ipServer = InetAddress.getByName(IP);
                                                           int portServer = Integer.parseInt("8080");
                                                           Socket socketClient = new Socket(ipServer, portServer);

                                                           ArrayList<String> lstMsg = Encode.getCreateAccount(name, Integer.toString(portPeer), pass, "login");
                                                           ObjectOutputStream serverOutputStream = new ObjectOutputStream(socketClient.getOutputStream());
                                                           serverOutputStream.writeObject(lstMsg);
                                                           serverOutputStream.flush();
                                                           ObjectInputStream serverInputStream = new ObjectInputStream(socketClient.getInputStream());
                                                           String msg = (String) serverInputStream.readObject();

                                                           socketClient.close();
                                                           if (msg.equals(Tags.SESSION_DENY_TAG)) {
                                                               lblError.setText("Account or password is incorrect!!");
                                                               lblError.setVisible(true);
                                                               return;
                                                           } else if (msg.equals("logined")) {
                                                               lblError.setText("Account is online. Please re-login!");
                                                               lblError.setVisible(true);
                                                               return;

                                                           }
                                                           new MainGui(IP, portPeer, name, msg);

                                                           frameLoginForm.dispose();
                                                       } catch (Exception e) {

                                                           lblError.setText("Server not turn on!");
                                                           lblError.setVisible(true);

                                                       }
                                                   } else {
                                                       lblError.setText("Account or password is empty");
                                                       lblError.setVisible(true);
                                                   }


                                               }
                                           });

        btnLogin.setBounds(325, 250, 150, 40);
        frameLoginForm.getContentPane().

                add(btnLogin);
        lblError.setVisible(false);
    }
}