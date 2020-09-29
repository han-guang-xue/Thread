package com.example.hostinfo.dao;

import com.example.hostinfo.bean.Anchor;
import com.example.hostinfo.bean.HostInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;


@Repository
public interface HostInfoDao {
    List<Anchor> selectAll(@Param("LastIndex") Integer lastIndex);

    List<Anchor> selectByToDay(@Param("ToDay") Timestamp ToDay);

    int insert(@Param("list") List<HostInfo> list);
}
