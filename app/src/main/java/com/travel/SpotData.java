package com.travel;

/**
 * Created by Tinghua on 3/6/2016.
 */
public class SpotData {
    private Data[] data;

    public SpotData() {

    }

    public void setData(Data[] data) {
        this.data = data;
    }

    public Data[] getData() {
        return data;
    }

    public class Data {

        private String spotId;
        private String spotName;
        private String spotAdd;
        private String spotLat;
        private String spotLng;
        private String picture1;
        private String picture2;
        private String picture3;
        private String openTime;
        private String ticketInfo;
        private String infoDetail;

        public Data() {

        }

        public void setSpotId(String spotId)
        {
            this.spotId = spotId;
        }

        public String getSpotId()
        {
            return spotId;
        }

        public void setSpotName(String spotName)
        {
            this.spotName = spotName;
        }

        public String getSpotName()
        {
            return spotName;
        }

        public void setSpotAdd(String spotAdd)
        {
            this.spotAdd = spotAdd;
        }

        public String getSpotAdd()
        {
            return spotAdd;
        }

        public void setSpotLat(String spotLat)
        {
            this.spotLat = spotLat;
        }

        public String getSpotLat()
        {
            return spotLat;
        }

        public void setSpotLng(String spotLng)
        {
            this.spotLng = spotLng;
        }

        public String getSpotLng()
        {
            return spotLng;
        }

        public void setPicture1(String picture1)
        {
            this.picture1 = picture1;
        }

        public String getPicture1()
        {
            return picture1;
        }

        public void setPicture2(String picture2)
        {
            this.picture2 = picture2;
        }

        public String getPicture2()
        {
            return picture2;
        }

        public void setPicture3(String picture3)
        {
            this.picture3 = picture3;
        }

        public String getPicture3()
        {
            return picture3;
        }

        public void setOpenTime(String openTime)
        {
            this.openTime = openTime;
        }

        public String getOpenTime()
        {
            return openTime;
        }

        public void setTicketInfo(String ticketInfo)
        {
            this.ticketInfo = ticketInfo;
        }

        public String getTicketInfo()
        {
            return ticketInfo;
        }

        public void setInfoDetail(String infoDetail)
        {
            this.infoDetail = infoDetail;
        }

        public String getInfoDetail()
        {
            return infoDetail;
        }

    }

}
