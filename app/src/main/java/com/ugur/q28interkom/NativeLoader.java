package com.ugur.q28interkom;

public final class NativeLoader {
    private static boolean loaded;

    private NativeLoader() {
    }

    public static synchronized String load() {
        if (loaded) {
            return "Native kutuphaneler zaten yuklu.";
        }

        String[] libs = {
                "IOTCAPIs",
                "PPCS_API",
                "AVAPIs",
                "aw_net_client",
                "viigoplus",
                "ipcamera"
        };

        StringBuilder result = new StringBuilder();
        for (String lib : libs) {
            try {
                System.loadLibrary(lib);
                result.append("OK: ").append(lib).append('\n');
            } catch (Throwable t) {
                result.append("HATA: ").append(lib).append(" -> ")
                        .append(t.getClass().getSimpleName()).append(": ")
                        .append(t.getMessage()).append('\n');
                return result.toString();
            }
        }

        loaded = true;
        return result.append("Tum native kutuphaneler yuklendi.").toString();
    }
}
