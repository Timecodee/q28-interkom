package com.ugur.q28interkom;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.SocketFactory;

import generalplus.com.GPCamLib.CamWrapper;

public class MainActivity extends Activity {
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    private EditText hostInput;
    private TextView status;
    private TextView logView;
    private ImageView preview;
    private volatile boolean streaming;

    private static final String[] HTTP_PATHS = new String[] {
            "/",
            "/index.html",
            "/?action=stream",
            "/videostream.cgi",
            "/video.cgi",
            "/mjpeg.cgi",
            "/livestream.cgi",
            "/stream.cgi",
            "/live",
            "/live.mjpeg",
            "/live/0",
            "/live/1",
            "/mjpeg",
            "/MJPEG",
            "/video",
            "/stream",
            "/webcam",
            "/video.mjpg",
            "/mjpg/video.mjpg",
            "/cgi-bin/hi3510/mjpegstream.cgi?-chn=11"
    };

    private static final int[] PORTS = new int[] {80, 8080, 8081, 554, 8554, 7070, 7878, 3333};
    private static final int[] HTTP_PORTS = new int[] {8080, 8081, 80};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildUi());
        append("Q28 WiFi agina baglan: Q28interkom");
        append("Varsayilan cihaz IP: 192.168.25.1");
    }

    @Override
    protected void onDestroy() {
        streaming = false;
        worker.shutdownNow();
        super.onDestroy();
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Q28 Interkom");
        title.setTextColor(Color.rgb(15, 23, 42));
        title.setTextSize(24);
        title.setGravity(Gravity.START);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        status = new TextView(this);
        status.setText("Hazir");
        status.setTextColor(Color.rgb(71, 85, 105));
        status.setPadding(0, dp(6), 0, dp(12));
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));

        hostInput = new EditText(this);
        hostInput.setSingleLine(true);
        hostInput.setInputType(InputType.TYPE_CLASS_TEXT);
        hostInput.setText("192.168.25.1");
        hostInput.setHint("Cihaz IP");
        root.addView(hostInput, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, dp(12), 0, dp(12));
        root.addView(buttons, new LinearLayout.LayoutParams(-1, -2));

        Button scan = button("Tara");
        scan.setOnClickListener(v -> scanDevice());
        buttons.addView(scan, new LinearLayout.LayoutParams(0, dp(48), 1));

        Button live = button("Canli dene");
        live.setOnClickListener(v -> tryLive());
        LinearLayout.LayoutParams liveParams = new LinearLayout.LayoutParams(0, dp(48), 1);
        liveParams.setMargins(dp(8), 0, 0, 0);
        buttons.addView(live, liveParams);

        Button nativeTest = button("Native test");
        nativeTest.setOnClickListener(v -> testNative());
        LinearLayout.LayoutParams nativeParams = new LinearLayout.LayoutParams(0, dp(48), 1);
        nativeParams.setMargins(dp(8), 0, 0, 0);
        buttons.addView(nativeTest, nativeParams);

        LinearLayout diagButtons = new LinearLayout(this);
        diagButtons.setOrientation(LinearLayout.HORIZONTAL);
        diagButtons.setPadding(0, 0, 0, dp(12));
        root.addView(diagButtons, new LinearLayout.LayoutParams(-1, -2));

        Button raw = button("Raw oku");
        raw.setOnClickListener(v -> rawProbe());
        diagButtons.addView(raw, new LinearLayout.LayoutParams(0, dp(48), 1));

        Button gpcam = button("GPCam dene");
        gpcam.setOnClickListener(v -> tryGpcam());
        LinearLayout.LayoutParams gpcamParams = new LinearLayout.LayoutParams(0, dp(48), 1);
        gpcamParams.setMargins(dp(8), 0, 0, 0);
        diagButtons.addView(gpcam, gpcamParams);

        preview = new ImageView(this);
        preview.setBackgroundColor(Color.rgb(15, 23, 42));
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(preview, new LinearLayout.LayoutParams(-1, 0, 1));

        ScrollView scroll = new ScrollView(this);
        logView = new TextView(this);
        logView.setTextColor(Color.rgb(30, 41, 59));
        logView.setTextSize(13);
        logView.setPadding(0, dp(12), 0, 0);
        scroll.addView(logView);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, dp(180)));

        return root;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(text);
        return b;
    }

    private void scanDevice() {
        String host = hostInput.getText().toString().trim();
        streaming = false;
        setStatus("Portlar taraniyor...");
        append("Tarama basladi: " + host);
        worker.submit(() -> {
            Network wifi = bindToWifiNetwork();
            if (wifi == null) {
                setStatus("Q28 WiFi agi uygulamaya baglanamadi.");
                append("WiFi network bulunamadi. Q28interkom agina bagli oldugundan emin ol.");
                appendNetworkState();
                return;
            }

            List<String> open = new ArrayList<>();
            for (int port : PORTS) {
                if (isOpen(wifi, host, port, 800)) {
                    open.add(String.valueOf(port));
                    append("Acik port bulundu: " + port);
                }
            }
            if (open.isEmpty()) {
                setStatus("Port bulunamadi. Telefon Q28 WiFi aginda mi?");
                append("Acik port bulunamadi.");
            } else {
                setStatus("Acik portlar: " + String.join(", ", open));
            }
            appendNetworkState();
        });
    }

    private void tryLive() {
        String host = hostInput.getText().toString().trim();
        streaming = false;
        setStatus("Canli yayin adresleri deneniyor...");
        append("HTTP/MJPEG adaylari deneniyor.");
        worker.submit(() -> {
            Network wifi = bindToWifiNetwork();
            if (wifi == null) {
                setStatus("Q28 WiFi agi uygulamaya baglanamadi.");
                append("WiFi network bulunamadi. Q28interkom agina bagli oldugundan emin ol.");
                appendNetworkState();
                return;
            }

            for (int port : HTTP_PORTS) {
                for (String path : HTTP_PATHS) {
                    String url = String.format(Locale.US, "http://%s:%d%s", host, port, path);
                    append("Deneniyor: " + url);
                    if (tryMjpeg(wifi, url)) {
                        setStatus("Canli yayin bulundu: " + url);
                        return;
                    }
                }
            }
            setStatus("HTTP canli yayin bulunamadi.");
            append("Not: NiceDVR calistigi icin cihaz buyuk ihtimalle TUTK/GPCam protokolu kullaniyor. APK tekrar yuklenince native baglanti katmanini ekleyecegiz.");
        });
    }

    private void testNative() {
        setStatus("Native kutuphaneler yukleniyor...");
        worker.submit(() -> {
            String result = NativeLoader.load();
            append(result);
            if (result.contains("HATA:")) {
                setStatus("Native yukleme hatasi var.");
            } else {
                setStatus("Native kutuphaneler yuklendi.");
            }
        });
    }

    private void rawProbe() {
        String host = hostInput.getText().toString().trim();
        setStatus("Ham port cevabi okunuyor...");
        worker.submit(() -> {
            Network wifi = bindToWifiNetwork();
            if (wifi == null) {
                setStatus("Q28 WiFi agi uygulamaya baglanamadi.");
                appendNetworkState();
                return;
            }
            for (int port : new int[] {8080, 8081}) {
                append("RAW port deneniyor: " + port);
                append(rawHttpProbe(wifi, host, port));
            }
            setStatus("Raw okuma tamamlandi.");
        });
    }

    private void tryGpcam() {
        String host = hostInput.getText().toString().trim();
        setStatus("GPCam baglantisi deneniyor...");
        worker.submit(() -> {
            Network wifi = bindToWifiNetwork();
            if (wifi == null) {
                setStatus("Q28 WiFi agi uygulamaya baglanamadi.");
                appendNetworkState();
                return;
            }

            append(NativeLoader.load());
            CamWrapper wrapper = CamWrapper.getComWrapperInstance();
            wrapper.setGoPlusMsgListener(new CamWrapper.GoPlusMsgListener() {
                @Override
                public void onStatus(int status, int arg1, int arg2, int arg3, int arg4, byte[] data) {
                    append("GPCam status cb: " + status + "," + arg1 + "," + arg2 + "," + arg3 + "," + arg4 + " data=" + (data == null ? 0 : data.length));
                }

                @Override
                public void onData(boolean isVideo, int type, byte[] data) {
                    append("GPCam data cb: video=" + isVideo + " type=" + type + " len=" + (data == null ? 0 : data.length));
                }
            });

            int[] modes = new int[] {0, 1, 2, 8080, 8081};
            for (int mode : modes) {
                try {
                    append("GPCamConnectToDevice host=" + host + " mode=" + mode);
                    int ret = wrapper.GPCamConnectToDevice(host, mode);
                    append("GPCam connect ret=" + ret);
                    try {
                        append("GPCamGetStatus=" + wrapper.GPCamGetStatus());
                    } catch (Throwable statusError) {
                        append("GPCamGetStatus hata: " + statusError.getClass().getSimpleName());
                    }
                    try {
                        append("GPCamSendGetStatus=" + wrapper.GPCamSendGetStatus());
                    } catch (Throwable statusError) {
                        append("GPCamSendGetStatus hata: " + statusError.getClass().getSimpleName());
                    }
                    try {
                        append("GPCamSendRestartStreaming=" + wrapper.GPCamSendRestartStreaming());
                    } catch (Throwable streamError) {
                        append("RestartStreaming hata: " + streamError.getClass().getSimpleName());
                    }
                    if (ret >= 0) {
                        setStatus("GPCam denemesi tamamlandi: ret=" + ret);
                        return;
                    }
                } catch (Throwable t) {
                    append("GPCam hata mode=" + mode + " -> " + t.getClass().getSimpleName() + ": " + safe(t.getMessage()));
                }
            }
            setStatus("GPCam baglanti denemesi bitti.");
        });
    }

    private Network bindToWifiNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) {
            append("ConnectivityManager yok.");
            return null;
        }

        for (Network network : cm.getAllNetworks()) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                ConnectivityManager.setProcessDefaultNetwork(network);
                append("Uygulama WiFi agina baglandi: " + network);
                return network;
            }
        }
        return null;
    }

    private boolean isOpen(Network network, String host, int port, int timeoutMs) {
        SocketFactory socketFactory = network.getSocketFactory();
        try (Socket socket = socketFactory.createSocket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private String rawHttpProbe(Network network, String host, int port) {
        SocketFactory socketFactory = network.getSocketFactory();
        try (Socket socket = socketFactory.createSocket()) {
            socket.connect(new InetSocketAddress(host, port), 1000);
            socket.setSoTimeout(1200);

            OutputStream out = socket.getOutputStream();
            out.write(("GET / HTTP/1.0\r\nHost: " + host + "\r\n\r\n").getBytes());
            out.flush();

            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[96];
            int read = in.read(buffer);
            if (read <= 0) {
                return "RAW " + port + ": veri yok";
            }
            return "RAW " + port + " len=" + read + " hex=" + toHex(buffer, read) + " ascii=" + toAscii(buffer, read);
        } catch (Throwable t) {
            return "RAW " + port + " hata: " + t.getClass().getSimpleName() + " " + safe(t.getMessage());
        }
    }

    private boolean tryMjpeg(Network network, String urlString) {
        HttpURLConnection conn = null;
        try {
            URLConnection urlConnection = network.openConnection(new URL(urlString));
            conn = (HttpURLConnection) urlConnection;
            conn.setConnectTimeout(900);
            conn.setReadTimeout(1400);
            conn.setRequestProperty("User-Agent", "Q28Interkom/0.1");
            int code = conn.getResponseCode();
            String contentType = conn.getContentType();
            append("HTTP " + code + " " + safe(contentType));
            if (code < 200 || code >= 300) return false;

            streaming = true;
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            byte[] buffer = new byte[8192];
            ByteArrayOutputStream frame = new ByteArrayOutputStream();
            int previous = -1;
            int current;
            long deadline = System.currentTimeMillis() + 8000;
            while (streaming && System.currentTimeMillis() < deadline && (current = in.read()) != -1) {
                frame.write(current);
                if (previous == 0xFF && current == 0xD9) {
                    byte[] jpeg = frame.toByteArray();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
                    if (bitmap != null) {
                        main.post(() -> preview.setImageBitmap(bitmap));
                        return true;
                    }
                    frame.reset();
                }
                previous = current;
                if (frame.size() > 1024 * 1024) frame.reset();
                if (in.available() > 0) {
                    int read = in.read(buffer, 0, Math.min(buffer.length, in.available()));
                    if (read > 0) frame.write(buffer, 0, read);
                }
            }
        } catch (IOException e) {
            append("Basarisiz: " + e.getClass().getSimpleName());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return false;
    }

    private void appendNetworkState() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        Network network = cm == null ? null : cm.getActiveNetwork();
        NetworkCapabilities caps = network == null ? null : cm.getNetworkCapabilities(network);
        append("WiFi aktif: " + (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)));
        if (cm != null) {
            for (Network item : cm.getAllNetworks()) {
                NetworkCapabilities itemCaps = cm.getNetworkCapabilities(item);
                if (itemCaps != null) {
                    append("Network " + item + " WiFi=" + itemCaps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            + " Cellular=" + itemCaps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            + " Internet=" + itemCaps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            + " Validated=" + itemCaps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
                }
            }
        }
    }

    private void setStatus(String value) {
        main.post(() -> status.setText(value));
    }

    private void append(String value) {
        main.post(() -> logView.append(value + "\n"));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String toHex(byte[] data, int length) {
        StringBuilder out = new StringBuilder();
        int limit = Math.min(length, 48);
        for (int i = 0; i < limit; i++) {
            if (i > 0) out.append(' ');
            out.append(String.format(Locale.US, "%02X", data[i] & 0xFF));
        }
        return out.toString();
    }

    private String toAscii(byte[] data, int length) {
        StringBuilder out = new StringBuilder();
        int limit = Math.min(length, 80);
        for (int i = 0; i < limit; i++) {
            int c = data[i] & 0xFF;
            if (c >= 32 && c <= 126) {
                out.append((char) c);
            } else {
                out.append('.');
            }
        }
        return out.toString();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
