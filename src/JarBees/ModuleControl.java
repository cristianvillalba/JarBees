/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JarBees;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Cristian.Villalba
 */
public class ModuleControl extends AbstractControl{
    private Vector3f direction;
    private Vector3f originaldirection;
    private float pulse = 0.0f;
    private float maxpulse = 2.0f;
    private float vel = 1.0f;
    private float finalvel = 0f;
    private float timetoaccel = 0f;
    private float maxtimetoaccel = 0.5f;
    private float initialradius = 5f;
    private float size = 1.0f;
    private float maxsize = 2.0f;
    private float sizevariance = 0.001f;
    private float sizepulsevariance = 1.1f;
    private float level;
    private ColorRGBA localcolor;
    
    public ModuleControl(Vector3f dir, float l, ColorRGBA col)
    {
        direction = dir;
        originaldirection = direction.clone();
        
        //Add some static noise at the beginning
        //float noise = (float)SimplexNoise.noise(originaldirection.x, originaldirection.y, originaldirection.z);
        //originaldirection.multLocal(1.0f + noise*0.2f);
            
        level = (l+ 1.5f)*2.5f;
        localcolor = col;
    }
    
    public void MakePulse(String th)
    {
        pulse += 0.30f;
        size *= 1.2f;
        
        if (pulse > maxpulse)
        {
            pulse = maxpulse;
        }
        
        if (size > maxsize)
        {
            size = maxsize;
        }
        
        float threadnumber = Float.parseFloat(th.replace("Id", ""));
        
        level = 0.1f * (threadnumber + 1.0f);
    }
    
    @Override
    protected void controlUpdate(float tpf) {

        if (size > 1.0f)
        {
            size -= sizevariance;
        }
        else
        {
            size = 1.0f;
        }
        
        
        //Heart beat that I did with try/error
        //goes from 4 to 0 with 2 pikes
        //pulse = 4*FastMath.exp(-5*((Main.time + originaldirection.y*0.05f)% 1.5f)) + 3*FastMath.exp(-5*((Main.time-0.4f + originaldirection.y *0.05f) % 1.5f)); //heart beat
        
        //Heart beat that I did with try/error
        //goes from 4 to 0 with 2 pikes
        pulse = 0.01f*FastMath.exp(-4.5f*FastMath.cos((Main.time + originaldirection.y*0.25f)*3.0f)) 
                + 0.01f*FastMath.exp(-4.0f*FastMath.cos((Main.time + originaldirection.y*0.25f)*3.0f -1.5f));

        //add some noise with a time offset to make it move
        float noise = (float)SimplexNoise.noise(originaldirection.x + Main.time*0.2f, originaldirection.y, originaldirection.z);
        direction.set(originaldirection.mult(pulse + initialradius + level + noise*2f));
        
        spatial.setLocalTranslation(direction);
        spatial.setLocalScale(size);
        
        localcolor.a = 0.2f + (pulse/0.95f)*0.8f;
        
        try{
            String geoname = ((Node)this.spatial).getChild(0).getName();
            
            if ((Main.time % 6f) >= 5.75f)
            {
                if (Main.modulelabellist.contains(geoname)){
                    Main.modulelabellist.remove(geoname);
                }
            }
        }
        catch(Exception n){
            
        }
        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
