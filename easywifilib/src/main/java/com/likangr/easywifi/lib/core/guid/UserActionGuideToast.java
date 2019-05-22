package com.likangr.easywifi.lib.core.guid;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.likangr.easywifi.lib.R;
import com.likangr.easywifi.lib.util.ToastHooker;

import java.lang.reflect.Field;

/**
 * @author likangren
 */
public class UserActionGuideToast {

    private static final String TAG = "UserActionGuideToast";

    private static Toast sToast;

    /**
     * @param context
     * @param actionTitle
     * @param actionTip
     * @param duration
     */
    public static void show(Context context, String actionTitle, String actionTip, int duration) {
        dismiss();
        sToast = Toast.makeText(context, null, duration);
        sToast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);

        View view = View.inflate(context, R.layout.toast_user_action_guide, null);
        sToast.setView(view);

        TextView tvActionTitle = view.findViewById(R.id.tv_action_title);
        TextView tvActionTip = view.findViewById(R.id.tv_action_tip);
        ImageView ivDismiss = view.findViewById(R.id.iv_dismiss);

        tvActionTitle.setText(actionTitle);
        tvActionTip.setText(actionTip);
        ivDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sToast.cancel();
            }
        });


        try {
            Field mTnFiled = sToast.getClass().getDeclaredField("mTN");
            mTnFiled.setAccessible(true);
            Object mTnObject = mTnFiled.get(sToast);

            Field mParamsFiled = mTnObject.getClass().getDeclaredField("mParams");
            mParamsFiled.setAccessible(true);
            Object mParamsObject = mParamsFiled.get(mTnObject);

            WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParamsObject;

            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        } catch (Exception e) {
            e.printStackTrace();
        }

        ToastHooker.show(sToast);
    }


    public static void dismiss() {
        if (sToast != null) {
            sToast.cancel();
        }
    }

}