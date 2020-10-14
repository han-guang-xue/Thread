package com.example.hostinfo.controller;

import com.example.hostinfo.bean.Anchor;
import com.example.hostinfo.conf.Task;
import com.example.hostinfo.dao.HostInfoDao;
import com.example.hostinfo.service.HostService;
import com.example.hostinfo.util.CalabashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HostInfoController {
    @Autowired
    HostInfoDao hostInfoDao;

    @Autowired Task task;

    @Autowired
    HostService hostService;


    @RequestMapping("dome")
    public List<Anchor> selectAll(){
        return hostInfoDao.selectAll(0);
    }

    @RequestMapping("insert")
    public String startInsert(){
        return hostService.insert();
    }

    @RequestMapping("restart")
    public Boolean startServer(@RequestParam(value = "ThreadNum",required = false) Integer threadNum,
                              @RequestParam(value = "Interval",required = false) Long interval,
                              @RequestParam(value = "Timeout",required = false) Integer timeout){
            task.shutdown();
            return task.runServer(threadNum,interval,timeout);
    }

    @GetMapping("getInfo")
    public Map<String,Object> getInfo(){
        Map<String,Object> resMap = new HashMap<>();
        resMap.put("Server(Calabash.Business.Daemon.Host)", CalabashUtil.status());
        resMap.put("FixedThreadPool", task.getPool());
        return resMap;
    }

    @RequestMapping("close")
    public void close(){
         task.shutdown();
    }


}
