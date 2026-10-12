# Q28 Interkom Android prototipi

Bu proje Torima Q28 / `Q28interkom` WiFi kamerali interkom icin hazirlanan ilk Android prototipidir.

Bilinen cihaz bilgileri:

| Alan | Deger |
| --- | --- |
| WiFi SSID | `Q28interkom` |
| Sifre | Yok |
| Telefon IP | `192.168.25.51` |
| Cihaz/gateway IP | `192.168.25.1` |
| Calisan referans app | NiceDVR |
| Calismayan app | Roadcam |

## Su an ne yapar?

- Q28 WiFi agindayken `192.168.25.1` uzerinde yaygin kamera portlarini tarar.
- Yaygin HTTP/MJPEG canli yayin adreslerini dener.
- NiceDVR XAPK icinden cikan TUTK/GPCam native kutuphanelerini yuklemek icin `Native test` butonu icerir.
- `com/weapons18/api/v` ve `generalplus/com/GPCamLib/CamWrapper` JNI kopru siniflari projeye eklendi.
- Bulut ortaminda debug APK uretmek icin GitHub Actions dosyasi icerir.

## Onemli teknik not

NiceDVR calistigi, Roadcam calismadigi icin Q28'in Roadcam/iCatch yerine TUTK/GPCam tarzinda bir baglanti katmani kullandigi gorunuyor.
Bu yuzden tam canli goruntu/kayit kontrolu icin NiceDVR XAPK tekrar analiz edilip native baglanti katmani eklenmeli.

NiceDVR XAPK tekrar yuklenince su betik gerekli `.so` dosyalarini projeye koymak icin kullanilacak:

```bash
tools/prepare_nicedvr_native_libs.sh "/path/to/NiceDVR.xapk"
```

Bu native dosyalar kisisel test icindir; herkese acik sekilde dagitilmamalidir.

## Bulutta APK uretme

GitHub'a yuklendiginde `Actions > Build Q28 APK > Run workflow` ile debug APK uretilir.
Olusan dosya `q28-interkom-debug-apk` artifact'i olarak indirilebilir.
