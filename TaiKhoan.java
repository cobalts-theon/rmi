import java.io.Serializable;

public class TaiKhoan implements Serializable {
    private static final long serialVersionUID = 1L;
    public String soTaiKhoan;
    public String hoTen;
    public long soDu;

    public TaiKhoan(String soTaiKhoan, String hoTen, long soDu) {
        this.soTaiKhoan = soTaiKhoan;
        this.hoTen = hoTen;
        this.soDu = soDu;
    }
}
