package com.hobot.sample.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.hobot.sample.app.manager.NebulaObservableManager;

/**
 * 转向灯广播。
 *
 * @author Hobot
 */
public class TurnLightReceiver extends BroadcastReceiver {
    private static final String TAG = TurnLightReceiver.class.getSimpleName();
    public static final String TURN_LIGHT_ACTION = "com.rmt.action.TURN_SIGNAL_CHANGED";

    public static final String STATE = "state";
    public static final String DIRECTION = "direction";

    private static final int OPENED = 1;
    private static final int CLOSED = 2;
    public static final int TURN_LEFT = 0;
    public static final int TURN_RIGHT = 1;

    // 信号灯关闭的消息
    private static final int MSG_CLOSE = 0;
    // 延迟去抖动
    private static final int MSG_DELAY_TIME = 1500;

    // Handler
    private static Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
//            Log.d(TAG,"TURN_LIGHT CLOSE");
            NebulaObservableManager.getInstance().closeTurnLight();
            return true;
        }
    });
    //    private long usbTime = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TURN_LIGHT_ACTION.equals(action)) {
            int state = intent.getIntExtra(STATE, -1);
            int direction = intent.getIntExtra(DIRECTION, -1);
//            Log.i(TAG, "onProtoOut: "+"TurnLightReceiver 接收到"+"state："+state+" direction："+direction + ";this="  + this);
//            Log.i("setDatache: 接受广播", + state + " ---- " + "direction: " + direction);
//            Log.d(TAG, "stdate: " + state + " ---- " + "direction: " + direction);
//            if(System.currentTimeMillis() - usbTime > 60*1000){
//                usbTime = System.currentTimeMillis();
//                LogUtils.saveFileToSMB("左右转向灯广播 state " + state+" direction:"+direction);
//            }
            switch (state) {
                case OPENED: {
                    mHandler.removeMessages(MSG_CLOSE);
                    direction(direction);
                    break;
                }
                case CLOSED: {
                    mHandler.sendEmptyMessageDelayed(MSG_CLOSE, MSG_DELAY_TIME);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void direction(int direction) {
        switch (direction) {
            case TURN_LEFT: {
//                Log.i("setDatache: 接受广播", "打左转向灯");
                NebulaObservableManager.getInstance().openTurnLight(NebulaObservableManager.TURN_LIGHT_LEFT);
                break;
            }
            case TURN_RIGHT: {
//                Log.i("setDatache: 接受广播", "打右转向灯");
                NebulaObservableManager.getInstance().openTurnLight(NebulaObservableManager.TURN_LIGHT_RIGHT);
                break;
            }
            default: {
                break;
            }
        }
    }
}
