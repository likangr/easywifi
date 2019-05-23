package com.likangr.easywifi.lib.core.task;

import android.os.Parcel;

import com.likangr.easywifi.lib.EasyWifi;

/**
 * @author likangren
 */
public abstract class SpecificWifiTask extends WifiTask {
    private PrepareEnvironmentTask mPrepareEnvironmentTask = PrepareEnvironmentTask.createForLib();

    protected SpecificWifiTask() {
    }

    protected SpecificWifiTask(WifiTaskCallback wifiTaskCallback) {
        super(wifiTaskCallback);
    }

    protected SpecificWifiTask(Parcel in) {
        super(in);
        mPrepareEnvironmentTask = in.readParcelable(PrepareEnvironmentTask.class.getClassLoader());
    }

    protected abstract void onEnvironmentPrepared();

    protected abstract void initPrepareEnvironment(PrepareEnvironmentTask prepareEnvironmentTask);

    @Override
    public synchronized void run() {

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
        mPrepareEnvironmentTask.run();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(mPrepareEnvironmentTask, flags);
    }

    @Override
    public synchronized void cancel() {
        super.cancel();
        mPrepareEnvironmentTask.cancel();
    }
}
