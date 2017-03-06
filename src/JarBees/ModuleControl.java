/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JarBees;

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
    private float vel = 1.0f;
    private float finalvel = 0f;
    private float timetoaccel = 0f;
    private float maxtimetoaccel = 0.5f;
    private float initialradius = 5f;
    private float size = 1.0f;
    private float sizevariance = 0.001f;
    private float sizepulsevariance = 1.1f;
    private float thread;
    
    public ModuleControl(Vector3f dir, String th)
    {
        direction = dir;
        originaldirection = direction.clone();
        
        float threadnumber = Float.parseFloat(th.replace("Id", ""));
        
        thread = (threadnumber + 1.0f)*sizepulsevariance;
    }
    
    public void MakePulse(String th)
    {
        pulse += 0.30f;
        size *= 1.2f;
        
        float threadnumber = Float.parseFloat(th.replace("Id", ""));
        
        thread = (threadnumber + 1.0f)*sizepulsevariance;
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
        
        ((Node)spatial).getChild(0).setLocalScale(size);
        spatial.setLocalTranslation(direction);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
