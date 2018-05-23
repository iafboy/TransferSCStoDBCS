package com.cncustompoc.SingletonSrvcs.Common;

import com.cncustompoc.SingletonSrvcs.domains.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class CommonParams {
    public static List<Message> messageList=new ArrayList<>();
    public static List<String> contentList=new ArrayList<>();
    public static String initTime="2016-01-10T18:53:22.000Z";
    public static LinkedBlockingQueue<String> messagequeue=new LinkedBlockingQueue();
    public final static String BASEPATH="com.cncustompoc.SingletonSrvcs";
    public final static String BASEPACKAGEPATH="com.cncustompoc.SingletonSrvcs";
    public final static String DAOPACKAGEPATH="com.cncustompoc.SingletonSrvcs.repository";
    public final static String MODELPACKAGEPATH="com.cncustompoc.SingletonSrvcs.domains";
    public final static String druidPrefix="mybatis";
}
