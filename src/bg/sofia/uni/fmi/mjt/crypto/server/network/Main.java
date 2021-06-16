package bg.sofia.uni.fmi.mjt.crypto.server.network;


public class Main {
    public static final String logDestination = "logger.txt";

    public static void main(String[] args) {
        CryptoCurrencyWalletServer cryptoCurrencyWalletServer = new CryptoCurrencyWalletServer();
        cryptoCurrencyWalletServer.start();
        cryptoCurrencyWalletServer.stop();

//        String line = "";
//        Scanner scanner = new Scanner(System.in);
//        while(true) {
//            line = scanner.nextLine();
//            if (line.equals("start")) {
//                cryptoCurrencyWalletServer.start();
//                System.out.println("server started");
//            } else if (line.equals("stop")) {
//                cryptoCurrencyWalletServer.stop();
//                System.out.println("server stopped");
//            } else if (line.equals("end")) {
//                System.out.println("goodbye");
//                break;
//            } else if (line.equals("load")) {
//                cryptoCurrencyWalletServer.load();
//                break;
//            } else if (line.equals("save")) {
//                cryptoCurrencyWalletServer.save();
//                break;
//            }
//        }
    }
}
