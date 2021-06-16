package bg.sofia.uni.fmi.mjt.crypto.server.network;

import bg.sofia.uni.fmi.mjt.crypto.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.crypto.server.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.crypto.server.currencyinfo.CoinApiClient;
import bg.sofia.uni.fmi.mjt.crypto.server.exceptions.CommandCreatorException;
import bg.sofia.uni.fmi.mjt.crypto.server.users.UsersController;
import com.google.gson.Gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CryptoCurrencyWalletServer {
    public final int serverPort = 1978;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024 * 1024;
    private final AtomicBoolean isRunning;

    private final CommandCreator commandCreator;
    private final CommandExecutor commandExecutor;
    private UsersController usersController;
    private final CoinApiClient coinApiClient;
    private final Logger logger;
    private Selector selector;

    public CryptoCurrencyWalletServer() {
        isRunning = new AtomicBoolean(false);

        commandCreator = new CommandCreator();

        coinApiClient = new CoinApiClient(HttpClient.newHttpClient());
        usersController = new UsersController(coinApiClient);
        commandExecutor = new CommandExecutor(usersController, coinApiClient);
        selector = null;

        logger = Logger.getGlobal();
        try {
            FileHandler fh = new FileHandler(Main.logDestination, true);
            logger.addHandler(fh);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "file logger could not be initialized");
        }

    }

    public void start() {
        if (!isRunning.get()) {
            isRunning.set(true);

            new Thread(this::run).start();
        }

    }

    public void stop() {
        isRunning.compareAndSet(true, false);
        selector.wakeup();
    }


    public void load() {
        try (FileReader fr = new FileReader("users.json")) {
            Gson gson = new Gson();
            usersController = gson.fromJson(fr, UsersController.class);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "could not load users");
        }
    }

    public void save() {
        try (FileWriter fw = new FileWriter("users.json")) {
            Gson gson = new Gson();
            fw.write(gson.toJson(usersController));
            fw.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "could not save users");
        }
    }

    public void run() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, serverPort));
            serverSocketChannel.configureBlocking(false);

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (isRunning.get()) {
                int readyChannels;
                    readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {

                        SocketChannel sc = (SocketChannel) key.channel();

                        buffer.clear();
                        int r = sc.read(buffer);
                        if (r < 0) {
                            sc.close();
                            continue;
                        }

                        buffer.flip();
                        byte[] byteArray = new byte[buffer.remaining()];
                        buffer.get(byteArray);
                        String text = new String(byteArray, StandardCharsets.UTF_8);

                        String reply;
                        try {
                            reply = commandExecutor.executeCommand(
                                    key, commandCreator.commandOf(text)) + '\n';
                        } catch (CommandCreatorException e) {
                            reply = "error in parsing command: " + e.getMessage();
                            logger.log(Level.WARNING, "error in parsing command");
                        }
                        buffer.clear();
                        buffer.put(reply.getBytes());
                        buffer.flip();
                        sc.write(buffer);

                    } else if (key.isAcceptable()) {
                        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
                        SocketChannel accept = sockChannel.accept();
                        accept.configureBlocking(false);
                        accept.register(selector, SelectionKey.OP_READ);
                    }

                    keyIterator.remove();
                }

            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "communication breakdown");
        }
    }
}
