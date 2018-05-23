package com.cncustompoc.SingletonSrvcs.domains;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class VibrationData {
    private String SOURCE_ID;
    private double ACCE_Y=0.0;
    private double ACCE_Z=0.0;
    private double ACCE_X=0.0;
    private double MOTION_X=0.0;
    private double MOTION_Y=0.0;
    private double MOTION_Z=0.0;
    private String RECEIVED_TIME_AS_STRING;
    private Timestamp RECEIVED_TIME;
    private String PRIORITY;
    private String EVENT_TIME_AS_STRING;
    private Timestamp EVENT_TIME;
    private String DESTINATION_EP_KEY;
    private String SOURCE_EP_KEY;
    private String MSG_UUID;
    private double TEMP=-99.0;
    private String SENDER_EP_KEY;
    private double RMS_Z=0.0;
    private double RMS_Y=0.0;
    private double RMS_X=0.0;
    private String CREATETIME;
}
