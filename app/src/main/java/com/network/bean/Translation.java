package com.network.bean;

import android.util.Log;

/**
 * Created by wangliming on 2017/12/11.
 */

public class Translation {
    private int status;
    private Content content;

    private static class Content {
        private String from;
        private String to;
        private String vendor;
        private String out;
        private int errNo;
    }

    //定义 输出返回数据 的方法
    public void show() {
        Log.e("meme",status+"");
        Log.e("meme",content.from+"");
        Log.e("meme",content.to+"");
        Log.e("meme",content.vendor+"");
        Log.e("meme",content.out+"");
        Log.e("meme",content.errNo+"");
    }

}
