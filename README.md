# Bai tap RMI - eBanking

Java thuan, giao dien Swing theo mau trang 36 cua Bai 8-2.pdf: bon dong nhap, bon nut ben phai va vung thong bao ben duoi. Khong can Maven, Gradle, rmic hay chay rmiregistry rieng. Can JDK 8 tro len va moi truong co giao dien de mo Client.

## Bien dich

Mo terminal tai thu muc nay:

```sh
javac -encoding UTF-8 *.java
```

Chi bien dich lai khi sua ma nguon. Sau khi co file .class, cac lan sau chi chay lenh java.

## Chay tren cung may

Terminal 1:

```sh
java Server
```

Terminal 2:

```sh
java Client
```

Client mac dinh dung tai khoan 0123. Mo them terminal 3 de dung tai khoan 1234:

```sh
java Client 127.0.0.1 1234
```

Du lieu ban dau:

| So tai khoan | Ho ten | So du |
| --- | --- | --- |
| 0123 | Nguyen Van An | 5000 |
| 1234 | Tran Thi Binh | 10000 |
| 5678 | Le Van Cuong | 15000 |

## Thu giong vi du trong slide

1. Tren Client 0123, bam Van tin: so du 5000.
2. Nhap so tien 1000, bam Nap tien: so du 6000.
3. Nhap so tien 500, bam Rut tien: so du 5500.
4. Tren Client 1234, nhap so tien 1000, tai khoan nhan 0123, noi dung `tra tien`, bam Chuyen khoan.
5. Client 0123 tu hien thong bao nhan tien qua callback, khong can bam Van tin. Bam Van tin se thay so du 6500.
6. Tat Server bang Ctrl+C, chay lai `java Server`: so du van duoc giu trong file.

So tien la so nguyen duong, nhap lien nhu 1000, khong nhap dau cham hay dau phay. Tai khoan goc duoc chon luc mo Client va khong sua trong cua so de callback luon dung tai khoan. Muon doi tai khoan thi mo Client voi so tai khoan khac.

## Chay tren mang LAN

Vi du IP Server la 192.168.1.10, IP Client la 192.168.1.20:

```sh
java Server 192.168.1.10
java Client 192.168.1.10 0123 192.168.1.20
```

Lenh Server chay tren may Server, lenh Client chay tren may Client. Tham so cuoi cua Client la IP cua chinh may Client de Server goi callback. Cho phep Java qua tuong lua tren hai may; Server dung TCP 1099, 1100, Client dung cong callback tu dong. Cac may can lien lac truc tiep duoc voi nhau.

## Cac file

- `NganHang.java`: giao tiep RMI cho cac giao dich.
- `ClientCallback.java`: giao tiep callback de Server goi Client.
- `TaiKhoan.java`: doi tuong Serializable truyen thong tin tai khoan.
- `NganHangImpl.java`: xu ly giao dich, doc ghi CSV va gui callback.
- `Server.java`: tao Registry va dang ky dich vu eBanking.
- `Client.java`: giao dien Swing va nhan callback.
- `taikhoan.csv`: du lieu tai khoan, duoc cap nhat sau moi giao dich.
- `.gitignore`: bo qua file bien dich va file tam.

Chay mot Server cho mot file CSV. Chi sua CSV khi Server da tat. Moi dong gom so tai khoan, ho ten va so du; khong dat dau phay trong ho ten. Day la bai mo phong, chon tai khoan bang tham so, khong co dang nhap hay mat khau.

Server khoa viec cap nhat so du khi co nhieu client, kiem tra so du, so tien va tai khoan nhan. Neu ghi file that bai thi hoan lai so du trong bo nho. Callback gui cho cac cua so dang mo cua tai khoan nhan; neu client da tat thi tien van duoc luu, khong luu thong bao de gui lai.
