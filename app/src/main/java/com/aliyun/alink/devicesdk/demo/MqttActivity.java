package com.aliyun.alink.devicesdk.demo;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aliyun.alink.devicesdk.app.DemoApplication;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttSubscribeRequest;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSubscribeListener;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.ALog;
import com.aliyun.alink.linksdk.tools.log.IDGenerater;

public class MqttActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MqttActivity";
    int mQosForPub = 0;
    int mIsRRPC = 0;
    TextView mPubTopic;
    TextView mPubText;
    TextView mSubTopic;

    private void setListener(int id) {
        try {
            LinearLayout demoLayout = findViewById(id);
            int size = demoLayout.getChildCount();
            for (int i = 0; i < size; i++) {
                View child = demoLayout.getChildAt(i);
                child.setOnClickListener(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);
        setListener(R.id.sub_dialog);
        setListener(R.id.pub_dialog);
        setListener(R.id.sub_button_item);

        mPubTopic = findViewById(R.id.publish_topic);
        mPubTopic.setText( "/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/user/update");

        mPubText = findViewById(R.id.pub_payload);
        mPubText.setText(
                "{\"id\":\""  + "1\", \"version\":\"1.0\"" + ",\"params\":{\"state\":\"1\"} }"
        );

        mSubTopic = findViewById(R.id.subscribe_topic);
        mSubTopic.setText( "/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/user/get");
    }

    public void onSetQos1ForPub(View view) {
        mQosForPub = 1;
        ALog.d(TAG,"mQosForPub:" + mQosForPub);
    }

    public void onSetQos0ForPub(View view) {
        mQosForPub = 0;
        ALog.d(TAG,"mQosForPub:" + mQosForPub);
    }

    public void onRRPC(View view) {
        mIsRRPC = 1;
        ALog.d(TAG,"mIsRRPC:" + mIsRRPC);
        mSubTopic.setText( "/ext/rrpc/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/xxx");
    }

    public void onNotRRPC(View view) {
        mIsRRPC = 0;
        ALog.d(TAG,"mIsRRPC:" + mIsRRPC);
        mSubTopic.setText( "/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/user/get");
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sub:{
                try {
                    MqttSubscribeRequest subscribeRequest = new MqttSubscribeRequest();
                    subscribeRequest.isSubscribe = true;
                    subscribeRequest.topic = mSubTopic.getText().toString();
                    subscribeRequest.qos = 0;
                    LinkKit.getInstance().subscribe(subscribeRequest, BaseTemplateActivity.mSubscribeListener);
                }catch (Exception e){
                    showToast("订阅异常 ");
                }
                break;
            }
            case R.id.button_pub: {

                try{
                    MqttPublishRequest request = new MqttPublishRequest();
                    request.qos = mQosForPub;
                    request.isRPC = false;
                    request.topic = mPubTopic.getText().toString();
                    request.msgId = String.valueOf(IDGenerater.generateId());
                    request.payloadObj = mPubText.getText().toString();
                    LinkKit.getInstance().publish(request, BaseTemplateActivity.mConnectSendListener);
                } catch (Exception e) {
                    showToast("发布异常 ");
                }

                break;
            }
            default:break;
        }
    }


}
