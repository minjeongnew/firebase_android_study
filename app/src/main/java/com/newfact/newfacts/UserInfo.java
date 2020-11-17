package com.newfact.newfacts;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {
    public String id = "";
    public String sex = "";
    public String age = "0";
    public String height="0";
    public String weight="0";

    public boolean milk_allergy=false;
    public boolean soybean_allergy=false;
    public boolean peach_allergy=false;
    public boolean tomato_allergy=false;
    public boolean squid_allergy=false;

    public UserInfo(){}

    public UserInfo( String id, String sex, String age, String height, String weight,
                    boolean milk_allergy, boolean soybean_allergy, boolean peach_allergy,
                    boolean tomato_allergy, boolean squid_allergy){
        this.id = id;
        this.sex = sex;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.milk_allergy = milk_allergy;
        this.soybean_allergy = soybean_allergy;
        this.peach_allergy = peach_allergy;
        this.tomato_allergy = tomato_allergy;
        this.squid_allergy = squid_allergy;
    }

    public String allergyToString(boolean allergy){
        if(allergy) return "1";
        else return "0";
    }


//    public Map<String, Object> toMap(){
//        HashMap<String, Object> result = new HashMap<>();
//        result.put("id", id);
//        result.put("sex", sex);
//        result.put("age", age);
//        result.put("height", height);
//        result.put("weight", weight);
//        String allergy;
//        allergy = allergyToString(milk_allergy)+"/"+
//                allergyToString(soybean_allergy)+"/"+
//                allergyToString(peach_allergy)+"/"+
//                allergyToString(tomato_allergy)+"/"+
//                allergyToString(squid_allergy)+"/0";
//        result.put("allergy", allergy);
//        return result;
//    }


}
