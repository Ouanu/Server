import data.ResData;
import utils.DirAndFileUtil;
import utils.SQLiteHelper;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Server extends Thread {
    private final ServerSocket serverSocket;
    private static final Object lock = new Object();
    private SQLiteHelper sqLiteHelper;


    public Server() throws IOException {
        int port = 9250;
        this.serverSocket = new ServerSocket(port);
        sqLiteHelper = new SQLiteHelper("C:\\Users\\Linkdamo\\Desktop\\server\\database\\RES_DATABASE.db");
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void execute() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new Task(socket)).start();
        }
    }

    private class Task implements Runnable {
        private final DirAndFileUtil util = new DirAndFileUtil(1);
        private final Socket socket;

        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            DataInputStream inputStream = null;
            DataOutputStream outputStream = null;
            synchronized (lock) {
                try {
                    inputStream = new DataInputStream(socket.getInputStream());
                    outputStream = new DataOutputStream(socket.getOutputStream());
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
                                System.out.println("有数据库");
                                outputStream.writeLong(1000L);
                                boolean isChange = false;
                                int cnt = inputStream.readInt(); // 需要处理的数据数量
                                System.out.println("需要处理的文件数量" + cnt);
                                for (int j = 0; j < cnt; j++) {
                                    long uid = inputStream.readLong();
                                    long newDate;
                                    if (sqLiteHelper.isExist(uid)) {
                                        newDate = inputStream.readLong();
                                        outputStream.writeInt(999);
                                        if(sqLiteHelper.getUpdateDate(uid) != newDate) {
                                            //判断修改时间是否不同，若有改变则更新文件
                                            outputStream.writeBoolean(true);
                                            util.receiveFiles(outputStream, inputStream);
                                            isChange = true;
                                        } else {
                                            outputStream.writeBoolean(false);
                                        }
                                    } else {
                                        newDate = inputStream.readLong();
                                        outputStream.writeInt(900);
                                        util.receiveFiles(outputStream, inputStream);
                                        isChange = true;
                                    }
                                    outputStream.flush();
                                }
                                if (isChange) {
                                    System.out.println("数据库需要更新");
                                    sqLiteHelper.shutdownSQL();
                                    sqLiteHelper.deleteSQL();
                                    outputStream.writeUTF("数据库关闭");
                                    int c = inputStream.readInt();
                                    System.out.println("数据库文件：" + c);
                                    boolean isSql = false;
                                    for (int t = 0; t < c; t++) {
                                        isSql = util.getSQL(inputStream);
                                    }
                                    if (isSql) {
                                        // 重新读取数据库
//                                        outputStream.writeUTF("数据库传输完成");
                                        sqLiteHelper.shutdownSQL();
                                        sqLiteHelper = new SQLiteHelper("C:\\Users\\Linkdamo\\Desktop\\server\\database\\RES_DATABASE.db");
                                    } else {
                                        System.out.println("数据库传输失败");
                                    }

                                } else {
                                    System.out.println("数据库不需要更新");
                                }
                            } else {
                                outputStream.writeLong(1111L);
                                // 没有数据库，同步所有文件(包括数据库)
                                util.downloadFiles(outputStream, inputStream);
                                int cnt = inputStream.readInt();
                                System.out.println("database :" + cnt);
                                boolean isSql = false;
                                for (int t = 0; t < cnt; t++) {
                                    isSql = util.getSQL(inputStream);
                                }
                                if (isSql) {
                                    // 重新读取数据库
                                    outputStream.writeUTF("数据库传输完成");
                                    sqLiteHelper = new SQLiteHelper("C:\\Users\\Linkdamo\\Desktop\\server\\database\\RES_DATABASE.db");
                                } else {
                                    System.out.println("数据库传输失败");
                                }
//                                sqLiteHelper = new SQLiteHelper("C:\\Users\\Linkdamo\\Desktop\\server\\database\\RES_DATABASE.db");
                            }
                            break;
                        default:
                            break;
                    }
//                    s = inputStream.readUTF(); //接收完成指令
                    System.out.println("FINISH");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
//                        socket.getOutputStream().flush();
//                        socket.getInputStream().reset();
                        inputStream.close();
                        outputStream.close();
                        socket.close();
//                        sqLiteHelper.shutdownSQL();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

//    public static void main(String[] args) {
//        try {
//            Server server = new Server();
//            server.execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    public String getIPAddress() {
        InetAddress localHost = getLocalHostExactAddress();

        return localHost.toString();
    }

    public ArrayList<String> getList() {
        HashMap<Long, ResData> idList = sqLiteHelper.getIdList();
        ArrayList<String> list = new ArrayList<>();
        for (Long aLong : idList.keySet()) {
            list.add(idList.get(aLong).getDirName());
        }
        return list;
    }

    public static InetAddress getLocalHostExactAddress() {
        try {
            InetAddress candidateAddress = null;

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();
                // 该网卡接口下的ip会有多个，也需要一个个的遍历，找到自己所需要的
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    // 排除loopback回环类型地址（不管是IPv4还是IPv6 只要是回环地址都会返回true）
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了 就是我们要找的
                            // ~~~~~~~~~~~~~绝大部分情况下都会在此处返回你的ip地址值~~~~~~~~~~~~~
                            return inetAddr;
                        }

                        // 若不是site-local地址 那就记录下该地址当作候选
                        if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }

                    }
                }
            }

            // 如果出去loopback回环地之外无其它地址了，那就回退到原始方案吧
            return candidateAddress == null ? InetAddress.getLocalHost() : candidateAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getV4IP() {
        String ip = "";
        String chinaz = "https://ip.chinaz.com/";

        String inputLine = "";
        String read = "";

        try {
            URL url = new URL(chinaz);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            while ((read = in.readLine()) != null) {
                inputLine += read;
            }
            System.out.println(inputLine);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 正则匹配标签
        Pattern pattern = Pattern.compile("\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>");
        Matcher matcher = pattern.matcher(inputLine);
        if (matcher.find()) {
            ip = matcher.group();
            System.out.println(ip);
        }
        return ip;
    }
}
