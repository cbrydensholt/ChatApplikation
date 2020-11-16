package com.company;

import java.io.*;
import java.net.Socket;

public class GammelServerWorkerSimple extends Thread {

    private final Socket clientSocket;

    public GammelServerWorkerSimple(Socket clientSocket){

        this.clientSocket = clientSocket;
    }

    @Override
    public void run(){

        try {
            HandleClient();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void HandleClient() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream(); //hvad vi skriver til serveren
        OutputStream outputStream = clientSocket.getOutputStream(); //serveren skriver det tilbage til os
        //med input og output streams har vi bidirectional commmunication med vores client connection

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)); //læser vores input
        String line;
        while ((line = reader.readLine()) != null){ //mens vi skriver noget i vores input:

            if("quit".equalsIgnoreCase(line)){ //hvis vi skriver quit så breaker vi programmet
                break;
            }

            String msg = "You typed: " + line + "\n";
            outputStream.write(msg.getBytes()); //getbytes er åbenbart krævet for at kunne parse en besked
        }

        clientSocket.close();
    }


}
