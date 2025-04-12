package com.xxl.mq.core.openapi.model;

import java.io.Serializable;
import java.util.Map;


/**
 * Created by xuxueli on 16/8/28.
 */
public class RegistryRequest extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * 1、注册请求逻辑：
     *      a、注册请求数据存储：Original 数据记录在 “xxl_mq_instance#registry_data” 表字段，更新心跳时间；
     *      b、注册请求数据格式：参考 RegistryRequest
     *      <pre>
     *      {
     *          "appname":"xxx",
     *          "instance":"{UUID}",
     *          "topicList":{
     *              "topic_01":"default",
     *              "topic_02":"{uuid_01}"
     *          }
     *      }
     *      </pre>
     *
     * 2、注册数据定期计算逻辑：
     *      1、新实体生成：appname、topic，自动初始化；自动建立关联关系；若存则忽略；
     *      2、注册信息计算：30s/次，三次心跳范围内判断instance活跃；appname维度聚合计算，同步至 “xxl_mq_application#registry_data”；
     *      3、注册信息数据结构：
     *      <pre>
     *      {
     *          "instanceList":{            // 根据appname查询活跃节点；根据节点顺序，计算instance负责 分区 范围；
     *              "instance_01":[0,5000],
     *              "instance_01":[5001,10000]
     *          },
     *          "topicList":{               // 根据appname查询活跃节点，聚合活跃节点下 topic 数据，计算topic下 group 列表信息；
     *              "topic01":["default"],
     *              "topic02":["uuid_01", "uuid_02"]
     *          }
     *      }
     *      </pre>
     *
     * 3、本次缓存计算逻辑：
     *      1、appname 缓存信息：缓存数据结构同上文 “注册数据定期计算逻辑”。
     *      2、topic 缓存信息：“xxl_mq_topic” 构建缓存；key 为 topic，存储数据包含 appname。借助该缓存，支持通过 topic 查询 appname 缓存，从而查看 instance分区分配信息 以及 topic在线分组 信息；
     *
     */

    private String appname;
    private String instanceUuid;
    private Map<String, String> topicData;

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getInstanceUuid() {
        return instanceUuid;
    }

    public void setInstanceUuid(String instanceUuid) {
        this.instanceUuid = instanceUuid;
    }

    public Map<String, String> getTopicData() {
        return topicData;
    }

    public void setTopicData(Map<String, String> topicData) {
       this.topicData = topicData;
    }

    @Override
    public String toString() {
        return "RegistryRequest{" +
                "appname='" + appname + '\'' +
                ", instanceUuid='" + instanceUuid + '\'' +
                ", topicData=" + topicData +
                '}';
    }

}
