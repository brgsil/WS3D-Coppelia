/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia;

import Demo.Environment;
import WS3DCoppelia.model.*;
import WS3DCoppelia.util.Constants.ThingsType;
import co.nstant.in.cbor.CborException;
import com.coppeliarobotics.remoteapi.zmq.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author bruno
 */
public class WS3DCoppelia {
    
    private RemoteAPIClient client;
    private RemoteAPIObjects._sim sim;
    private List<Agent> inWorldAgents = Collections.synchronizedList(new ArrayList());
    private List<Thing> inWorldThings = Collections.synchronizedList(new ArrayList());
    private float width = 5, heigth = 5;
    
    
    public WS3DCoppelia(){
        client = new RemoteAPIClient();
        sim = client.getObject().sim();
        
//        try {
//            sim.saveModel(sim.getObject("/agent[0]"), System.getProperty("user.dir") + "/workspace/agent_model.ttm");
//        } catch (CborException ex) {
//            Logger.getLogger(WS3DCoppelia.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    public WS3DCoppelia(float width_, float heigth_){
        client = new RemoteAPIClient();
        sim = client.getObject().sim();
        
        width = width_;
        heigth = heigth_;  
    }
        
    
    class mainTimerTask extends TimerTask {

        WS3DCoppelia wov;
        boolean enabled = true;

        public mainTimerTask(WS3DCoppelia wovi) {
            wov = wovi;
        }

        public void run() {
            if (enabled) {
                wov.updateState();
            }
        }

        public void setEnabled(boolean value) {
            enabled = value;
        }
    }
    
    public void updateState(){
        synchronized(inWorldThings){
            List<Thing> excludedThings = inWorldThings.stream().filter(t->t.removed).collect(Collectors.toList());
            inWorldThings.removeAll(excludedThings);
            for(Thing thg : inWorldThings){
                thg.run();
            }
        }
        synchronized(inWorldAgents){
            for(Agent agt : inWorldAgents){
                agt.run(inWorldThings);
            }
        }
    }
    
    public void startSimulation() throws java.io.IOException, CborException{
        client.setStepping(false);
        sim.startSimulation();
        
        float startTime = sim.getSimulationTime();
        while(sim.getSimulationTime() - startTime < 1){}
        
        Long floorHandle =  sim.getObject("/Floor");
        List<Float> floorSize = sim.getShapeBB(floorHandle);
        floorSize.set(0, width);
        floorSize.set(1, heigth);
        sim.setShapeBB(floorHandle, floorSize);
        
        Timer t = new Timer();
        WS3DCoppelia.mainTimerTask tt = new WS3DCoppelia.mainTimerTask(this);
        t.scheduleAtFixedRate(tt, 100, 100);
    }
    
    public void stopSimulation() throws CborException{
        sim.stopSimulation();
    }
    
    public Agent createAgent(float x, float y){
        if (Math.abs(x) > width / 2)
            x = Math.copySign(width / 2, x);
        if (Math.abs(y) > heigth / 2)
            y = Math.copySign(heigth / 2, y);
        
        Agent newAgent = new Agent(sim, x, y, width, heigth);
        synchronized(inWorldAgents){
            inWorldAgents.add(newAgent);
        }
        return newAgent;
    }
    
    public Thing createThing(ThingsType category, float x, float y){
        if (Math.abs(x) > width / 2)
            x = Math.copySign(width / 2, x);
        if (Math.abs(y) > heigth / 2)
            y = Math.copySign(heigth / 2, y);
        
        Thing newThing = new Thing(sim, category, x, y);
        synchronized (inWorldThings) {
            inWorldThings.add(newThing);
        }
        return newThing;
    }
    
    public boolean isOccupied(float x, float y){
        synchronized(inWorldThings){
            for(Thing thg : inWorldThings){
                if (thg.isInOccupancyArea(x, y)) return true;
            }
        }
        synchronized(inWorldAgents){
            for(Agent agt : inWorldAgents){
                if (agt.isInOccupancyArea(x, y)) return true;
            }
        }
        
        return false;
    }
    
    public float getWorldWidth(){
        return width;
    }
    
    public float getWorldHeigth(){
        return heigth;
    }
    
}
