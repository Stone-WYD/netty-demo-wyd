package com.wyd.server.handler;

import com.wyd.message.RpcRequestMessage;
import com.wyd.message.RpcResponseMessage;
import com.wyd.server.service.ServicesFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) throws Exception {
        RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(msg.getSequenceId());
        try {
            Class<?> serviceClass = Class.forName(msg.getInterfaceName());
            Object service = ServicesFactory.getService(serviceClass);
            Method method = serviceClass.getMethod(msg.getMethodName(), msg.getParameterTypes());
            Object result = method.invoke(service, msg.getParameterValue());

            response.setReturnValue(result);
        }catch (Exception e){
            e.printStackTrace();
            String errorMsg = e.getCause().getMessage();
            response.setExceptionValue(new Exception("远程调用出错：" + errorMsg ));
        }
        ctx.writeAndFlush(response);
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        RpcRequestMessage msg = new RpcRequestMessage(1,
                "com.wyd.server.service.HelloService",
                "sayHello",
                String.class,
                new Class[]{String.class},
                new Object[]{"张三"}
                );
        Class<?> serviceClass = Class.forName(msg.getInterfaceName());
        Object service = ServicesFactory.getService(serviceClass);
        Method method = serviceClass.getMethod(msg.getMethodName(), msg.getParameterTypes());
        Object result = method.invoke(service, msg.getParameterValue());

        System.out.println(result);
    }
}
