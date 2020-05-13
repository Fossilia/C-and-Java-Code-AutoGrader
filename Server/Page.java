/**
 * @Author Faisal Bagalagel
 * @since April 05, 2020
 * COMP 2800
 * Assignment10
 * Page.java
 */

//package com.fossilia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Page extends JPanel implements ActionListener {
    String serverip = "127.0.0.1"; //change this in order to run the program with a different IP address of the server program
    JLabel nameLabel, fileLabel;
    JTextField nameField, fileField;
    JButton uploadButton;
    File fileToSend;
    String nameToSend, filename;

    public Page(){
        GridLayout layout = new GridLayout(3, 2);
        layout.setVgap(20);
        layout.setHgap(50);

        setLayout(layout);
        nameLabel = new JLabel("student name ");
        add(nameLabel);
        nameField = new JTextField(30); //where user inputs the student name
        add(nameField);

        fileLabel = new JLabel("file name ");
        add(fileLabel);
        fileField = new JTextField(30); //where user inputs the name of the file they are sending
        add(fileField);

        uploadButton = new JButton("upload"); //starts the page client to connect to the server
        uploadButton.addActionListener(this);
        add(uploadButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) { //when upload button is pressed
        nameToSend = nameField.getText(); //get the student name from the first textfield
        filename = fileField.getText(); //get the file name from the second text field
        fileToSend = new File(filename); //find file to send
        try {
            sendFile(); //start page client
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Used to start the page client and send multiple strings and a file
     * @throws IOException
     */
    public void sendFile() throws IOException {
        FileInputStream fis;
        BufferedInputStream bis;
        OutputStream os;
        Socket sock;
        DataOutputStream dos;
        DataInputStream dis;

        try {
            //starting client by making a new Socket connection
            System.out.println("Connecting to Server...");
            sock = new Socket(serverip, 55588);
            //opening input and output streams
            dis = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
            System.out.println("Connected to Server.");

            //----------------sending name------------------------------------
            dos.writeUTF(nameToSend); //the files name to be sent
            dos.flush();

            //----------------sending file name------------------------------------
            dos.writeUTF(filename); //the files name to be sent
            dos.flush();

            //-------------------------sending file----------------------------
            byte [] mybytearray  = new byte [(int)fileToSend.length()];
            fis = new FileInputStream(fileToSend);
            bis = new BufferedInputStream(fis);
            bis.read(mybytearray,0,mybytearray.length);
            os = sock.getOutputStream();
            os.write(mybytearray,0,mybytearray.length);
            os.flush();

            fis.close();
            bis.close();
            os.close();
            dos.close();
            //--------------------receive report string------------------------
            sock = new Socket(serverip, 55588);
            dos = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
            dis = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
            removeAll(); //clearing jpanel
            revalidate();
            repaint();
            setLayout(new BorderLayout()); //set new layout
            add(new TextArea(dis.readUTF())); //add a text area that will display the received string report

            sock.close();
            dos.close();
            dis.close();

        }
        finally {
            //servsock.close();
        }
    }
}
