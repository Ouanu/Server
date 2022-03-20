import utils.DirAndFileUtil;
import utils.SQLiteHelper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Server extends Thread {
    private ServerSocket serverSocket;
    private int port = 9250;
    private static Object lock = new Object();
    private static SQLiteHelper sqLiteHelper = new SQLiteHelper("C:\\Users\\Linkdamo\\Desktop\\server\\RES_DATABASE.db");


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
        private DirAndFileUtil util = new DirAndFileUtil(1);
        private Socket socket;

        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            synchronized (lock) {
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
                            if (sqLiteHelper.getDatabaseStatement()) {
                                int cnt = inputStream.readInt(); // 需要处理的数据数量
                                for (int j = 0; j < cnt; j++) {
                                    long uid = inputStream.readLong();
                                    if (sqLiteHelper.isExist(uid)) {
                                        long newDate = inputStream.readLong();
                                        if(sqLiteHelper.getUpdateDate(uid) != newDate) {
                                            //判断修改时间是否不同，若有改变则更新文件
                                            util.downloadFiles(outputStream, inputStream);
                                        } else {
                                            continue;
                                        }
                                    }
                                }
                            } else {
                                // 没有数据库，同步所有文件(包括数据库)
                                util.downloadFiles(outputStream, inputStream);
                                sqLiteHelper = new SQLiteHelper("C:\\Users\\Linkdamo\\Desktop\\server\\RES_DATABASE.db");
                            }
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
