package com.sny.tangyong.basic;

import android.app.Activity;
import android.os.Bundle;


/**
 * 后台进程非常耗费内存和电池
 * <p>
 * 例如，隐式广播可以启动许多已注册侦听它的后台进程，即使这些进程可能没有执行许多工作。 这会严重影响设备性能和用户体验。
 * <p>
 * 为缓解这个问题，Android N 应用了以下限制：
 * <p>
 * 面向 Preview 的应用不会收到 CONNECTIVITY_ACTION 广播，即使它们在清单中注册接收这些广播。
 * 运行的应用如果使用 Context.registerReceiver() 注册 BroadcastReceiver，则仍可在主线程上侦听 CONNECTIVITY_CHANGE。
 * 应用无法发送或接收 ACTION_NEW_PICTURE 或 ACTION_NEW_VIDEO 广播。
 * 此项优化会影响所有应用，而不仅仅是面向 Preview 的应用。
 */
public class BackGroundOptizationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
