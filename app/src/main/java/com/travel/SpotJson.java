package com.travel;

/**
 * Created by Tinghua on 2016/1/8.
 * 台灣景點資訊
 */

public class SpotJson {

    public PostInfos Infos;

    public SpotJson() {

    }

    public void setInfos(PostInfos Infos)
    {
        this.Infos = Infos;
    }

    public PostInfos getInfos()
    {
        return Infos;
    }



    public class PostInfos {

        public PostInfo[] Info;

        public PostInfos() {

        }

        public void setInfo(PostInfo[] Info)
        {
            this.Info = Info;
        }

        public PostInfo[] getInfo()
        {
            return Info;
        }



        public class PostInfo {

            private String Add;
            private String Class1;
            private String Class2;
            private String Class3;
            private String Description;
            private String Gov;
            private String Id;
            private String Keyword;
            private String Level;
            private String Map;
            private String Name;
            private String Opentime;
            private String Orgclass;
            private String Parkinginfo;
            private String Parkinginfo_Px;
            private String Parkinginfo_Py;
            private String Picdescribe1;
            private String Picdescribe2;
            private String Picdescribe3;
            private String Picture1;
            private String Picture2;
            private String Picture3;
            private String Px;
            private String Py;
            private String Region;
            private String Remarks;
            private String Tel;
            private String Ticketinfo;
            private String Toldescribe;
            private String Town;
            private String Travellinginfo;
            private String Website;
            private String Zipcode;
            private String Zone;

            public PostInfo() {

            }

            public void setAdd(String Add)
            {
                this.Add = Add;
            }

            public String getAdd()
            {
                return Add;
            }

            public void setClass1(String Class1)
            {
                this.Class1 = Class1;
            }

            public String getClass1()
            {
                return Class1;
            }

            public void setClass2(String Class2)
            {
                this.Class2 = Class2;
            }

            public String getClass2()
            {
                return Class2;
            }

            public void setClass3(String Class3)
            {
                this.Class3 = Class3;
            }

            public String getClass3()
            {
                return Class3;
            }

            public void setDescription(String Description)
            {
                this.Description = Description;
            }

            public String getDescription()
            {
                return Description;
            }

            public void setGov(String Gov)
            {
                this.Gov = Gov;
            }

            public String getGov()
            {
                return Gov;
            }

            public void setId(String Id)
            {
                this.Id = Id;
            }

            public String getId()
            {
                return Id;
            }

            public void setKeyword(String Keyword)
            {
                this.Keyword = Keyword;
            }

            public String getKeyword()
            {
                return Keyword;
            }

            public void setLevel(String Level)
            {
                this.Level = Level;
            }

            public String getLevel()
            {
                return Level;
            }

            public void setMap(String Map)
            {
                this.Map = Map;
            }

            public String getMap()
            {
                return Map;
            }

            public void setName(String Name)
            {
                this.Name = Name;
            }

            public String getName()
            {
                return Name;
            }

            public void setOpentime(String Opentime)
            {
                this.Opentime = Opentime;
            }

            public String getOpentime()
            {
                return Opentime;
            }

            public void setOrgclass(String Orgclass)
            {
                this.Orgclass = Orgclass;
            }

            public String getOrgclass()
            {
                return Orgclass;
            }

            public void setParkinginfo(String Parkinginfo)
            {
                this.Parkinginfo = Parkinginfo;
            }

            public String getParkinginfo()
            {
                return Parkinginfo;
            }

            public void setParkinginfo_Px(String Parkinginfo_Px)
            {
                this.Parkinginfo_Px = Parkinginfo_Px;
            }

            public String getParkinginfo_Px()
            {
                return Parkinginfo_Px;
            }

            public void setParkinginfo_Py(String Parkinginfo_Py)
            {
                this.Parkinginfo_Py = Parkinginfo_Py;
            }

            public String getParkinginfo_Py()
            {
                return Parkinginfo_Py;
            }

            public void setPicdescribe1(String Picdescribe1)
            {
                this.Picdescribe1 = Picdescribe1;
            }

            public String getPicdescribe1()
            {
                return Picdescribe1;
            }

            public void setPicdescribe2(String Picdescribe2)
            {
                this.Picdescribe2 = Picdescribe2;
            }

            public String getPicdescribe2()
            {
                return Picdescribe2;
            }

            public void setPicdescribe3(String Picdescribe3)
            {
                this.Picdescribe3 = Picdescribe3;
            }

            public String getPicdescribe3()
            {
                return Picdescribe3;
            }

            public void setPicture1(String Picture1)
            {
                this.Picture1 = Picture1;
            }

            public String getPicture1()
            {
                return Picture1;
            }

            public void setPicture2(String Picture2)
            {
                this.Picture2 = Picture2;
            }

            public String getPicture2()
            {
                return Picture2;
            }

            public void setPicture3(String Picture3)
            {
                this.Picture3 = Picture3;
            }

            public String getPicture3()
            {
                return Picture3;
            }

            public void setPx(String Px)
            {
                this.Px = Px;
            }

            public String getPx()
            {
                return Px;
            }

            public void setPy(String Py)
            {
                this.Py = Py;
            }

            public String getPy()
            {
                return Py;
            }

            public void setRegion(String Region)
            {
                this.Region = Region;
            }

            public String getRegion()
            {
                return Region;
            }

            public void setRemarks(String Remarks)
            {
                this.Remarks = Remarks;
            }

            public String getRemarks()
            {
                return Remarks;
            }

            public void setTel(String Tel)
            {
                this.Tel = Tel;
            }

            public String getTel()
            {
                return Tel;
            }

            public void setTicketinfo(String Ticketinfo)
            {
                this.Ticketinfo = Ticketinfo;
            }

            public String getTicketinfo()
            {
                return Ticketinfo;
            }

            public void setToldescribe(String Toldescribe)
            {
                this.Toldescribe = Toldescribe;
            }

            public String getToldescribe()
            {
                return Toldescribe;
            }

            public void setTown(String Town)
            {
                this.Town = Town;
            }

            public String getTown()
            {
                return Town;
            }

            public void setTravellinginfo(String Travellinginfo)
            {
                this.Travellinginfo = Travellinginfo;
            }

            public String getTravellinginfo()
            {
                return Travellinginfo;
            }

            public void setWebsite(String Website)
            {
                this.Website = Website;
            }

            public String getWebsite()
            {
                return Website;
            }

            public void setZipcode(String Zipcode)
            {
                this.Zipcode = Zipcode;
            }

            public String getZipcode()
            {
                return Zipcode;
            }

            public void setZone(String Zone)
            {
                this.Zone = Zone;
            }

            public String getZone()
            {
                return Zone;
            }
        }
    }
}