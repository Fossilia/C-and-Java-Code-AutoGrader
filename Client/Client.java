/**
 * @Author Faisal Bagalagel
 * @since April 05, 2020
 * COMP 2800
 * Assignment10
 * Client.java
 */

//package com.fossilia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame implements ActionListener{
    TextField field;
    JPanel jpanel;
    JPanel panel;

    public static void main(String[] args) {
        new Client();
    }

    //------------------used to create ip textfield-----------------------
    public Client() {
        setTitle("Client");
        setSize(500, 500);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        panel = new JPanel();
        Label label = new Label("Enter the ip address"); //get ip address from user in textfield
        panel.add(label);
        field = new TextField(20);
        field.addActionListener(this);
        panel.add(field);
        add(panel);
        setVisible(true);
    }

        @Override
        public void actionPerformed(ActionEvent e) { //when user presses enter after entering ip address

            Class<?> clazz = null;
            Object c = null;
            String name = "Page";
            try {
                try {
                    Client.runClient(field.getText()); //runs the client and tries to receive the file
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                clazz = Class.forName(name); //load class file from file received
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            try {
                c = clazz.newInstance(); //create a new instance of the class
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
            if(jpanel!=null){
                jpanel.removeAll();
            }
            jpanel = (JPanel)c; //load jpanel from class file
            add(jpanel);
            jpanel.revalidate();
            jpanel.repaint();
            setVisible(true);
        }

    /**
     * used to connect to server and receive page file
     * @param ip server ip client connects to
     * @throws IOException
     * @throws InterruptedException
     */
    public static void runClient(String ip) throws IOException, InterruptedException {
        int bytesRead;
        int current;
        FileOutputStream fos;
        BufferedOutputStream bos;
        DataInputStream dis;
        Socket sock;
        String name;

        //------------------connect to server----------------------------
        System.out.println("Connecting to server...");
        sock = new Socket(ip, 55588);
        dis = new DataInputStream(sock.getInputStream());
        System.out.println("Connected to server.");

        //-----------------------receive file----------------------------
        byte [] mybytearray  = new byte [6022386];
        InputStream is = sock.getInputStream();
        fos = new FileOutputStream("Page.class");
        bos = new BufferedOutputStream(fos);
        bytesRead = is.read(mybytearray,0,mybytearray.length);
        current = bytesRead;

        do {
            bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
            if(bytesRead >= 0){
                current += bytesRead;
            }
        }
        while(bytesRead > -1);

        System.out.println("Received Page.class.");
        bos.write(mybytearray, 0 , current);
        bos.flush();
        //Thread.sleep(10000);
        fos.close();
        bos.close();
        sock.close();
    }

}
