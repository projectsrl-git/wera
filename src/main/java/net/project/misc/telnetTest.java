
package net.project.misc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class telnetTest {

    public static void main(String[] args) {

        Socket _socket = null;

        try {
            int argc = args.length;

            if (argc < 2) {
                System.out.println("Uso: telnetTest <host destinazione> <porta> [<host src> [<messaggio>]]");
                return;
            }

            String host = args[0];
            String port = args[1];
            int iport = Integer.parseInt(port);

            String srcIP = "";
            int timeout = 30000;

            if (argc > 2) {
                srcIP = args[2];
            }

            String mess = "";
            if (argc > 3) {
                mess = args[3];
            }

            if (srcIP.equals("")) {
                _socket = new Socket(host, iport);
            } else {
                InetAddress localIP = InetAddress.getByName(srcIP);
                _socket = new Socket(host, iport, localIP, 0);
            }
            _socket.setSoTimeout(timeout);
            _socket.setSoLinger(true, 10000);
            _socket.setTcpNoDelay(true);

            DataInputStream input = new DataInputStream(_socket.getInputStream());
            DataOutputStream output = new DataOutputStream(_socket.getOutputStream());

            output.write(mess.getBytes(), 0, mess.length());
            output.flush();

            _socket.close();
            System.out.println("Ok");

        } catch (Throwable ex) {
            ex.printStackTrace(System.out);
        }

    }

}
