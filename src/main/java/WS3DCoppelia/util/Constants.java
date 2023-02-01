/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.util;

import com.coppeliarobotics.remoteapi.zmq.RemoteAPIObjects;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author bruno
 */
public class Constants {

    public static final List<Float> THING_SIZE = Arrays.asList(new Float[]{(float) 0.1, (float) 0.1, (float) 0.1});
    public static final float BRICK_HEIGTH = (float) 0.2;
    
    public static final List<Float> RED_COLOR = Arrays.asList(new Float[]{(float) 0.95, (float) 0.25, (float) 0.25});
    public static final List<Float> GREEN_COLOR = Arrays.asList(new Float[]{(float) 0.25, (float) 0.95, (float) 0.25});
    public static final List<Float> BLUE_COLOR = Arrays.asList(new Float[]{(float) 0.25, (float) 0.25, (float) 0.95});
    public static final List<Float> YELLOW_COLOR = Arrays.asList(new Float[]{(float) 0.95, (float) 0.95, (float) 0.25});
    public static final List<Float> MAGENTA_COLOR = Arrays.asList(new Float[]{(float) 0.95, (float) 0.25, (float) 0.95});
    public static final List<Float> WHITE_COLOR = Arrays.asList(new Float[]{(float) 0.95, (float) 0.95, (float) 0.95});
    public static final List<Float> ORANGE_COLOR = Arrays.asList(new Float[]{(float) 0.95, (float) 0.65, (float) 0.25});
    
    public static String BASE_SCRIPT = "#python\n"
            + "\n"
            + "from math import sqrt\n"
            + "\n"
            + "lin_vel = 0.02 # m/s\n"
            + "ang_vel = 0.02  # rad/s\n"
            + "agent_uid = %d\n"
            + "target_uid = %d\n"
            + "fuel_id = 'fuel_%d'\n"
            + "\n"
            + "def sysCall_init():\n"
            + "    # do some initialization here\n"
            + "    sim.setFloatSignal(fuel_id, 1000)\n"
            + "\n"
            + "def sysCall_actuation():\n"
            + "    # put your actuation code here\n"
            + "    agent_handle = sim.getObjectFromUid(agent_uid)\n"
            + "    target_handle = sim.getObjectFromUid(target_uid)\n"
            + "    \n"
            + "    agent_err_pos = sim.getObjectPosition(agent_handle, target_handle)#[:-1] # only x, y position\n"
            + "    agent_err_ori = sim.getObjectOrientation(agent_handle, target_handle)\n"
            + "    \n"
            + "    fuel = sim.getFloatSignal(fuel_id)\n"
            + "    \n"
            + "    if fuel > 0:\n"
            + "        if abs(agent_err_ori[-1]) > ang_vel: # only angle in relation to z-axis\n"
            + "            ang_dir = 1 - 2 * (agent_err_ori[-1] >= 0)\n"
            + "            agent_err_ori[-1] = agent_err_ori[-1] + ang_dir * ang_vel\n"
            + "            sim.setObjectOrientation(agent_handle, target_handle, agent_err_ori)\n"
            + "        else:\n"
            + "            dist = sqrt(agent_err_pos[0]**2 + agent_err_pos[1]**2)\n"
            + "            if dist > lin_vel:\n"
            + "                dir_vec = [lin_vel * i / dist for i in agent_err_pos[:-1]]\n"
            + "                agent_err_pos[0] = agent_err_pos[0] - dir_vec[0]\n"
            + "                agent_err_pos[1] = agent_err_pos[1] - dir_vec[1]\n"
            + "                sim.setObjectPosition(agent_handle, target_handle, agent_err_pos)\n"
            + "            \n"
            + "    \n"
            + "    sim.setFloatSignal(fuel_id, fuel - 0.1)\n"
            + "\n"
            + "def sysCall_sensing():\n"
            + "    # put your sensing code here\n"
            + "    pass\n"
            + "\n"
            + "def sysCall_cleanup():\n"
            + "    # do some clean-up here\n"
            + "    pass\n"
            + "\n";
   
    public static double THING_OCCUPANCY_RADIUS = 0.15;
    public static double AGENT_OCCUPANCY_RADIUS = 0.2;
    
    /**
     * Resources Generator package constants
     */
    public static final int TIMEFRAME = 3; //default in minutes
    ////////Poisson distribution
    //the average rate of generation of each kind of crystal:
    public static final double redLAMBDA = 1;
    public static final double greenLAMBDA = 0.4;
    public static final double blueLAMBDA = 0.5;
    public static final double yellowLAMBDA = 0.7;
    public static final double magentaLAMBDA = 0.3;
    public static final double whiteLAMBDA = 0.2;
    public static final double pFoodLAMBDA = 1;
    public static final double npFoodLAMBDA = 0.7;
    public static double SECURITY = 30; //empiric
    
    //Leaflet
    public static int LEAFLET_NUMBER_OF_ITEMS = 3;
    public static int MAX_NUMBER_ITEMS_PER_COLOR = 3;
    public static int NUM_LEAFLET_PER_AGENTS = 3;

    public interface ThingsType {
        public int shape();
        public List<Float> color();
        public String typeName();
    }
    public enum FoodTypes implements ThingsType{
        PFOOD(RemoteAPIObjects._sim.primitiveshape_spheroid, RED_COLOR, 300, "P_Food"),
        NPFOOD(RemoteAPIObjects._sim.primitiveshape_spheroid, ORANGE_COLOR, 150, "NP_Food");
        
        private final int shape;
        private final List<Float> color;
        private final String type_name;
        private final float energy;
        
        FoodTypes(int shape, List<Float> color, float energy, String name){
            this.shape = shape;
            this.color = color;
            this.type_name = name;
            this.energy = energy;
        }
        
        @Override
        public int shape() { return shape; }
        @Override
        public List<Float> color() { return color; }
        @Override
        public String typeName() { return type_name; }
        public float energy() { return energy; }
    }
    
    public enum JewelTypes implements ThingsType{
        RED_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, RED_COLOR, "Red_Jewel"),
        GREEN_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, GREEN_COLOR, "Green_Jewel"),
        BLUE_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, BLUE_COLOR, "Blue_Jewel"),
        YELLOW_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, YELLOW_COLOR, "Yellow_Jewel"),
        MAGENTA_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, MAGENTA_COLOR, "Magenta_Jewel"),
        WHITE_JEWEL(RemoteAPIObjects._sim.primitiveshape_cone, WHITE_COLOR, "White_Jewel");
        
        private final int shape;
        private final List<Float> color;
        private final String type_name;
        
        JewelTypes(int shape, List<Float> color, String name){
            this.shape = shape;
            this.color = color;
            this.type_name = name;
        }
        
        @Override
        public int shape() { return shape; }
        @Override
        public List<Float> color() { return color; }
        @Override
        public String typeName() { return type_name; }
    }
    
    public enum BrickTypes implements ThingsType{
        RED_BRICK(RemoteAPIObjects._sim.primitiveshape_cuboid, RED_COLOR, "Red_Brick");
        
        private final int shape;
        private final List<Float> color;
        private final String type_name;
        
        BrickTypes(int shape, List<Float> color, String name){
            this.shape = shape;
            this.color = color;
            this.type_name = name;
        }
        
        @Override
        public int shape() { return shape; }
        @Override
        public List<Float> color() { return color; }
        @Override
        public String typeName() { return type_name; }
    }
    
    public static int getPaymentColor(JewelTypes type){
        switch(type) {
            case RED_JEWEL:
                return 10;
            case GREEN_JEWEL:
                return 8;
            case BLUE_JEWEL:
                return 6;
            case MAGENTA_JEWEL:
                return 4;
            case YELLOW_JEWEL:
                return 2;
            case WHITE_JEWEL:
                return 1;
            default:
                return 0;
        }
    }
}
