package com.shadyhippo.LWGame.level;

import static org.lwjgl.glfw.GLFW.*;

import com.shadyhippo.LWGame.graphics.Shader;
import com.shadyhippo.LWGame.graphics.Texture;
import com.shadyhippo.LWGame.graphics.VertexArray;
import com.shadyhippo.LWGame.input.Input;
import com.shadyhippo.LWGame.math.Matrix4f;
import com.shadyhippo.LWGame.math.Vector3f;

public class Bird {
	
	private float SIZE = 1.0f;
	private VertexArray mesh;
	private Texture texture;
	
	
	private Vector3f position = new Vector3f();
	private float rot;
	private float delta = 0.0f;
	private boolean jump = false;
	
	private boolean control = true;
	
	public Bird() {
		float[] vertices = new float[] {
			-SIZE / 2.0f, -SIZE / 2.0f, 0.15f,
			-SIZE / 2.0f,  SIZE / 2.0f, 0.15f,
			 SIZE / 2.0f,  SIZE / 2.0f, 0.15f,
			 SIZE / 2.0f, -SIZE / 2.0f, 0.15f
		};
		
		byte[] indices = new byte[] {
			0, 1, 2,
			2, 3, 0
		};
		
		float[] tcs = new float[] {
			0, 1, 
			0, 0, 
			1, 0,
			1, 1
		};
		
		mesh = new VertexArray(vertices, indices, tcs);
		texture = new Texture("res/bird.png");
	}
	
	public void update() {
		position.y -= delta;
		if (Input.isKeyPressed(GLFW_KEY_SPACE) && jump != true && control) {
			jump = true;
			delta = -0.18f;
		} else if (!Input.isKeyPressed(GLFW_KEY_SPACE)) {
			jump = false;
			delta+= 0.01f;
		} else {
			delta += 0.01f;
		}
		rot = -delta * (150.0f);
	}
	

	public void fall() {
		delta -= 0.15f;
	}
	
	public void render() {
		Shader.BIRD.enable();
		Shader.BIRD.setUniforMat4f("ml_matrix",  Matrix4f.translate(position).multiply(Matrix4f.rotate(rot)));
		texture.bind();
		mesh.render();
		Shader.BIRD.disable();
	}
	
	public float getY() {
		return position.y;
	}

	public float getSize() {
		return SIZE;
	}
	
	public boolean getControl() {
		return control;
	}
	
	public void setControl(boolean newCont) {
		control = newCont;
	}
}
