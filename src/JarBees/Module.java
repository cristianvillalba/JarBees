/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JarBees;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author Cristian.Villalba
 */
public class Module{
    private Material mat_core;
    private ModuleControl modulecontrol;
    private Node centernode;
    private float width = 1.0f;
    private float height = 1.0f;
    
    public Module(AssetManager asset, Node root, String name, String th, float level, float azim, float alt, boolean totexture)
    {
        //mat_core = new Material(asset, "Common/MatDefs/Misc/Particle.j3md");
        //mat_core.setTexture("Texture", asset.loadTexture("Textures/Smoke.png"));
        //mat_core.setTexture("GlowMap", asset.loadTexture("Textures/GlowMapOrange.png"));
        
        mat_core = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
        //ColorRGBA localcolor = ColorRGBA.Orange.clone();
        ColorRGBA localcolor = GenerateColor(th);
        mat_core.setColor("Color", localcolor);
        mat_core.setTexture("ColorMap", asset.loadTexture("Textures/Smoke.png"));
        
        mat_core.getAdditionalRenderState().setDepthTest(false);
        mat_core.getAdditionalRenderState().setDepthWrite(false);
        mat_core.setTransparent(true);
        mat_core.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.AlphaAdditive); 
      
        Quad quadmesh = new Quad(width, height);
        Geometry quadgeo = new Geometry("quadcore", quadmesh);
        
        quadgeo.setMaterial(mat_core);
        quadgeo.setName(name);
        quadgeo.setUserData("type", "module");
        quadgeo.setUserData("touched", false);
        quadgeo.setLocalTranslation(-width/2, -height/2, 0f);
        quadgeo.setQueueBucket(RenderQueue.Bucket.Translucent);
        //Vector3f normal = Vector3f.UNIT_X.clone();
        Matrix3f rot1 = new Matrix3f();
        Matrix3f rot2 = new Matrix3f();
        float azimuth = azim;
        float altitude = alt;
        
        if (level != 0.0f){
            azimuth += FastMath.nextRandomFloat()*0.2f - 0.1f;
            altitude += FastMath.nextRandomFloat()*0.2f - 0.1f;
        }
        
        rot1.fromAngleAxis(azimuth, Vector3f.UNIT_Y);
        rot2.fromAngleAxis(altitude, Vector3f.UNIT_Z);
        
        Vector3f normal = rot1.mult(rot2).mult(Vector3f.UNIT_Y.clone());
        
        centernode = new Node();
        centernode.setName(name);
        centernode.setUserData("type", "node");
        centernode.setUserData("touched", false);
        
        if (!totexture){
            centernode.setQueueBucket(RenderQueue.Bucket.Transparent);
        }
        
        modulecontrol = new ModuleControl(normal, level, localcolor, totexture);
        centernode.addControl(modulecontrol);
        
        centernode.attachChild(quadgeo);
        
        BillboardControl billcontrol = new BillboardControl();
        centernode.addControl(billcontrol);
        
        root.attachChild(centernode);
    }
    
    public static ColorRGBA GenerateColor(String th)
    {
        float hue = 0.0f;
        switch(th)
        {
            case "0":
            {
                hue = 0.3f;
                break;
            }
            case "1":
            {
                hue = 0.35f;
                break;
            }
            case "2":
            {
                hue = 0.4f;
                break;
            }
            case "3":
            {
                hue = 0.45f;
                break;
            }
            default:
            {
                hue = 0.3f;
                break;
            }
        }
        float[] color = new float[]{hue,1.0f,0.6f};
        int[] colorfinal = new int[3];
        ColorHSL.hsl2rgb(color,colorfinal);
        
        color[0] = colorfinal[0]/256.0f;
        color[1] = colorfinal[1]/256.0f;
        color[2] = colorfinal[2]/256.0f;
        return new ColorRGBA(color[0],color[1],color[2],1.0f); 
    }
    
    public void Pulsate(String th)
    {
        modulecontrol.MakePulse(th);
    }
    
    public Node GetMainNode()
    {
        return centernode;
    }
}
