package com.likang.easywifi.lib.core.guid;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.likang.easywifi.lib.R;
import com.likang.easywifi.lib.util.ToastHooker;

import java.lang.reflect.Field;

/**
 * @author likangren
 */
public class UserActionGuideToast {

    private static final String TAG = "UserActionGuideToast";


    public static void showGuideToast(Context context, String actionTitle, String actionTip, int duration) {

        final Toast toast = Toast.makeText(context, null, duration);
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);

        View view = View.inflate(context, R.layout.toast_user_action_guide, null);
        toast.setView(view);

        TextView tvActionTitle = view.findViewById(R.id.tv_action_title);
        TextView tvActionTip = view.findViewById(R.id.tv_action_tip);
        ImageView ivDismiss = view.findViewById(R.id.iv_dismiss);

        tvActionTitle.setText(actionTitle);
        tvActionTip.setText(actionTip);
        ivDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast.cancel();
            }
        });


        try {
            Field mTnFiled = toast.getClass().getDeclaredField("mTN");
            mTnFiled.setAccessible(true);
            Object mTnObject = mTnFiled.get(toast);

            Field mParamsFiled = mTnObject.getClass().getDeclaredField("mParams");
            mParamsFiled.setAccessible(true);
            Object mParamsObject = mParamsFiled.get(mTnObject);

            WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParamsObject;

            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        } catch (Exception e) {
            e.printStackTrace();
        }

        ToastHooker.show(toast);
    }


}