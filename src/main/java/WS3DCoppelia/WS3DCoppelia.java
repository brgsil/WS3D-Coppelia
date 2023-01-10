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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bruno
 */
public class WS3DCoppelia {
    
    private RemoteAPIClient client;
    private RemoteAPIObjects._sim sim;
    private List<Agent> inWorldAgents = new ArrayList();
    private List<Thing> inWorldThings = new ArrayList();
    
    
    public WS3DCoppelia(){
        client = new RemoteAPIClient();
        sim = client.getObject().sim();
        
//        try {
//            sim.saveModel(sim.getObject("/agent[0]"), System.getProperty("user.dir") + "/workspace/agent_model.ttm");
//        } catch (CborException ex) {
//            Logger.getLogger(WS3DCoppelia.class.getName()).log(Level.SEVERE, null, ex);
//        }
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
        for(Thing thg : inWorldThings){
            thg.run();
        }
        for(Agent agt : inWorldAgents){
            agt.run(inWorldThings);
        }
    }
    
    public void startSimulation() throws java.io.IOException, CborException{
        client.setStepping(false);
        sim.startSimulation();
        
        float startTime = sim.getSimulationTime();
        while(sim.getSimulationTime() - startTime < 1){}
        
        Timer t = new Timer();
        WS3DCoppelia.mainTimerTask tt = new WS3DCoppelia.mainTimerTask(this);
        t.scheduleAtFixedRate(tt, 100, 100);
    }
    
    public void stopSimulation() throws CborException{
        sim.stopSimulation();
    }
    
    public Agent createAgent(float x, float y){
        Agent newAgent = new Agent(sim, x, y);
        inWorldAgents.add(newAgent);
        return newAgent;
    }
    
    public Thing createThing(ThingsType category, float x, float y){
        Thing newThing = new Thing(sim, category, x, y);
        inWorldThings.add(newThing);
        return newThing;
    }
    
}
