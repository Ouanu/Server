import utils.DirAndFileUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Server extends Thread {
    private ServerSocket serverSocket;
    private int port = 9250;


    public Server() throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    private void execute() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new Task(socket)).start();
        }
    }

    private static class Task implements Runnable {
        private DirAndFileUtil util = new DirAndFileUtil();
        private Socket socket;

        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                // 链接检查
                String s = inputStream.readUTF();
                System.out.println(s);
                outputStream.writeUTF("Hi !");
                // 选择要做的事
                int i = inputStream.readInt();
                System.out.println("Code:" + i);
                outputStream.writeUTF("Server is ready.");
                switch (i) {
                    case 12345:
                        util.synchronizeFiles(inputStream, outputStream);
                        break;
                    case 23456:
                        //同步文件至服务器
                        util.downloadFiles(outputStream, inputStream);
                        break;
                    case 34567:
                        //同步检查
                        break;
                    default:
                        break;
                }
                s = inputStream.readUTF(); //接收完成指令
                System.out.println(s);
                inputStream.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
