package com.tortoiseshell.tortoisechat.serverside;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Main {
    static int port = 8181;
    public static void main(String[] args)
    {
        try{
            HashMap<Integer,Integer> users = null;
            File hasRan = new File("clients.ser");
            if (!hasRan.exists()){
            users = new HashMap<>();
            System.out.println(new Date() + " Created new clients session");
            }
            else{
                users = Deserialize();
                System.out.println(new Date() + " Loaded clients data.");
            }
            ServerSocket server = new ServerSocket(port);
            while (true)
            {
                Socket newClient = server.accept();
                InetSocketAddress clientIp =(InetSocketAddress) newClient.getRemoteSocketAddress();
                String clientAdress = clientIp.getAddress().getHostAddress();
                System.out.println(new Date() + " New client entered at IP: "+ clientAdress + " at port: " + clientIp.getPort() );
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
                        ClientSocket newC = new ClientSocket(newClient);
                        new Thread(newC).start();
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

}

class ClientSocket implements Runnable
{
    private final Socket thisCleint;
    private static List<ClientSocket> clients = new ArrayList<>();
    DataInputStream inputStream;
    DataOutputStream outputStream;

    public ClientSocket(Socket thisCleint) throws IOException {
        this.thisCleint = thisCleint;
        clients.add(this);
        inputStream = new DataInputStream(thisCleint.getInputStream());
        outputStream = new DataOutputStream(thisCleint.getOutputStream());
    }

    @Override
    public void run() {
        try {
            outputStream.writeUTF("Server booted at port " +Main.port);
            String message ="";
            while (true) {
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
                    System.out.println(new Date() +" "+ message);
                    broadcast(message);
                }
            }

        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
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