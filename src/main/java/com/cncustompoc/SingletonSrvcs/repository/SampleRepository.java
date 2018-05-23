package com.cncustompoc.SingletonSrvcs.repository;

import com.cncustompoc.SingletonSrvcs.domains.VibrationData;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper()
public interface SampleRepository {
    @Insert("insert into FAWIOTHISTORY(SOURCE_ID,ACCE_Y,ACCE_Z,ACCE_X,MOTION_X,RECEIVED_TIME_AS_STRING,RECEIVED_TIME,PRIORITY,EVENT_TIME_AS_STRING,EVENT_TIME,DESTINATION_EP_KEY,SOURCE_EP_KEY,MSG_UUID,TEMP,SENDER_EP_KEY,RMS_Z,RMS_Y,RMS_X,CREATETIME) values (#{SOURCE_ID},#{ACCE_Y},#{ACCE_Z},#{ACCE_X},#{MOTION_X},#{RECEIVED_TIME_AS_STRING},#{RECEIVED_TIME},#{PRIORITY},#{EVENT_TIME_AS_STRING},#{EVENT_TIME},#{DESTINATION_EP_KEY},#{SOURCE_EP_KEY},#{MSG_UUID},#{TEMP},#{SENDER_EP_KEY},#{RMS_Z},#{RMS_Y},#{RMS_X},#{CREATETIME})")
    public void insertVibrationData(VibrationData vibrationData);
}
