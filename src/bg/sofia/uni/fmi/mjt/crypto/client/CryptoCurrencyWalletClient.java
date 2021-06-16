package bg.sofia.uni.fmi.mjt.crypto.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class CryptoCurrencyWalletClient {

    private static final int SERVER_PORT = 1978;
    private static final String SERVER_HOST = "localhost";
    private static final ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);

    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();

             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            while (true) {
                System.out.print("=> ");
                String message = scanner.nextLine() + System.lineSeparator();

                buffer.clear();
                buffer.put(message.getBytes());
                buffer.flip();
                socketChannel.write(buffer);

                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, StandardCharsets.UTF_8);

                System.out.println(reply);

                if (message.equals("disconnect" + System.lineSeparator())) {
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("There is a problem with the network");
        }
    }
}
