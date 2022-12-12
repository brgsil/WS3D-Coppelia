/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.model;

import WS3DCoppelia.util.Constants;
import co.nstant.in.cbor.CborException;
import com.coppeliarobotics.remoteapi.zmq.RemoteAPIObjects;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bruno
 */
public class Agent {
    private RemoteAPIObjects._sim sim;
    private String fuel_id;
    private Long agentHandle;
    private Long targetHandle;
    
    private List<Float> pos;
    private List<Float> ori;
    private float fuel;    
    
    private boolean initialized = false;
    
    public Agent(RemoteAPIObjects._sim sim_, float x, float y){
        sim = sim_;  
        pos = Arrays.asList(new Float[]{x, y, (float) 0.16});
        ori = Arrays.asList(new Float[]{(float) 0, (float) 0, (float) 0});
        
    }
    
    public void init(){
        try {
            agentHandle = sim.loadModel(System.getProperty("user.dir") + "/workspace/agent_model.ttm");
            
            sim.setObjectPosition(agentHandle, sim.handle_world, pos);
            sim.setObjectOrientation(agentHandle, sim.handle_world, ori);
            
            targetHandle = sim.createDummy(0.01);
            sim.setObjectPosition(targetHandle, sim.handle_world, pos);
            
            Long agentScriptHandle = sim.getScript(sim.scripttype_childscript, agentHandle);
            
            String agentScriptCode = String.format(Constants.BASE_SCRIPT, 
                                                    sim.getObjectUid(agentHandle), 
                                                    sim.getObjectUid(targetHandle), 
                                                    sim.getObjectUid(agentHandle));
            sim.setScriptStringParam(agentScriptHandle, sim.scriptstringparam_text, agentScriptCode);
            sim.initScript(agentScriptHandle);
            
            fuel_id = "fuel_" + sim.getObjectUid(agentHandle).toString();
            
        } catch (CborException ex) {
            System.out.println("Err");
        }
    }
    
    public void updateState(){
        try {
            fuel = sim.getFloatSignal(fuel_id);
            
            pos = sim.getObjectPosition(agentHandle, sim.handle_world);
            ori = sim.getObjectOrientation(agentHandle, sim.handle_world);
            
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void run(){
        if (!initialized){
            this.init();
            initialized = true;
        }
    }
    
    public float getFuel(){
        return fuel;
    }
    
    public float getPitch(){
        return ori.get(2);
    }
}
