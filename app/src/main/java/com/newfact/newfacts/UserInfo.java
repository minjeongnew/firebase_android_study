package com.newfact.newfacts;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {
    // 사용자 데이터를 저장해놓는 UserInfo 클래스
    // 하나의 데이터만을 공유하기 때문에 싱글톤 클래스로 작성함

    private static final UserInfo instance = new UserInfo();

    private UserInfo() {
    }

    public static UserInfo getInstance() {
        return instance;
    }

    private String id = null;
    private String sex = null;
    private String age = null;
    private String height = null;
    private String weight = null;

    private String allergy = null;
    private String nutrition= null;

    // setter 메소드
    public void setId(String id){ this.id = id; }
    public void setSex(String sex){ this.sex = sex; }
    public void setAge(String age){ this.age = age; }
    public void setHeight(String height){ this.height = height; }
    public void setWeight(String weight){ this.weight = weight; }
    public void setAllergy(String allergy){ this.allergy = allergy;}
    public void setNutrition(String nutrition){this.nutrition = nutrition;}

    // getter 메소드
    public String getId(){return this.id;}
    public String getSex(){return this.sex;}
    public String getAge(){return this.age;}
    public String getHeight(){return this.height;}
    public String getWeight(){return this.weight;}
    public String getAllergy(){return this.allergy;}
    public String getNutrition(){return this.nutrition;}

}
