package cviceni_grid;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.InputStream;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import oglutils.OGLBuffers;
import oglutils.OGLRenderTarget;
import oglutils.OGLTextRenderer;
import oglutils.OGLTexture;
import oglutils.OGLTexture2D;
import oglutils.OGLUtils;
import oglutils.ShaderUtils;
import oglutils.ToFloatArray;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;


public class Renderer implements GLEventListener, MouseListener,
		MouseMotionListener, KeyListener {

	double x, y, z;
	int width, height, ox, oy, switchShaderType, switchPolygonMode, switchLightMode, switchFunctionType, switchReflectorMode, switchVertexMode, switchTexture;
	final static int NORMAL_TYPE = 1;
	final static int NORMAL_TYPE_FRAG_LIGHT = 2;
	final static int POLYGON_MODE_FILL = 1;
	final static int POLYGON_MODE_LINE = 2;
	final static int LIGHT_MODE_POSITION = 1;
	final static int LIGHT_MODE_COLOR = 2;
	final static int LIGHT_MODE_TEXTURE = 3;
	final static int LIGHT_MODE_DIFFUSION = 4;
	final static int LIGHT_MODE_BLINN_PHONG_HALF_VECTOR = 5;
	final static int LIGHT_MODE_BLINN_PHONG_REFLECT_VECTOR = 6;
	final static int LIGHT_MODE_TEXTURE_NORMAL = 7;
	final static int LIGHT_MODE_TEXTURE_PARALLAX = 8;
	final static int LIGHT_MODE_TONE_SHADING = 9;
	final static int LIGHT_MODE_TONE_SHADING_V2 = 10;
	final static int FUNCTION_GRID = 1;
	final static int FUNCTION_CARTESIAN_1 = 2;
	final static int FUNCTION_CARTESIAN_2 = 3;
	final static int FUNCTION_CARTESIAN_3 = 4;	
	final static int FUNCTION_CYLINDER_1 = 5;
	final static int FUNCTION_CYLINDER_2 = 6;
	final static int FUNCTION_CYLINDER_3 = 7;
	final static int FUNCTION_SFERIC_1 = 8;
	final static int FUNCTION_SFERIC_2 = 9;
	final static int FUNCTION_SFERIC_3 = 10;
	final static int REFLECTOR_ON = 1;
	final static int REFLECTOR_OFF = 2;
	final static int LIGHT_MODE_VERTEX = 1;
	final static int LIGHT_MODE_FRAGMENT_BLINN = 2;
	final static int LIGHT_MODE_FRAGMENT_PHONG = 3;
	final static int TEXTURE_BRICKS = 1;
	final static int TEXTURE_OLD_BRICKS = 2;
	final static int TEXTURE_GROUND = 3;
	final static int TEXTURE_SIDEWALK = 4;
	public static final int PROCESS_BLURE = 1;
	public static final int PROCESS_GRAYSCALE_EYE = 2;
	public static final int PROCESS_GRAYSCALE = 3;
	public static final int PROCESS_INVERSE = 4;
	public static final int PROCESS_SOBEL = 5;
	public static final int PROCESS_DEPTH = 6;
	public static final int PROCESS_BOXES = 7;
	public static final int PROCESS_FISH_EYE = 8;
	public static final int PROCESS_DEPTH_LINEARIZED = 9;
	Vec3D WHITE_TEXT = new Vec3D(1.0, 1.0, 1.0);
	Vec3D BLACK_TEXT = new Vec3D(0.0, 0.0, 0.0);
	
	Vec3D lightPosition;
	//Vec3D lightPosition;
	Vec3D eyePosition;
	Vec3D spotDir;

	
	OGLTexture2D texture, texture2, texture3, texture4;
	OGLTexture2D textureNormal, textureNormal2, textureNormal3, textureNormal4;
	OGLTexture2D textureHeight, textureHeight2, textureHeight3, textureHeight4;
	OGLTexture2D textureColor, textureSobol, textureBackFaces;
	OGLTexture2D texPikachu, texSouthPark;
	OGLTexture2D texDepth, texNormal, texColor;
	OGLTexture2D texDepthEdge, texNormalEdge, texColorEdge, texCelShading;
	OGLTexture.Viewer viewer;
	
	OGLRenderTarget renderTarget, renderTargetEdgeDetect, renderTargetDestination;	
	OGLTexture2D.Viewer textureViewer;
	OGLBuffers buffers, bufferQuad;
	OGLTextRenderer textRenderer = new OGLTextRenderer();

	int shaderProgram, shaderProgramPost,  shaderEdgeDetect;
	int locMat, locMatLF;
	int locMatView, locMatViewLF;
	int locMatProj;
	int loc_lightPosition, loc_lightPositionLF;
	int loc_eyePosition, loc_eyePositionLF;
	int loc_reflectorLF, loc_reflectorNormal,  loc_spotDir;
	int loc_lightMode, loc_lightVertexMode;
	int loc_function_normal;
	int loc_function_LF;
	int locSwitchShaderPost, locTime, switchShaderPost;
	int locViewMatBF, locProjMatBF, locFunctionBF;
	

	Camera cam = new Camera();
	Mat4 proj; // created in reshape()
	
	float time = 0;

	public void init(GLAutoDrawable glDrawable) {
		
		
		GL2 gl = glDrawable.getGL().getGL2();
		
		OGLUtils.printOGLparameters(gl);
		OGLUtils.shaderCheck(gl);						
		
		textRenderer = new OGLTextRenderer();		
		viewer = new OGLTexture2D.Viewer(gl);
		
		shaderProgram = ShaderUtils.loadProgram(gl, "/cviceni/start");
		shaderProgramPost = ShaderUtils.loadProgram(gl, "/atest/postProces");
		shaderEdgeDetect = ShaderUtils.loadProgram(gl, "/cviceni/edge");
		
		createBuffers(gl);	
		
		renderTarget = new OGLRenderTarget(gl, 500, 500,3);
		renderTargetEdgeDetect = new OGLRenderTarget(gl, 500, 500, 4);
		renderTargetDestination = new OGLRenderTarget(gl, 500, 500, 3);
		locSwitchShaderPost = gl.glGetUniformLocation(shaderProgramPost, "switchShaderPost");
		locTime = gl.glGetUniformLocation(shaderProgramPost, "time");
		switchShaderPost = 10;
		
		
		//Shader - Per Pixel LIGHTING
		locMatView = gl.glGetUniformLocation(shaderProgram, "viewMat");
		locMatProj = gl.glGetUniformLocation(shaderProgram, "projMat");
		locMat = gl.glGetUniformLocation(shaderProgram, "mat");
		loc_lightPosition = gl.glGetUniformLocation(shaderProgram, "lightPosition");
		loc_eyePosition = gl.glGetUniformLocation(shaderProgram, "eyePosition");
		loc_function_normal = gl.glGetUniformLocation(shaderProgram, "functionType");
		loc_lightMode = gl.glGetUniformLocation(shaderProgram, "lightMode");
		loc_spotDir = gl.glGetUniformLocation(shaderProgram, "spotDir");
		loc_reflectorNormal = gl.glGetUniformLocation(shaderProgram, "reflectorMode");
		

		
		
		switchShaderType = NORMAL_TYPE;
		switchPolygonMode = POLYGON_MODE_FILL;
		switchLightMode = LIGHT_MODE_TEXTURE;
		switchFunctionType = FUNCTION_CARTESIAN_1;
		switchReflectorMode = REFLECTOR_OFF;
		switchVertexMode = LIGHT_MODE_VERTEX;		
		switchTexture = 5;
		switchShaderPost = PROCESS_SOBEL;

		lightPosition = new Vec3D(20.0, 20.0, 10.0);
		eyePosition = new Vec3D(20.0, 20.0, 10.0);
		
		cam = cam.withPosition(new Vec3D(0,0,32))
						.withAzimuth(Math.PI * 0.5)
						.withZenith(Math.PI * -0.5);
		gl.glEnable(GL2.GL_DEPTH_TEST);			
		
		// Texturovani
        texture = new OGLTexture2D(gl, "/textures/bricks.jpg");
        textureNormal = new OGLTexture2D(gl, "/textures/bricksn.png");
		textureHeight = new OGLTexture2D(gl, "/textures/bricksh.png");
        
        
        texture2 = new OGLTexture2D(gl, "/textures/Brick_OldDestroyed_1k_d.tga");
		textureNormal2 = new OGLTexture2D(gl, "/textures/Brick_OldDestroyed_1k_n.tga");
		textureHeight2 = new OGLTexture2D(gl, "/textures/Brick_OldDestroyed_1k_h.tga");
		
		texture3 = new OGLTexture2D(gl, "/textures/Ground_Dirt_1k_d.tga");
		textureNormal3 = new OGLTexture2D(gl, "/textures/Ground_Dirt_1k_n.tga");
		textureHeight3 = new OGLTexture2D(gl, "/textures/Ground_Dirt_1k_h.tga");
        
		texture4 = new OGLTexture2D(gl, "/textures/Concrete_sidewalk_1k_d.tga");
		textureNormal4 = new OGLTexture2D(gl, "/textures/Concrete_sidewalk_1k_n.tga");
		textureHeight4 = new OGLTexture2D(gl, "/textures/Concrete_sidewalk_1k_h.tga");                
		
		
		texSouthPark = new OGLTexture2D(gl, "/textures/south-park.jpg");
		texPikachu = new OGLTexture2D(gl, "/textures/pikachu.jpg");
		textureViewer = new OGLTexture2D.Viewer(gl); // vyhazuje error
		
		//gl.glEnable(GL2.GL_CULL_FACE);
	    //gl.glCullFace(GL2.GL_FRONT);
	    //gl.glFrontFace(GL2.GL_CW);
	}

	void createBuffers(GL2 gl) {
		buffers = GridFactory_stripV2.createOGLBuffers(gl, 101, 101);	
		
		float[] quad = { 1, -1, 1, 1, -1, 1, -1, -1 };
		
		OGLBuffers.Attrib[] attributesQuad = {
				new OGLBuffers.Attrib("inPosition", 2)};

		bufferQuad = new OGLBuffers(gl, quad, attributesQuad, null);
	}

	
	public void display(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
		
		// -/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		gl.glUseProgram(shaderProgram);
		renderTarget.bind();
		gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);																	
		lightPosition = new Vec3D(20.0, 20.0, 10.0);
		settingTextures();
		

		if (switchReflectorMode == REFLECTOR_ON) {
			lightPosition = cam.getPosition();
		} else {
			lightPosition = new Vec3D(20.0, 20.0, 10.0);
		}
		gl.glUniformMatrix4fv(locMatProj, 1, false, ToFloatArray.convert(proj), 0);
		gl.glUniformMatrix4fv(locMatView, 1, false, ToFloatArray.convert(cam.getViewMatrix()), 0);
		gl.glUniformMatrix4fv(locMat, 1, false, ToFloatArray.convert(cam.getViewMatrix().mul(proj)), 0);
		gl.glUniform3f(loc_lightPosition, (float) lightPosition.getX(), (float) lightPosition.getY(),
				(float) lightPosition.getZ());
		gl.glUniform1i(loc_lightMode, switchLightMode);
		gl.glUniform1i(loc_reflectorNormal, switchReflectorMode);
		gl.glUniform1i(loc_function_normal, switchFunctionType);
		buffers.draw(GL2.GL_TRIANGLE_STRIP, shaderProgram);		
						
		texDepth = renderTarget.getDepthTexture();
		texColor = renderTarget.getColorTexture(0);
		texNormal = renderTarget.getColorTexture(2);
		
		
		
		// -/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/
		// SOBEL EDGE DETECT - Normal and depth map
		gl.glUseProgram(shaderEdgeDetect);
		renderTargetEdgeDetect.bind();
		gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		texColor.bind(shaderEdgeDetect, "texColor", 2);
		texDepth.bind(shaderEdgeDetect, "texDepth", 1);
		texNormal.bind(shaderEdgeDetect, "texNormal", 0);
		bufferQuad.draw(GL2.GL_QUADS, shaderEdgeDetect);
		
		
		texDepthEdge = renderTargetEdgeDetect.getColorTexture(0);  // EDGE DETECTION ON DEPTH
		texNormalEdge = renderTargetEdgeDetect.getColorTexture(1); // EDGE DETECTION ON NORMAL
		texColorEdge = renderTargetEdgeDetect.getColorTexture(2);  // EDGE DETECTION ON COLOR BASE
		texCelShading = renderTargetEdgeDetect.getColorTexture(3);  // CEL SHADING
		
		
		// -/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/
		// POST PROCESS
		gl.glUseProgram(shaderProgramPost);				
		//gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);		// set the default render target (screen)
		//gl.glViewport(0, 0, width, height);
		renderTargetDestination.bind();
		gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		gl.glUniform1i(locSwitchShaderPost, switchShaderPost);		
		time += 0.1;
		gl.glUniform1f(locTime, time);				
		renderTarget.getColorTexture(0).bind(shaderProgramPost, "texture", 1); // use the result of the previous draw as a texture for the next
		renderTarget.getDepthTexture().bind(shaderProgramPost, "textureDepth", 0);		
		bufferQuad.draw(GL2.GL_QUADS, shaderProgramPost);	// draw the full-screen quad
		
		gl.glUseProgram(0);				
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);		// set the default render target (screen)
		gl.glViewport(0, 0, width, height);
		gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		//bufferQuad.draw(GL2.GL_QUADS, shaderProgramPost);	// draw the full-screen quad
		// -/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/-/
		//textRenderer.drawStr2D(glDrawable, 3, 3, "Original", WHITE_TEXT);
		textureSobol = renderTarget.getColorTexture(1);						// ziskani vystup s detekci hran pomoci sobelova operatoru
		textureViewer.view(textureSobol, -1, -0.5, 0.5, 1);				 	 // Detekce hran
		textRenderer.drawStr2D(glDrawable, 10, 260, "Sobel operator on texture", WHITE_TEXT);
		textureViewer.view(texColor, -1, -1, 0.5, 1); // ColorBuffer - normalni rezim
		textRenderer.drawStr2D(glDrawable, 10, 10, "Original", WHITE_TEXT);
		
		textureViewer.view(renderTargetDestination.getColorTexture(1), -0.5, -0.5, 0.5, 1);				 	 // Detekce hran
		textRenderer.drawStr2D(glDrawable, 260, 260, "Sobel Operator on RT", WHITE_TEXT);
		textureViewer.view(renderTargetDestination.getColorTexture(2), -0.5, -1, 0.5, 1); // ColorBuffer - normalni rezim
		textRenderer.drawStr2D(glDrawable, 260, 10, "Linearized depth map", BLACK_TEXT);
		
		textureViewer.view(renderTarget.getDepthTexture(), -1, -1, 1.005, 1.005); // WHITE BOX
		textureViewer.view(renderTargetDestination.getColorTexture(), 0.0, -1, 1, 1); // POST PROCESSING
		textureViewer.view(renderTarget.getDepthTexture(), 0.0, -1, 1.005, 1.005); // WHITE BOX
		
		
		textureViewer.view(texCelShading, -1.0, 0, 1, 1); 
		textRenderer.drawStr2D(glDrawable, 10, 2*275-30, "Cel shading", BLACK_TEXT);
		textRenderer.drawStr2D(glDrawable, 10, 2*275-30, "Cel shading", WHITE_TEXT);
		textureViewer.view(renderTarget.getDepthTexture(), -1.0, 0, 1.005, 1.005); // WHITE BOX

		
		// PRAVY HORNI ROH - RenderTargetEdgeDetect
		textureViewer.view(texNormal, 0.5, 0.0, 0.5, 1);				 	 // normalTexture
		textRenderer.drawStr2D(glDrawable, 760, 2*275-30, "Normal texture", WHITE_TEXT);
		textureViewer.view(texColorEdge, 0.5, 0.5, 0.5, 1); // depthTexture EDGES
		textRenderer.drawStr2D(glDrawable, 760, 760, "Edge detection from Color", WHITE_TEXT);
		textureViewer.view(texNormalEdge, 0.0, 0.0, 0.5, 1);// normalTexture EDGES
		textRenderer.drawStr2D(glDrawable, 2*275-30, 2*275-30, "Edge detection from Normal", WHITE_TEXT);		
		textureViewer.view(texDepthEdge, 0, 0.5, 0.5, 1); 
		textRenderer.drawStr2D(glDrawable, 2*275-30, 760, "Mix edge detection", WHITE_TEXT);
		
		
		
		
		
		
		drawStrings(glDrawable);
		normalShaderDrawString(glDrawable);							
	}		

	private void settingTextures() {
		switch (switchTexture) {
			case TEXTURE_BRICKS:
				texture.bind(shaderProgram, "tex", 2);
				textureNormal.bind(shaderProgram, "texNormal", 1);
				textureHeight.bind(shaderProgram, "texHeight", 0);
				break;
			case TEXTURE_OLD_BRICKS:
				texture2.bind(shaderProgram, "tex", 2);
				textureNormal2.bind(shaderProgram, "texNormal", 1);
				textureHeight2.bind(shaderProgram, "texHeight", 0);
				break;
			case TEXTURE_GROUND:
				texture3.bind(shaderProgram, "tex", 2);
				textureNormal3.bind(shaderProgram, "texNormal", 1);
				textureHeight3.bind(shaderProgram, "texHeight", 0);
				break;
			case TEXTURE_SIDEWALK:
				texture4.bind(shaderProgram, "tex", 2);
				textureNormal4.bind(shaderProgram, "texNormal", 1);
				textureHeight4.bind(shaderProgram, "texHeight", 0);
				break;
			case 5:
				texPikachu.bind(shaderProgram, "tex", 0);
				break;
			case 6:
				texSouthPark.bind(shaderProgram, "tex", 0);
				break;
		}

	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		this.width = width;
		this.height = height;
		proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.1, 1000.0);
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		ox = e.getX();
		oy = e.getY();
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		cam = cam.addAzimuth((double) Math.PI * (ox - e.getX()) / width)
				.addZenith((double) Math.PI * (e.getY() - oy) / width);
		ox = e.getX();
		oy = e.getY();
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_C:
			switchShaderPost++;
			if(switchShaderPost>10) switchShaderPost = 1;
			break;
		case KeyEvent.VK_W:
			cam = cam.forward(0.5);
			break;
		case KeyEvent.VK_D:
			cam = cam.right(0.5);
			break;
		case KeyEvent.VK_S:
			cam = cam.backward(0.5);
			break;
		case KeyEvent.VK_A:
			cam = cam.left(0.5);
			break;
		case KeyEvent.VK_CONTROL:
			cam = cam.down(1);
			break;
		case KeyEvent.VK_SHIFT:
			cam = cam.up(1);
			break;
		case KeyEvent.VK_SPACE:
			cam = cam.withFirstPerson(!cam.getFirstPerson());
			break;
		case KeyEvent.VK_G:
			cam = cam.mulRadius(0.9f);
			break;
		case KeyEvent.VK_H:
			cam = cam.mulRadius(1.1f);
			break;		
		case KeyEvent.VK_P:
			switchPolygonMode++;
			if(switchPolygonMode>2) switchPolygonMode = 1;
			break;
		case KeyEvent.VK_L:
			switchLightMode++;
			if(switchLightMode>10) switchLightMode = 1;
			if(switchTexture>4){
				if(switchLightMode==7 || switchLightMode==8){
					switchLightMode=9;
				}
			}
			break;
		case KeyEvent.VK_K:
			switchLightMode--;
			if(switchLightMode<1) switchLightMode = 10;
			if(switchTexture>4){
				if(switchLightMode==7 || switchLightMode==8){
					switchLightMode=9;
				}
			}
			break;					
		case KeyEvent.VK_U:
			if(switchShaderType == NORMAL_TYPE){
				if(switchFunctionType > 9) switchFunctionType=1;
				else switchFunctionType++;			
			} else {
				if(switchFunctionType > 1) switchFunctionType=1;
				else switchFunctionType++;			
			}
			
			break;
		case KeyEvent.VK_I:
			switchTexture++;
			if(switchTexture>6) switchTexture = 1;
			System.out.println(switchTexture);
			break;
		case KeyEvent.VK_M:
			cam = cam.withPosition(new Vec3D(0,0,32))
			//.withAzimuth(Math.PI * 1.25)
			//.withZenith(Math.PI * -0.125);
					.withAzimuth(Math.PI * 0.5)
					.withZenith(Math.PI * -0.5);
			break;
		case KeyEvent.VK_N:
			cam = cam.withPosition(eyePosition)
			.withAzimuth(Math.PI * 1.25)
			.withZenith(Math.PI * -0.125);
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void dispose(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
		gl.glDeleteProgram(shaderProgram);
	}
	
	private void drawStrings(GLAutoDrawable glDrawable){
		String process= "[C] - Post Processing: ";
		String text = "";
		switch(switchShaderPost){
			case PROCESS_BLURE:
				text ="Blure"; 
				break;
			case PROCESS_GRAYSCALE_EYE:
				text ="Grayscale colors for eye"; 
				break;
			case PROCESS_GRAYSCALE:
				text ="Grayscale colors"; 
				break;
			case PROCESS_INVERSE:
				text ="Inverse colors"; 
				break;
			case PROCESS_SOBEL:
				text ="Sobel operator for detection edges + Cel shading"; 
				break;
			case PROCESS_DEPTH:
				text ="Depth map"; 
				break;
			case PROCESS_BOXES:
				text ="Flow boxes"; 
				break;
			case PROCESS_FISH_EYE:
				text ="Fish_Eye"; 
				break;
			case PROCESS_DEPTH_LINEARIZED:
				text ="Depth map - Linearized"; 
				break;
			case 10:
				text =" ---- "; 
				break;
		}
		if(switchShaderPost == 6 || switchShaderPost == 9 || switchShaderPost == 4){
			textRenderer.drawStr2D(glDrawable, 2*260, 25, "[ I ] - Change texture", BLACK_TEXT);
			textRenderer.drawStr2D(glDrawable, 860, 480, "Camera pos(0,0,32) - [M] ", BLACK_TEXT);
			textRenderer.drawStr2D(glDrawable, 849, 460, "Camera pos(20,20,10)- [N]", BLACK_TEXT);
			textRenderer.drawStr2D(glDrawable, 2*260, 10, process + text, BLACK_TEXT);
		} else {
			textRenderer.drawStr2D(glDrawable, 2*260, 25, "[ I ] - Change texture", WHITE_TEXT);
			textRenderer.drawStr2D(glDrawable, 860, 480, "Camera pos(0,0,32) - [M] ", WHITE_TEXT);
			textRenderer.drawStr2D(glDrawable, 849, 460, "Camera pos(20,20,10)- [N]", WHITE_TEXT);
			textRenderer.drawStr2D(glDrawable, 2*260, 10, process + text, WHITE_TEXT);
		}
				
	}
	
	private void normalShaderDrawString(GLAutoDrawable glDrawable){
		textRenderer.drawStr2D(glDrawable, 3, height - 20, "Shader: Per Pixel", WHITE_TEXT);
		switch(switchFunctionType){
			case FUNCTION_CARTESIAN_1:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "[U] - Model:  Cartesian - cos(sqrt(x*x + y*y))", WHITE_TEXT);
				break;
			case FUNCTION_CARTESIAN_2:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "[U] - Model:  Cartesian - 'Navy Sail' - own", WHITE_TEXT);
				break;
			case FUNCTION_CARTESIAN_3:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "[U] - Model:  Cartesian - 'Function 3' - own", WHITE_TEXT);
				break;
			case FUNCTION_GRID:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "[U] - Model:  GRID", WHITE_TEXT);
				break;
			case FUNCTION_CYLINDER_1:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "[U] - Model:  Cylinder - Sombrero", WHITE_TEXT);
				break;
			case FUNCTION_CYLINDER_2:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "[U] - Model:  Cylinder - Tent'", WHITE_TEXT);
				break;
			case FUNCTION_CYLINDER_3:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "[U] - Model:  Cylinder - 'Function 3' - own", WHITE_TEXT);
				break;
			case FUNCTION_SFERIC_1:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "[U] - Model:  Sferic - Sphere", WHITE_TEXT);
				break;
			case FUNCTION_SFERIC_2:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "[U] - Model:  Sferic - Elephant head'", WHITE_TEXT);
				break;
			case FUNCTION_SFERIC_3:
				textRenderer.drawStr2D(glDrawable, 3, height - 35, "[U] - Model:  Sferic - 'Function 3' - own", WHITE_TEXT);
				break;
		}
		
		switch(switchLightMode){
			case LIGHT_MODE_POSITION:
				textRenderer.drawStr2D(glDrawable, 3, height - 50, "[K], [L] - Light mode:  Position", WHITE_TEXT);
				break;
			case LIGHT_MODE_COLOR:
				textRenderer.drawStr2D(glDrawable, 3, height - 50, "[K], [L] - Light mode:  Color", WHITE_TEXT);
				break;
			case LIGHT_MODE_TEXTURE:
				textRenderer.drawStr2D(glDrawable, 3, height - 50, "[K], [L] - Light mode:  Texture", WHITE_TEXT);
				break;
			case LIGHT_MODE_DIFFUSION:
				textRenderer.drawStr2D(glDrawable, 3, height - 50, "[K], [L] - Light mode:  Diffusion", WHITE_TEXT);
				textRenderer.drawStr2D(glDrawable, 3, height - 65, "      Attenuation:  No", WHITE_TEXT);
				break;
			case LIGHT_MODE_BLINN_PHONG_HALF_VECTOR:
				textRenderer.drawStr2D(glDrawable, 3, height - 50, "[K], [L] - Light mode:  Blinn-Phong - Half vector", WHITE_TEXT);
				drawStringReflector(glDrawable);
				break;				
			case LIGHT_MODE_BLINN_PHONG_REFLECT_VECTOR:
				textRenderer.drawStr2D(glDrawable, 3, height - 50, "[K], [L] - Light mode:  Blinn-Phong - Reflect vector", WHITE_TEXT);
				drawStringReflector(glDrawable);
				break;
			case LIGHT_MODE_TEXTURE_NORMAL:
				textRenderer.drawStr2D(glDrawable, 3, height - 50, "[K], [L] - Light mode:  Normal mapping", WHITE_TEXT);
				textRenderer.drawStr2D(glDrawable, 3, height - 110, "[I] - Change texture", WHITE_TEXT);
				drawStringReflector(glDrawable);
				break;
			case LIGHT_MODE_TEXTURE_PARALLAX:
				textRenderer.drawStr2D(glDrawable, 3, height - 50, "[K], [L] - Light mode:  Parallax mapping", WHITE_TEXT);
				textRenderer.drawStr2D(glDrawable, 3, height - 110, "[I] - Change texture", WHITE_TEXT);
				drawStringReflector(glDrawable);
				break;
			case LIGHT_MODE_TONE_SHADING:
				textRenderer.drawStr2D(glDrawable, 3, height - 50, "[K], [L] - Light mode:  Tone shading v1.0", WHITE_TEXT);
				break;
			case LIGHT_MODE_TONE_SHADING_V2:
				textRenderer.drawStr2D(glDrawable, 3, height - 50, "[K], [L] - Light mode:  Tone shading - School v.", WHITE_TEXT);
				break;
		}
		
		
	}
	
	private void drawStringReflector(GLAutoDrawable glDrawable){
		if(switchReflectorMode == REFLECTOR_ON)	textRenderer.drawStr2D(glDrawable, 3, height - 80, "R - Reflector:  On", WHITE_TEXT);
		else 									textRenderer.drawStr2D(glDrawable, 3, height - 80, "R - Reflector:  Off", WHITE_TEXT);
		textRenderer.drawStr2D(glDrawable, 3, height - 95, "      Attenuation:  Yes", WHITE_TEXT);
		
	}		
}