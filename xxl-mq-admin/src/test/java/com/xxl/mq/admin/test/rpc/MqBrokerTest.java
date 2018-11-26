package com.xxl.mq.admin.test.rpc;

import com.xxl.mq.client.broker.IXxlMqBroker;
import com.xxl.rpc.remoting.invoker.call.CallType;
import com.xxl.rpc.remoting.invoker.reference.XxlRpcReferenceBean;
import com.xxl.rpc.remoting.net.NetEnum;
import com.xxl.rpc.serialize.Serializer;

public class MqBrokerTest {

    public static void main(String[] args) {
        String address = "127.0.0.1:7080";

        IXxlMqBroker xxlMqBroker = (IXxlMqBroker) new XxlRpcReferenceBean(NetEnum.NETTY, Serializer.SerializeEnum.HESSIAN.getSerializer(), CallType.SYNC,
                IXxlMqBroker.class, null, 10000, address, null, null, null).getObject();

        // test
        xxlMqBroker.addMessages(null);

    }
}
