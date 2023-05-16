package main.java.com.tortoise.chat.tortoisechat.ServerSide;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static int port = 8181;
    public static void main(String[] args)
    {
        try{
            ServerSocket server = new ServerSocket(port);
            while (true)
            {
                Socket newClient = server.accept();
                System.out.println("New client entered!!");
                ClientSocket newC = new ClientSocket(newClient);
                new Thread(newC).start();
            }

        }
        catch(IOException ie)
        {
            ie.printStackTrace();
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
                    System.exit(0);
                    thisCleint.close();
                    outputStream.close();
                    inputStream.close();
                }
                if (message.contains("!calc")) {
                    calc(inputStream, outputStream);
                } else {
                    System.out.println(message);
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
            int num1 = i.readInt();
            o.writeUTF("&&Enter second number: ");
            o.flush();
            int num2 = i.readInt();


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
}