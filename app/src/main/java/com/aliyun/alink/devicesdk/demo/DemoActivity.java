package com.aliyun.alink.devicesdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.alink.devicesdk.app.AppLog;
import com.aliyun.alink.devicesdk.app.DemoApplication;
import com.aliyun.alink.devicesdk.app.DeviceInfoData;
import com.aliyun.alink.devicesdk.manager.IDemoCallback;
import com.aliyun.alink.devicesdk.manager.InitManager;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.MqttConfigure;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.log.IDGenerater;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;


/*
 * Copyright (c) 2014-2016 Alibaba Group. All rights reserved.
 * License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

public class DemoActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "DemoActivity";

    private TextView errorTV = null;
    private AtomicInteger testDeviceIndex = new AtomicInteger(0);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppLog.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        errorTV = findViewById(R.id.id_error_info);
        setListener();
    }

    private void setListener() {
        try {
            LinearLayout demoLayout = findViewById(R.id.id_demo_layout);
            int size = demoLayout.getChildCount();
            for (int i = 0; i < size; i++) {
                if (i == size - 1) {
                    break;
                }
                View child = demoLayout.getChildAt(i);
                child.setOnClickListener(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.w(TAG, "setListener exception " + e);
        }
    }

    public void startOTATest(View view) {
        if (!checkReady()) {
            return;
        }

        Intent intent = new Intent(this, OTAActivity.class);
        startActivity(intent);
    }


    public void startBreezeOTATest(View view) {
        if (!checkReady()) {
            return;
        }

//        Intent intent = new Intent(this, BreezeOtaActivity.class);
//        startActivity(intent);
    }

    public void startLPTest(View view) {
        if (!checkReady()) {
            return;
        }
        if (LinkKit.getInstance().getDeviceThing() == null) {
            showToast("物模型功能未启用");
            return;
        }
        Intent intent = new Intent(this, TSLActivity.class);
        startActivity(intent);
    }

    public void startLabelTest(View view) {
        if (!checkReady()) {
            return;
        }
        Intent intent = new Intent(this, LabelActivity.class);
        startActivity(intent);
    }

    public void startCOTATest(View view) {
        if (!checkReady()) {
            return;
        }
        Intent intent = new Intent(this, COTAActivity.class);
        startActivity(intent);
    }

    public void startShadowTest(View view) {
        if (!checkReady()) {
            return;
        }
        Intent intent = new Intent(this, ShadowActivity.class);
        startActivity(intent);
    }

    public void startGatewayTest(View view) {
        if (!checkReady()) {
            return;
        }
        if (LinkKit.getInstance().getGateway() == null) {
            showToast("网关功能未启用");
            return;
        }
        Intent intent = new Intent(this, GatewayActivity.class);
        startActivity(intent);
    }

    private boolean checkReady() {
        if (DemoApplication.userDevInfoError) {
            showToast("设备三元组信息res/raw/deviceinfo格式错误");
            return false;
        }
        if (!DemoApplication.isInitDone) {
            showToast("初始化尚未成功，请稍后点击");
            return false;
        }
        errorTV.setVisibility(View.GONE);
        return true;
    }

    public void startH2FileTest(View view) {
        if (!checkReady()) {
            return;
        }
        Intent intent = new Intent(this, H2FileManagerActivity.class);
        startActivity(intent);
    }

    public void startLogPush(View view) {
        if (!checkReady()) {
            return;
        }
        Intent intent = new Intent(this, LogPushActivity.class);
        startActivity(intent);
    }

    public void startMqttTest(View view) {
        if (!checkReady()) {
            return;
        }
        Intent intent = new Intent(this, MqttActivity.class);
        startActivity(intent);
    }

    private void startResetTest(View v) {
        Intent intent = new Intent(this, ResetActivity.class);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_start_LP:
                startLPTest(v);
                break;
            case R.id.id_start_label:
                startLabelTest(v);
                break;
            case R.id.id_start_cota:
                startCOTATest(v);
                break;
            case R.id.id_start_shadow:
                startShadowTest(v);
                break;
            case R.id.id_start_gateway:
                startGatewayTest(v);
                break;
            case R.id.id_start_ota:
                startOTATest(v);
                break;
            case R.id.id_start_breeze_ota:
                startBreezeOTATest(v);
                break;
            case R.id.id_start_h2_file:
                startH2FileTest(v);
                break;
            case R.id.id_test_init:
                connect();
                break;
            case R.id.id_test_deinit:
                deinit();
                break;
            case R.id.id_mqtt_test:
//                testJniLeakWithCoAP();
                startMqttTest(v);
                break;
            case R.id.id_test_reset:
                startResetTest(v);
                break;
            case R.id.id_log_push:
                startLogPush(v);
                break;
        }
    }

    private static ArrayList<DeviceInfoData> getTestDataList() {
        ArrayList<DeviceInfoData> infoDataArrayList = new ArrayList<DeviceInfoData>();

        DeviceInfoData test6 = new DeviceInfoData();
        test6.productKey = DemoApplication.productKey;
        test6.deviceName = DemoApplication.deviceName;
        test6.deviceSecret = DemoApplication.deviceSecret;
        infoDataArrayList.add(test6);
        return infoDataArrayList;
    }

    /**
     * 初始化
     * 耗时操作，建议放到异步线程
     */
    private void connect() {
        AppLog.d(TAG, "connect() called");
        // SDK初始化
        DeviceInfoData deviceInfoData = getTestDataList().get(testDeviceIndex.getAndIncrement() % getTestDataList().size());
        DemoApplication.productKey = deviceInfoData.productKey;
        DemoApplication.deviceName = deviceInfoData.deviceName;
        DemoApplication.deviceSecret = deviceInfoData.deviceSecret;


        new Thread(new Runnable() {
            @Override
            public void run() {
                InitManager.init(DemoActivity.this, DemoApplication.productKey, DemoApplication.deviceName,
                        DemoApplication.deviceSecret, DemoApplication.productSecret, DemoApplication.mqttHost,new IDemoCallback() {

                            @Override
                            public void onError(AError aError) {
                                AppLog.d(TAG, "onError() called with: aError = [" + InitManager.getAErrorString(aError) + "]");
                                // 初始化失败，初始化失败之后需要用户负责重新初始化
                                // 如一开始网络不通导致初始化失败，后续网络恢复之后需要重新初始化

                                if (aError != null) {
//                                    AppLog.d(TAG, "初始化失败，错误信息：" + aError.getCode() + "-" + aError.getSubCode() + ", " + aError.getMsg());
                                    showToast("初始化失败，错误信息：" + aError.getCode() + "-" + aError.getSubCode() + ", " + aError.getMsg());
                                } else {
//                                    AppLog.d(TAG, "初始化失败");
                                    showToast("初始化失败");
                                }
                            }

                            @Override
                            public void onInitDone(Object data) {
                                AppLog.d(TAG, "onInitDone() called with: data = [" + data + "]");
                                DemoApplication.isInitDone = true;
                                showToast("初始化成功");
//                                AppLog.d(TAG, "初始化成功");
                            }
                        });
            }
        }).start();
    }

    /**
     * 耗时操作，建议放到异步线程
     * 反初始化同步接口
     */
    private void deinit() {
        AppLog.d(TAG, "deinit");
        DemoApplication.isInitDone = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 同步接口
                LinkKit.getInstance().deinit();
                showToast("反初始化成功");
//                AppLog.d(TAG, "反初始化成功");
            }
        }).start();
    }

    private void publishTest() {
        try {
            AppLog.d(TAG, "publishTest called.");
            MqttPublishRequest request = new MqttPublishRequest();
            // 支持 0 和 1， 默认0
            request.qos = 1;
            request.isRPC = false;
            request.topic = "/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/user/update";
            request.msgId = String.valueOf(IDGenerater.generateId());
            // TODO 用户根据实际情况填写 仅做参考
            request.payloadObj = "{\"id\":\"" + request.msgId + "\", \"version\":\"1.0\"" + ",\"params\":{\"state\":\"1\"} }";
            LinkKit.getInstance().publish(request, new IConnectSendListener() {
                @Override
                public void onResponse(ARequest aRequest, AResponse aResponse) {
                    AppLog.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + aResponse + "]");
                    showToast("发布成功");
                }

                @Override
                public void onFailure(ARequest aRequest, AError aError) {
                    AppLog.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
                    showToast("发布失败 " + (aError != null ? aError.getCode() : "null"));
                }
            });
        } catch (Exception e) {
            showToast("发布异常 ");
        }
    }


    private ScheduledFuture future = null;

    @Override
    protected void onResume() {
        super.onResume();
//        testJniLeak();
//        future =future ThreadPool.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                publishTest();
//            }
//        }, 0, 15, TimeUnit.SECONDS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (future != null) {
                future.cancel(true);
                future = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
