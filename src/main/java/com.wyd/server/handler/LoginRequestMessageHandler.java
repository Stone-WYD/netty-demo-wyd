package com.wyd.server.handler;

import com.wyd.message.LoginRequestMessage;
import com.wyd.message.LoginResponseMessage;
import com.wyd.server.service.UserServiceFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage loginRequest) throws Exception {

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (UserServiceFactory.getUserService().login(username,password)) {
            ctx.writeAndFlush(new LoginResponseMessage(true, "登录成功！"));
        }else ctx.writeAndFlush(new LoginResponseMessage(false,"用户名或者密码不正确！"));

    }
}
