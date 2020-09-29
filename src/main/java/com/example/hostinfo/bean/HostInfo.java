package com.example.hostinfo.bean;
import java.sql.Timestamp;

public class HostInfo {
    private Integer UId;
    private String RoomId;
    private String Platform;
    private Integer HotNum;
    private Integer FocusNum;
    private Integer VipNum;
    private String State;
    private Timestamp CreateTime;

    public Integer getUId() {
        return UId;
    }

    public void setUId(Integer UId) {
        this.UId = UId;
    }

    public String getRoomId() {
        return RoomId;
    }

    public void setRoomId(String roomId) {
        RoomId = roomId;
    }

    public String getPlatform() {
        return Platform;
    }

    public void setPlatform(String platform) {
        Platform = platform;
    }

    public Integer getHotNum() {
        return HotNum;
    }

    public void setHotNum(Integer hotNum) {
        HotNum = hotNum;
    }

    public Integer getFocusNum() {
        return FocusNum;
    }

    public void setFocusNum(Integer focusNum) {
        FocusNum = focusNum;
    }

    public Integer getVipNum() {
        return VipNum;
    }

    public void setVipNum(Integer vipNum) {
        VipNum = vipNum;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public Timestamp getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(Timestamp createTime) {
        CreateTime = createTime;
    }
}
