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
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bruno
 */
public class Agent extends Identifiable {
    private RemoteAPIObjects._sim sim;
    private String fuel_id;
    private Long agentHandle;
    private Long targetHandle;
    private Long worldScript;
    
    private List<Float> pos;
    private List<Float> ori;
    private float fuel;    
    private List<Thing> thingsInVision =  Collections.synchronizedList(new ArrayList());
    private Bag bag = new Bag();
    private int score = 0;
    private Leaflet[] leaflets = new Leaflet[Constants.NUM_LEAFLET_PER_AGENTS];
    
    private boolean initialized = false;
    private double fovAngle = 0.5;
    private int maxFov = 100;
    private boolean rotate = false;
    private final float xLimit;
    private final float yLimit;
    
    private Map<String, Object> commandQueue = Collections.synchronizedMap(new LinkedHashMap());
    
    public Agent(RemoteAPIObjects._sim sim_, float x, float y, float width, float heigth){
        sim = sim_;  
        pos = Arrays.asList(new Float[]{x, y, (float) 0.16});
        ori = Arrays.asList(new Float[]{(float) 0, (float) 0, (float) 0});
        xLimit = width / 2;
        yLimit = heigth / 2;
        for (int i = 0; i < Constants.NUM_LEAFLET_PER_AGENTS; i++){
            leaflets[i] = new Leaflet();
        }
    }
    
    public void init(){
        try {
            agentHandle = sim.loadModel(System.getProperty("user.dir") + "/workspace/agent_model.ttm");
            
            targetHandle = (Long) sim.callScriptFunction("init_agent", worldScript, agentHandle, pos, ori, Constants.BASE_SCRIPT);
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
        synchronized (inWorldThings) {
            for(Thing thing : inWorldThings){
                if (thing.isInitialized()){
                    List<Float> posThing = thing.getRelativePos(pos);
                    float x = posThing.get(0);
                    float y = posThing.get(1);

                    float thingPitch = ((float) Math.atan2(x, y)) - this.getPitch();
                    if (x < maxFov && (-fovAngle < thingPitch && thingPitch < fovAngle)){
                        thingsSeen.add(thing);
                    }
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
                    case "sackIt":
                        this.execSackIt((Thing) commandQueue.get(command));
                        break;
                    case "deliver":
                        this.execDeliver((Integer) commandQueue.get(command));
                        break;
                    case "stop":
                        this.execStop();
                        break;
                    default:
                }
            }
            commandQueue.clear();
        }
    }
    
    public void run(List<Thing> inWorldThings, Long worldScript_){
        if (!initialized){
            worldScript = worldScript_;
            this.init();
            initialized = true;
        }
        
        this.updateState(inWorldThings);
        this.execCommands();
    }
    
    public void moveTo(float x, float y){
        commandQueue.put("move", Arrays.asList(new Float[]{x, y}));
    }
    
    public void eatIt(Thing thing){
        if (thing.isFood())
            commandQueue.put("eat", thing);
    }
    
    public void rotate(){
        commandQueue.put("rotate", "");
    }
    
    public void stop(){
        commandQueue.put("stop", "");
    }
    
    public void sackIt(Thing thing){
        commandQueue.put("stop", thing);
    }
    
    public void deliver(int leafletId){
        commandQueue.put("deliver", leafletId);
    }
    
    private void execMove(List<Float> params){
        try {
            float goalX = params.get(0);
            if (Math.abs(goalX) > xLimit)
                goalX = Math.copySign(xLimit, goalX);
            float goalY = params.get(1);
            if (Math.abs(goalY) > yLimit)
                goalY = Math.copySign(yLimit, goalY);
            double goalPitch = Math.atan2(goalY - pos.get(1), goalX - pos.get(0));
            
            List<Float> targetPos = Arrays.asList(new Float[]{goalX, goalY, (float) 0});
            List<Float> targetOri = new ArrayList<>(ori);
            targetOri.set(2, (float) goalPitch);
            
            sim.callScriptFunction("move_agent", worldScript, agentHandle, targetHandle, targetPos, targetOri);
            
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
            sim.callScriptFunction("rotate_agent", worldScript, agentHandle, targetHandle);
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    private void execStop(){
        try {
            sim.callScriptFunction("stop_agent", worldScript, agentHandle, targetHandle);
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    private void execSackIt(Thing thing){
        try {
            thing.remove();
            bag.insertItem(thing.thingType(), 1);
            for (int i = 0; i < Constants.NUM_LEAFLET_PER_AGENTS; i++){
                leaflets[i].updateProgress(bag);
            }
        } catch (CborException ex) {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void execDeliver(int leafletId){
        boolean deliverable = false;

        int pos = 0;
        for (int i = 0; i < Constants.NUM_LEAFLET_PER_AGENTS; i++){
            if(leaflets[i].checkId(leafletId) && leaflets[i].isCompleted()){
                pos = i;
                deliverable = true;
            }
        }

        if (deliverable){
            score += leaflets[pos].getPayment();
            leaflets[pos].setDelivered(true);
            for(Entry<Constants.JewelTypes, Integer> requirement : leaflets[pos].getRequirements().entrySet()){
                bag.removeItem(requirement.getKey(), requirement.getValue());
            }
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

    public boolean isInOccupancyArea(float x, float y) {
        return Math.hypot( Math.abs(pos.get(0) - x),
                Math.abs(pos.get(1) - y))
                <= Constants.AGENT_OCCUPANCY_RADIUS;
    }
    
    public Bag getBag(){
        return bag;
    }
    
    public Leaflet[] getLeaflets(){
        return leaflets;
    }
    
    public void generateNewLeaflets(){
        for (int i = 0; i < Constants.NUM_LEAFLET_PER_AGENTS; i++){
            leaflets[i] = new Leaflet();
            leaflets[i].updateProgress(bag);
        }
    }
}
