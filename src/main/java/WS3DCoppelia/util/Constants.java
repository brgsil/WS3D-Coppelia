/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WS3DCoppelia.util;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author bruno
 */
public class Constants {

    public static final List<Float> THING_SIZE = Arrays.asList(new Float[]{(float) 0.1, (float) 0.1, (float) 0.1});
    
    public static final List<Float> RED_COLOR = Arrays.asList(new Float[]{(float) 0.95, (float) 0.25, (float) 0.25});
    
    public static String BASE_SCRIPT = "#python\n"
            + "\n"
            + "from math import sqrt\n"
            + "\n"
            + "lin_vel = 0.01 # m/s\n"
            + "ang_vel = 0.05  # rad/s\n"
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

}
