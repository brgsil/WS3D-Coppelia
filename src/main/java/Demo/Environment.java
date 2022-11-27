/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Demo;

/**
 *
 * @author bruno
 */

import co.nstant.in.cbor.CborException;
import com.coppeliarobotics.remoteapi.zmq.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Environment {
    
    private RemoteAPIClient client;
    private RemoteAPIObjects._sim sim;
    private Long agentHandle;
    private Long applesTreeHandle;
    private Long targetHandle;
    
    private List<Float> agentPos;
    private float agentPitch;
    private List<Long> applesVision = new ArrayList();
    private float agentFuel;
    private Map<Long, List<Float>> applesPos = new HashMap();
    private boolean rotateAgent = false;
    private boolean stopAgent = false;
    private float goalX = 0;
    private float goalY = 0;
    private long appleToEat = -1;
    
    public void startSimulation() throws java.io.IOException, CborException{
        client = new RemoteAPIClient();
        sim = client.getObject().sim();

        client.setStepping(false);
        sim.startSimulation();
        
        agentHandle = sim.getObject("/agent2");
        applesTreeHandle = sim.getObject("/apples");
        targetHandle = sim.getObject("/target");
        
        float startTime = sim.getSimulationTime();
        while(sim.getSimulationTime() - startTime < 1){}
        
        Timer t = new Timer();
        Environment.mainTimerTask tt = new Environment.mainTimerTask(this);
        t.scheduleAtFixedRate(tt, 100, 200);
    }
    
    public void stopSimulation() throws CborException{
        sim.stopSimulation();
    }
    
    public List<Float> getAgentPosition(){
        return this.agentPos;
    }
    
    public float getAgentPitch() {
        return this.agentPitch;
    }
    
    public List<Long> getApplesInVision() {
        return this.applesVision;
    }
    
    public void moveTo(float x, float y) {
        this.stopAgent = false;
        this.rotateAgent = false;
        
        this.goalX = x;
        this.goalY = y;
    }
    
    public void rotate() {
        this.stopAgent = false;
        this.rotateAgent = true;
    }
    
    public void stopAgent() {
        this.stopAgent = true;
    }
    
    public void eatApple(Long appleHandle) {
        this.appleToEat = appleHandle;
    }
    
    public Float getFuel() {
        return this.agentFuel;
    }
    
    public void waitSim(float waitTime) throws CborException{
        float startTime = sim.getSimulationTime();
        while(sim.getSimulationTime() - startTime < waitTime){}
    }
    
    public List<Float> getApplePosition(Long appleHandle) {
        return this.applesPos.get(appleHandle);
    }
    
    public void updateState(){
        try {
            this.agentPos = sim.getObjectPosition(agentHandle, sim.handle_world);

            // Euler angles (alpha, beta and gamma)
            List<Float> rotation = sim.getObjectOrientation(agentHandle, sim.handle_world);
            this.agentPitch = rotation.get(2);

            int i = 0;
            Long appleHandle = sim.getObjectChild(applesTreeHandle, i);

            List<Long> applesSeen = new ArrayList();
            while (appleHandle != -1) {
                i += 1;
                List<Float> posRelApple = sim.getObjectPosition(appleHandle, agentHandle);
                float x = posRelApple.get(0);
                float y = posRelApple.get(1);
                float z = posRelApple.get(2);
                if (x < 100 && (-0.5*x < y && y < 0.5*x) && z > -1){
                    applesSeen.add(appleHandle);
                    if (!this.applesPos.containsKey(appleHandle)){
                        List<Float> posApple = sim.getObjectPosition(appleHandle, sim.handle_world);
                        this.applesPos.put(appleHandle, posApple);
                    }
                }

                appleHandle = sim.getObjectChild(applesTreeHandle, i);
            }
            
            this.applesVision = applesSeen;
            
            if (stopAgent){
                List<Float> targetPos = Arrays.asList(new Float[]{(float) 0, (float) 0, (float) 0});
                sim.setObjectPosition(targetHandle, agentHandle, targetPos);
                sim.setObjectOrientation(targetHandle, agentHandle, targetPos);
            } else if (rotateAgent){
                List<Float> targetPos = Arrays.asList(new Float[]{(float) 0, (float) 0, (float) 0});
                sim.setObjectPosition(targetHandle, agentHandle, targetPos);

                List<Float> euler = sim.getObjectOrientation(targetHandle, agentHandle);
                euler.set(2, (float) 3);
                sim.setObjectOrientation(targetHandle, agentHandle, euler);
            } else {
                List<Float> pos = sim.getObjectPosition(agentHandle, sim.handle_world);
                double goalPitch = Math.atan2(this.goalY - pos.get(1), this.goalX - pos.get(0));

                List<Float> euler = sim.getObjectOrientation(agentHandle, sim.handle_world);
                euler.set(2, (float) goalPitch);

                sim.setObjectOrientation(targetHandle, sim.handle_world, euler);
                List<Float> targetPos = Arrays.asList(new Float[]{this.goalX, this.goalY, (float) 0});
                sim.setObjectPosition(targetHandle, sim.handle_world, targetPos);
            }
            
            if (appleToEat != -1){
                List<Float> pos = sim.getObjectPosition(appleToEat, sim.handle_world);
                pos.set(2, (float) -1);
                sim.setObjectPosition(appleToEat, sim.handle_world, pos);

                Float fuel = sim.getFloatSignal("fuel");
                fuel = fuel < 800 ? fuel + 200 : 1000;
                sim.setFloatSignal("fuel", fuel );
                
                appleToEat = -1;
            }            
        } catch (CborException ex) {}
    }
    
    class mainTimerTask extends TimerTask {

        Environment wov;
        boolean enabled = true;

        public mainTimerTask(Environment wovi) {
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
}
