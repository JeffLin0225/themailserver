package com.example.themailserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.*;
import java.util.Properties;

public class SmtpServer {

    private static final int PORT = 2525;

    public static void startServer() throws IOException {
        // 使用 try-with-resources 確保 ServerSocket 會在結束時被關閉
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("SMTP server started on port " + PORT);

            while (true) {
                // Accept client connections
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Send welcome message
            out.println("220 localhost SMTP Server Ready");

            String from = null;
            String to = null;
            String subject = null;
            StringBuilder messageBody = new StringBuilder();

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Received: " + line);

                if (line.startsWith("HELO")) {
                    out.println("250 Hello");
                } else if (line.startsWith("MAIL FROM")) {
                    from = line.substring(10).trim();  // Extract the sender email
                    out.println("250 OK");
                } else if (line.startsWith("RCPT TO")) {
                    to = line.substring(8).trim();    // Extract the recipient email
                    out.println("250 OK");
                } else if (line.startsWith("DATA")) {
                    out.println("354 Start mail input");
                    // Read email content
                    while ((line = in.readLine()) != null && !line.equals(".")) {
                        if (line.startsWith("Subject:")) {
                            subject = line.substring(9).trim();
                        }
                        messageBody.append(line).append("\n");
                    }
                    out.println("250 OK: Message accepted");
                    
                    try {
                        forwardEmailToSMTPServer(to, subject, messageBody.toString());
                        System.out.println("轉寄了");
                    } catch (MessagingException e) {
                        System.err.println("Error forwarding email to SMTP server: " + e.getMessage());
                        e.printStackTrace();
                    }
                    System.out.println("傳給 hmailserver 了");
                    // Save the message to a local file
                    saveEmail(from, to, subject, messageBody.toString());
                    System.out.println("存在本地");
                    
                } else if (line.equals("QUIT")) {
                    out.println("221 Bye");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveEmail(String from, String to, String subject, String body) {
        // 使用當前時間戳生成文件名
        String timestamp = new Date().toString().replace(":", "_").replace(" ", "_");
        String filename = "mail_" + timestamp + ".eml";
        
        // 郵件內容格式
        String fileContent = "From: " + from + "\n" +
                "To: " + to + "\n" +
                "Subject: " + subject + "\n" +
                "\n" +
                body;

        // 指定絕對路徑
        String directoryPath = "C:\\Users\\jaxian\\Desktop\\themail";  // 替換成您希望存儲的路徑
        Path filePath = Paths.get(directoryPath + filename);

        // 確保目錄存在
        try {
            Files.createDirectories(Paths.get(directoryPath));  // 創建目錄（如果不存在）
            Files.write(filePath, fileContent.getBytes()); // 保存文件
            System.out.println("Email saved to file: " + filePath.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error saving email: " + e.getMessage());
        }
    }
    
    private static void forwardEmailToSMTPServer(String recipient, String subject, String messageBody) throws MessagingException {
        // Set SMTP properties (update with your own SMTP server details)
        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "25");
        props.put("mail.smtp.auth", "true");

        // Session
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("jeff@jxdns.ddnsking.com", "123456");
            }
        });

        // Create the email message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("jeff@jxdns.ddnsking.com"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        message.setText(messageBody);

        // Send the email
        Transport.send(message);
    }
    
}
