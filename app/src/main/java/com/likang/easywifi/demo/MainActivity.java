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
import android.widget.Toast;

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
import com.likang.easywifi.lib.core.task.WifiTaskCallback;
import com.likang.easywifi.lib.util.Logger;
import com.likang.easywifi.lib.util.WifiUtils;

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

    private WifiTaskCallback mOnSetWifiEnabledCallback = new WifiTaskCallback<SetWifiEnabledTask>() {
        @Override
        public void onTaskStartRun(SetWifiEnabledTask wifiTask) {
            Logger.d(TAG, "onTaskStartRun enabled=" + wifiTask.isEnabled());
            setPbVisible(true);
        }

        @Override
        public void onTaskRunningCurrentStep(SetWifiEnabledTask wifiTask) {
            Logger.d(TAG, "onTaskRunningCurrentStep enabled=" + wifiTask.isEnabled() + ",currentStep=" + wifiTask.getRunningCurrentStep());
        }

        @Override
        public void onTaskSuccess(SetWifiEnabledTask wifiTask) {
            Logger.d(TAG, "onTaskSuccess enabled=" + wifiTask.isEnabled());
            setPbVisible(false);
        }

        @Override
        public void onTaskFail(SetWifiEnabledTask wifiTask) {
            Logger.d(TAG, "onTaskRunningCurrentStep enabled=" + wifiTask.isEnabled() + ",failReason=" + wifiTask.getFailReason());
            setPbVisible(false);
        }

    };


    private WifiTaskCallback mOnScanWifiCallback = new WifiTaskCallback<ScanWifiTask>() {
        @Override
        public void onTaskStartRun(ScanWifiTask wifiTask) {
            Logger.d(TAG, "onTaskStartRun");
            setPbVisible(true);
        }

        @Override
        public void onTaskRunningCurrentStep(ScanWifiTask wifiTask) {
            Logger.d(TAG, "onTaskRunningCurrentStep currentStep=" + wifiTask.getRunningCurrentStep());
        }

        @Override
        public void onTaskSuccess(ScanWifiTask wifiTask) {
            Logger.d(TAG, "onTaskSuccess");
            setPbVisible(false);
            List<ScanResult> scanResults = EasyWifi.getScanResults();
            updateWifiSsidListFromScanResults(scanResults);
        }

        @Override
        public void onTaskFail(ScanWifiTask wifiTask) {
            Logger.d(TAG, "onTaskFail failReason=" + wifiTask.getFailReason());
            setPbVisible(false);
        }

    };
    private WifiTaskCallback mOnGetConnectionInfoCallback = new WifiTaskCallback<GetConnectionInfoTask>() {
        @Override
        public void onTaskStartRun(GetConnectionInfoTask wifiTask) {
            Logger.d(TAG, "onTaskStartRun");
        }

        @Override
        public void onTaskRunningCurrentStep(GetConnectionInfoTask wifiTask) {
            Logger.d(TAG, "onTaskRunningCurrentStep currentStep=" + wifiTask.getRunningCurrentStep());
        }

        @Override
        public void onTaskSuccess(GetConnectionInfoTask wifiTask) {
            WifiInfo wifiInfo = wifiTask.getWifiInfo();
            Logger.d(TAG, "onTaskSuccess wifiInfo=" + wifiInfo);
            mTvConnectionInfo.setText(String.format("connection info: %s %s", wifiInfo.getSSID(), wifiInfo.getBSSID()));
        }

        @Override
        public void onTaskFail(GetConnectionInfoTask wifiTask) {
            int failReason = wifiTask.getFailReason();
            Logger.d(TAG, "onTaskFail failReason=" + failReason);
            mTvConnectionInfo.setText("connection info: unknown");
        }

    };
    private WifiTaskCallback mOnConnectToWifiCallback = new WifiTaskCallback<ConnectToWifiTask>() {
        @Override
        public void onTaskStartRun(ConnectToWifiTask wifiTask) {
            Logger.d(TAG, "onTaskStartRun");
            setPbVisible(true);
        }

        @Override
        public void onTaskRunningCurrentStep(ConnectToWifiTask wifiTask) {
            Logger.d(TAG, "onTaskRunningCurrentStep currentStep=" + wifiTask.getRunningCurrentStep());
        }

        @Override
        public void onTaskSuccess(ConnectToWifiTask wifiTask) {
            Logger.d(TAG, "onTaskSuccess");
            setPbVisible(false);
            updateConnectionInfo();
        }

        @Override
        public void onTaskFail(ConnectToWifiTask wifiTask) {
            int failReason = wifiTask.getFailReason();
            Logger.d(TAG, "onTaskFail failReason=" + failReason);
            setPbVisible(false);
            if (failReason == EasyWifi.TASK_FAIL_REASON_CONNECT_TO_WIFI_ERROR_AUTHENTICATING) {
                Toast.makeText(MainActivity.this, "密码错误，请重新输入", Toast.LENGTH_SHORT).show();
                showPsdDialog(null, wifiTask.getWifiConfiguration());
            }
        }

    };
    private WifiTaskCallback mOnCheckIsAlreadyConnectedCallback = new WifiTaskCallback<CheckIsAlreadyConnectedTask>() {
        @Override
        public void onTaskStartRun(CheckIsAlreadyConnectedTask wifiTask) {
            Logger.d(TAG, "onTaskStartRun");
            setPbVisible(true);
        }

        @Override
        public void onTaskRunningCurrentStep(CheckIsAlreadyConnectedTask wifiTask) {
            Logger.d(TAG, "onTaskRunningCurrentStep currentStep=" + wifiTask.getRunningCurrentStep());
        }

        @Override
        public void onTaskSuccess(CheckIsAlreadyConnectedTask wifiTask) {
            boolean isAlreadyConnected = wifiTask.getIsAlreadyConnected();
            Logger.d(TAG, "onTaskSuccess isAlreadyConnected=" + isAlreadyConnected + ",ssid=" + wifiTask.getSsid() + ",bssid" + wifiTask.getBssid());
            final ScanResult scanResult = wifiTask.getScanResult();
            setPbVisible(false);

            if (!isAlreadyConnected) {

                final WifiConfiguration configuredWifiConfiguration = EasyWifi.getConfiguredWifiConfiguration(scanResult);
                final boolean[] configuredWifiPasswordIsWrong = {false};
                if (configuredWifiConfiguration != null) {

                    configuredWifiPasswordIsWrong[0] = WifiUtils.isConfiguredWifiPasswordIsWrong(configuredWifiConfiguration);

                    if (!configuredWifiPasswordIsWrong[0]) {
                        ConnectToWifiTask connectToWifiTask = new ConnectToWifiTask(EasyWifi.TIME_OUT_SET_WIFI_ENABLED_DEFAULT,
                                EasyWifi.TIME_OUT_CONNECT_TO_WIFI_DEFAULT,
                                configuredWifiConfiguration,
                                mOnConnectToWifiCallback);
                        EasyWifi.executeTask(connectToWifiTask);
                        return;
                    }
                }

                showPsdDialog(scanResult, configuredWifiConfiguration);

            }
        }

        @Override
        public void onTaskFail(CheckIsAlreadyConnectedTask wifiTask) {
            int failReason = wifiTask.getFailReason();
            Logger.d(TAG, "onTaskFail failReason=" + failReason);
            setPbVisible(false);
        }

    };

    private void showPsdDialog(final ScanResult scanResult, final WifiConfiguration configuredWifiConfiguration) {
        final EditText editText = new EditText(MainActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).setTitle(String.format("请输入%s的密码",
                scanResult != null ? scanResult.SSID : configuredWifiConfiguration.SSID)).setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String password = editText.getText().toString().trim();

                        if (configuredWifiConfiguration != null) {
                            int updateNetwork = WifiUtils.updateConfiguredWifiPassword(EasyWifi.getWifiManager(), configuredWifiConfiguration, password);

                            if (updateNetwork == -1) {
                                //don't has update permission.
                                Toast.makeText(MainActivity.this, "需要您在系统设置中修改wifi密码", Toast.LENGTH_SHORT).show();
                            } else {

                                ConnectToWifiTask connectToWifiTask = new ConnectToWifiTask(EasyWifi.TIME_OUT_SET_WIFI_ENABLED_DEFAULT,
                                        EasyWifi.TIME_OUT_CONNECT_TO_WIFI_DEFAULT,
                                        configuredWifiConfiguration,
                                        mOnConnectToWifiCallback);
                                EasyWifi.executeTask(connectToWifiTask);
                            }

                        } else {
                            ConnectToWifiTask connectToWifiTask = new ConnectToWifiTask(EasyWifi.TIME_OUT_SET_WIFI_ENABLED_DEFAULT,
                                    scanResult,
                                    password,
                                    EasyWifi.TIME_OUT_CONNECT_TO_WIFI_DEFAULT,
                                    mOnConnectToWifiCallback);
                            EasyWifi.executeTask(connectToWifiTask);
                        }

                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        setPbVisible(false);
                    }
                });
        builder.create().show();
    }

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

        resumeTaskIfNeed(savedInstanceState);

    }

    private void resumeTaskIfNeed(Bundle savedInstanceState) {
        if (savedInstanceState != null) {

            ArrayList<WifiTask> taskList = savedInstanceState.getParcelableArrayList(SAVE_INSTANCE_STATE_KEY_WIFI_TASK);

            for (WifiTask wifiTask : taskList) {

                if (wifiTask instanceof SetWifiEnabledTask) {
                    wifiTask.setWifiTaskCallback(mOnSetWifiEnabledCallback);
                } else if (wifiTask instanceof ScanWifiTask) {
                    wifiTask.setWifiTaskCallback(mOnScanWifiCallback);
                    ((ScanWifiTask) wifiTask).setSingleTaskActivity(this);
                } else if (wifiTask instanceof ConnectToWifiTask) {
                    wifiTask.setWifiTaskCallback(mOnConnectToWifiCallback);
                } else if (wifiTask instanceof CheckIsAlreadyConnectedTask) {
                    wifiTask.setWifiTaskCallback(mOnCheckIsAlreadyConnectedCallback);
                } else if (wifiTask instanceof GetConnectionInfoTask) {
                    wifiTask.setWifiTaskCallback(mOnGetConnectionInfoCallback);
                }
                Logger.d(TAG, "resume WifiTask=" + wifiTask);
                EasyWifi.executeTask(wifiTask);
            }
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
    }


    public void scanWifi(View view) {
        ScanWifiTask scanWifiTask = new ScanWifiTask(EasyWifi.TIME_OUT_SCAN_WIFI_DEFAULT,
                EasyWifi.TIME_OUT_SET_WIFI_ENABLED_DEFAULT,
                EasyWifi.SCAN_WIFI_WAY_INITIATIVE,
                true,
                this,
                mOnScanWifiCallback);

        EasyWifi.executeTask(scanWifiTask);
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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ScanResult scanResult = mScanResults.get(position);

        CheckIsAlreadyConnectedTask checkIsAlreadyConnectedTask = new CheckIsAlreadyConnectedTask(scanResult, mOnCheckIsAlreadyConnectedCallback);
        EasyWifi.executeTask(checkIsAlreadyConnectedTask);
    }


    private void setPbVisible(boolean visible) {
        mPbWait.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_INSTANCE_STATE_KEY_WIFI_TASK, EasyWifi.getCurrentTasks());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EasyWifi.cancelAllTasks();
    }
}
