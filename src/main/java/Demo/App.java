/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Demo;

import WS3DCoppelia.WS3DCoppelia;
import WS3DCoppelia.model.Agent;
import WS3DCoppelia.model.Thing;
import WS3DCoppelia.util.Constants.ThingsType;
import br.unicamp.cst.util.viewer.MindViewer;
import co.nstant.in.cbor.CborException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    public static void main(String[] args) throws CborException, IOException {
        Environment env = new Environment();
        //env.startSimulation();
        AgentMind a = new AgentMind(env);
//        WS3DCoppelia world = new WS3DCoppelia();
//        Agent agt = world.createAgent(1,1);
//        Thing th = world.createThing(ThingsType.PFOOD, 1,-1);
//        world.startSimulation();
//        agt.rotate();
//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        agt.moveTo(1, -1);
//        try {
//            Thread.sleep(15000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.out.println("EAT");
//        System.out.println(agt.getFuel());
//        agt.eatIt(th);
//        
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        System.out.println(agt.getFuel());
    }
}
