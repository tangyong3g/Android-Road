package com.sny.tangyong.basic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * 说明:
 * <p/>
 * 本Demo用来说明Looper的用法和Handler的关系
 * <p/>
 * 实现主线程给子线程发送消息，结果是在Log里面可以看到在子线程里面处理消息。
 * <p/>
 * 关于Looper的知识，
 * 引用 http://www.cnblogs.com/ctou45/archive/2012/01/06/2314191.html
 * <p/>
 * 1: Looper类用来为一个线程开启一个消息循环。
 * 2: 通常是通过Handler对象来与Looper进行交互的。Handler可看做是Looper的一个接口，用来向指定的Looper发送消息及定义处理方法。
 * 3: 在非主线程中直接new Handler() 会报如下的错误:
 * 4:  Looper.loop(); 让Looper开始工作，从消息队列里取消息，处理消息。
 * 5:  基于以上知识，可实现主线程给子线程（非主线程）发送消息。 [也就是本例子中所实现的功能
 *
 * @author 师爷GBK[ty_sany@163.com] 2013-7-17
 */
public class LooperActivity extends Activity implements OnClickListener {

    public static final int MSG_MAIN_TO_SUB = 1;
    String TAG = "LooperActivity";
    LooperThread mLooperThread;
    Button mBtn;
    Context mcontext;

    /**
     * 代码来自于Android源码 Looper 里面有
     * <p/>
     * 说明:
     *
     * @author 师爷GBK[ty_sany@163.com]
     *         2013-7-17
     */
    class LooperThread extends Thread {

        Handler mHandler;

        @Override
        public void run() {
            Looper.prepare();

            //此时Hand与当前线程中的Looper绑定了。因为在此线程中完成的实例化
            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    int what = msg.what;
                    switch (what) {
                        case MSG_MAIN_TO_SUB:
                            //mBtn.setText("hello");
                            Toast.makeText(mcontext, "hello", 1000).show();
                            Log.i(TAG, "从主线程发来了消息!");
                            break;

                        default:
                            break;
                    }
                }

                ;
            };
            Looper.loop();
        }

        public Handler getThreadHandler() {
            return mHandler;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.looper_layout);
        mBtn = (Button) findViewById(R.id.send_msg_id);
        mBtn.setOnClickListener(this);

        mcontext = this;
        //开启线程
        mLooperThread = new LooperThread();
        mLooperThread.start();
    }

    @Override
    public void onClick(View v) {

        Message msg = new Message();
        msg.what = MSG_MAIN_TO_SUB;

        Handler mHandler = mLooperThread.getThreadHandler();
        mHandler.sendMessageDelayed(msg, 0);
    }


}
