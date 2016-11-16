package com.yan.email;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/**
 * Created by Yan on 2016/11/13.
 */

public class SocketProcess {
    private Socket socket;
    private PrintWriter output; // 输出流
    private BufferedReader input; // 输入流
    private String user;
    private String pass;

    public SocketProcess(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public int getsum() throws IOException {
        //获取最新的邮件，必须重连
        int count = -1;
        //重新连接pop3
        socket = new Socket("pop3.163.com", 110);
        output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println(input.readLine());
        String username = user.substring(0, user.indexOf("@"));
        output.println("user " + username);
        output.flush();
        System.out.println(input.readLine());
        output.println("pass " + pass);
        output.flush();
        String rest = input.readLine();
        System.out.println(rest);
        if (rest.toLowerCase().contains("+ok")) {
            count = Integer.parseInt(rest.split(" ")[1]);
        }
        return count;
    }

    public int getsumnoc() throws IOException {
        int count = -1;
        if (socket == null || socket.isClosed() || !socket.getInetAddress().getHostName().equals("pop3.163.com")) {
            //获取最新的邮件，必须重连
            //重新连接pop3
            socket = new Socket("pop3.163.com", 110);
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println(input.readLine());
            String username = user.substring(0, user.indexOf("@"));
            output.println("user " + username);
            output.flush();
            System.out.println(input.readLine());
            output.println("pass " + pass);
            output.flush();
            System.out.println(input.readLine());
        }
        output.println("stat");
        output.flush();
        String rest = input.readLine();
        System.out.println(rest);
        if (rest.toLowerCase().contains("+ok")) {
            count = Integer.parseInt(rest.split(" ")[1]);
        }
        return count;
    }

    public boolean isok() throws IOException {
        if (socket == null || socket.isClosed() || !socket.getInetAddress().getHostName().equals("pop3.163.com")) {
            //重新连接pop3
            socket = new Socket("pop3.163.com", 110);
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println(input.readLine());
            String username = user.substring(0, user.indexOf("@"));
            output.println("user " + username);
            output.flush();
            System.out.println(input.readLine());
            output.println("pass " + pass);
            output.flush();
            String rest = input.readLine();
            System.out.println(rest);
            if (rest.toLowerCase().contains("+ok")) {
                System.out.println("登陆成功");
                return true;
            }
            return false;
        }
        return true;
    }

    //加入一个int参数，用于说明已读取的条数
    public ArrayList<Itemmail> getlist(int n) throws IOException {
        ArrayList<Itemmail> itemmails = new ArrayList<>();
        if (socket == null || socket.isClosed() || !socket.getInetAddress().getHostName().equals("pop3.163.com")) {
            socket = new Socket("pop3.163.com", 110);
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println(input.readLine());
            String username = user.substring(0, user.indexOf("@"));
            output.println("user " + username);
            output.flush();
            System.out.println(input.readLine());
            output.println("pass " + pass);
            output.flush();
            String result = input.readLine();
            System.out.println(result);
        }
        //获取sum
        output.println("stat");
        output.flush();
        String rest = input.readLine();
        System.out.println(rest);
        int sum = 0;
        if (rest.toLowerCase().contains("+ok")) {
            sum = Integer.parseInt(rest.split(" ")[1]);
        }
        System.out.println(sum);
        String server = null;
        int start = sum - n;
        int end = start - 10;
        //一次最多读十条
        for (int i = start; i > 0 && i > end; i--) {
            System.out.println(i);
            output.println("retr " + i);
            output.flush();
            server = null;
            Itemmail itemmail = new Itemmail();
            while (!(server = input.readLine()).equals(".")) {

                if (server.contains("(CST)")) {
                    String date = server;
                    date = date.trim();
                    date = date.substring(0, date.indexOf("(") - 1);
                    if (date.contains("Date:")) {
                        date = date.substring(6);
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
                    try {
                        Date d = sdf.parse(date);
                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        itemmail.time = sdf.format(d);
                    } catch (ParseException e) {
                        //e.printStackTrace();
                        itemmail.time = date;
                    }
                }
                if (server.contains("From:") && server.contains("<") && server.contains(">")) {
                    itemmail.from = server.substring(server.indexOf("<") + 1, server.indexOf(">"));
                }
                if (server.contains("Subject")) {
                    itemmail.subject = server;
                }
                itemmail.content += server;
                System.out.println(server);
            }
            itemmails.add(itemmail);
            System.out.println(i);
            System.out.println(i);
            System.out.println(i);
            System.out.println(i);
        }
        return itemmails;
    }

    //发送邮件
    public boolean sendMessage(String to, String subject, String content) throws IOException {
        if (socket == null || socket.isClosed() || !socket.getInetAddress().getHostName().equals("smtp.163.com")) {
            System.out.println(user);
            System.out.println(pass);
            socket = new Socket("smtp.163.com", 25);
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println(input.readLine());

            output.println("helo smtp.163.com");
            output.flush();
            System.out.println(input.readLine());
            //验证登陆
            output.println("auth login");
            output.flush();
            System.out.println(input.readLine());
            //用户名
            String base64user = Base64.encodeToString(user.getBytes(), Base64.NO_WRAP);
            output.println(base64user);
            output.flush();
            System.out.println(input.readLine());
            //密码
            String base64pass = Base64.encodeToString(pass.getBytes(), Base64.NO_WRAP);
            output.println(base64pass);
            output.flush();
            System.out.println(input.readLine());
        }
        //sendmessage
        output.println("mail from: <" + user + ">");
        output.flush();
        System.out.println(input.readLine()); //收件人
        output.println("rcpt to: <" + to + ">");
        output.flush();
        System.out.println(input.readLine());
        //内容
        output.println("data");
        output.flush();
        System.out.println(input.readLine());
        String con = "From: 网易邮箱<" + user + ">\r\n";
        con += "To: <" + to + ">\r\n";
        con = con + "Subject: " + subject + "\r\n";
        con = con + "Content-Type: text/plain;charset=\"utf-8\"\r\n";
        con = con + "\r\n";
        con = con + content + "\r\n";
        con = con + ".\r\n";
        output.println(con);
        output.flush();
        String rest = input.readLine();
        System.out.println(rest);
        if (rest == null || !rest.toLowerCase().contains("ok")) {
            return false;
        }
        return true;
    }

    public Itemmail getitemmail(int position) throws IOException {
        Itemmail itemmail = new Itemmail();
        if (socket == null || socket.isClosed() || !socket.getInetAddress().getHostName().equals("pop3.163.com")) {
            socket = new Socket("pop3.163.com", 110);
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println(input.readLine());
            String username = user.substring(0, user.indexOf("@"));
            output.println("user " + username);
            output.flush();
            System.out.println(input.readLine());
            output.println("pass " + pass);
            output.flush();
            System.out.println(input.readLine());
        }
        output.println("retr " + position);
        output.flush();
        String server = null;
        while (!(server = input.readLine()).equals(".")) {
            if (server.contains("(CST)")) {
                String date = server;
                date = date.trim();
                date = date.substring(0, date.indexOf("(") - 1);
                if (date.contains("Date:")) {
                    date = date.substring(6);
                }
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
                try {
                    Date d = sdf.parse(date);
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    itemmail.time = sdf.format(d);
                } catch (ParseException e) {
                    //e.printStackTrace();
                    itemmail.time = date;
                }
            }
            if (server.contains("From:") && server.contains("<") && server.contains(">")) {
                itemmail.from = server.substring(server.indexOf("<") + 1, server.indexOf(">"));
            }
            if (server.contains("Subject")) {
                itemmail.subject = server;
            }
            itemmail.content += server;
            System.out.println(server);
        }
        return itemmail;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
