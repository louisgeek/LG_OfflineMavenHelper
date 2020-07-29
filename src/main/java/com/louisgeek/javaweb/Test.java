package com.louisgeek.javaweb;

import java.io.*;
import java.net.Socket;

public class Test {
    public static void main(String[] args) throws Exception {
        String json = "";
        try {
            //创建一个客户端socket，
            Socket socket = new Socket("10.8.2.90", 3000);
            //向服务器端传递信息
            OutputStream ots = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(ots);
            //向服务器发送json
            pw.write("{\"request\":{\"function\":\"1005\",\"sip_type\":\"bsoft1\"},\"comment\":\"\"}");
            pw.flush();
            //关闭输出流
            socket.shutdownOutput();
            //获取服务器端传递的数据
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String info = null;
            StringBuilder jsonStringBuilder = new StringBuilder();
            while ((info = br.readLine()) != null) {
                jsonStringBuilder.append(info);
            }
            json = "" + jsonStringBuilder;
            System.out.println(jsonStringBuilder);
            //关闭资源
            System.out.println("准备关闭资源");
            br.close();
            isr.close();
            is.close();
            pw.close();
            ots.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
