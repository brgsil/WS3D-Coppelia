/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.model;

import WS3DCoppelia.util.Constants;
import static WS3DCoppelia.util.Constants.RED_COLOR;
import static WS3DCoppelia.util.Constants.THING_SIZE;
import WS3DCoppelia.util.Constants.ThingsType;
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
public class Thing {
    private RemoteAPIObjects._sim sim;
    private Long thingHandle;
    private List<Float> pos;
    public boolean removed = false;
    
    private ThingsType category;
    
    private boolean initialized = false;
    
    public Thing(RemoteAPIObjects._sim sim_, ThingsType category_, float x, float y){
        sim = sim_;
        pos = Arrays.asList(new Float[]{x, y, (float) 0.05});
        category = category_;
        
    }
    
    public void init(){
        
        try {
            thingHandle = sim.createPrimitiveShape(category.shape(), THING_SIZE, 0);
            
            sim.setObjectPosition(thingHandle, RemoteAPIObjects._sim.handle_world, pos);
            sim.setObjectColor(thingHandle,
                    0,
                    RemoteAPIObjects._sim.colorcomponent_ambient_diffuse,
                    category.color());
            //Long applesParentHandle = sim.getObject("/apples");
            //sim.setObjectParent(thingHandle, applesParentHandle, true);
            
        } catch (CborException ex) {
            Logger.getLogger(Thing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void run(){
        if (!initialized){
            this.init();
            initialized = true;
        }
    }
    
    public List<Float> getPos(){
        return pos;
    }
    
    public ThingsType thingType(){
        return category;
    }
    
    public boolean isFood() { return category == ThingsType.PFOOD || category == ThingsType.NPFOOD; }
    
    public float energy() { return category.energy(); }
    
    public void remove() throws CborException{
        sim.removeObjects(Arrays.asList(new Long[]{thingHandle}));
        removed = true;
    }

    public List<Float> getRelativePos(Long sourceHandle) throws CborException {
        List<Float> relPos = sim.getObjectPose(thingHandle, sourceHandle);
        return relPos;
    }
    
    public boolean isInOccupancyArea(float x, float y){
        return Math.hypot( Math.abs(pos.get(0) - x),
                Math.abs(pos.get(1) - y))
                <= Constants.THING_OCCUPANCY_RADIUS;
    }
    
    public boolean isInitialized(){
        return initialized;
    }
    
}
