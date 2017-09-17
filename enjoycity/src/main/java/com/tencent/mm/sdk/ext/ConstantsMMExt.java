package com.tencent.mm.sdk.ext;

public class ConstantsMMExt
{
  public static final String COLUMN_NAME_RET_CODE = "retCode";
  public static final String COLUMN_NAME_SELF_ID = "selfId";
  public static final String HARD_CODE_SOURCE = "openapi";
  
  public static final class RegisterMsgListener
  {
    public static final int REGISTER_MSG_LISTENER_MSG_STATE_ALL = 1;
    public static final int REGISTER_MSG_LISTENER_MSG_STATE_UNREAD = 2;
    public static final int REGISTER_MSG_LISTENER_MSG_TYPE_IMAGE = 8;
    public static final int REGISTER_MSG_LISTENER_MSG_TYPE_OTHER = 1;
    public static final int REGISTER_MSG_LISTENER_MSG_TYPE_TEXT = 2;
    public static final int REGISTER_MSG_LISTENER_MSG_TYPE_VOICE = 4;
    public static final int REGISTER_MSG_LISTENER_OP_REG = 1;
    public static final int REGISTER_MSG_LISTENER_OP_UN_REG = 2;
    public static final int REGISTER_MSG_LISTENER_SCENE_FAV_SINGLE_CHAT = 1;
    public static final int REGISTER_MSG_LISTENER_SCENE_GROUT_CHAT = 4;
    public static final int REGISTER_MSG_LISTENER_SCENE_SINGLE_CHAT = 2;
  }
  
  public static final class RetCode
  {
    public static final int RET_FAILED = 4;
    public static final int RET_NOT_LOGIN = 3;
    public static final int RET_OK = 1;
    public static final int RET_WRONG_ARGS = 2;
  }
}
