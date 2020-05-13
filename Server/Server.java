/**
 * @Author Faisal Bagalagel
 * @since April 05, 2020
 * COMP 2800
 * Assignment10
 * Server.java
 */

//package com.fossilia;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    public static void main(String[] args) throws IOException {
        FileInputStream fis;
        BufferedInputStream bis;
        OutputStream os;
        ServerSocket servsock = null;
        Socket sock;
        DataOutputStream dos;
        DataInputStream dis;
        String name;
        String filename;

        int bytesRead;
        int current;
        FileOutputStream fos;
        BufferedOutputStream bos;

        try {
            servsock = new ServerSocket(55588); //start server by opening a server socket
            while (true) {
                System.out.println("Waiting for client...");
                sock = servsock.accept();
                System.out.println("Connected to client: " + sock);
                // send file

                //-------------------------sending file----------------------------
                File myFile = new File ("Page.class"); //the file to be sent
                byte [] mybytearray  = new byte [(int)myFile.length()];
                fis = new FileInputStream(myFile);
                bis = new BufferedInputStream(fis);
                bis.read(mybytearray,0,mybytearray.length);
                os = sock.getOutputStream();
                os.write(mybytearray,0,mybytearray.length);
                os.flush();
                System.out.println("Page.class sent out.");

                System.out.println("Waiting to receive student file...");

                //------------------close connection to Client-------------------------------
                bis.close();
                os.close();
                sock.close();
                //servsock.close();
                //---------------------connecting to page-------------------------------
                //servsock = new ServerSocket(55588);
                //sock = new Socket("127.0.0.1", 55589);
                System.out.println("Connecting to Page...");
                sock = servsock.accept();
                System.out.println("Connected to Page.");

                dis = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
                dos = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));

                //------------------get student name---------------------------------------
                name = dis.readUTF();
                //System.out.println("student: " + name);

                //-------------------create student folder------------------------------------
                File r = new File(name);
                r.mkdirs();

                //-----------------get file name-------------------------------------------
                filename = dis.readUTF();
                //System.out.println("Filename: " + filename);

                //-----------------------receive file to be tested----------------------------
                // receive file
                mybytearray  = new byte [6022386];
                InputStream is = sock.getInputStream();
                fos = new FileOutputStream(name+"/"+filename);
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

                bos.write(mybytearray, 0 , current);
                bos.flush();

                System.out.println("Submission recieved from "+name);
                is.close();
                fos.close();
                bos.close();
                sock.close();

                sock = servsock.accept();
                dis = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
                dos = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));

                //------------------------grade file received and send report string-----------------
                dos.writeUTF(fileTester(args[0], filename, name));
                dos.flush();
                System.out.println("Grading completed.");
                //-----------------------close connection to page-------------------------

                dis.close();
                dos.close();
                sock.close();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //assert servsock != null;
            //servsock.close();
        }
    }

    /**
     * Gets the extension of a string filename
     * @param path the filename to get extension from
     * @return extension of filename if it has one, otherwise null
     */
    public static String getExtension(String path){
        if(path.lastIndexOf(".")!=-1){
            return path.substring(path.lastIndexOf(".")+1);
        }
        else{
            return null;
        }
    }

    /**
     * does the compiling, executing, and testing for the c file that is sent by the client, also saves a report on the system
     * @param testcase
     * @param filename
     * @param studentName
     * @return returns the report as a string for the server to send
     * @throws IOException
     * @throws InterruptedException
     */
    public static String fileTester(String testcase, String filename, String studentName) throws IOException, InterruptedException {
        BufferedReader br = new BufferedReader(new FileReader(testcase));
        String line = br.readLine();
        ArrayList<ArrayList<String>> inputgroups = new ArrayList<>();
        ArrayList<String> outputs = new ArrayList<>();
        String fileNameWithoutExt = filename.substring(0, filename.lastIndexOf(".")-1);
        String fileExt = getExtension(filename);

        //--------------------loading all data from testcase file into arraylists to be used for testing later-----------
        while(line!=null){
            while(!line.equals("#")){ //if input group and output group didnt end ended
                //System.out.println(line); //testing
                ArrayList<String> inputs = new ArrayList<>(); //make new input group
                while(!line.equals("*")) { //go through inputs
                    inputs.add(line); //add input to input group
                    line = br.readLine(); //next line
                }
                inputgroups.add(inputs); //add input group to groups
                line = br.readLine(); //next line
                outputs.add(line); //add output
                line = br.readLine(); //next line
            }
            line = br.readLine(); //next line
        }

        //---------------------compiling c file--------------------------------------------------
        if(fileExt.equals("c")){ //c file
        	Process p = Runtime.getRuntime().exec("gcc "+studentName+"/"+filename+" -o "+fileNameWithoutExt); //run file
        }
        else if(fileExt.equals("java")){ //java file
        	Process p = Runtime.getRuntime().exec("java "+studentName+"/"+filename); //run file
        }
        else{
        	System.out.println("Invalid file sent, file must be a C or Java file.");
        	System.exit(0);
        }
        String report = "";

        //---------------checking if there were no errors with compilation----------------------------------------
        BufferedReader errin = new BufferedReader(new InputStreamReader(p.getErrorStream())); //getting output
        String error = errin.readLine();
        if(error==null){
            System.out.println("Compiled successfully.");
            report+="Compiled successfully.\n";
            //----------------------------getting permissions to access file--------------------------------------------------
            Runtime.getRuntime().exec("chmod +xrw "+filename);
            p.waitFor();

            for(int i=0; i<outputs.size(); i++){ //run for each case in testcase file
                //-------------------------executing the file------------------------------------------------
                p = Runtime.getRuntime().exec("./"+fileNameWithoutExt);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream())); //writing input
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream())); //getting output

                //write input to file
                for(int k=0; k<inputgroups.get(i).size(); k++){
                    //System.out.println("Input going in :"+inputgroups.get(i).get(k));
                    out.write(inputgroups.get(i).get(k)+"\n"); //write input
                    //out.newLine(); //next line
                }
                out.flush(); //send inputs

                String s = null;
                while ((s = in.readLine()) != null) { //check output
                    //System.out.println(s+" this is output from file compared to "+outputs.get(i));
                    if(s.equals(outputs.get(i))){ //if it matches output from text file
                        //System.out.println("Test case "+(i+1)+": pass");
                        report+="Test case "+(i+1)+": pass\n";
                    }
                    else{ //otherwise
                        //System.out.println("Test case "+(i+1)+": fail");
                        report+="Test case "+(i+1)+": fail\n";
                    }
                }
            }
            System.out.println("Execution completed.");
        }
        else{
            //------------------if file fails to compile-------------------------------------------
            System.out.println("Failed to compile.");
            report+="Failed to compile.\n";
            System.out.println(error);
        }
        br.close();

        //-------------------------create report file---------------------------------------------
        BufferedWriter bw = new BufferedWriter(new FileWriter(studentName+"/report"));
        bw.write(report);
        bw.close();

        return report;
    }
}
