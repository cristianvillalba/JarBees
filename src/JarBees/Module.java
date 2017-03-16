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
    
    public Module(AssetManager asset, Node root, String name, String th)
    {
        //mat_core = new Material(asset, "Common/MatDefs/Misc/Particle.j3md");
        //mat_core.setTexture("Texture", asset.loadTexture("Textures/Smoke.png"));
        //mat_core.setTexture("GlowMap", asset.loadTexture("Textures/GlowMapOrange.png"));
        
        mat_core = new Material(asset, "Common/MatDefs/Misc/Unshaded.j3md");
        ColorRGBA localcolor = ColorRGBA.Orange.clone();
        mat_core.setColor("Color", localcolor);
        mat_core.setTexture("ColorMap", asset.loadTexture("Textures/Smoke.png"));
        mat_core.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
             
        Quad quadmesh = new Quad(width, height);
        Geometry quadgeo = new Geometry("quadcore", quadmesh);
        
        quadgeo.setMaterial(mat_core);
        quadgeo.setName(name);
        quadgeo.setUserData("type", "module");
        quadgeo.setLocalTranslation(-width/2, -height/2, 0f);
        quadgeo.setQueueBucket(RenderQueue.Bucket.Transparent);
           
        Vector3f normal = new Vector3f();
        normal.set(FastMath.nextRandomFloat() - 0.5f, FastMath.nextRandomFloat() - 0.5f, FastMath.nextRandomFloat() - 0.5f);
        normal.normalizeLocal();
        
        centernode = new Node();
        centernode.setName(name);
        centernode.setUserData("type", "node");
        
        modulecontrol = new ModuleControl(normal, th, localcolor);
        centernode.addControl(modulecontrol);
        
        centernode.attachChild(quadgeo);
        
        BillboardControl billcontrol = new BillboardControl();
        centernode.addControl(billcontrol);
        
        root.attachChild(centernode);
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
