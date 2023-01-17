/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.model;

import WS3DCoppelia.util.Constants.ThingsType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bruno
 */
public class Bag {
    
    private Map<ThingsType, Integer> content = new HashMap();
    
    public Bag(){
        for(ThingsType type : ThingsType.values()){
            content.put(type, 0);
        }
    }
    
    public void insertItem(ThingsType type, int num){
        int current = content.get(type);
        content.put(type, current + num);
    }
    
    public boolean removeItem(ThingsType type, int num){
        int current = content.get(type);
        if(current >= num){
            content.put(type, current - num);
            return true;
        }
        return false;
    }
    
    public int getTotalCountOf(ThingsType type){
        return content.get(type);
    }
}
