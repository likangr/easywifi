package com.likang.easywifi.lib.core.task;

/**
 * @author likangren
 */
public interface WifiTaskCallback<T extends WifiTask> {

    void onTaskStartRun(T wifiTask);

    void onTaskRunningCurrentStep(T wifiTask);

    void onTaskSuccess(T wifiTask);

    void onTaskFail(T wifiTask);

    void onTaskCancel(T wifiTask);

}
