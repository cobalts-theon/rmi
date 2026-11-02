import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NganHang extends Remote {
    TaiKhoan vanTin(String soTaiKhoan) throws RemoteException;
    long napTien(String soTaiKhoan, long soTien) throws RemoteException;
    long rutTien(String soTaiKhoan, long soTien) throws RemoteException;
    long chuyenKhoan(String gui, String nhan, long soTien, String noiDung) throws RemoteException;
    void dangKy(String soTaiKhoan, ClientCallback client) throws RemoteException;
    void huyDangKy(String soTaiKhoan, ClientCallback client) throws RemoteException;
}
