/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.model;

import co.nstant.in.cbor.CborException;
import com.coppeliarobotics.remoteapi.zmq.RemoteAPIObjects;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author bruno
 */
public class Agent {
    private RemoteAPIObjects._sim sim;
    private Long agentHandle;
    private Long targetHandle;
    private List<Float> pos;
    private List<Float> ori;
    
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
            
            Long agentScript = sim.getScript(sim.scripttype_childscript, agentHandle);
            sim.callScriptFunction("set_uid", agentScript, sim.getObjectUid(agentHandle), sim.getObjectUid(targetHandle));
            
        } catch (CborException ex) {
            System.out.println("Err");
        }
    }
    
    public void run(){
        if (!initialized){
            this.init();
            initialized = true;
        }
    }
}
