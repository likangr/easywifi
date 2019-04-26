package com.likang.easywifi.demo;

import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.likang.easywifi.lib.EasyWifi;
import com.likang.easywifi.lib.core.task.CheckIsAlreadyConnectedTask;
import com.likang.easywifi.lib.core.task.ConnectToWifiTask;
import com.likang.easywifi.lib.core.task.GetConnectionInfoTask;
import com.likang.easywifi.lib.core.task.ScanWifiTask;
import com.likang.easywifi.lib.core.task.SetWifiEnabledTask;
import com.likang.easywifi.lib.core.task.WifiTask;
import com.likang.easywifi.lib.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";
    private static final String SAVE_INSTANCE_STATE_KEY_WIFI_TASK = "wifi_task";
    private TextView mTvConnectionInfo;
    private ListView mLvWifiList;
    private ProgressBar mPbWait;
    private ArrayList<String> mWifiSsidList = new ArrayList<>();
    private List<ScanResult> mScanResults;
    private ArrayAdapter<String> mWifiListAdapter;
    private WifiTask mCurWifiTask;


    private SetWifiEnabledTask.OnSetWifiEnabledCallback mOnSetWifiEnabledCallback = new SetWifiEnabledTask.OnSetWifiEnabledCallback() {
        @Override
        public void onSetWifiEnabledPreparing(boolean enabled) {
            Logger.d(TAG, "enabled=" + enabled + " onSetWifiEnabledPreparing");
            setPbVisible(true);
        }

        @Override
        public void onSetWifiEnabledPreparingNextStep(boolean enabled, int nextStep) {
            Logger.d(TAG, "enabled=" + enabled + " onSetWifiEnabledPreparingNextStep nextStep=" + nextStep);
        }

        @Override
        public void onSetWifiEnabledStart(boolean enabled) {
            Logger.d(TAG, "enabled=" + enabled + " onSetWifiEnabledStart");
        }

        @Override
        public void onSetWifiEnabledSuccess(boolean enabled) {
            Logger.d(TAG, "enabled=" + enabled + " onSetWifiEnabledSuccess");
            setPbVisible(false);
            mCurWifiTask = null;
        }

        @Override
        public void onSetWifiEnabledFail(boolean enabled, int setWifiEnabledFailReason) {
            Logger.d(TAG, "enabled=" + enabled + " onSetWifiEnabledFail setWifiEnabledFailReason=" + setWifiEnabledFailReason);
            setPbVisible(false);
            mCurWifiTask = null;
        }
    };

    private ScanWifiTask.OnScanWifiCallback mOnScanWifiCallback = new ScanWifiTask.OnScanWifiCallback() {
        @Override
        public void onScanWifiPreparing() {
            Logger.d(TAG, "onScanWifiPreparing");
            setPbVisible(true);
        }

        @Override
        public void onScanWifiPreparingNextStep(int nextStep) {
            Logger.d(TAG, "onScanWifiPreparingNextStep nextStep=" + nextStep);
        }

        @Override
        public void onScanWifiStart() {
            Logger.d(TAG, "onScanWifiStart");
        }

        @Override
        public void onScanWifiSuccess() {
            Logger.d(TAG, "onScanWifiSuccess");
            setPbVisible(false);
            List<ScanResult> scanResults = EasyWifi.getScanResults();
            updateWifiSsidListFromScanResults(scanResults);
            mCurWifiTask = null;
        }

        @Override
        public void onScanWifiFail(int scanWifiFailReason) {
            Logger.d(TAG, "onScanWifiFail scanWifiFailReason=" + scanWifiFailReason);
            setPbVisible(false);
            mCurWifiTask = null;
        }
    };

    private GetConnectionInfoTask.OnGetConnectionInfoCallback mOnGetConnectionInfoCallback = new GetConnectionInfoTask.OnGetConnectionInfoCallback() {
        @Override
        public void onGetConnectionInfoPreparing() {
            Logger.d(TAG, "onGetConnectionInfoPreparing");
        }

        @Override
        public void onGetConnectionInfoPreparingNextStep(int nextStep) {
            Logger.d(TAG, "onGetConnectionInfoPreparingNextStep nextStep=" + nextStep);
        }

        @Override
        public void onGetConnectionInfoSuccess(WifiInfo wifiInfo) {
            Logger.d(TAG, "onGetConnectionInfoSuccess wifiInfo=" + wifiInfo);
            mTvConnectionInfo.setText(String.format("connection info: %s %s", wifiInfo.getSSID(), wifiInfo.getBSSID()));
            mCurWifiTask = null;
        }

        @Override
        public void onGetConnectionInfoFail(int getConnectionFailReason) {
            Logger.d(TAG, "onGetConnectionInfoFail getConnectionFailReason=" + getConnectionFailReason);
            mTvConnectionInfo.setText("connection info: unknown");
            mCurWifiTask = null;
        }
    };


    private ConnectToWifiTask.OnConnectToWifiCallback mOnConnectToWifiCallback = new ConnectToWifiTask.OnConnectToWifiCallback() {
        @Override
        public void onConnectToWifiPreparing() {
            Logger.d(TAG, "onConnectToWifiPreparing");
        }

        @Override
        public void onConnectToWifiPreparingNextStep(int nextStep) {
            Logger.d(TAG, "onConnectToWifiPreparingNextStep nextStep=" + nextStep);
        }

        @Override
        public void onConnectToWifiStart() {
            Logger.d(TAG, "onConnectToWifiStart");
        }

        @Override
        public void onConnectToWifiConnecting(int connectingDetail) {
            Logger.d(TAG, "onConnectToWifiConnecting connectingDetail=" + connectingDetail);
        }

        @Override
        public void onConnectToWifiSuccess() {
            Logger.d(TAG, "onConnectToWifiSuccess");
            setPbVisible(false);
            mCurWifiTask = null;
            updateConnectionInfo();
        }

        @Override
        public void onConnectToWifiFail(int connectToWifiFailReason) {
            Logger.d(TAG, "onConnectToWifiFail connectToWifiFailReason=" + connectToWifiFailReason);
            setPbVisible(false);
            mCurWifiTask = null;

            if (connectToWifiFailReason == EasyWifi.TASK_FAIL_REASON_CONNECT_TO_WIFI_ERROR_AUTHENTICATING) {

            }

        }
    };


    private CheckIsAlreadyConnectedTask.OnCheckIsAlreadyConnectedCallback mOnCheckIsAlreadyConnectedCallback = new CheckIsAlreadyConnectedTask.OnCheckIsAlreadyConnectedCallback() {
        @Override
        public void onCheckIsAlreadyConnectedCallbackPreparing() {
            Logger.d(TAG, "onCheckIsAlreadyConnectedCallbackPreparing");
            setPbVisible(true);
        }

        @Override
        public void onCheckIsAlreadyConnectedCallbackPreparingNextStep(int nextStep) {
            Logger.d(TAG, "onCheckIsAlreadyConnectedCallbackPreparingNextStep nextStep=" + nextStep);
        }

        @Override
        public void onCheckIsAlreadyConnectedCallbackSuccess(boolean isAlreadyConnected, String ssid, String bssid) {
            Logger.d(TAG, "onCheckIsAlreadyConnectedCallbackSuccess isAlreadyConnected=" + isAlreadyConnected + ",ssid=" + ssid + ",bssid" + bssid);
        }


        @Override
        public void onCheckIsAlreadyConnectedCallbackSuccess(boolean isAlreadyConnected, final ScanResult scanResult) {
            Logger.d(TAG, "onCheckIsAlreadyConnectedCallbackSuccess isAlreadyConnected=" + isAlreadyConnected + ",scanResult=" + scanResult);
            if (isAlreadyConnected) {

                setPbVisible(false);

            } else {

                WifiConfiguration configuredWifiConfiguration = EasyWifi.getConfiguredWifiConfiguration(scanResult);
                if (configuredWifiConfiguration != null) {

                    ConnectToWifiTask connectToWifiTask = new ConnectToWifiTask(EasyWifi.TIME_OUT_SET_WIFI_ENABLED_DEFAULT,
                            EasyWifi.TIME_OUT_CONNECT_TO_WIFI_DEFAULT,
                            configuredWifiConfiguration,
                            mOnConnectToWifiCallback);
                    EasyWifi.executeTask(connectToWifiTask);
                    mCurWifiTask = connectToWifiTask;
                } else {

                    final EditText editText = new EditText(MainActivity.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).setTitle(String.format("请输入%s的密码", scanResult.SSID)).setView(editText)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ConnectToWifiTask connectToWifiTask = new ConnectToWifiTask(EasyWifi.TIME_OUT_SET_WIFI_ENABLED_DEFAULT,
                                            scanResult,
                                            editText.getText().toString().trim(),
                                            EasyWifi.TIME_OUT_CONNECT_TO_WIFI_DEFAULT,
                                            mOnConnectToWifiCallback);
                                    EasyWifi.executeTask(connectToWifiTask);
                                    mCurWifiTask = connectToWifiTask;
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    setPbVisible(false);
                                }
                            });
                    builder.create().show();

                }


            }
        }

        @Override
        public void onCheckIsAlreadyConnectedCallbackFail(int getConnectionFailReason) {
            Logger.d(TAG, "onCheckIsAlreadyConnectedCallbackFail getConnectionFailReason=" + getConnectionFailReason);
            setPbVisible(false);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvConnectionInfo = findViewById(R.id.tv_connection_info);
        mLvWifiList = findViewById(R.id.lv_wifi_list);
        mPbWait = findViewById(R.id.pb_wait);

        mWifiListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mWifiSsidList);
        mLvWifiList.setAdapter(mWifiListAdapter);
        mLvWifiList.setOnItemClickListener(this);

        updateConnectionInfo();

        if (savedInstanceState != null) {

            mCurWifiTask = savedInstanceState.getParcelable(SAVE_INSTANCE_STATE_KEY_WIFI_TASK);
            if (mCurWifiTask == null) {
                return;
            }

            if (mCurWifiTask instanceof SetWifiEnabledTask) {
                mCurWifiTask.setWifiTaskCallback(mOnSetWifiEnabledCallback);
            } else if (mCurWifiTask instanceof ScanWifiTask) {
                mCurWifiTask.setWifiTaskCallback(mOnScanWifiCallback);
                ((ScanWifiTask) mCurWifiTask).setSingleTaskActivity(this);
            } else if (mCurWifiTask instanceof GetConnectionInfoTask) {
                mCurWifiTask.setWifiTaskCallback(mOnGetConnectionInfoCallback);
            } else if (mCurWifiTask instanceof ConnectToWifiTask) {
                mCurWifiTask.setWifiTaskCallback(mOnConnectToWifiCallback);
            } else if (mCurWifiTask instanceof CheckIsAlreadyConnectedTask) {
                mCurWifiTask.setWifiTaskCallback(mOnCheckIsAlreadyConnectedCallback);
            }
            Logger.d(TAG, "resume WifiTask=" + mCurWifiTask);
            EasyWifi.executeTask(mCurWifiTask);
        }

    }

    public void openWifi(View view) {
        setWifiEnabled(true);
    }

    public void closeWifi(View view) {
        setWifiEnabled(false);
    }


    private void setWifiEnabled(final boolean enabled) {
        SetWifiEnabledTask setWifiEnabledTask = new SetWifiEnabledTask(enabled,
                EasyWifi.TIME_OUT_SET_WIFI_ENABLED_DEFAULT,
                mOnSetWifiEnabledCallback);
        EasyWifi.executeTask(setWifiEnabledTask);
        mCurWifiTask = setWifiEnabledTask;
    }


    public void scanWifi(View view) {
        ScanWifiTask scanWifiTask = new ScanWifiTask(EasyWifi.TIME_OUT_SCAN_WIFI_DEFAULT,
                EasyWifi.TIME_OUT_SET_WIFI_ENABLED_DEFAULT,
                EasyWifi.SCAN_WIFI_WAY_INITIATIVE,
                true,
                this,
                mOnScanWifiCallback);

        EasyWifi.executeTask(scanWifiTask);
        mCurWifiTask = scanWifiTask;
    }

    private void updateWifiSsidListFromScanResults(List<ScanResult> scanResults) {
        mScanResults = scanResults;
        mWifiSsidList.clear();
        for (ScanResult scanResult : mScanResults) {
            mWifiSsidList.add(scanResult.SSID + " " + scanResult.BSSID);
        }
        mWifiListAdapter.notifyDataSetChanged();
    }

    private void updateConnectionInfo() {
        GetConnectionInfoTask getConnectionInfoTask = new GetConnectionInfoTask(mOnGetConnectionInfoCallback);
        EasyWifi.executeTask(getConnectionInfoTask);
        mCurWifiTask = getConnectionInfoTask;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ScanResult scanResult = mScanResults.get(position);

        CheckIsAlreadyConnectedTask checkIsAlreadyConnectedTask = new CheckIsAlreadyConnectedTask(scanResult, mOnCheckIsAlreadyConnectedCallback);
        EasyWifi.executeTask(checkIsAlreadyConnectedTask);
        mCurWifiTask = checkIsAlreadyConnectedTask;

    }


    private void setPbVisible(boolean visible) {
        mPbWait.setVisibility(visible ? View.VISIBLE : View.GONE);
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_INSTANCE_STATE_KEY_WIFI_TASK, mCurWifiTask);
    }
}
