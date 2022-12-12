/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.model;

import static WS3DCoppelia.util.Constants.RED_COLOR;
import static WS3DCoppelia.util.Constants.THING_SIZE;
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
    
    private boolean initialized = false;
    
    public Thing(RemoteAPIObjects._sim sim_, float x, float y){
        sim = sim_;
        pos = Arrays.asList(new Float[]{x, y, (float) 0.05});
    }
    
    public void init(){
        try {
            thingHandle = sim.createPrimitiveShape(sim.primitiveshape_spheroid, THING_SIZE, 0);
            
            sim.setObjectPosition(thingHandle, sim.handle_world, pos);
            sim.setObjectColor(thingHandle,
                    0,
                    sim.colorcomponent_ambient_diffuse,
                    RED_COLOR);
            Long applesParentHandle = sim.getObject("/apples");
            sim.setObjectParent(thingHandle, applesParentHandle, true);
            
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
    
}
