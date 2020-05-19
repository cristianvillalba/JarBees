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
import com.jme3.material.RenderState;
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
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.style.BaseStyles;
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
    
    public static ArrayList<String> modulelabellist = new ArrayList<String>();
    private float checkprocesstime;
    private float checkprocessmaxtime = 1f;
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
    private Future future;
    private Future futurepidlist;
    private Node linkedlines;
    private Node modules;
    private ColorRGBA linecolor;
    
    private int PID;
    
    public static float time = 0f;
    public static Main instance;
    private int nextItem = 1;
    private Container pidlistwin = null;
    private ListBox listpid = null;
    
    public static void main(String[] args) {
        Main app = new Main();
        
        if (args.length == 1){
            app.setPID(args[0]);
            app.start();
        }
        else{
            app.setPID("-1");
            app.start();
        }
        
        instance = app;
    }

    @Override
    public void simpleInitApp() {
  
        allmodules = new ArrayList<Module>();
        flyCam.setMoveSpeed(10f);
        linkedlines = new Node();
        modules = new Node();
        this.setPauseOnLostFocus(false);
        
        // Initialize the globals access so that the default
        // components can find what they need.
        GuiGlobals.initialize(this);
        // Load the 'glass' style
        BaseStyles.loadGlassStyle();
        // Set 'glass' as the default style when not specified
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        
        
        initCrossHairs();
        
        
        InitCore();
        
        RegisterInput();
        
        mat_lines = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        linecolor = ColorRGBA.Orange.clone();
        linecolor.a = 0.6f;
        mat_lines.setColor("Color", linecolor);
        mat_lines.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.AlphaAdditive);
        mat_lines.getAdditionalRenderState().setDepthTest(false);
        mat_lines.getAdditionalRenderState().setDepthWrite(false);
        mat_lines.setTransparent(true);
        
        linkedlines.setQueueBucket(RenderQueue.Bucket.Translucent);
        
        rootNode.attachChild(linkedlines);
        rootNode.attachChild(modules);
        
        if (PID == -1){
            GenerateTestModules();
        }
        
        InitGUI();
    }
    
    private void InitGUI(){
        // Create a simple container for our elements
        Container myWindow = new Container();
        guiNode.attachChild(myWindow);

        // Put it somewhere that we will see it.
        // Note: Lemur GUI elements grow down from the upper left corner.
        myWindow.setLocalTranslation(10, cam.getHeight()- 10, 0);

        // Add some elements
        myWindow.addChild(new Label("Process Viewer"));
        Button clickMe = myWindow.addChild(new Button("Load PID"));
        clickMe.addClickCommands(new Command<Button>() {
                @Override
                public void execute( Button source ) {
                    //System.out.println("The world is yours.");
                    instance.CallProcessPIDs();
                }
            });
    }
    
    public void CallProcessPIDs()
    {
        futurepidlist = executor.submit(HookProcessList);
    }
    
    public void ClosePIDList()
    {
        guiNode.detachChild(pidlistwin);
        pidlistwin = null;
    }
    
    public void ProcessPIDList(ArrayList<String> data)
    {
        this.PIDlist(data);
    }
    
    public void PIDlist(ArrayList<String> data){
        // Create a simple container for our elements
        if (pidlistwin != null)
            return;
        
        pidlistwin = new Container();
        listpid = new ListBox();
        guiNode.attachChild(pidlistwin);

        // Put it somewhere that we will see it.
        // Note: Lemur GUI elements grow down from the upper left corner.
        pidlistwin.setLocalTranslation(110, cam.getHeight()- 10, 0);

        listpid.setVisibleItems(20);
 
        for( int i = 0; i < data.size(); i++ ) {        
            listpid.getModel().add(data.get(i));
            nextItem++;
        }    
        
        // Add some elements
        pidlistwin.addChild(new Label("PID List"));
        pidlistwin.addChild(listpid);
        Button clickMe = pidlistwin.addChild(new Button("Load"));
        clickMe.addClickCommands(new Command<Button>() {
                @Override
                public void execute( Button source ) {
                    instance.ClearModules();
                    instance.SetNewPID();
                    instance.ClosePIDList();
                }
            });
    }
    
    public void SetNewPID()
    {
        Integer selection = this.listpid.getSelectionModel().getSelection();
        
        String selectedvalue = this.listpid.getModel().get(selection).toString();
        System.out.println("Selected:" + selectedvalue);
        
        String parsedpid = selectedvalue.split("\\|")[0];
        
        this.setPID(parsedpid);
    }
    
    public void ClearModules()
    {
        linkedlines.detachAllChildren();
        modules.detachAllChildren();
        allmodules.clear();
        modulehashmap.clear();
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
        
        //System.out.println(data);
        //loop in reverse order due to stack trace
        String thread = "none";
        float level = 0.0f;
        float azimuth = FastMath.nextRandomFloat()*FastMath.TWO_PI;
        float altitude = FastMath.nextRandomFloat()*FastMath.PI;
        
        for (int i = (data.size() - 1); i > 0; i--)
        {
            String idthread = data.get(i).split("\\|")[0];
            
            if (!idthread.equals(thread))
            {
                thread = idthread;
                level = 0.0f; //reset level radius
                azimuth = FastMath.nextRandomFloat()*FastMath.TWO_PI;
                altitude = FastMath.nextRandomFloat()*FastMath.PI;
                
            }
            InteractModule(data.get(i), level, azimuth, altitude);
            level+= 0.55f;
        }
        
        for (int i = 0; i < data.size() - 1; i++)
        {
            String id0 = data.get(i).split("\\|")[0];
            String id1 = data.get(i + 1).split("\\|")[0];
            String name1 = data.get(i).split("\\|")[1];
            String name2 = data.get(i + 1).split("\\|")[1];
            
            if (id0.equals(id1))
            {
                String convertedthread = "1";
                try{
                    convertedthread = thread.replace("Id","").replace(" ","");
                }
                catch(Exception n)
                {

                }
                ColorRGBA color = Module.GenerateColor(convertedthread);
                GenerateLinkedLine(name1, name2, color);
            }
        }
        
        
    }
    
    private void GenerateLinkedLine(String n1, String n2, ColorRGBA color)
    {
        Module mod1 = modulehashmap.get(n1);
        Module mod2 = modulehashmap.get(n2);
        
        if (mod1 == null || mod2 == null){
            return;
        }
        
        Line line = new Line(mod1.GetMainNode().getWorldBound().getCenter().clone(), mod2.GetMainNode().getWorldBound().getCenter().clone());
        line.setLineWidth(2);
                
        Geometry geometry = new Geometry("line", line);
        Material mat = mat_lines.clone();
        mat.setColor("Color", color);
        geometry.setMaterial(mat);
        
        LinkedLineControl linecontrol = new LinkedLineControl(mod1, mod2, line);
        
        geometry.addControl(linecontrol);
        
        linkedlines.attachChild(geometry);
    }
    
    private void InteractModule(String name, float level, float azim, float alt){
        String realname = name.split("\\|")[1];
        String thread = name.split("\\|")[0];
        String convertedthread = "1";
        
        try{
            convertedthread = thread.replace("Id","").replace(" ","");
        }
        catch(Exception n)
        {
            
        }
        
        if (!modulehashmap.containsKey(realname)) //should I generate each module on thread or only one?
        {
            Module mod = new Module(assetManager, modules, realname, convertedthread, level, azim, alt);
            allmodules.add(mod);
            modulehashmap.put(realname, mod);
        }
        else
        {
            modulehashmap.get(realname).Pulsate(thread);
        }/*
        if (!modulehashmap.containsKey((realname+convertedthread)))
        {
            Module mod = new Module(assetManager, modules, realname, convertedthread, level, azim, alt);
            allmodules.add(mod);
            modulehashmap.put(realname+convertedthread, mod);
        }
        else
        {
            modulehashmap.get(realname+convertedthread).Pulsate(thread);
        }*/
    }
    
    private void GenerateTestModules()
    {
        ArrayList<String> testdata = new ArrayList<String>();
        
        int thread = 0;
        for (int i = 0 ; i < 100; i++){
            thread = (int)i / 3;
            testdata.add(thread + "|DKA"+ i);
        }
     
        ProcessModules(testdata);
    }
    
    @Override
    public void simpleUpdate(float tpf) {

        collideraytime += tpf;
        checkprocesstime += tpf;
        time += tpf;
        
       
        if (collideraytime > collideraymaxtime)
        {
            PutModuleInfo();
            collideraytime = 0f;
        }
            
        if (PID != -1){
            if (checkprocesstime > checkprocessmaxtime)
            {
                checkprocesstime = 0f;

                if (future == null)
                {
                    future = executor.submit(HookProcess);    //  Thread starts!
                }
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
        
        if (futurepidlist != null)
        {
            if(futurepidlist.isDone()){
                
                try{
                    ProcessPIDList((ArrayList<String>)futurepidlist.get());
                }
                catch(Exception m)
                {
                    
                }
                
                futurepidlist = null;
            }
            else if(futurepidlist.isCancelled()){
                futurepidlist = null;
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
                  
                  if (!modulelabellist.contains(closest.getGeometry().getName())){
                      AddLabel(closest.getGeometry()); 
                      modulelabellist.add(closest.getGeometry().getName());
                  }           
              }
          }
          
        } else {
        }
    }
    
    private void AddLabel(Geometry geo)
    {
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 0.02f);
        ch.setText(geo.getName()); // crosshairs
        ch.setColor(ColorRGBA.White);
        ch.setLocalTranslation(geo.getWorldTranslation());
        ch.setQueueBucket(RenderQueue.Bucket.Translucent);
        
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
                                  
            // -pv no invasive
            // -p PID
            // -c send commands to the debugger after initialization
            //~ display thread info * all threads
            //k is stack with b meaning 3 parameters, and first 0x80 lines
            
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
                        threadid = m.group(0).toString().replace("Id", "").replace(" ", "");
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
    
    private Callable<ArrayList<String>> HookProcessList = new Callable<ArrayList<String>>(){
        public ArrayList<String> call() throws Exception {
            ArrayList<String> data = new ArrayList<String>();
             
            ProcessBuilder processBuilder = new ProcessBuilder("tasklist.exe");
            Process process = null;
            String line;

            try {
                process = processBuilder.start();
            } catch (IOException ex) {
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));

     
            try{
                line = null;
                boolean read = false;
                
                while((line = input.readLine())!= null){
                    if (line.contains("=")){
                        read = true;
                        continue;
                    }
                    
                    if (read){
                        String pidname = line.substring(0, 26);
                        String pid = line.substring(26, 35);


                        data.add(pid.trim() + "|" + pidname.trim());
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
