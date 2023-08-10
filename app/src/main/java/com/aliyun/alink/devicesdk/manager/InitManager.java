package com.aliyun.alink.devicesdk.manager;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.alink.devicesdk.app.AppLog;
import com.aliyun.alink.devicesdk.app.DemoApplication;
import com.aliyun.alink.dm.api.DeviceInfo;
import com.aliyun.alink.dm.api.IoTApiClientConfig;
import com.aliyun.alink.linkkit.api.ILinkKitConnectListener;
import com.aliyun.alink.linkkit.api.IoTDMConfig;
import com.aliyun.alink.linkkit.api.IoTH2Config;
import com.aliyun.alink.linkkit.api.IoTMqttClientConfig;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linkkit.api.LinkKitInitParams;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.MqttConfigure;
import com.aliyun.alink.linksdk.cmp.api.ConnectSDK;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.connect.hubapi.HubApiRequest;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectNotifyListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper;
import com.aliyun.alink.linksdk.tools.AError;

import java.util.HashMap;
import java.util.Map;

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

public class InitManager {
    private static final String TAG = "InitManager";

    /**
     * 如果需要动态注册设备获取设备的deviceSecret， 可以参考本接口实现。
     * 动态注册条件检测：
     * 1.云端开启该设备动态注册功能；
     * 2.首先在云端创建 pk，dn；
     * @param context 上下文
     * @param productKey 产品类型
     * @param deviceName 设备名称 需要现在云端创建
     * @param productSecret 产品密钥
     * @param listener 密钥请求回调
     */
    public static void registerDevice(Context context, String productKey, String deviceName, String productSecret, IConnectSendListener listener) {
        DeviceInfo myDeviceInfo = new DeviceInfo();
        myDeviceInfo.productKey = productKey;
        myDeviceInfo.deviceName = deviceName;
        myDeviceInfo.productSecret = productSecret;
        LinkKitInitParams params = new LinkKitInitParams();
        params.connectConfig = new IoTApiClientConfig();
        // 如果明确需要切换域名，可以设置 connectConfig 中 domain 的值；
        params.deviceInfo = myDeviceInfo;
        HubApiRequest hubApiRequest = new HubApiRequest();
        hubApiRequest.path = "/auth/register/device";
        // 调用动态注册接口
        LinkKit.getInstance().deviceRegister(context, params, hubApiRequest, listener);
    }

    /**
     * Android 设备端 SDK 初始化示例代码
     * @param context 上下文
     * @param productKey 产品类型
     * @param deviceName 设备名称
     * @param deviceSecret 设备密钥
     * @param productSecret 产品密钥
     * @param callback 初始化建联结果回调
     */
    public static void init(final Context context, String productKey, String deviceName, String deviceSecret, String productSecret, String mqttHost, final IDemoCallback callback) {
        final LinkKitInitParams params = new LinkKitInitParams();

        //Step1: 构造三元组信息对象
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.productKey = productKey;  // 产品类型
        deviceInfo.deviceName = deviceName;  // 设备名称
        deviceInfo.deviceSecret = deviceSecret;  // 设备密钥
        deviceInfo.productSecret = productSecret;  // 产品密钥
        params.deviceInfo = deviceInfo;

        //Step2: 全局默认域名
        IoTApiClientConfig userData = new IoTApiClientConfig();
        params.connectConfig = userData;

        //Step3: 物模型缓存
        Map<String, ValueWrapper> propertyValues = new HashMap<>();
        /**
         * 物模型的数据会缓存到该字段中. 不可删除或者设置为空, 否则功能会异常
         * 用户调用物模型上报接口之后，物模型会有相关数据缓存。
         */
        params.propertyValues = propertyValues;

        //Step4: mqtt设置
        /**
         * 慎用
         * Mqtt 相关参数设置,包括接入点等信息.具体见deviceinfo文件说明
         * 域名、产品密钥、认证安全模式等；
         */
        IoTMqttClientConfig clientConfig = new IoTMqttClientConfig(productKey, deviceName, deviceSecret);
        clientConfig.receiveOfflineMsg = false;//cleanSession=1 不接受离线消息
        //mqtt接入点信息. 详情请参照https://help.aliyun.com/document_detail/147356.htm
        clientConfig.channelHost = mqttHost;
        params.mqttClientConfig = clientConfig;

        //Step5: 高阶功能功能配置,默认均为关闭状态
        IoTDMConfig ioTDMConfig = new IoTDMConfig();
        // 默认开启物模型功能，开启之后init方法会等到物模型初始化（包含请求云端物模型）完成之后才返回onInitDone
        ioTDMConfig.enableThingModel = true;
        // 默认不开启网关功能，开启之后，初始化的时候会初始化网关模块，获取云端网关子设备列表
        ioTDMConfig.enableGateway = false;
        // 默认不开启，是否开启日志推送功能
        ioTDMConfig.enableLogPush = false;
        params.ioTDMConfig = ioTDMConfig;

        //Step6: 下行消息处理回调设置
        LinkKit.getInstance().registerOnPushListener(notifyListener);

        //Step7: 一型一密免预注册设置(可选)
        //对于一型一密免预注册的设备, 设备连云时要用上deviceToken和clientId
        MqttConfigure.deviceToken = DemoApplication.deviceToken;
        MqttConfigure.clientId = DemoApplication.clientId;

        //Step8: H2文件上传设置(可选)
        /**
         * 如果要用到HTTP2文件上传, 需要用户设置域名
         */
        IoTH2Config ioTH2Config = new IoTH2Config();
        ioTH2Config.clientId = "client-id";
        ioTH2Config.endPoint = "https://" + productKey + ioTH2Config.endPoint;// 线上环境
        params.iotH2InitParams = ioTH2Config;

        //Step9: id2相关设置(可选)
        /**
         *  如果要用到id2的方式鉴权, 请确认在id2控制台开启相关服务后, 再打开下列代码
         *  同时，在./app/src/main/res/raw/deviceinfo中，将deviceSecret设置为itls_secret，并填入productSecret
         */
        //  Id2ItlsSdk.init(context);
        //  if ("itls_secret".equals(deviceSecret)){
        //     clientConfig.channelHost = productKey + ".itls.cn-shanghai.aliyuncs.com:1883";//线上
        //     clientConfig.productSecret = productSecret;
        //     clientConfig.secureMode = 8;

        //     对于对于企业实例, 或者2021年07月30日之后（含当日）开通的物联网平台服务下公共实例的客户，需要通过如下方式指定实例id(格式如iot-xxxxxx)
        //     MqttConfigure.extraMqttClientIdItems=",instanceId=" + "${实例id}";
        //   }

        /**
         * 设备初始化建联
         * onError 初始化建联失败，如果因网络问题导致初始化失败，需要用户重试初始化
         * onInitDone 初始化成功
         */
        LinkKit.getInstance().init(context, params, new ILinkKitConnectListener() {
            @Override
            public void onError(AError error) {
                AppLog.d(TAG, "onError() called with: error = [" + getAErrorString(error) + "]");
                callback.onError(error);
            }

            @Override
            public void onInitDone(Object data) {
                AppLog.d(TAG, "onInitDone() called with: data = [" + data + "]");
                callback.onInitDone(data);
            }
        });
    }

    /**
     * 下行监听器，云端 MQTT 下行数据都会通过这里回调
     */
    private static IConnectNotifyListener notifyListener = new IConnectNotifyListener() {
        /**
         * onNotify 会触发的前提是 shouldHandle 没有指定不处理这个topic
         * @param connectId 连接类型，这里判断是否长链 connectId == ConnectSDK.getInstance().getPersistentConnectId()
         * @param topic 下行的topic
         * @param aMessage 下行的数据内容
         */
        @Override
        public void onNotify(String connectId, String topic, AMessage aMessage) {
            String data = new String((byte[]) aMessage.data);
            // 服务端返回数据示例  data = {"method":"thing.service.test_service","id":"123374967","params":{"vv":60},"version":"1.0.0"}
            AppLog.d(TAG, "onNotify() called with: connectId = [" + connectId + "], topic = [" + topic + "], aMessage = [" + data + "]");

            if (ConnectSDK.getInstance().getPersistentConnectId().equals(connectId) && !TextUtils.isEmpty(topic) &&
                    topic.startsWith("/ext/rrpc/")) {
                ToastUtils.showToast("收到云端自定义RRPC下行：topic=" + topic + ",data=" + data);
                //示例 topic=/ext/rrpc/1138654706478941696//a1ExY4afKY1/testDevice/user/get
                //AppLog.d(TAG, "receice Message=" + new String((byte[]) aMessage.data));
                // 服务端返回数据示例  {"method":"thing.service.test_service","id":"123374967","params":{"vv":60},"version":"1.0.0"}
                MqttPublishRequest request = new MqttPublishRequest();
                request.isRPC = false;
                request.topic = topic;
                String[] array = topic.split("/");
                String resId = array[3];
                request.msgId = resId;

                String alinkdId = null;

                try{
                    JSONObject jsonObject = JSONObject.parseObject(data);
                    alinkdId = jsonObject.getString("id");
                }catch (Exception e){
                    AppLog.e(TAG,"parse alinkId failed, exit");
                    return;
                }

                // TODO 用户根据实际情况填写 仅做参考
                request.payloadObj = "{\"id\":\"" + alinkdId + "\", \"code\":\"200\"" + ",\"data\":{\"aa\":1} }";
                LinkKit.getInstance().publish(request, new IConnectSendListener() {
                    @Override
                    public void onResponse(ARequest aRequest, AResponse aResponse) {
                        // 响应成功
                        // ToastUtils.showToast("云端系统RRPC下行响应成功");
                    }

                    @Override
                    public void onFailure(ARequest aRequest, AError aError) {
                        // 响应失败
                        // ToastUtils.showToast("云端系统RRPC下行响应失败");
                    }
                });
            } else if (ConnectSDK.getInstance().getPersistentConnectId().equals(connectId) && !TextUtils.isEmpty(topic) &&
                    topic.startsWith("/sys/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/rrpc/request/")) {
                ToastUtils.showToast("收到云端系统RRPC下行：topic=" + topic + ",data=" + data);
                // AppLog.d(TAG, "receice Message=" + new String((byte[]) aMessage.data));
                // 服务端返回数据示例  {"method":"thing.service.test_service","id":"123374967","params":{"vv":60},"version":"1.0.0"}
                MqttPublishRequest request = new MqttPublishRequest();
                // 支持 0 和 1， 默认0
                // request.qos = 0;
                request.isRPC = false;
                request.topic = topic.replace("request", "response");
                String[] array = topic.split("/");
                String resId = array[6];
                request.msgId = resId;
                // TODO 用户根据实际情况填写 仅做参考
                request.payloadObj = "{\"id\":\"" + resId + "\", \"code\":\"200\"" + ",\"data\":{} }";

                LinkKit.getInstance().publish(request, new IConnectSendListener() {
                    @Override
                    public void onResponse(ARequest aRequest, AResponse aResponse) {
                        //  ToastUtils.showToast("云端系统RRPC下行响应成功");
                    }

                    @Override
                    public void onFailure(ARequest aRequest, AError aError) {
                        // ToastUtils.showToast("云端系统RRPC下行响应失败");
                    }
                });
            } else if (ConnectSDK.getInstance().getPersistentConnectId().equals(connectId) && !TextUtils.isEmpty(topic) &&
                    topic.startsWith("/sys/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/broadcast/request/")) {
                /**
                 * topic 格式：/sys/${pk}/${dn}/broadcast/request/+
                 * 无需订阅，云端免订阅，默认无需业务进行ack，但是也支持用户云端和设备端约定业务ack
                 * 示例：/sys/a14NQ5RLiZA/android_lp_test1/broadcast/request/1229336863924294656
                 * 注意：触发端数据需要进行Base64编码，否则会出现端上乱码，
                 * 如云端： org.apache.commons.codec.binary.Base64.encodeBase64String("broadcastContent".getBytes())
                 */
                //
                ToastUtils.showToast("收到云端批量广播下行：topic=" + topic + ",data=" + data);
                //TODO 根据批量广播做业务逻辑处理

            } else if (ConnectSDK.getInstance().getPersistentConnectId().equals(connectId) && !TextUtils.isEmpty(topic) &&
                    topic.startsWith("/broadcast/" + DemoApplication.productKey )) {
                //
                /**
                 * topic 需要用户自己订阅才能收到，topic 格式：/broadcast/${pk}/${自定义action}，需要和云端发送topic一致
                 * 示例：/broadcast/a14NQ5RLiZA/oldBroadcast
                 * 注意：触发端数据需要进行Base64编码，否则会出现端上乱码，
                 * 如云端： org.apache.commons.codec.binary.Base64.encodeBase64String("broadcastContent".getBytes())
                 */
                ToastUtils.showToast("收到云端广播下行：topic=" + topic + ",data=" + data);
                //TODO 根据广播做业务逻辑处理
            } else {
                ToastUtils.showToast("收到云端下行：topic=" + topic + ",data=" + data);
                /**
                 * TODO
                 * 根据订阅的具体 topic 做业务处理
                 */
            }
        }

        /**
         * @param connectId 连接类型，这里判断是否长链 connectId == ConnectSDK.getInstance().getPersistentConnectId()
         * @param topic 下行topic
         * @return 是否要处理这个topic，如果为true，则会回调到onNotify；如果为false，onNotify不会回调这个topic相关的数据。建议默认为true。
         */
        @Override
        public boolean shouldHandle(String connectId, String topic) {
            return true;
        }

        /**
         * @param connectId 连接类型，这里判断是否长链 connectId == ConnectSDK.getInstance().getPersistentConnectId()
         * @param connectState {@link ConnectState}
         *     CONNECTED, 连接成功
         *     DISCONNECTED, 已断链
         *     CONNECTING, 连接中
         *     CONNECTFAIL; 连接失败
         */
        @Override
        public void onConnectStateChange(String connectId, ConnectState connectState) {
            AppLog.d(TAG, "onConnectStateChange() called with: connectId = [" + connectId + "], connectState = [" + connectState + "]");
        }
    };

    public static String getAErrorString(AError error) {
        if (error == null) {
            return null;
        }
        return JSONObject.toJSONString(error);
    }
}
