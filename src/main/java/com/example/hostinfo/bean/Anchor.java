package com.example.hostinfo.bean;

public class Anchor {

    private Integer Id;
    private String RoomId;
    private String PlantformUid;
    private String Plantform;
    private String UpdateTime;

    public String getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(String updateTime) {
        UpdateTime = updateTime;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public String getRoomId() {
        return RoomId;
    }

    public void setRoomId(String roomId) {
        RoomId = roomId;
    }

    public String getPlantformUid() {
        return PlantformUid;
    }

    public void setPlantformUid(String plantformUid) {
        PlantformUid = plantformUid;
    }

    public String getPlantform() {
        return Plantform;
    }

    public void setPlantform(String plantform) {
        Plantform = plantform;
    }
}
