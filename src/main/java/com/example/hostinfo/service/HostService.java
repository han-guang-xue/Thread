package com.example.hostinfo.service;

import com.example.hostinfo.bean.Anchor;
import com.example.hostinfo.bean.HostInfo;
import com.example.hostinfo.dao.HostInfoDao;
import com.example.hostinfo.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@Service
public class HostService {

    @Autowired
    HostInfoDao hostInfoDao;

    public List<Anchor> getData_toDay(Long interval){
        return hostInfoDao.selectByToDay(new Timestamp(System.currentTimeMillis() - interval));
    }

    public String insert() {
        List<HostInfo> list;
        try {
            list = FileUtil.getRes();
        } catch (IOException e) {
            e.printStackTrace();
            return "fail";
        }
        if(null != list && list.size() != 0){
            hostInfoDao.insert(list);
        }
        return "successed";
    }

}
