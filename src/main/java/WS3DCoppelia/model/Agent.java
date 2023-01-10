/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.model;

import WS3DCoppelia.util.Constants;
import co.nstant.in.cbor.CborException;
import com.coppeliarobotics.remoteapi.zmq.RemoteAPIObjects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private List<Thing> thingsInVision =  Collections.synchronizedList(new ArrayList());;
    
    private boolean initialized = false;
    private double fovAngle = 0.5;
    private int maxFov = 100;
    private boolean rotate = false;
    
    private Map<String, Object> commandQueue = Collections.synchronizedMap(new LinkedHashMap());
    
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
    
    public void updateState(List<Thing> inWorldThings){
        try {
            fuel = sim.getFloatSignal(fuel_id);
            
            pos = sim.getObjectPosition(agentHandle, sim.handle_world);
            ori = sim.getObjectOrientation(agentHandle, sim.handle_world);
            
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        List<Thing> thingsSeen = new ArrayList();
        for(Thing thing : inWorldThings){
            if (!thing.removed){
                List<Float> posThing = thing.getPos();
                float x = posThing.get(0) - pos.get(0);
                float y = posThing.get(1) - pos.get(1);

                if (x < maxFov && (-fovAngle*x < y && y < fovAngle*x)){
                    thingsSeen.add(thing);
                }
            }
        }
        
        synchronized (thingsInVision){
            thingsInVision.clear();
            thingsInVision.addAll(thingsSeen);
        }
        
        if (rotate)
            execRotate();
        
    }
    
    public void execCommands(){
        synchronized (commandQueue){
            for(String command : commandQueue.keySet()){
                switch (command){
                    case "move":
                        this.execMove((List<Float>) commandQueue.get(command));
                        break;
                    case "eat":
                        this.execEatIt((Thing) commandQueue.get(command));
                        break;
                    case "rotate":
                        this.execRotate();
                        rotate = true;
                        break;
                    default:
                }
            }
            commandQueue.clear();
        }
    }
    
    public void run(List<Thing> inWorldThings){
        if (!initialized){
            this.init();
            initialized = true;
        }
        
        this.updateState(inWorldThings);
        this.execCommands();
    }
    
    public void moveto(float x, float y){
        commandQueue.put("move", Arrays.asList(new Float[]{x, y}));
    }
    
    public void eatIt(Thing thing){
        if (thing.isFood())
            commandQueue.put("eat", thing);
    }
    
    public void rotate(){
        commandQueue.put("rotate", "");
    }
    
    private void execMove(List<Float> params){
        try {
            float goalX = params.get(0);
            float goalY = params.get(1);
            double goalPitch = Math.atan2(goalY - pos.get(1), goalX - pos.get(0));
            
            List<Float> targetOri = new ArrayList<>(ori);
            targetOri.set(2, (float) goalPitch);
            
            sim.setObjectOrientation(targetHandle, sim.handle_world, targetOri);
            sim.setObjectOrientation(agentHandle, sim.handle_world, targetOri);
            List<Float> targetPos = Arrays.asList(new Float[]{goalX, goalY, (float) 0});
            sim.setObjectPosition(targetHandle, sim.handle_world, targetPos);
            rotate = false;
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void execEatIt(Thing food){
        try {
            food.remove();
            float fuel_ = sim.getFloatSignal(fuel_id);
            float new_fuel = fuel_ + food.energy() > 1000 ? 1000 : fuel_ + food.energy();
            sim.setFloatSignal(fuel_id, new_fuel);
            
            rotate = false;
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void execRotate(){
        try {
            List<Float> targetPos = Arrays.asList(new Float[]{(float) 0, (float) 0, (float) 0});
        
            sim.setObjectPosition(targetHandle, agentHandle, targetPos);
        
            List<Float> euler = sim.getObjectOrientation(targetHandle, agentHandle);
            euler.set(2, (float) 3);
            sim.setObjectOrientation(targetHandle, agentHandle, euler);
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public float getFuel(){
        return fuel;
    }
    
    public float getPitch(){
        return ori.get(2);
    }
    
    public List<Float> getPosition(){
        return pos;
    }
    
    public List<Thing> getThingsInVision(){
        return thingsInVision;
    }
}
