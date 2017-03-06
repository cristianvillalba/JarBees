package JarBees;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Quad;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication implements ActionListener{
    private Material mat_lines;
    private ArrayList<Module> allmodules;
    private float collideraytime;
    private float collideraymaxtime = 0.25f;
    private CollisionResults results = new CollisionResults();
    private Ray ray = new Ray(Vector3f.ZERO.clone(), Vector3f.UNIT_Z.clone());
    private Map<String, Module> modulehashmap = new HashMap<String, Module>();
    private float checkprocesstime;
    private float checkprocessmaxtime = 1f;
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
    private Future future;
    private Node linkedlines;
    
    private int PID;
    
    public static void main(String[] args) {
        Main app = new Main();
        
        if (args.length == 1){
            app.setPID(args[0]);
            app.start();
        }
    }

    @Override
    public void simpleInitApp() {
  
        allmodules = new ArrayList<Module>();
        flyCam.setMoveSpeed(10f);
        linkedlines = new Node();
        this.setPauseOnLostFocus(false);
        
        initCrossHairs();
        
        
        InitCore();
        
        RegisterInput();
        
        mat_lines = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_lines.setColor("Color", ColorRGBA.White);
        rootNode.attachChild(linkedlines);
    }
    
    public void setPID(String value)
    {
        try{
           int pidvalue = Integer.parseInt(value);
           PID = pidvalue;
        }
        catch(Exception n)
        {
            
        }
    }
    
    private void initCrossHairs()
    {
        setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
        settings.getWidth() / 2 - ch.getLineWidth()/2, settings.getHeight() / 2 + ch.getLineHeight()/2, 0);
        guiNode.attachChild(ch);
    }
    
    public void RegisterInput() {
        inputManager.addMapping("shoot",new MouseButtonTrigger(MouseInput.BUTTON_LEFT), new KeyTrigger(keyInput.KEY_RETURN));
        
        inputManager.addListener(this, "shoot");
    }
    
    private void InitCore()
    {
        FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
        BloomFilter bf = new BloomFilter(BloomFilter.GlowMode.Objects);
        bf.setExposurePower(1.0f);
        fpp.addFilter(bf);
        viewPort.addProcessor(fpp);
    }
    
    private void ProcessModules(ArrayList<String> data)
    {        
        linkedlines.detachAllChildren();
        
        for (int i = 0; i < data.size(); i++)
        {
            InteractModule(data.get(i));
        }
        
        for (int i = 0; i < data.size() - 1; i++)
        {
            String id0 = data.get(i).split("\\|")[0];
            String id1 = data.get(i + 1).split("\\|")[0];
            String name1 = data.get(i).split("\\|")[1];
            String name2 = data.get(i + 1).split("\\|")[1];
            
            if (id0.equals(id1))
            {
                GenerateLinkedLine(name1, name2);
            }
        }
        
        
    }
    
    private void GenerateLinkedLine(String n1, String n2)
    {
        Module mod1 = modulehashmap.get(n1);
        Module mod2 = modulehashmap.get(n2);
        
        Line line = new Line(mod1.GetMainNode().getWorldTranslation().clone(), mod2.GetMainNode().getWorldTranslation().clone());
        line.setLineWidth(2);
                
        Geometry geometry = new Geometry("line", line);
        geometry.setMaterial(mat_lines);
        
        LinkedLineControl linecontrol = new LinkedLineControl(mod1, mod2, line);
        
        geometry.addControl(linecontrol);
        
        linkedlines.attachChild(geometry);
    }
    
    private void InteractModule(String name){
        String realname = name.split("\\|")[1];
        String thread = name.split("\\|")[0];
        
        if (!modulehashmap.containsKey(realname))
        {
            Module mod = new Module(assetManager, rootNode, realname, thread);
            allmodules.add(mod);
            modulehashmap.put(realname, mod);
        }
        else
        {
            modulehashmap.get(realname).Pulsate(thread);
        }
    }
    
    @Override
    public void simpleUpdate(float tpf) {

        collideraytime += tpf;
        checkprocesstime += tpf;
        
        if (collideraytime > collideraymaxtime)
        {
            PutModuleInfo();
            collideraytime = 0f;
        }
        
        if (checkprocesstime > checkprocessmaxtime)
        {
            checkprocesstime = 0f;
            
            if (future == null)
            {
                future = executor.submit(HookProcess);    //  Thread starts!
            }
        }
        
        if (future != null)
        {
            if(future.isDone()){
                
                try{
                    ProcessModules((ArrayList<String>)future.get());
                }
                catch(Exception m)
                {
                    
                }
                
                future = null;
            }
            else if(future.isCancelled()){
                future = null;
            }
        }
            
    }
    
    private void PutModuleInfo()
    {
        results.clear();

        ray.setOrigin(cam.getLocation());
        ray.setDirection(cam.getDirection());

        rootNode.collideWith(ray, results);


        if (results.size() > 0) {
          CollisionResult closest = results.getClosestCollision();
          
          if (closest.getGeometry().getUserData("type") != null)
          {
              if (closest.getGeometry().getUserData("type").equals("module")){
                AddLabel(closest.getGeometry());
              }
          }
          
        } else {
        }
    }
    
    private void AddLabel(Geometry geo)
    {
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 0.2f);
        ch.setText(geo.getName()); // crosshairs
        ch.setColor(ColorRGBA.Blue);
        ch.setLocalTranslation(geo.getWorldTranslation());
        ch.setQueueBucket(RenderQueue.Bucket.Transparent);
        
        BillboardControl billcontrol = new BillboardControl();
        ch.addControl(billcontrol);
        
        LabelControl labelcontrol = new LabelControl();
        ch.addControl(labelcontrol);
        
        rootNode.attachChild(ch);
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.contains("shoot") && isPressed)
        {
            //int rand = FastMath.nextRandomInt(0, allmodules.size() - 1);    
            //allmodules.get(rand).Pulsate();
        }
    }
    
    private Callable<ArrayList<String>> HookProcess = new Callable<ArrayList<String>>(){
        public ArrayList<String> call() throws Exception {
            Pattern p1;
            Pattern p2;
            Pattern p3;
            String threadid = "0";
            ArrayList<String> data = new ArrayList<String>();
            
            p1 = Pattern.compile("([A-Za-z0-9_-]*)!([:A-Za-z0-9_-]*\\+*)");
            p2 = Pattern.compile("\\s([A-Za-z0-9_-]*\\+)");
            p3 = Pattern.compile("\\s*([0-9]*\\s*)Id");
                                  
            ProcessBuilder processBuilder = new ProcessBuilder("dbgruntime/cdb.exe","-p", "" + PID + "","-pv","-c","~*kb80;q");
            Process process = null;
            String line;

            try {
                process = processBuilder.start();
            } catch (IOException ex) {
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));

     
            try{
                line = null;
                while((line = input.readLine())!= null){
                    Matcher m = p3.matcher(line);
                    
                    while(m.find())
                    {
                        threadid = m.group(0).toString();
                    }
                    
                    m = p1.matcher(line);

                    while(m.find())
                    {
                        data.add(threadid + "|" + m.group(0).toString());
                    }

                    m = p2.matcher(line);

                    while(m.find())
                    {
                        data.add(threadid + "|" + m.group(0).toString());
                    }

                }
            }
            catch(Exception n)
            {

            }

            return data;
        }
    };
    
    @Override
    public void destroy() {
        super.destroy();
        executor.shutdown();
    }
}
