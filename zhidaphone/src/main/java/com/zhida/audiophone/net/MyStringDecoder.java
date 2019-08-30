package com.zhida.audiophone.net;

import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * Created by YYY on 2019/8/19.
 */

@ChannelHandler.Sharable
public class MyStringDecoder extends MessageToMessageDecoder<ByteBuf> {

    // TODO Use CharsetDecoder instead.
    private final Charset charset;

    /**
     * Creates a new instance with the current system character set.
     */
    public MyStringDecoder() {
        this(Charset.defaultCharset());
    }

    /**
     * Creates a new instance with the specified character set.
     */
    public MyStringDecoder(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        /**
        Log.d(MyStringDecoder.class.getSimpleName(),"decode()decode()decode()");
        Log.d(MyStringDecoder.class.getSimpleName(),"长度:"+msg.readableBytes());
        int size=msg.readableBytes();
        byte[] date= new byte[size];
        msg.readBytes(date);
        String bb=new String(date);
        if (bb.startsWith("config"))
        {
            out.add(bb);
        }else
        {
            out.add(date);
        }
        **/
        out.add(msg.toString(charset));
    }
}
