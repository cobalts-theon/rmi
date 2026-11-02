import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            // Truyen IP server khi chay tren mang LAN
            System.setProperty("java.rmi.server.hostname", args.length > 0 ? args[0] : "127.0.0.1");
            NganHangImpl nganHang = new NganHangImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("eBanking", nganHang);
            System.out.println("Server eBanking dang chay, cong 1099 va 1100.");
            System.out.println("Du lieu: taikhoan.csv");
        } catch (Exception e) {
            System.err.println("Khong khoi dong duoc server: " + e.getMessage());
            System.exit(1);
        }
    }
}
