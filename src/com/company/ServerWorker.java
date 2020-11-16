package com.company;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;


public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private OutputStream outputStream;
    private String login = null;
    private HashSet<String> topicSet = new HashSet<>();

    public ServerWorker(Server server, Socket clientSocket) { //constructor til brugere der kan connecte.
        this.server = server;
        this.clientSocket = clientSocket;
        //vi kan nu passe server instancen til hver server worker.
    }

    @Override
    public void run() {

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
        this.outputStream = clientSocket.getOutputStream(); //serveren skriver det tilbage til os
        //med input og output streams har vi bidirectional commmunication med vores client connection

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)); //læser vores input
        String line;
        while ((line = reader.readLine()) != null) { //mens vi skriver noget i vores input:
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0]; //første index i vores array, dvs. vi filtererr white space og andre ord ifht. vores quit command
                if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) { //hvis vi skriver quit så breaker vi programmet
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) { //cmd er den første token, altså vores identifier (msg) i dette tilfælde.
                    //vi vil have den splitter de to første, msg, username, første besked  men ikke resten af vores msgbody, som er selve beskeden visender.
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokensMsg); //tokens er hvad der skrives i terminalen af brugerne
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                } else if("leave".equalsIgnoreCase(cmd)){
                    handleLeave(tokens);
                }
                else {
                    String msg = "unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }

        clientSocket.close();
    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);
        List<ServerWorker> workerList = server.getWorkerList();
        String onlineMsg = "User logged off: " + login + "\r\n";
        //for ikke at se en selv som logger af
        for (ServerWorker worker : workerList) {
            if (!login.equals(worker.getLogin())) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }

    public String getLogin() {

        return login;

    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {

        if (tokens.length == 3) {
            String login = tokens[1]; //brugernavn
            String password = tokens[2];
            //vi tager login og password af hvad useren skriver. Vi hard hardcodet ét set login info.
            if ((login.equals("guest") && password.equals("guest")) || (login.equals("jim") && password.equals("jim"))) {
                String msg = "ok login" + "\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User " + login + " Has logged in");

                //vi vil gerne sende en besked til hver connected bruger, en liste over connectede brugere
                List<ServerWorker> workerList = server.getWorkerList();
                //vi itererer igennem vores connectede bruger og sender en besked til hver af dem.
                //send current user all other online logins
                //vi vil ikke sende vores egen login status til os selv.
                for (ServerWorker worker : workerList) {
                    if (worker.getLogin() != null) {
                        if (!login.equals(worker.getLogin())) {
                            String alreadyOnline = "Already online: " + worker.getLogin() + "\n";
                            send(alreadyOnline);
                        }
                    }
                }
                //send other online users current users status.
                String onlineMsg = "User logged on: " + login + "\r\n";
                //for ikke at se en selv som logger på
                for (ServerWorker worker : workerList) {
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }
            } else {
                String msg = "error logging in" + "\n";
                outputStream.write(msg.getBytes());
            }
        }
    }

    //for at sende en besked til vores connecetede brugere
    //bruger vi output stream af hver connected bruger til at skrive til dem
    private void send(String msg) throws IOException {

        if (login != null) {

            outputStream.write(msg.getBytes());


        }
    }
    //format "msg" "#topic" msg....   for topics
    //format: "msg" "username" msg.... for messages til enkelte brugere
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String msgBody = tokens[2];

        boolean isTopic = sendTo.charAt(0) == '#';

        //vi har en liste over alle brugere med forbindelse. Hvis beskeden er til en bruger der er logget ind, sendes beskeden
        List<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker : workerList) {
            if (isTopic) {
                if (worker.isMemberOfTopic(sendTo)){
                    String outMsg = "Message from group " + sendTo + " from user " + login + " :" + msgBody + "\n";
                    worker.send(outMsg);
                }
            } else {
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    String outMsg = "Message from " + login + " in chatroom: " + msgBody + "\n";
                    worker.send(outMsg);
                }
            }
        }

    }

    public boolean isMemberOfTopic(String topic){

        return topicSet.contains(topic);

    }

    //vores topic/chat room join følger formattet join #topic msgbdy
    public void handleJoin(String[] tokens) {

        if (tokens.length > 1) {

            String topic = tokens[1];
            topicSet.add(topic); //tilføjer et topic til et hashset
        }
    }

    public void handleLeave(String[] tokens){

        if (tokens.length > 1){
            String topic = tokens[1];
            topicSet.remove(topic);
        }

    }

}