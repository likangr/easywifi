package com.likangr.easywifi.lib.core.task;

import android.os.Parcel;

import com.likangr.easywifi.lib.EasyWifi;

/**
 * @author likangren
 */
public abstract class SpecificWifiTask extends WifiTask {
    private PrepareEnvironmentTask mPrepareEnvironmentTask = new PrepareEnvironmentTask();

    SpecificWifiTask() {
    }

    SpecificWifiTask(WifiTaskCallback wifiTaskCallback) {
        super(wifiTaskCallback);
    }

    SpecificWifiTask(Parcel in) {
        super(in);
        mPrepareEnvironmentTask = in.readParcelable(PrepareEnvironmentTask.class.getClassLoader());
    }

    abstract void onEnvironmentPrepared();

    abstract void initPrepareEnvironment(PrepareEnvironmentTask prepareEnvironmentTask);

    @Override
    public synchronized void run() {
        super.run();

        initPrepareEnvironment(mPrepareEnvironmentTask);
        mPrepareEnvironmentTask.setWifiTaskCallback(new WifiTaskCallback<PrepareEnvironmentTask>() {
            @Override
            public void onTaskStartRun(PrepareEnvironmentTask wifiTask) {
                callOnTaskStartRun();
            }

            @Override
            public void onTaskRunningCurrentStep(PrepareEnvironmentTask wifiTask) {
                callOnTaskRunningCurrentStep(wifiTask.mRunningCurrentStep);
            }

            @Override
            public void onTaskSuccess(PrepareEnvironmentTask wifiTask) {
                if (!callOnTaskRunningCurrentStep(EasyWifi.CURRENT_STEP_PREPARE_SUCCESS)) {
                    return;
                }
                onEnvironmentPrepared();
            }

            @Override
            public void onTaskFail(PrepareEnvironmentTask wifiTask) {
                callOnTaskFail(wifiTask.getFailReason());
            }

            @Override
            public void onTaskCancel(PrepareEnvironmentTask wifiTask) {

            }

        });
        EasyWifi.executeTask(mPrepareEnvironmentTask);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(mPrepareEnvironmentTask, flags);
    }

    @Override
    public synchronized void cancel() {
        super.cancel();
        EasyWifi.cancelTask(mPrepareEnvironmentTask);
    }
}
