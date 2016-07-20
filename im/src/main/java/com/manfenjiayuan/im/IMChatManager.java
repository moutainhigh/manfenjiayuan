package com.manfenjiayuan.im;

/**
 * 管理消息的收发，完成会话管理等功能。
 * Created by bingshanguxue on 16/3/17.
 */
public class IMChatManager {

    /**
     * 从本地数据库加载聊天记录到内存的操作(强烈建议在每次进入应用的时候调用,保证进入主页面后本地会话和群组都load完毕)
     * 另外如果登陆过，app长期在后台再进的时候也可能会导致加载到内存的群组和会话为空，
     * 可以在主页面的oncreate里也加上这两句代码，当然，更好的办法应该是放在程序的开屏页
     * */
    public void loadAllConversations(){
    }

    /**
     * 获取聊天记录
     * */
    public void getConversation(Long uid){

    }

    /**
     * 发送消息
     * */
    public void sendMessage(){

    }

}
