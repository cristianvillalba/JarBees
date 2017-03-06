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
import com.jme3.scene.shape.Line;

/**
 *
 * @author Cristian.Villalba
 */
public class LinkedLineControl extends AbstractControl{
    private Module origin;
    private Module destination;
    private Line source;
  
    public LinkedLineControl(Module m1, Module m2, Line s)
    {
        origin = m1;
        destination = m2;
        source = s;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
       source.updatePoints(origin.GetMainNode().getWorldTranslation().clone(), destination.GetMainNode().getWorldTranslation().clone());
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
