<<<<<<< HEAD
package client;

import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 9090);
        new LoginFrame(socket);
    }
}
=======
public static
>>>>>>> 5775168b6626de6d3a1a06ac098687c15f20323f
