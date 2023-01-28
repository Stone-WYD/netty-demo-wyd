package com.wyd.server.handler;

import com.wyd.message.LoginRequestMessage;
import com.wyd.message.LoginResponseMessage;
import com.wyd.server.service.UserServiceFactory;
import com.wyd.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage loginRequest) throws Exception {

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (UserServiceFactory.getUserService().login(username,password)) {
            SessionFactory.getSession().bind(ctx.channel(),username);
            ctx.writeAndFlush(new LoginResponseMessage(true, "登录成功！"));
        }else ctx.writeAndFlush(new LoginResponseMessage(false,"用户名或者密码不正确！"));

    }
}
