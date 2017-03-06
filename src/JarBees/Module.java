/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package JarBees;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author Cristian.Villalba
 */
public class Module{
    private Geometry modulegeo;
    private Material mat_core;
    private ModuleControl modulecontrol;
    private Node centernode;
    
    public Module(AssetManager asset, Node root, String name, String th)
    {
        mat_core = new Material(asset, "Common/MatDefs/Misc/Particle.j3md");
        mat_core.setTexture("Texture", asset.loadTexture("Textures/Smoke.png"));
        mat_core.setTexture("GlowMap", asset.loadTexture("Textures/GlowMap.png"));
        //mat_core.setColor("GlowColor", new ColorRGBA(0.0f, 0.0f,1.0f, 0.2f));
             
        Quad quadmesh = new Quad(1.0f, 1.0f);
        Geometry quadgeo = new Geometry("quadcore", quadmesh);
        
        quadgeo.setMaterial(mat_core);
        quadgeo.setName(name);
        quadgeo.setUserData("type", "module");
        
        BillboardControl billcontrol = new BillboardControl();
        quadgeo.addControl(billcontrol);
        
        Vector3f normal = new Vector3f();
        normal.set(FastMath.nextRandomFloat() - 0.5f, FastMath.nextRandomFloat() - 0.5f, FastMath.nextRandomFloat() - 0.5f);
        normal.normalizeLocal();
        
        centernode = new Node();
        centernode.setName(name);
        centernode.setUserData("type", "node");
        
        modulecontrol = new ModuleControl(normal, th);
        centernode.addControl(modulecontrol);
        
        centernode.attachChild(quadgeo);
        
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
