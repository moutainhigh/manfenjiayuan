/*
 * 文件名称: SequenceService.java
 * 版权信息: Copyright 2001-2011 ZheJiang Collaboration Data System Co., LTD. All right reserved.
 * ----------------------------------------------------------------------------------------------
 * 修改历史:
 * ----------------------------------------------------------------------------------------------
 * 修改原因: 新增
 * 修改人员: LuoJingtian
 * 修改日期: 2011-12-28
 * 修改内容: 
 */
package com.mfh.framework.database.seq;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.utils.UuidUtil;
import com.mfh.framework.anlaysis.logger.ZLogger;

/**
 * 序列业务接口
 */
public class SequenceServiceImpl implements SequenceService{
    
    /** 字符串序列的默认长度 */
    private static final int DEFAULT_SEQ_LENGTH = 10;
    
    /** 64个0 */
    private static final String PAD = "0000000000000000000000000000000000000000000000000000000000000000";
    
    private SequenceDao sequenceDao = new SequenceDao();
    
    /**
     * 获取序列(不超过64位)
     * @param sequeceName 序列名称
     * @author LuoJingtian created on 2011-12-28 
     * @since SHK BMP 1.0
     */
    @Override
    public String getNextSeqStringValue(String sequeceName) {
        return getNextSeqStringValue(sequeceName, DEFAULT_SEQ_LENGTH);
    }
    
    /**
     * 获取序列(不超过64位)
     * @param sequeceName 序列名称
     * @param seqLength 序列长度
     * @author LuoJingtian created on 2012-1-16
     * @since SHK BMP 1.0
     */
    @Override
    public String getNextSeqStringValue(String sequeceName, int seqLength) {
        int length = (seqLength > 0 && seqLength < DEFAULT_SEQ_LENGTH) ? seqLength : DEFAULT_SEQ_LENGTH;
        long sequenceValue = getNextSeqLongValue(sequeceName);
        String strValue = String.valueOf(sequenceValue);
        if (strValue.length() <= length) {
            return PAD.substring(0, length - strValue.length()) + strValue;
        }
        else {
            return strValue;
        }
    }
    
    /**
     * 获取随机的UUID作为序列
     * @return UUID
     * @author LuoJingtian created on 2012-1-16 
     * @since SHK BMP 1.0
     */
    @Override
    public String getUUID() {
        return UuidUtil.getUuid();
    }
    
    /**
     * 获取序列
     * @param sequeceName 序列名称
     * @author LuoJingtian created on 2011-12-28 
     * @since SHK BMP 1.0
     */
    @Override
    public synchronized long getNextSeqLongValue(String sequeceName) {
        Sequence sequence = sequenceDao.getSequence(sequeceName);
        if (sequence == null){
            throw new RuntimeException("指定的序列名:" + sequeceName + "不存在,数据库初始化有错误!");
        }
        ZLogger.d(String.format("Sequence: %s", JSONObject.toJSONString(sequence)));
        long sequenceValue = sequence.getSequenceValue();//当前值
        long nextValue;
        if (sequenceValue < 0)//第一次
            nextValue = sequence.getMinValue();
        else{
            nextValue = sequenceValue + sequence.getStepValue();
            if (sequence.getMaxValue() > 0 && nextValue >= sequence.getMaxValue()) {
                if (sequence.isCycle()){
                    nextValue = sequence.getMinValue();
                    ZLogger.wf(String.format("序列(%d)已经达到最大值(%d),使用最小值(%d)重新生成新的序列!",
                            nextValue, sequence.getMaxValue(), sequence.getMinValue()));
                }
                else{
                    ZLogger.ef(String.format("序列(%d)已经达到最大值(%d),不能生成新的序列!",
                            nextValue, sequence.getMaxValue()));
                    throw new RuntimeException(String.format("序列(%d)已经达到最大值(%d),不能生成新的序列!",
                            nextValue, sequence.getMaxValue()));
                }
            }
        }
        sequence.setSequenceValue(nextValue);
        sequenceDao.updateSequence(sequence);
        return nextValue;
    }
    
    @Override
    public int getNextSeqIntValue(String sequeceName) {
        return (int)getNextSeqLongValue(sequeceName);
    }
    
    // -------------------------------- 以下为Getter/Setter方法 -------------------------------- //
    
    @Override
    public void checkSequence(String seqName, long minValue, Long maxValue){        
        if (minValue < 0)
            throw new RuntimeException("非法的最小值,应该是正值!");
        Sequence sequence = sequenceDao.getSequence(seqName);
        if (sequence == null){
            sequence = new Sequence();
            sequence.setId(seqName);
            sequence.setSequenceValue(-1);
            sequence.setMinValue(minValue);
            sequence.setMaxValue(maxValue);//-1代表无穷大
            sequenceDao.insertSequence(sequence);
        }
        else{
            boolean bUpdated = false;
            if (sequence.getMinValue() != minValue){
                sequence.setMinValue(minValue);
                bUpdated = true;
            }

            if (maxValue == null){
                maxValue = -1L;
            }
            if (sequence.getMaxValue() != maxValue){
                sequence.setMaxValue(maxValue);
                bUpdated = true;
            }
            if (bUpdated){
                sequenceDao.updateSequence(sequence);
            }
        }
        ZLogger.d("更新序列：" + sequence.toString());
    }

    @Override
    public void setSequenceValue(String sequeceName, long curValue) {
        Sequence sequence = sequenceDao.getSequence(sequeceName);
        if (sequence == null){
            ZLogger.ef("指定的序列名:" + sequeceName + "不存在,设置序列编号!");
            return;
        }

        long sequenceValue = sequence.getSequenceValue();//当前值
        if (sequenceValue < curValue){
            sequence.setSequenceValue(curValue);
            sequenceDao.updateSequence(sequence);
        }
        ZLogger.d(String.format("Sequence: %s", JSONObject.toJSONString(sequence)));

    }

    @Override
    public void setSequenceValue(String sequeceName, long curValue, Long maxValue) {
        Sequence sequence = sequenceDao.getSequence(sequeceName);
        if (sequence == null){
            ZLogger.ef("指定的序列名:" + sequeceName + "不存在,设置序列编号!");
            return;
        }

        long sequenceValue = sequence.getSequenceValue();//当前值
        if (sequenceValue < curValue){
            sequence.setSequenceValue(curValue);
        }
        if (maxValue == null){
            maxValue = -1L;
        }
        if (sequence.getMaxValue() != maxValue){
            sequence.setMaxValue(maxValue);
        }

        sequenceDao.updateSequence(sequence);

        ZLogger.d(String.format("Sequence: %s", JSONObject.toJSONString(sequence)));

    }
}
