/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Demo;

/**
 *
 * @author bruno
 */

import WS3DCoppelia.WS3DCoppelia;
import WS3DCoppelia.model.Agent;
import WS3DCoppelia.model.Thing;
import WS3DCoppelia.util.Constants;
import co.nstant.in.cbor.CborException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Environment {
    
    public WS3DCoppelia world;
    public Agent creature;
    
    public Environment(){
        world = new WS3DCoppelia();
        creature = world.createAgent(1,1);
        world.createThing(Constants.ThingsType.PFOOD, 1,-1);
        world.createThing(Constants.ThingsType.PFOOD, (float) 1.4,-1);
        world.createThing(Constants.ThingsType.PFOOD, (float) 0.6,-1);
        try {
            world.startSimulation();
        } catch (IOException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CborException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
