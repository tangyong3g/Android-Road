package com.sny.tangyong.basic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;


/**
 * 实战:
 * <p/>
 * 主线程接单，然后把各种任务放到 了线程中去生产，生产结果 显示在主线程中。
 * <p/>
 * 在主线程中产生生产任务
 * 在了线程中不停的接单，
 * 单处理好后返回给主线程显示
 */
public class LooperVersion2 extends Activity implements OnClickListener {

    public static final int MSG_MAIN_TO_SUB_PROCESS = 0;
    public static final int MSG_SUB_TO_MAIN_RES = 1;
    String TAG = "LooperActivity";
    LooperThread mLooperThread;
    Button mBtn;
    Context mcontext;
    Handler mMainHandler ;
    LinearLayout lyContainer;
    int count =0;


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
        Handler mMainHandler;


        LooperThread(Handler mainHandler){
           this.mMainHandler = mainHandler;
        }

        @Override
        public void run() {

            Looper.prepare();

            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    int what = msg.what;
                    switch (what) {
                        case MSG_MAIN_TO_SUB_PROCESS:


                            Logger.getLogger("Tyler.tang").info("在开始加工机器人，线程" + Thread.currentThread().getId());
                            long start = Calendar.getInstance().getTimeInMillis();

                            try{
                                Thread.currentThread().sleep(500);
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }


                            long time = Calendar.getInstance().getTimeInMillis() - start;

                            //生产机器人
                            Mathine mathine = (Mathine)msg.obj;
                            mathine = processMathine(mathine);

                            mathine.useTime = time;
                            mathine.threadId = Thread.currentThread().getId();


                            //把生产好的机器人发送到主线程中去
                            Message msg_rs  = new Message();
                            msg_rs.what = MSG_SUB_TO_MAIN_RES;
                            msg_rs.obj = mathine;


                            Logger.getLogger("Tyler.tang").info("机器人加工结束线程"+Thread.currentThread().getId());
                            mMainHandler.sendMessage(msg_rs);

                            break;
                        default:
                            break;
                    }
                }

            };
            Looper.loop();
        }

        public Handler getThreadHandler() {
            return mHandler;
        }
    }


    /**
     * 生产
     *
     * @return
     */
    public Mathine createMahtin() {

        Mathine mathine = new Mathine();
        return mathine;
    }

    /**
     * 加工
     *
     * @param mathine
     * @return
     */
    public Mathine processMathine(Mathine mathine) {

        if (mathine != null) {
            mathine.fly();
            mathine.run();
        }
        return mathine;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.looper_layout);
        mBtn = (Button) findViewById(R.id.send_msg_id);
        mBtn.setOnClickListener(this);

        lyContainer = (LinearLayout)findViewById(R.id.id_container_my);
        mcontext = this;

        mMainHandler = new MainHandler();
        //开启线程
        mLooperThread = new LooperThread(mMainHandler);
        mLooperThread.start();

    }

    @Override
    public void onClick(View v) {

        Handler mHandler = mLooperThread.getThreadHandler();
        Logger.getLogger("Tyler.tang").info("开始生产机器人..");

        //发出指令生产机器人
        int count = 20;
        //在线程中创建新的机器人，发送到子线程中去生产
        for(int i = 0 ; i < count; i++ ){
            try{

                Thread currentThread = Thread.currentThread();
//                currentThread.sleep(1000);

                Mathine mathine = new Mathine();
                Message msg_pro = new Message();

                Message msg  = new Message();
                msg.obj = mathine;
                msg.what = MSG_MAIN_TO_SUB_PROCESS;

                mLooperThread.getThreadHandler().sendMessage(msg);

            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }


    /**
     * 机器人
     */
    class Mathine {

        int height;
        int weight;
        String name;
        long useTime;
        long threadId;

        public void fly() {
            Logger.getLogger("Tyler.tang").info("我在加工学习飞..");

            Random random = new Random();
            height = random.nextInt();
            weight = random.nextInt(6);
        }

        public void run() {
            Logger.getLogger("Tyler.tang").info("我在加工学习跑");
        }

        public String display() {

            StringBuffer bu = new StringBuffer();
            bu.append("mathineNo:"+weight);
            bu.append("\t");
            bu.append("use time: "+useTime);
            bu.append("\t");
            bu.append("threadId:"+threadId);
            bu.append("\t");
            return bu.toString();

        }
    }


    class MainHandler extends  Handler{

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == MSG_SUB_TO_MAIN_RES){
                //显示加工出来的结果
                Mathine mathine = (Mathine)msg.obj;

                TextView tx = new TextView(mcontext);
                tx.setText(mathine.display() + "\n");

                ViewGroup.LayoutParams ly = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tx.setLayoutParams(ly);
                lyContainer.addView(tx);

                Logger.getLogger("Tyler.tang").info("加工完成：线程：" + Thread.currentThread().getId());
                lyContainer.invalidate();

                count++;

                String r = getResources().getString(R.string.send_msg);
                mBtn.setText(r+"\t"+count);

            }
        }
    }


}
