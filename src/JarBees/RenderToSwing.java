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

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import static java.awt.GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This test renders a scene to an offscreen framebuffer, then copies
 * the contents to a Swing JFrame. Note that some parts are done inefficently,
 * this is done to make the code more readable.
 */
public class RenderToSwing extends SimpleApplication implements SceneProcessor {

    private Geometry offBox;
    private float angle = 0;

    private FrameBuffer offBuffer;
    private ViewPort offView;
    private Texture2D offTex;
    private Camera offCamera;
    private ImageDisplay display;

    private static final int width = 800, height = 600;

    private final ByteBuffer cpuBuf = BufferUtils.createByteBuffer(width * height * 4);
    private final byte[] cpuArray = new byte[width * height * 4];
    private final BufferedImage image = new BufferedImage(width, height,
                                            BufferedImage.TYPE_INT_ARGB);
    
    
    private class WeirdFrame extends JFrame {
        public WeirdFrame() {
            super("WeirdFrame");
            super.setUndecorated(true);
            
            setBackground(new Color(0,0,0,0));
            setSize(new Dimension(800,600));
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            //setDefaultLookAndFeelDecorated(false);

            JPanel panel = new JPanel() {
                /*@Override
                protected void paintComponent(Graphics g) {
                    if (g instanceof Graphics2D) {
                        final int R = 240;
                        final int G = 240;
                        final int B = 240;

                        Paint p =
                            new GradientPaint(0.0f, 0.0f, new Color(R, G, B, 0),
                                0.0f, getHeight(), new Color(R, G, B, 255), true);
                        Graphics2D g2d = (Graphics2D)g;
                        g2d.setPaint(p);
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                    }
                }*/
            };
            setContentPane(panel);
            panel.setOpaque(false);
            //setLayout(new GridBagLayout());
            //add(new JButton("I am a Button"))
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

            //int rule = AlphaComposite.SRC_IN;
            //Composite comp = AlphaComposite.getInstance(rule , 1.0f );
            //g2d.setComposite(comp );
         
            //g2d.setColor(Color.yellow);
            //g2d.clearRect(0, 0, getWidth(), getHeight());
            
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

    public static void main(String[] args){
        RenderToSwing app = new RenderToSwing();
        app.setPauseOnLostFocus(false);
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1, 1);
        app.setSettings(settings);
        app.start(Type.OffscreenSurface);
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
                WeirdFrame frame = new WeirdFrame();
                display = new ImageDisplay();
                display.setPreferredSize(new Dimension(width, height));
                frame.getContentPane().add(display);
                frame.addWindowListener(new WindowAdapter(){
                    @Override
                    public void windowClosed(WindowEvent e){
                        stop();
                    }
                });
                frame.setVisible(true);
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
            int alphavalue = 256 - oldcolor.getRed();
            //int alphavalue = 128;
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
        offCamera.setLocation(new Vector3f(0f, 0f, -5f));
        offCamera.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);

        //setup framebuffer's texture
//        offTex = new Texture2D(width, height, Format.RGBA8);

        //setup framebuffer to use renderbuffer
        // this is faster for gpu -> cpu copies
        offBuffer.setDepthBuffer(Format.Depth);
        offBuffer.setColorBuffer(Format.RGBA8);
//        offBuffer.setColorTexture(offTex);
        
        //set viewport to render to offscreen framebuffer
        offView.setOutputFrameBuffer(offBuffer);

        // setup framebuffer's scene
        Box boxMesh = new Box(1, 1, 1);
        Material material = assetManager.loadMaterial("Interface/Logo/Logo.j3m");
        offBox = new Geometry("box", boxMesh);
        offBox.setMaterial(material);

        // attach the scene to the viewport to be rendered
        offView.attachScene(offBox);
    }

    @Override
    public void simpleInitApp() {
        setupOffscreenView();
        createDisplayFrame();
    }

    @Override
    public void simpleUpdate(float tpf){
        Quaternion q = new Quaternion();
        angle += tpf;
        angle %= FastMath.TWO_PI;
        q.fromAngles(angle, 0, angle);

        offBox.setLocalRotation(q);
        offBox.updateLogicalState(tpf);
        offBox.updateGeometricState();
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


}
