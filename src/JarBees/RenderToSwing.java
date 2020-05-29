/*
 * Copyright (c) 2009-2020 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package JarBees;

import static JarBees.Main.instance;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.SceneProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import static java.awt.GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT;
import java.awt.GraphicsEnvironment;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.*;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class RenderToSwing extends SimpleApplication implements SceneProcessor {

    private Geometry offBox;
    private float angle = 0;
    public static float time;

    private FrameBuffer offBuffer;
    private ViewPort offView;
    private Texture2D offTex;
    private Camera offCamera;
    private ImageDisplay display;
    private Node newRootNode;

    private static final int width = 800, height = 600;

    private final ByteBuffer cpuBuf = BufferUtils.createByteBuffer(width * height * 4);
    private final byte[] cpuArray = new byte[width * height * 4];
    private final BufferedImage image = new BufferedImage(width, height,
                                            BufferedImage.TYPE_INT_ARGB);
    
    private Node linkedlines;
    private Material mat_lines;
    private ColorRGBA linecolor;
    private Node modules;
    private ArrayList<Module> allmodules;
    private Map<String, Module> modulehashmap = new HashMap<String, Module>();
    
    public static RenderToSwing mainapp;
    public static WeirdFrame weirdframe;
    
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
    public static Future futurepidlist;
    private Future future;
    private Future futurecpu;
    public static JMenu itemLoadPid;
    private int PID = -1;
    private String PROCESSNAME;
    public static float CPUUTIL;
    
    private float checkprocesstime;
    private float checkprocessmaxtime = 1f;
    
    private class WeirdMenu extends JMenuBar{
        @Override
            protected void paintComponent(Graphics g) {
                if (g instanceof Graphics2D) {
                    final int R = 240;
                    final int G = 240;
                    final int B = 240;
 
                    Paint p =
                        new GradientPaint(0.0f, 0.0f, new Color(R, G, B, 255),
                            getWidth(), getHeight(), new Color(R, G, B, 0), true);
                    Graphics2D g2d = (Graphics2D)g;
                    g2d.setPaint(p);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
    }
    
    private class WeirdFrame extends JFrame {
        private JPanel currentPanel;
        
        public WeirdFrame() {
            super("WeirdFrame");
            super.setUndecorated(true);
            
            setBackground(new Color(0,0,0,0));
            setSize(new Dimension(800,600));
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            currentPanel = new JPanel() {
            };
            setContentPane(currentPanel);
            currentPanel.setOpaque(false);
            
            this.createMenu();
            //setLayout(new GridBagLayout());
            //add(new JButton("I am a Button"))
        }
        
        public void createMenu(){
            WeirdMenu menuBar = new WeirdMenu();
            this.setJMenuBar(menuBar);

            JMenu menuTortureMethods = new JMenu("JarBees v 0.0.1");
            menuBar.add(menuTortureMethods);

            RenderToSwing.itemLoadPid = new JMenu("Load Process PID");
            RenderToSwing.itemLoadPid.setAutoscrolls(true);
            //RenderToSwing.itemLoadPid.scrollRectToVisible(true);
            MenuScroller.setScrollerFor(RenderToSwing.itemLoadPid, 24, 50, 0, 0);
            menuTortureMethods.add(RenderToSwing.itemLoadPid);

            final JMenuItem itemRefresh = new JMenuItem("Refresh PID list");
            menuTortureMethods.add(itemRefresh);
            itemRefresh.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    RenderToSwing.mainapp.CallProcessPIDs();
                }
            });

            
            JMenuItem itemExit = new JMenuItem("Exit");
            menuTortureMethods.add(itemExit);
            itemExit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    RenderToSwing.weirdframe.dispose();
                    mainapp.stop();
                }
            });
        }
    }

    private class ImageDisplay extends JPanel {

        private long t;
        private long total;
        private int frames;
        private int fps;

        public ImageDisplay()
        {
            setOpaque(false);
        }
        
        @Override
        public void paintComponent(Graphics gfx) {

            super.paintComponent(gfx);
            Graphics2D g2d = (Graphics2D) gfx;
            
            if (t == 0)
                t = timer.getTime();

            synchronized (image){
                g2d.drawImage(image, null, 0, 0);
            }

            long t2 = timer.getTime();
            long dt = t2 - t;
            total += dt;
            frames ++;
            t = t2;

            if (total > timer.getResolution()) {
                fps = frames;
                total = 0;
                frames = 0;
            }
           
            
            g2d.setColor(Color.GREEN);
            g2d.drawString("FPS: "+fps, 0, getHeight() - 100);
        }
    }
    
    public static class FrameDragListener extends MouseAdapter {

        private final JFrame frame;
        private Point mouseDownCompCoords = null;

        public FrameDragListener(JFrame frame) {
            this.frame = frame;
        }

        public void mouseReleased(MouseEvent e) {
            mouseDownCompCoords = null;
        }

        public void mousePressed(MouseEvent e) {
            mouseDownCompCoords = e.getPoint();
        }

        public void mouseDragged(MouseEvent e) {
            Point currCoords = e.getLocationOnScreen();
            frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
        }
    }

    public static void main(String[] args){
        mainapp = new RenderToSwing();
        mainapp.setPauseOnLostFocus(false);
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1, 1);
        mainapp.setSettings(settings);
        mainapp.start(Type.OffscreenSurface);
    }
    
    public void CallProcessPIDs()
    {
        RenderToSwing.futurepidlist = executor.submit(HookProcessList);
    }
    
    public void ProcessCPU(ArrayList<String> data)
    {
        for (int i = 0; i< data.size(); i++)
        {
            if (data.get(i).length()<28){
                continue;
            }
            
            String processname = data.get(i).substring(0, 28).trim();
            String cpu = data.get(i).substring(28).trim();
            
            if (processname.toLowerCase().equals(this.PROCESSNAME.toLowerCase()))
            {
                try
                {
                    RenderToSwing.CPUUTIL = Float.parseFloat(cpu);
                    System.out.println(RenderToSwing.CPUUTIL);
                }
                catch(Exception n)
                {
                    
                }
                
            }
        }
        
    }

    public void createDisplayFrame(){
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        boolean isPerPixelTranslucencySupported = 
            gd.isWindowTranslucencySupported(PERPIXEL_TRANSLUCENT);

        //If translucent windows aren't supported, exit.
        if (!isPerPixelTranslucencySupported) {
            System.out.println(
                "Per-pixel translucency is not supported");
                System.exit(0);
        }
        
        //JFrame.setDefaultLookAndFeelDecorated(true); //put default style
        
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                if (RenderToSwing.weirdframe != null)
                    return;
                
                RenderToSwing.weirdframe = new WeirdFrame();
                display = new ImageDisplay();
                display.setPreferredSize(new Dimension(width, height));
                RenderToSwing.weirdframe.getContentPane().add(display);
                RenderToSwing.weirdframe.addWindowListener(new WindowAdapter(){
                    @Override
                    public void windowClosed(WindowEvent e){
                        stop();
                    }
                });
                
                FrameDragListener frameDragListener = new FrameDragListener(RenderToSwing.weirdframe);
                RenderToSwing.weirdframe.addMouseListener(frameDragListener);
                RenderToSwing.weirdframe.addMouseMotionListener(frameDragListener);
                      
                RenderToSwing.weirdframe.setVisible(true);
            }
        });
    }
    
    public static int getIntFromColor(int Red, int Green, int Blue, int alpha){
        alpha = (alpha << 24) & 0xFF000000; //Shift red 16-bits and mask out other stuff
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.

        return alpha | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }
    
    public static void convertScreenShot(IntBuffer bgraBuf, BufferedImage out){
        WritableRaster wr = out.getRaster();
        DataBufferInt db = (DataBufferInt) wr.getDataBuffer();

        int[] cpuArray = db.getData();

        // copy native memory to java memory
        bgraBuf.clear();
        bgraBuf.get(cpuArray);

        int width  = wr.getWidth();
        int height = wr.getHeight();

      
        for (int y = 0; y < cpuArray.length; y++){
            Color oldcolor = new Color(cpuArray[y], true);

            int alphavalue = oldcolor.getGreen();
            //Every RED color (in this case Blue because of the buffer) will have transparent value
            
            if (oldcolor.getRed() == 0 && oldcolor.getGreen() == 0 && oldcolor.getBlue() == 255){
               alphavalue = 0;
            } 
          
            cpuArray[y] = RenderToSwing.getIntFromColor(oldcolor.getBlue(), oldcolor.getGreen(), oldcolor.getRed(), alphavalue);
            
        }
    }

    public void updateImageContents(){
        cpuBuf.clear();
        renderer.readFrameBuffer(offBuffer, cpuBuf);

        synchronized (image) {
            RenderToSwing.convertScreenShot(cpuBuf.asIntBuffer(), image);    
        }

        if (display != null)
            display.repaint();
    }
    
    private void InitCore()
    {
        FilterPostProcessor fpp=new FilterPostProcessor(assetManager);
        BloomFilter bf = new BloomFilter(BloomFilter.GlowMode.Objects);
        bf.setExposurePower(1.0f);
        fpp.addFilter(bf);
        offView.addProcessor(fpp);
    }
    
    private void InitMaterials()
    {
        mat_lines = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        linecolor = ColorRGBA.Orange.clone();
        linecolor.a = 0.6f;
        mat_lines.setColor("Color", linecolor);
        mat_lines.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.AlphaAdditive);
        mat_lines.getAdditionalRenderState().setDepthTest(false);
        mat_lines.getAdditionalRenderState().setDepthWrite(false);
        mat_lines.setTransparent(true);
        
        linkedlines.setQueueBucket(RenderQueue.Bucket.Translucent);
    }
    
    private void setupJMonkey()
    {
        allmodules = new ArrayList<Module>();
        linkedlines = new Node();
        modules = new Node();
        
        InitCore();
        
        InitMaterials();
        
        newRootNode.attachChild(linkedlines);
        newRootNode.attachChild(modules);
        
        //if (PID == -1){
        GenerateTestModules();
        //}
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
            Module mod = new Module(assetManager, modules, realname, convertedthread, level, azim, alt, true);
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

    public void setupOffscreenView(){
        offCamera = new Camera(width, height);

        // create a pre-view. a view that is rendered before the main view
        offView = renderManager.createPreView("Offscreen View", offCamera);
        offView.setBackgroundColor(ColorRGBA.Red);
        offView.setClearFlags(true, true, true);
        
        // this will let us know when the scene has been rendered to the 
        // frame buffer
        offView.addProcessor(this);

        // create offscreen framebuffer
        offBuffer = new FrameBuffer(width, height, 1);

        //setup framebuffer's cam
        offCamera.setFrustumPerspective(45f, 1f, 1f, 1000f);
        offCamera.setLocation(new Vector3f(0f, 0f, -50f));
        offCamera.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        //setup framebuffer's texture
//        offTex = new Texture2D(width, height, Format.RGBA8);

        //setup framebuffer to use renderbuffer
        // this is faster for gpu -> cpu copies
        offBuffer.setDepthBuffer(Format.Depth);
        offBuffer.setColorBuffer(Format.RGBA8);
//        offBuffer.setColorTexture(of

        // setup framebuffer's scenefTex);
        
        //set viewport to render to offscreen framebuffer
        offView.setOutputFrameBuffer(offBuffer);
        Box boxMesh = new Box(1, 1, 1);
        Material material = assetManager.loadMaterial("Interface/Logo/Logo.j3m");
        offBox = new Geometry("box", boxMesh);
        offBox.setMaterial(material);
        
        newRootNode = new Node();
        newRootNode.setName("ROOT");
        //newRootNode.attachChild(offBox);
        // attach the scene to the viewport to be rendered
        offView.attachScene(newRootNode);
    }
    
    public void ClearModules()
    {
        linkedlines.detachAllChildren();
        modules.detachAllChildren();
        allmodules.clear();
        modulehashmap.clear();
    }
    
    public void ProcessPIDList(ArrayList<String> data){
        
        RenderToSwing.itemLoadPid.removeAll();
        
        for( int i = 0; i < data.size(); i++ ) {        
            JMenuItem item = new JMenuItem(data.get(i));
            RenderToSwing.itemLoadPid.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    RenderToSwing.mainapp.ClearModules();
                    String parsedpid = ae.getActionCommand().split("\\|")[0];
                    String parsedname = ae.getActionCommand().split("\\|")[1];
        
                    RenderToSwing.mainapp.setPID(parsedpid);
                    RenderToSwing.mainapp.setProcessName(parsedname);
                }
            });
        }    
        
    }
    
    public void setProcessName(String n)
    {
        if (n.contains("."))
        {
           this.PROCESSNAME = n.split("\\.")[0];
        }
        else{
        this.PROCESSNAME = n;
        }
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

    @Override
    public void simpleInitApp() {
        setupOffscreenView();
        setupJMonkey();
        createDisplayFrame();
        CallProcessPIDs();
    }

    @Override
    public void simpleUpdate(float tpf){
        RenderToSwing.time += tpf;
        checkprocesstime += tpf;
        
        Quaternion q = new Quaternion();
        angle += tpf*0.1f;
        angle %= FastMath.TWO_PI;
        q.fromAngles(0, angle, 0);
        
        if (PID != -1){
            if (checkprocesstime > checkprocessmaxtime)
            {
                checkprocesstime = 0f;

                if (future == null)
                {
                    future = executor.submit(HookProcess);    //  Thread starts!
                }
                
                if (futurecpu == null)
                {
                    futurecpu = executor.submit(HookCPU);    //  Thread starts!
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

        if (RenderToSwing.futurepidlist != null)
        {
            if(RenderToSwing.futurepidlist.isDone()){
                
                try{
                    ProcessPIDList((ArrayList<String>)futurepidlist.get());
                }
                catch(Exception m)
                {
                    
                }
                
                RenderToSwing.futurepidlist = null;
            }
            else if(RenderToSwing.futurepidlist.isCancelled()){
                RenderToSwing.futurepidlist = null;
            }
        }
        
        if (futurecpu != null)
        {
            if(futurecpu.isDone()){
                
                try{
                    ProcessCPU((ArrayList<String>)futurecpu.get());
                }
                catch(Exception m)
                {
                    
                }
                
                futurecpu = null;
            }
            else if(futurecpu.isCancelled()){
                futurecpu = null;
            }
        }
        
        //offBox.setLocalRotation(q);
        //offBox.updateLogicalState(tpf);
        //offBox.updateGeometricState();
        //newRootNode.setLocalRotation(q);
        //linkedlines.setLocalRotation(q);
        //modules.setLocalRotation(q);
        if (PID == -1){
            offCamera.setLocation(new Vector3f(FastMath.sin(angle)*25f, 0f, FastMath.cos(angle)*25f));
        }
        else
        {
            offCamera.setLocation(new Vector3f(FastMath.sin(angle)*100f, 0f, FastMath.cos(angle)*100f));
        }
        offCamera.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);
        
        //newRootNode.setLocalRotation(q);
        newRootNode.updateLogicalState(tpf);
        newRootNode.updateGeometricState();
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void preFrame(float tpf) {
    }

    @Override
    public void postQueue(RenderQueue rq) {
    }

    /**
     * Update the CPU image's contents after the scene has
     * been rendered to the framebuffer.
     */
    @Override
    public void postFrame(FrameBuffer out) {
        updateImageContents();
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void setProfiler(AppProfiler profiler) {

    }
    
    private Callable<ArrayList<String>> HookCPU = new Callable<ArrayList<String>>(){
        public ArrayList<String> call() throws Exception {
            ArrayList<String> data = new ArrayList<String>();
            
            ProcessBuilder processBuilder = new ProcessBuilder("wmic.exe","path", "Win32_PerfFormattedData_PerfProc_Process","get","Name,PercentProcessorTime");
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
                    data.add(line);
                }
            }
            catch(Exception n)
            {

            }

            return data;
        }
    };
    
    
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
                        if (!data.contains(threadid + "|" + m.group(0).toString()))
                            data.add(threadid + "|" + m.group(0).toString());
                    }

                    m = p2.matcher(line);

                    while(m.find())
                    {
                        if (!data.contains(threadid + "|" + m.group(0).toString()))
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
