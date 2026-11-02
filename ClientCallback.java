import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void thongBao(String noiDung) throws RemoteException;
}
