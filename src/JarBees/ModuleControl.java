/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JarBees;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
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
    private float thread;
    private ColorRGBA localcolor;
    
    public ModuleControl(Vector3f dir, String th, ColorRGBA col)
    {
        direction = dir;
        originaldirection = direction.clone();
        
        float threadnumber = Float.parseFloat(th.replace("Id", ""));
        
        thread = 0.2f * (threadnumber + 1.0f);
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
        
        thread = 0.1f * (threadnumber + 1.0f);
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
        
        pulse -= vel * tpf;
        
        if (pulse < 0)
        {
            pulse = 0f;
        }
        
        direction.set(originaldirection.mult(pulse + initialradius + thread));
        
        spatial.setLocalTranslation(direction);
        spatial.setLocalScale(size);
        
        localcolor.a = 0.5f  + (size - 1.0f)*0.5f;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
