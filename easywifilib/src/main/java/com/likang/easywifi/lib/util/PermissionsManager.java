package com.likang.easywifi.lib.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


/**
 * @author likangren
 */
public class PermissionsManager {

    private static final String TAG = "PermissionsManager";

    public static IReqListener1 mListenerImpl1;
    public static IReqListener2 mListenerImpl2;
    // 音频获取源
    public static int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    public static int sampleRateInHz = 44100;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    public static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    public static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    public static int bufferSizeInBytes = 0;

    /**
     * 检查是否有某个权限
     *
     * @param permissions
     * @return
     */
    public static boolean check(@NonNull String permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            if (permissions.equals(Manifest.permission.CAMERA)) {
                return isHasCameraPermission();
            } else if (permissions.equals(Manifest.permission.RECORD_AUDIO)) {
                return isHasAudioPermission();
            }

        }

        return ContextCompat.checkSelfPermission(ApplicationHolder.getApplication(), permissions) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 请求多个权限
     *
     * @param permissions
     * @param reqCode
     * @param page
     * @param reqListener1
     */
    public static void request(@NonNull String[] permissions, int reqCode, Object page, IReqListener1 reqListener1) {

        if (page instanceof Activity || page instanceof Fragment) {
            mListenerImpl1 = reqListener1;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                int[] grantResults = new int[permissions.length];

                for (int i = 0; i < permissions.length; i++) {
                    grantResults[i] = PackageManager.PERMISSION_GRANTED;
                }

                onRequestPermissionsResult(reqCode, permissions, grantResults);
            } else {

                if (page instanceof Activity) {
                    ((Activity) page).requestPermissions(permissions, reqCode);
                } else {
                    ((Fragment) page).requestPermissions(permissions, reqCode);
                }
            }
        } else {
            throw new IllegalArgumentException();
        }

    }

    /**
     * 请求单个权限
     *
     * @param permissions
     * @param reqCode
     * @param page
     * @param reqListener2
     */
    public static void request(@NonNull String permissions, int reqCode, Object page, IReqListener2 reqListener2) {
        if (page instanceof Activity || page instanceof Fragment) {
            mListenerImpl2 = reqListener2;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                onRequestPermissionsResult(reqCode, new String[]{permissions}, new int[]{PackageManager.PERMISSION_GRANTED});
            } else {
                if (page instanceof Activity) {
                    ((Activity) page).requestPermissions(new String[]{permissions}, reqCode);
                } else {
                    ((Fragment) page).requestPermissions(new String[]{permissions}, reqCode);
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 单个权限 检查并获取
     *
     * @param permissions
     * @param reqCode
     * @param page
     * @param reqListener2
     * @return
     */
    public static boolean checkAndRequest(@NonNull String permissions, int reqCode, Object page, IReqListener2 reqListener2) {

        if (check(permissions)) {
            return true;
        } else {
            request(permissions, reqCode, page, reqListener2);
            return false;
        }
    }

    /**
     * 多个权限 检查并获取
     *
     * @param permissions
     * @param reqCode
     * @param page
     * @param reqListener1
     * @return
     */
    public static boolean checkAndRequest(@NonNull String[] permissions, int reqCode, Object page, IReqListener1 reqListener1) {

        if (check(permissions)) {
            return true;
        } else {
            request(permissions, reqCode, page, reqListener1);
            return false;
        }
    }

    public static void onRequestPermissionsResult(int reqCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mListenerImpl1 != null) {
            try {

                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            mListenerImpl1.onResult(reqCode, permissions, false, grantResults);
                            return;
                        }
                    }
                    mListenerImpl1.onResult(reqCode, permissions, true, grantResults);
                } else {
                    mListenerImpl1.onResult(reqCode, permissions, false, grantResults);
                }


            } finally {
                mListenerImpl1 = null;
            }
        } else if (mListenerImpl2 != null) {
            try {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mListenerImpl2.onGranted(reqCode, permissions[0]);
                } else {
                    mListenerImpl2.onDenied(reqCode, permissions[0]);
                }
            } finally {
                mListenerImpl2 = null;
            }
        }
    }

    /**
     * 检查是否全部拥有这些权限
     *
     * @param permissions
     * @return
     */
    public static boolean check(@NonNull String[] permissions) {

        for (int i = 0; i < permissions.length; i++) {

            if (!check(permissions[i])) {
                return false;
            }

        }
        return true;
    }

    /**
     * 检查是否需要提示用户权限基本原理
     *
     * @param permission
     * @param page
     * @return
     */
    public static boolean shouldShowRequestPermissionRationale(@NonNull String permission, Object page) {

        if (page instanceof Activity || page instanceof Fragment) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return false;
            } else {
                if (page instanceof Activity) {
                    return ((Activity) page).shouldShowRequestPermissionRationale(permission);
                } else {
                    return ((Fragment) page).shouldShowRequestPermissionRationale(permission);
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
    }


    /**
     * 判断是是否有录音权限
     */
    public static boolean isHasAudioPermission() {

//        开始录制音频
        try {
            bufferSizeInBytes = 0;
            bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                    channelConfig, audioFormat);
            AudioRecord audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                    channelConfig, audioFormat, bufferSizeInBytes);
            // 防止某些手机崩溃，例如联想
            audioRecord.startRecording();
            byte[] tempBuffer = new byte[bufferSizeInBytes];
            int read = audioRecord.read(tempBuffer, 0, bufferSizeInBytes);
            audioRecord.stop();
            audioRecord.release();
            return read >= 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 返回true 表示可以使用  返回false表示不可以使用
     */
    public static boolean isHasCameraPermission() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters(); //针对魅族手机
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }

        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
    }

    /**
     * 请求单个权限回调
     */
    public interface IReqListener2 {
        void onGranted(int reqCode, String permissions);

        void onDenied(int reqCode, String permissions);
    }

    /**
     * 请求多个权限回调
     */
    public interface IReqListener1 {
        void onResult(int reqCode, String[] permissions, boolean result, int[] grantResults);
    }
}
