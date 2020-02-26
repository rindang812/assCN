package login;

import client.MainGui;
import tags.Encode;
import tags.Tags;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class Register {
    private JFrame framRegisterForm;
    private JLabel lblError;
    private static String name = "", pass = "", IP = "";
    private JTextField txtUsername;
    private JPasswordField txtPassWord;
    private JPasswordField txtRePassWord;
    private static int portServer;

    private JButton btnConfirm;
    private JButton btnCancel;

    public Register() {
        initialize();
    }

    public Register(String IPRe, int port) {
        portServer = port;
        IP = IPRe;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {

                    Register window = new Register();
                    window.framRegisterForm.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    private void initialize() {
        framRegisterForm = new JFrame();
        framRegisterForm.setTitle("Register Form");
        framRegisterForm.setResizable(false);
        framRegisterForm.setBounds(100, 100, 600, 420);
        framRegisterForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        framRegisterForm.getContentPane().setLayout(null);

        JLabel lblWelcome = new JLabel("Register Form");
        lblWelcome.setForeground(UIManager.getColor("RadioButtonMenuItem.selectionBackground"));
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 25));
        lblWelcome.setBounds(210, 13, 312, 48);
        lblWelcome.setIcon(new javax.swing.ImageIcon(Login.class.getResource("/image/bigRegister.png")));
        framRegisterForm.getContentPane().add(lblWelcome);

        JLabel lblUserName = new JLabel("User Name");
        lblUserName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUserName.setBounds(10, 100, 106, 38);
        framRegisterForm.getContentPane().add(lblUserName);
        lblUserName.setIcon(new javax.swing.ImageIcon(Login.class.getResource("/image/user.png")));

        JLabel lblPassWord = new JLabel("Pass Word");
        lblPassWord.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPassWord.setBounds(10, 170, 106, 38);
        framRegisterForm.getContentPane().add(lblPassWord);
        lblPassWord.setIcon(new javax.swing.ImageIcon(Login.class.getResource("/image/password.png")));

        JLabel lblRePassWord = new JLabel("Re-enter Pass Word");
        lblRePassWord.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRePassWord.setBounds(10, 240, 170, 38);
        framRegisterForm.getContentPane().add(lblRePassWord);
        lblRePassWord.setIcon(new javax.swing.ImageIcon(Login.class.getResource("/image/password.png")));

        lblError = new JLabel("");
        lblError.setBounds(66, 287, 399, 20);
        framRegisterForm.getContentPane().add(lblError);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsername.setColumns(10);
        txtUsername.setBounds(178, 100, 366, 30);
        framRegisterForm.getContentPane().add(txtUsername);

        txtPassWord = new JPasswordField();
        txtPassWord.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassWord.setColumns(10);
        txtPassWord.setBounds(178, 170, 366, 30);
        framRegisterForm.getContentPane().add(txtPassWord);

        txtRePassWord = new JPasswordField();
        txtRePassWord.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtRePassWord.setColumns(10);
        txtRePassWord.setBounds(178, 240, 366, 30);
        framRegisterForm.getContentPane().add(txtRePassWord);

        btnConfirm = new JButton("Confirm");
        btnConfirm.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnConfirm.setIcon(new javax.swing.ImageIcon(Login.class.getResource("/image/confirm.png")));

        btnConfirm.addActionListener(new
                                             ActionListener() {

                                                 public void actionPerformed(ActionEvent arg0) {
                                                     name = txtUsername.getText();
                                                     pass = txtPassWord.getText();
                                                     lblError.setVisible(false);
                                                     if (!pass.equals(txtRePassWord.getText()))
                                                     {
                                                         lblError.setText("Re-pass not same pass. Please write again!");
                                                         lblError.setVisible(true);
                                                         txtPassWord.setText("");
                                                         txtRePassWord.setText("");
                                                         return;
                                                     }

                                                     try {
                                                         Random rd = new Random();
                                                         int portPeer = 10000 + rd.nextInt() % 1000;
                                                         Socket socketClient = new Socket(IP, portServer);
                                                         ArrayList<String> lstMsg = Encode.getCreateAccount(name, Integer.toString(portPeer), pass, "register");
                                                         ObjectOutputStream serverOutputStream = new ObjectOutputStream(socketClient.getOutputStream());
                                                         serverOutputStream.writeObject(lstMsg);
                                                         serverOutputStream.flush();
                                                         ObjectInputStream serverInputStream = new ObjectInputStream(socketClient.getInputStream());
                                                         String msg = (String) serverInputStream.readObject();

                                                         socketClient.close();
                                                         if (msg.equals("registersuccess")) {
                                                             lblError.setText("Register success!");
                                                             lblError.setVisible(true);
                                                             framRegisterForm.dispose();
                                                         } else if (msg.equals("reregister")) {
                                                             lblError.setText("This username has been used. Please choose difference name!");
                                                             lblError.setVisible(true);

                                                         } else if (msg.equals("empty")) {
                                                             lblError.setText("User name or password is empty. Please check again!");
                                                             lblError.setVisible(true);
                                                         }
                                                         txtUsername.setText("");
                                                         txtPassWord.setText("");
                                                         return;

                                                     } catch (Exception e) {

                                                         lblError.setText("Server not turn on!");
                                                         lblError.setVisible(true);


                                                     }

                                                 }


                                             });
        btnConfirm.setBounds(390, 310, 150, 40);

        framRegisterForm.getContentPane().

                add(btnConfirm);
        lblError.setVisible(false);


        btnCancel = new

                JButton("Cancel");
        btnCancel.setFont(new

                Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setIcon(new javax.swing.ImageIcon(Login.class.

                getResource("/image/stop.png")));

        btnCancel.addActionListener(new
                                            ActionListener() {
                                                //Login
                                                public void actionPerformed(ActionEvent arg0) {
                                                    framRegisterForm.dispose();

                                                }
                                            });
        btnCancel.setBounds(180, 310, 150, 40);
        framRegisterForm.getContentPane().add(btnCancel);
        lblError.setVisible(false);
    }
}
