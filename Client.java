import java.awt.*;
import java.awt.event.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import javax.swing.*;

public class Client extends JFrame {
    private final JTextField taiKhoan = new JTextField();
    private final JTextField soTien = new JTextField();
    private final JTextField nguoiNhan = new JTextField();
    private final JTextField noiDung = new JTextField();
    private final JTextArea ketQua = new JTextArea();
    private final JButton[] nut = new JButton[4];
    private NganHang nganHang;
    private ClientCallback callback;
    private String taiKhoanDangKy;

    public Client(String host, String so) {
        super(so);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(590, 460);
        setLocationByPlatform(true);
        taiKhoan.setText(so);
        taiKhoan.setEditable(false);
        ketQua.setEditable(false);
        ketQua.setLineWrap(true);
        ketQua.setWrapStyleWord(true);
        ketQua.setFont(new Font("Dialog", Font.PLAIN, 14));
        JPanel form = new JPanel(new GridLayout(4, 3, 2, 2));
        String[] nhan = {"Tài khoản gốc:", "Số tiền:", "Tài khoản nhận:", "Nội dung:"};
        String[] tenNut = {"Vấn tin", "Nạp tiền", "Rút tiền", "Chuyển khoản"};
        JTextField[] oNhap = {taiKhoan, soTien, nguoiNhan, noiDung};
        for (int i = 0; i < 4; i++) {
            JLabel label = new JLabel(nhan[i]);
            label.setFont(new Font("Dialog", Font.BOLD, 14));
            form.add(label);
            form.add(oNhap[i]);
            nut[i] = new JButton(tenNut[i]);
            final int chon = i;
            nut[i].addActionListener(e -> giaoDich(chon));
            form.add(nut[i]);
        }
        form.setPreferredSize(new Dimension(580, 220));
        add(form, BorderLayout.NORTH);
        add(new JScrollPane(ketQua), BorderLayout.CENTER);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { dongCua(); }
        });
        setVisible(true);
        batNut(false);
        // Ket noi o luong nen de giao dien khong bi dung
        new SwingWorker<TaiKhoan, Void>() {
            protected TaiKhoan doInBackground() throws Exception {
                nganHang = (NganHang) LocateRegistry.getRegistry(host, 1099).lookup("eBanking");
                callback = new ThongBaoClient();
                nganHang.dangKy(so, callback);
                taiKhoanDangKy = so;
                return nganHang.vanTin(so);
            }
            protected void done() {
                try {
                    ghi("Số dư: " + get().soDu);
                    batNut(true);
                } catch (Exception e) {
                    ghi("Lỗi kết nối: " + loi(e));
                    ghi("Hãy chạy Server trước, kiểm tra tài khoản rồi mở lại Client.");
                }
            }
        }.execute();
    }

    private class ThongBaoClient extends UnicastRemoteObject implements ClientCallback {
        ThongBaoClient() throws java.rmi.RemoteException { super(0); }
        public void thongBao(String tin) {
            // Cap nhat Swing tu luong giao dien
            SwingUtilities.invokeLater(() -> ghi(tin));
        }
    }

    private void batNut(boolean bat) {
        for (JButton b : nut) b.setEnabled(bat);
    }

    private void ghi(String tin) {
        ketQua.append(tin + "\n");
        ketQua.setCaretPosition(ketQua.getDocument().getLength());
    }

    private String loi(Exception e) {
        Throwable goc = e;
        while (goc.getCause() != null) goc = goc.getCause();
        return goc.getMessage() == null ? goc.toString() : goc.getMessage();
    }

    private void giaoDich(int chon) {
        String nhan = nguoiNhan.getText().trim();
        String nd = noiDung.getText().trim();
        long tien = 0;
        if (chon != 0) {
            try {
                tien = Long.parseLong(soTien.getText().trim());
                if (tien <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                ghi("Lỗi: nhập số tiền nguyên lớn hơn 0.");
                return;
            }
        }
        final long so = tien;
        batNut(false);
        new SwingWorker<String, Void>() {
            protected String doInBackground() throws Exception {
                String tk = taiKhoanDangKy;
                if (chon == 0) return "Số dư: " + nganHang.vanTin(tk).soDu;
                if (chon == 1) return "Đã nạp: " + so + ";   Số dư hiện tại: " + nganHang.napTien(tk, so);
                if (chon == 2) return "Đã rút: " + so + ";   Số dư hiện tại: " + nganHang.rutTien(tk, so);
                long soDu = nganHang.chuyenKhoan(tk, nhan, so, nd);
                return "Đã chuyển: " + so + "; Tài khoản nhận: " + nhan
                        + ";     Nội dung: " + nd + "\nSố dư hiện tại: " + soDu;
            }
            protected void done() {
                try { ghi(get()); } catch (Exception e) { ghi("Lỗi: " + loi(e)); }
                batNut(true);
            }
        }.execute();
    }

    private void dongCua() {
        batNut(false);
        new Thread(() -> {
            try {
                if (taiKhoanDangKy != null) nganHang.huyDangKy(taiKhoanDangKy, callback);
            } catch (Exception e) {
                System.err.println("Khong huy duoc dang ky callback");
            } finally {
                try { if (callback != null) UnicastRemoteObject.unexportObject(callback, true); }
                catch (Exception e) { System.err.println(e.getMessage()); }
                System.exit(0);
            }
        }).start();
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        String so = args.length > 1 ? args[1] : "0123";
        if (args.length > 2) System.setProperty("java.rmi.server.hostname", args[2]);
        SwingUtilities.invokeLater(() -> new Client(host, so));
    }
}
