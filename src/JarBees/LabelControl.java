/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JarBees;

import com.jme3.font.BitmapText;
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
public class LabelControl extends AbstractControl{
    private float visibilitytime = 0f;
    private float visibilitymaxtime = 2f;
    private float interpolatedvalue;
    private ColorRGBA newcolor = ColorRGBA.White;
   
    @Override
    protected void controlUpdate(float tpf) {
        visibilitytime += tpf;
        
        
        interpolatedvalue = FastMath.interpolateLinear(visibilitytime/visibilitymaxtime, 1.0f, 0.0f);
        newcolor.a = interpolatedvalue;
        
        ((BitmapText)spatial).setColor(newcolor);
        
        if (visibilitytime > visibilitymaxtime)
        {
            spatial.removeFromParent();
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
