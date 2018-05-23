package com.cncustompoc.SingletonSrvcs.Services;

import com.cncustompoc.SingletonSrvcs.domains.VibrationData;
import com.cncustompoc.SingletonSrvcs.repository.SampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StorageDataService {
    Logger logger= LoggerFactory.getLogger(StorageDataService.class);
    @Autowired
    private SampleRepository sampleRepository;

    @Transactional(rollbackFor = Throwable.class)
    public boolean saveInfoToDB(List<VibrationData> detailinfos){
        for(VibrationData detailinfo:detailinfos) {
            sampleRepository.insertVibrationData(detailinfo);
            logger.debug("saved inforamtion "+detailinfo.toString());
        }
        return true;
    }
}
