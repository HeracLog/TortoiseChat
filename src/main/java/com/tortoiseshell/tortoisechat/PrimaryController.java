package com.tortoiseshell.tortoisechat;

import java.io.IOException;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PrimaryController {
    @FXML
    TextField usernameField,durationField,portField;
    @FXML
    Button startButton,stopButton,ban,mute;
    @FXML
    Label errorLabel;
    @FXML
    Pane logPane,backPane,optionPane;
    @FXML
    TextArea logArea;

    TranslateTransition optionEnter,optionLeave,logEnter,logLeave,backEnter,backLeave;

    static int port;
    Thread server;

    public void initialize()
    {
        optionEnter = new TranslateTransition();
        optionLeave = new TranslateTransition();
        logEnter = new TranslateTransition();
        logLeave = new TranslateTransition();
        backEnter = new TranslateTransition();
        backLeave = new TranslateTransition();
        optionEnter.setNode(optionPane);
        optionEnter.setByX(15-167);
        optionEnter.setDuration(Duration.millis(400));
        optionLeave.setNode(optionPane);
        optionLeave.setByX(167-15);
        optionLeave.setDuration(Duration.millis(400));
        logEnter.setNode(logPane);
        logEnter.setByX(293-600);
        logEnter.setDuration(Duration.millis(400));
        logLeave.setNode(logPane);
        logLeave.setByX(600-293);
        logLeave.setDuration(Duration.millis(400));
        backEnter.setNode(backPane);
        backEnter.setByX(-303-1);
        backEnter.setDuration(Duration.millis(400));
        backLeave.setNode(backPane);
        backLeave.setByX(1+303);
        backLeave.setDuration(Duration.millis(400));
    }

    @FXML
    protected void onStart()
    {
        backEnter.play();
        optionEnter.play();
        logEnter.play();
        startButton.setDisable(true);
        stopButton.setDisable(false);
        durationField.setDisable(false);
        usernameField.setDisable(false);
        ban.setDisable(false);
        mute.setDisable(false);
        port = Integer.parseInt(portField.getText());
        portField.setDisable(true);
        ServerThread st = new ServerThread(logArea);
        server = new Thread(st);
        server.start();
    }
    @FXML
    protected void onStop()
    {
        backLeave.play();
        optionLeave.play();
        logLeave.play();
        startButton.setDisable(false);
        stopButton.setDisable(true);
        durationField.setDisable(true);
        usernameField.setDisable(true);
        ban.setDisable(true);
        mute.setDisable(true);
        portField.setDisable(false);
        logArea.setText("");
        server.interrupt();

    }
    @FXML
    protected void wip()
    {
        logArea.setText(logArea.getText() + "\n#Work in progress");
        logArea.appendText("");
    }

}

class ServerThread implements Runnable
{
    TextArea logArea;
    ServerSocket server;
    List<ClientSocket> clients = new ArrayList<>();
    public ServerThread(TextArea logArea){
        this.logArea = logArea;
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }
    public static void Serialize(HashMap<Integer,Integer> map)
    {
        try
           {
              FileOutputStream fos = new FileOutputStream("clients.ser");
              ObjectOutputStream oos = new ObjectOutputStream(fos);
              oos.writeObject(map);
              oos.close();
              fos.close();
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
    }
    public static HashMap<Integer,Integer> Deserialize()
    {
        try
      {
         FileInputStream fis = new FileInputStream("clients.ser");
         ObjectInputStream ois = new ObjectInputStream(fis);
         HashMap<Integer,Integer> map = (HashMap<Integer,Integer>) ois.readObject();
         fis.close();
         ois.close();
         return map;
      }catch(IOException ioe)
      {
         ioe.printStackTrace();
         return null;
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
        return null;
    }
    }
    @Override
    public void run() {
        try{
            HashMap<Integer,Integer> users = null;
            File hasRan = new File("clients.ser");
            if (!hasRan.exists()){
            users = new HashMap<>();
            logArea.setText(logArea.getText()+ "\n" +new Date() + " Created new clients session" );
            logArea.appendText("");
            }
            else{
                users = Deserialize();
                logArea.setText(logArea.getText()+ "\n" + new Date() + " Loaded clients data.");
                logArea.appendText("");
            }
            server = new ServerSocket(PrimaryController.port);
            while (!Thread.interrupted())
            {
                Socket newClient = server.accept();
                InetSocketAddress clientIp =(InetSocketAddress) newClient.getRemoteSocketAddress();
                String clientAdress = clientIp.getAddress().getHostAddress();
                logArea.setText(logArea.getText()+ "\n" + new Date() + " New client entered at IP: "+ clientAdress + " at port: " + clientIp.getPort());
                logArea.appendText("");
                DataInputStream in = new DataInputStream(newClient.getInputStream());
                DataOutputStream out = new DataOutputStream(newClient.getOutputStream());
                int actionCode = in.readInt();
                int usrHash = in.readInt();
                int passHash = in.readInt();

                if (actionCode == 111)
                {
                    if (!users.containsKey(usrHash))
                    {
                        users.put(usrHash, passHash);
                        out.writeInt(0);
                        out.flush();
                        Serialize(users);
                    }
                    else
                    {   
                        out.writeInt(1);
                    }
                }
                else if (actionCode == 112)
                {
                    if (users.containsKey(usrHash) && users.get(usrHash) == passHash)
                    {
                        out.writeInt(0);
                        out.flush();
                        ClientSocket newC = new ClientSocket(newClient,logArea);
                        Thread thread = new Thread(newC);
                        thread.start();
                        clients.add(newC);
                    }   
                    else
                    {
                        out.writeInt(1);
                        out.flush();
                    }
                }
            }

        }
        catch(IOException ie)
        {
            ie.printStackTrace();
        }
    }
    public synchronized void stop() {
        for (ClientSocket client : clients) {
            try {
                client.stop();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}

class ClientSocket implements Runnable
{
    private final Socket thisCleint;
    private static List<ClientSocket> clients = new ArrayList<>();
    DataInputStream inputStream;
    DataOutputStream outputStream;
    TextArea logArea;
    boolean stop = false;

    public ClientSocket(Socket thisCleint, TextArea logArea) throws IOException {
        this.thisCleint = thisCleint;
        clients.add(this);
        inputStream = new DataInputStream(thisCleint.getInputStream());
        outputStream = new DataOutputStream(thisCleint.getOutputStream());
        this.logArea = logArea;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                stop();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }));
    }

    @Override
    public void run() {
        try {
            outputStream.writeUTF("Server booted at port " +PrimaryController.port);
            String message ="";
            while (!stop) {
                message = inputStream.readUTF();
                if (message.contains("!exit")) {
                    clients.remove(this);
                    inputStream.close();
                    outputStream.close();
                    thisCleint.close();
                    Thread.currentThread().interrupt();
                }
                if (message.contains("!calc")) {
                    calc(inputStream, outputStream);
                } else {
                    logArea.setText(logArea.getText() + "\n" + new Date() +" "+ message);
                    logArea.appendText("");
                    broadcast(message);
                }
            }

        }
        catch(SocketException so)
        {
            so.printStackTrace();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        finally
        {
            try{
                stop();
            }
            catch(IOException i)
            {
                i.printStackTrace();
            }
        }
    }
    public void stop() throws IOException
    {
        thisCleint.close();
        outputStream.close();
        inputStream.close();        
        stop = true;
    }
    public void broadcast(String message)
    {
        for (ClientSocket client: clients)
        {
            try {
                client.sendMessage(message, client.outputStream);
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }
    public void sendMessage(String message, DataOutputStream outputStr) throws IOException {
        outputStr.writeUTF(message);
        outputStr.flush();
    }
    public void calc(DataInputStream i, DataOutputStream o)
    {
        try{
            o.writeUTF("%*Enter an opertaion: ");
            o.flush();
            String opp = i.readUTF();
            o.writeUTF("&&Enter first number: ");
            o.flush();
            int num1 = getDig(i.readUTF());
            o.writeUTF("&&Enter second number: ");
            o.flush();
            int num2 = getDig(i.readUTF());

            System.out.print(new Date() + " ");
            if (opp.contains("+")) {
                o.writeUTF(String.valueOf(num1 + num2));
                System.out.println(num1 + num2);
                o.flush();
            }
            else
            if (opp.contains("-")){
                o.writeUTF(String.valueOf(num1 - num2));
                System.out.println(num1-num2);
                o.flush();
            }
            else if (opp.contains("/")) {
                o.writeUTF(String.valueOf((double) num1 / (double) num2));
                System.out.println((double) num1 / (double) num2);
                o.flush();
            }
            else if (opp.contains("*")) {
                o.writeUTF(String.valueOf(num1 * num2));
                System.out.println(num1*num2);
                o.flush();
            }
            else if (opp.contains("^")) {
                o.writeUTF(String.valueOf(Math.pow(num1, num2)));
                System.out.println(Math.pow(num1, num2));
                o.flush();
            }
            else {
                o.writeUTF("Invalid");
                o.flush();
            }
        }
        catch(IOException io){
            return;
        }
    }
    public int getDig(String s)
    {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<s.length();i++)
        {
            if(Character.isDigit(s.charAt(i)))
            {
                sb.append(s.charAt(i));
            }
        }
        return Integer.parseInt(sb.toString());
    }
}
