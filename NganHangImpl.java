import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class NganHangImpl extends UnicastRemoteObject implements NganHang {
    private final Map<String, TaiKhoan> danhSach = new LinkedHashMap<>();
    private final Map<String, List<ClientCallback>> clients = new HashMap<>();
    private final Path file = Paths.get("taikhoan.csv");

    public NganHangImpl() throws IOException {
        super(1100);
        // Doc tai khoan tu file CSV
        List<String> dong = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 1; i < dong.size(); i++) {
            if (dong.get(i).trim().isEmpty()) continue;
            String[] cot = dong.get(i).split(",", -1);
            if (cot.length != 3) throw new IOException("Dong CSV khong hop le: " + (i + 1));
            long soDu = Long.parseLong(cot[2]);
            if (soDu < 0 || danhSach.containsKey(cot[0])) throw new IOException("Tai khoan khong hop le");
            danhSach.put(cot[0], new TaiKhoan(cot[0], cot[1], soDu));
        }
    }

    private TaiKhoan tim(String so) throws RemoteException {
        TaiKhoan tk = danhSach.get(so);
        if (tk == null) throw new RemoteException("Tai khoan khong ton tai: " + so);
        return tk;
    }

    private void kiemTraTien(long tien) throws RemoteException {
        if (tien <= 0) throw new RemoteException("So tien phai lon hon 0");
    }

    private long congTien(long a, long b) throws RemoteException {
        if (a > Long.MAX_VALUE - b) throw new RemoteException("So du vuot gioi han");
        return a + b;
    }

    private void luuFile() throws RemoteException {
        List<String> dong = new ArrayList<>();
        dong.add("soTaiKhoan,hoTen,soDu");
        for (TaiKhoan tk : danhSach.values()) {
            dong.add(tk.soTaiKhoan + "," + tk.hoTen + "," + tk.soDu);
        }
        // Ghi file tam de tranh ghi do du lieu
        Path tam = Paths.get("taikhoan.csv.tmp");
        try {
            Files.write(tam, dong, StandardCharsets.UTF_8);
            Files.move(tam, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new RemoteException("Khong luu duoc file tai khoan", e);
        }
    }

    public synchronized TaiKhoan vanTin(String so) throws RemoteException {
        TaiKhoan tk = tim(so);
        return new TaiKhoan(tk.soTaiKhoan, tk.hoTen, tk.soDu);
    }

    public synchronized long napTien(String so, long tien) throws RemoteException {
        kiemTraTien(tien);
        TaiKhoan tk = tim(so);
        long cu = tk.soDu;
        tk.soDu = congTien(cu, tien);
        try { luuFile(); } catch (RemoteException e) { tk.soDu = cu; throw e; }
        return tk.soDu;
    }

    public synchronized long rutTien(String so, long tien) throws RemoteException {
        kiemTraTien(tien);
        TaiKhoan tk = tim(so);
        if (tk.soDu < tien) throw new RemoteException("So du khong du");
        long cu = tk.soDu;
        tk.soDu -= tien;
        try { luuFile(); } catch (RemoteException e) { tk.soDu = cu; throw e; }
        return tk.soDu;
    }

    public long chuyenKhoan(String gui, String nhan, long tien, String noiDung) throws RemoteException {
        List<ClientCallback> nguoiNhan;
        long conLai;
        synchronized (this) {
            kiemTraTien(tien);
            if (gui.equals(nhan)) throw new RemoteException("Hai tai khoan phai khac nhau");
            TaiKhoan a = tim(gui);
            TaiKhoan b = tim(nhan);
            if (a.soDu < tien) throw new RemoteException("So du khong du");
            long cuA = a.soDu, cuB = b.soDu;
            long moiB = congTien(cuB, tien);
            a.soDu -= tien;
            b.soDu = moiB;
            try { luuFile(); } catch (RemoteException e) {
                a.soDu = cuA;
                b.soDu = cuB;
                throw e;
            }
            conLai = a.soDu;
            nguoiNhan = new ArrayList<>(clients.getOrDefault(nhan, Collections.emptyList()));
        }
        // Goi callback ngoai khoa de client khong lam tre giao dich khac
        for (ClientCallback client : nguoiNhan) {
            try {
                client.thongBao("Đã nhận: " + tien + "; Tài khoản gởi: " + gui + ";     Nội dung: " + noiDung);
            } catch (RemoteException e) {
                huyDangKy(nhan, client);
            }
        }
        return conLai;
    }

    public synchronized void dangKy(String so, ClientCallback client) throws RemoteException {
        tim(so);
        if (client == null) throw new RemoteException("Client khong hop le");
        List<ClientCallback> ds = clients.computeIfAbsent(so, k -> new ArrayList<>());
        if (!ds.contains(client)) ds.add(client);
    }

    public synchronized void huyDangKy(String so, ClientCallback client) {
        List<ClientCallback> ds = clients.get(so);
        if (ds != null) {
            ds.remove(client);
            if (ds.isEmpty()) clients.remove(so);
        }
    }
}
