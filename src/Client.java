import utils.DirAndFileUtil;

import java.io.*;
import java.net.Socket;

public class Client {

    private int port = 9250;
    private String address = "127.0.0.1";
    private Socket socket;

    public Client() {
        try {
            socket = new Socket(address, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute() throws IOException {
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        DirAndFileUtil util = new DirAndFileUtil(2);

        if (socket.isConnected()) {
            outputStream.writeUTF("Hello there!!");
            String s = inputStream.readUTF();
            System.out.println(s);
            outputStream.writeInt(23456);
            s = inputStream.readUTF();
            System.out.println(s);

        }

        util.synchronizeFiles(inputStream, outputStream);

//        util.downloadFiles(outputStream, inputStream);
        outputStream.writeUTF("BYE");
        outputStream.close();
        inputStream.close();
        socket.close();
    }


    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
