package org.thehellnet.java.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by sardylan on 24/03/15.
 */
public class HellSocket {
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    private String host;
    private int port;

    public HellSocket(String host, int port) {
        this.host = host;
        this.port = port;
        this.socket = null;
    }

    public void connect() throws IOException {
        if (socket != null) {
            return;
        }

        socket = new Socket(this.host, this.port);
        out = new PrintWriter(socket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void disconnect() throws IOException {
        if (socket == null || !socket.isConnected()) {
            return;
        }

        in.close();
        out.close();
        socket.close();
    }

    public void send(String message) {
        if (out == null || !socket.isConnected()) {
            return;
        }
        if (message == null || message.length() == 0) {
            return;
        }

        out.write(message);
        out.flush();
    }

    public String recv() {
        if(in == null || !socket.isConnected()) {
            return "";
        }

        String message = "";

        try {
            message = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message.trim();
    }

    public final boolean isConnected() {
        return socket != null && socket.isConnected();
    }
}
