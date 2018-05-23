package com.cncustompoc.SingletonSrvcs.Controller;

import com.cncustompoc.SingletonSrvcs.Services.StorageDataService;
import com.cncustompoc.SingletonSrvcs.domains.VibrationData;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@EnableScheduling
public class MessageRestController {
    Logger logger= LoggerFactory.getLogger(MessageRestController.class);

    private String password="butCh@4Hurry";
    private String username="cloud.admin";
    @Value("${faw.iot.domainname}")
    private String domainname;
    @Value("${faw.iot.prefixA}")
    private String prefixA;
    @Value("${faw.iot.prefixB}")
    private String prefixB;

    private final String apiAuthUrl="https://uscom-east-1.storage.oraclecloud.com/auth/v1.0";

    private final String dataUrl="https://uscom-east-1.storage.oraclecloud.com/v1/Storage-gse00015250/data4oac";

    @Autowired
    private StorageDataService storageDataService;

    private static final BloomFilter<String> dealIdBloomFilter = BloomFilter.create(new Funnel<String>() {
        private static final long serialVersionUID = 1L;
        @Override
        public void funnel(String arg0, PrimitiveSink arg1) {
            arg1.putString(arg0, Charsets.UTF_8);
        }
    }, 1024*1024*32);


    public RestTemplate restTemplate()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        X509TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        DefaultHttpClient base = new DefaultHttpClient();
        ctx.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        ClientConnectionManager ccm = base.getConnectionManager();
        SchemeRegistry sr = ccm.getSchemeRegistry();
        sr.register(new Scheme("https", 443, ssf));
        DefaultHttpClient httpClient = new DefaultHttpClient(ccm, base.getParams());
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }

    private String getAuthToken() {
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate= null;
        try {
            restTemplate = this.restTemplate();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return null;
        }
        //MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        //headers.setContentType(type);
        headers.add("X-Storage-User",domainname+":"+username);
        headers.add("X-Storage-Pass",password);
        HttpEntity<String> requestEntity = new HttpEntity<String>( headers);
        logger.debug(requestEntity.toString());
        HttpEntity<String> response=restTemplate.exchange(apiAuthUrl,HttpMethod.GET,requestEntity,String.class);
        HttpHeaders httpHeaders= response.getHeaders();
        logger.debug(response.toString());
        if(httpHeaders.containsKey("X-Auth-Token")){
             return httpHeaders.getFirst("X-Auth-Token");
        }
        return null;
    }

    @Scheduled(cron = "${faw.schedule.trigger}")
    private void updateTodayFiles() {
        String authToken = this.getAuthToken();
        if(authToken==null) {
            logger.error("authToken is empty");
            return;
        }
        List<String> files = getTodaysFilesList(authToken);
        if (files != null) {
            for (String file : files) {

                boolean exists = dealIdBloomFilter.mightContain(file);
                logger.info("=====have file "+file+" "+exists);
                if (!exists) {
                    String detailinfo = getDetailFromFile(authToken, file);
                    List<VibrationData> vibrationDatas=transferData(detailinfo);
                        if(vibrationDatas!=null&&storageDataService.saveInfoToDB(vibrationDatas)) {
                            dealIdBloomFilter.put(file);
                            logger.info(file+" saved to DB");
                        }
                }
            }
        }
    }

    private List<VibrationData> transferData(String detailinfo) {
        try {
            logger.debug(detailinfo);
            CSVParser parser = CSVParser.parse(detailinfo, CSVFormat.EXCEL.withHeader());
            List<CSVRecord> records=parser.getRecords();
            if(records!=null&&records.size()>0) {
                List<VibrationData> result=Lists.newArrayList();
                for (CSVRecord record : records) {
                    VibrationData vd=new VibrationData();
                    if(record.isMapped("ACCE_X"))
                    vd.setACCE_X(Double.valueOf(record.get("ACCE_X")).doubleValue());
                    if(record.isMapped("ACCE_Y"))
                    vd.setACCE_Y(Double.valueOf(record.get("ACCE_Y")).doubleValue());
                    if(record.isMapped("ACCE_Z"))
                    vd.setACCE_Z(Double.valueOf(record.get("ACCE_Z")).doubleValue());
                    if(record.isMapped("MOTION_X"))
                    vd.setMOTION_X(Double.valueOf(record.get("MOTION_X")).doubleValue());
                    if(record.isMapped("MOTION_Y"))
                    vd.setMOTION_Y(Double.valueOf(record.get("MOTION_Y")).doubleValue());
                    if(record.isMapped("MOTION_Z"))
                    vd.setMOTION_Z(Double.valueOf(record.get("MOTION_Z")).doubleValue());
                    if(record.isMapped("RMS_X"))
                    vd.setRMS_X(Double.valueOf(record.get("RMS_X")).doubleValue());
                    if(record.isMapped("RMS_Y"))
                    vd.setRMS_Y(Double.valueOf(record.get("RMS_Y")).doubleValue());
                    if(record.isMapped("RMS_Z"))
                    vd.setRMS_Z(Double.valueOf(record.get("RMS_Z")).doubleValue());
                    if(record.isMapped("TEMP"))
                    vd.setTEMP(Double.valueOf(record.get("TEMP")).doubleValue());
                    vd.setDESTINATION_EP_KEY(record.get("DESTINATION_EP_KEY"));
                    vd.setEVENT_TIME_AS_STRING(record.get("EVENT_TIME_AS_STRING"));
                    vd.setRECEIVED_TIME_AS_STRING(record.get("RECEIVED_TIME_AS_STRING"));
                    vd.setSENDER_EP_KEY(record.get("SENDER_EP_KEY"));
                    vd.setSOURCE_EP_KEY(record.get("SENDER_EP_KEY"));
                    if(record.isMapped("MSG_UUID"))
                    vd.setMSG_UUID(record.get("MSG_UUID"));
                    vd.setPRIORITY(record.get("PRIORITY"));
                    if(record.isMapped("SOURCE_ID"))
                    vd.setSOURCE_ID(record.get("SOURCE_ID"));
                    if(record.isMapped("RECEIVED_TIME"))
                    vd.setRECEIVED_TIME(new Timestamp(Long.valueOf(record.get("RECEIVED_TIME")).longValue()));
                    if(record.isMapped("EVENT_TIME"))
                    vd.setEVENT_TIME(new Timestamp(Long.valueOf(record.get("EVENT_TIME")).longValue()));
                    vd.setCREATETIME(record.get("CREATETIME"));
                    result.add(vd);
                }
                return result;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    private List<String> getTodaysFilesList(String authToken){
        DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
        String dateStr=df.format(new Date());
        String url1=dataUrl+"?prefix="+prefixA+"-";
        String url2=dataUrl+"?prefix="+prefixB+"-";
        if(authToken!=null&&!"".equals(authToken)) {
            String files1 = getFilesList(authToken, url1);
            String files2 = getFilesList(authToken, url2);
            List ls = Lists.newArrayList();
            ls = addToList(ls, files1);
            ls = addToList(ls, files2);
            return ls;
        }
        return null;
    }
    private String getDetailFromFile(String authToken,String filename){
        String url=dataUrl+"/"+filename;
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate= null;
        try {
            restTemplate = this.restTemplate();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return null;
        }
        //MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        //headers.setContentType(type);
        headers.add("X-Auth-Token",authToken);
        HttpEntity<String> requestEntity = new HttpEntity<String>( headers);
        HttpEntity<String> response=restTemplate.exchange(url,HttpMethod.GET,requestEntity,String.class);
        if(response!=null){
            return response.getBody();
        }
        return null;
    }
    private List addToList(List ls,String content){
        logger.debug(content);
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        String line;
        StringBuffer strbuf=new StringBuffer();
        try{
            while ( (line = br.readLine()) != null ) {
                if(!line.trim().equals("")){
                    ls.add(line);
                }
            }
        }catch(Exception ex){
            logger.error(ex.toString(),ex);
        }finally{
            return ls;
        }
    }
    private String getFilesList(String authToken,String url){
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate= null;
        try {
            restTemplate = this.restTemplate();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return null;
        }
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("X-Auth-Token",authToken);
        HttpEntity<String> requestEntity = new HttpEntity<String>( headers);
        HttpEntity<String> response=restTemplate.exchange(url,HttpMethod.GET,requestEntity,String.class);
        if(response!=null){
            return response.getBody();
        }
        return null;
    }
}
