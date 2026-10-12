package generalplus.com.GPCamLib;

import android.os.Handler;

public class CamWrapper {
    private static final CamWrapper INSTANCE = new CamWrapper();
    private GoPlusMsgListener msgListener;
    private Handler viewHandler;
    private int viewHandlerId;
    private boolean isNewFile;
    private boolean isSupportPWlength;
    private String parameterFile;
    private String downloadPath;

    public interface GoPlusMsgListener {
        void onStatus(int status, int arg1, int arg2, int arg3, int arg4, byte[] data);

        void onData(boolean isVideo, int type, byte[] data);
    }

    public static CamWrapper getComWrapperInstance() {
        return INSTANCE;
    }

    private native int GPCamSendGetParameterFile(String path);

    private native void GPCamSetDownloadPath(String path);

    public native int GPCamAbort(int value);

    public native void GPCamCheckFileMapping();

    public native void GPCamClearCommandQueue();

    public native int GPCamConnectToDevice(String uidOrIp, int mode);

    public native void GPCamDisconnect();

    public native byte GPCamGetFileExt(int index);

    public native byte[] GPCamGetFileExtraInfo(int index);

    public native int GPCamGetFileIndex(int index);

    public native String GPCamGetFileName(int index);

    public native int GPCamGetFileSize(int index);

    public native boolean GPCamGetFileTime(int index, byte[] output);

    public native int GPCamGetStatus();

    public native int GPCamSendAudioOnOff(boolean enabled);

    public native int GPCamSendCVFirmwareDownload(long offset, long size);

    public native int GPCamSendCVFirmwareRawData(long offset, byte[] data);

    public native int GPCamSendCVFirmwareUpgrade(long size);

    public native int GPCamSendCapturePicture();

    public native int GPCamSendDeleteFile(int index);

    public native int GPCamSendFirmwareDownload(long offset, long size);

    public native int GPCamSendFirmwareRawData(long offset, byte[] data);

    public native int GPCamSendFirmwareUpgrade();

    public native int GPCamSendGetFileRawdata(int index);

    public native int GPCamSendGetFileThumbnail(int index);

    public native int GPCamSendGetFullFileList();

    public native int GPCamSendGetParameter(int parameterId);

    public native int GPCamSendGetSetPIP(int value);

    public native int GPCamSendGetStatus();

    public native int GPCamSendPausePlayback();

    public native int GPCamSendPowerOff();

    public native int GPCamSendRecordCmd();

    public native int GPCamSendRestartStreaming();

    public native int GPCamSendSetMode(int mode);

    public native int GPCamSendSetParameter(int parameterId, int value, byte[] payload);

    public native int GPCamSendStartPlayback(int index);

    public native int GPCamSendStopPlayback();

    public native int GPCamSendVendorCmd(byte[] payload, int length);

    public native boolean GPCamSetFileNameMapping(String path);

    public native int GPCamSetNextPlaybackFileListIndex(int index);

    public void GPCamDataCallBack(boolean isVideo, int type, byte[] data) {
        GoPlusMsgListener listener = msgListener;
        if (listener != null) {
            listener.onData(isVideo, type, data);
        }
    }

    public void GPCamStatusCallBack(int status, int arg1, int arg2, int arg3, int arg4, byte[] data) {
        GoPlusMsgListener listener = msgListener;
        if (listener != null) {
            listener.onStatus(status, arg1, arg2, arg3, arg4, data);
        }
    }

    public String GetGPCamSendGetParameterFile() {
        return parameterFile;
    }

    public String GetGPCamSetDownloadPath() {
        return downloadPath;
    }

    public void SetGPCamSendGetParameterFile(String path) {
        parameterFile = path;
        GPCamSendGetParameterFile(path);
    }

    public void SetGPCamSetDownloadPath(String path) {
        downloadPath = path;
        GPCamSetDownloadPath(path);
    }

    public void SetViewHandler(Handler handler, int id) {
        viewHandler = handler;
        viewHandlerId = id;
    }

    public Handler getViewHandler() {
        return viewHandler;
    }

    public int getViewHandlerId() {
        return viewHandlerId;
    }

    public boolean getIsNewFile() {
        return isNewFile;
    }

    public boolean getIsSupportPWlength() {
        return isSupportPWlength;
    }

    public void removeDevMsgListener() {
        msgListener = null;
    }

    public void setGoPlusMsgListener(GoPlusMsgListener listener) {
        msgListener = listener;
    }

    public void setIsNewFile(boolean value) {
        isNewFile = value;
    }

    public void setIsSupportPWlength(boolean value) {
        isSupportPWlength = value;
    }
}
