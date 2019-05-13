package com.shadyhippo.LWGame;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import com.shadyhippo.LWGame.graphics.Shader;
import com.shadyhippo.LWGame.input.Input;
import com.shadyhippo.LWGame.level.Level;
import com.shadyhippo.LWGame.math.Matrix4f;

public class Main implements Runnable {
	
	private int width = 1280;
	private int height = 720;
	
	private Thread thread;
	public boolean running = false;
	
	private long window;
	
	private Level level;
	
	public void run() {
		init();
		
		long lastTime = System.nanoTime();
		double delta = 0.0;
		double ns = 1000000000.0 / 60.0;
		int updates = 0;
		int frames = 0;
		long timer = System.currentTimeMillis();
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			if (delta >= 1.0) {
				update();
				updates++;
				delta--;
			}
			render();
			frames++;
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				glfwSetWindowTitle(window, "Flappy | Updates: " + updates + " | FPS: " + frames);
				updates = 0;
				frames = 0;
			}
			if (glfwWindowShouldClose(window)) {
				running = false;
			}
		}
		
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	private void init() {
		//setup an error callback. The default prints whatever is in System.err
		GLFWErrorCallback.createPrint(System.err).set();
		
		//inits and if it doesn't work handles it
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}
		
		//configuring GLFW
		glfwDefaultWindowHints(); //optional, not really sure why it's doing this
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); //keeps the window hidden after creation?
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); //it's resizable
		
		//creating window
		window = glfwCreateWindow(width, height, "Flappy", NULL, NULL);
		if (window == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}
		
//		//setup key callback. called every key press, release, or repeat
//		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
//			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
//				glfwSetWindowShouldClose(window, true);
//			}
//		});

		//see Input class
		glfwSetKeyCallback(window, new Input());
		
		// Get the thread stack and push a new frame (this is the best way according to memory management
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically
		
		//puts the OpenGL context onto this thread
		glfwMakeContextCurrent(window);
		//enable v-sync with 1, disable with 0
		glfwSwapInterval(1);
		//Make window visible
		glfwShowWindow(window);
		
		//gives LWJGL access to the OpenGL context
		GL.createCapabilities();
		
		
		//openGl init stuffs
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glEnable(GL_DEPTH_TEST);
		glActiveTexture(GL_TEXTURE1);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		System.out.println("OpenGL: " + glGetString(GL_VERSION));
		Shader.loadAll();
		
		Matrix4f pr_matrix = Matrix4f.orthographic(-10.0f, 10.0f, -10.0f * 9.0f / 16.0f, 10.0f * 9.0f / 16.0f, -1.0f, 1.0f);
		Shader.BG.setUniforMat4f("pr_matrix", pr_matrix);
		Shader.BG.setUniform1i("tex", 1);
		
		Shader.BIRD.setUniforMat4f("pr_matrix", pr_matrix);
		Shader.BIRD.setUniform1i("tex", 1);
		
		Shader.PIPE.setUniforMat4f("pr_matrix", pr_matrix);
		Shader.PIPE.setUniform1i("tex", 1);
				
		level = new Level();
		
	}
	
	public void start() {
		running = true;
		thread = new Thread(this, "Game");
		thread.start();
	}
	
	private void update() {
		glfwPollEvents();
		level.update();
		if (level.isGameOver()) {
			level = new Level();
		}
	}
	
	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		level.render();
		int err = glGetError();
		if (err != GL_NO_ERROR) {
			System.out.println(err);
		}
		glfwSwapBuffers(window);
	}
	
	public static void main(String[] args) {
		new Main().start();
	}
}
