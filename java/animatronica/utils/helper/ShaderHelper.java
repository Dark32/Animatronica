package animatronica.utils.helper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.Level;

import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

import animatronica.utils.config.AnimatronicaConfiguration;
import animatronica.utils.event.ClientTickHandler;
import cpw.mods.fml.common.FMLLog;



public final class ShaderHelper {

	private static final int VERT = ARBVertexShader.GL_VERTEX_SHADER_ARB;
	private static final int FRAG = ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;
	public static final String SHADER_FILM_GRAIN = "/assets/animatronica/shader/film_grain.frag";

	public static int pylonGlow = 0;
	public static int enchanterRune = 0;
	public static int manaPool = 0;
	public static int doppleganger = 0;
	public static int halo = 0;
	public static int dopplegangerBar = 0;
	public static int terraPlateRune = 0;
	public static int filmGrain = 0;

	public static void initShaders() {
		if(!useShaders())
			return;

		filmGrain = createProgram(null, SHADER_FILM_GRAIN);
	}

	public static void useShader(int shader, ShaderCallback callback) {
		if(!useShaders())
			return;

		ARBShaderObjects.glUseProgramObjectARB(shader);

		if(shader != 0) {
			int time = ARBShaderObjects.glGetUniformLocationARB(shader, "time");
			ARBShaderObjects.glUniform1iARB(time, ClientTickHandler.ticksInGame);

			if(callback != null)
				callback.call(shader);
		}
	}

	public static void useShader(int shader) {
		useShader(shader, null);
	}

	public static void releaseShader() {
		useShader(0);
	}

	public static boolean useShaders() {
		return AnimatronicaConfiguration.useShaders && OpenGlHelper.shadersSupported;
	}

	// Most of the code taken from the LWJGL wiki
	// http://lwjgl.org/wiki/index.php?title=GLSL_Shaders_with_LWJGL

	private static int createProgram(String vert, String frag) {
		int vertId = 0, fragId = 0, program = 0;
		if(vert != null)
			vertId = createShader(vert, VERT);
		if(frag != null)
			fragId = createShader(frag, FRAG);

		program = ARBShaderObjects.glCreateProgramObjectARB();
		if(program == 0)
			return 0;

		if(vert != null)
			ARBShaderObjects.glAttachObjectARB(program, vertId);
		if(frag != null)
			ARBShaderObjects.glAttachObjectARB(program, fragId);

		ARBShaderObjects.glLinkProgramARB(program);
		if(ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
			FMLLog.log(Level.ERROR, getLogInfo(program));
			return 0;
		}

		ARBShaderObjects.glValidateProgramARB(program);
		if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
			FMLLog.log(Level.ERROR, getLogInfo(program));
			return 0;
		}

		return program;
	}

	private static int createShader(String filename, int shaderType){
		int shader = 0;
		try {
			shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

			if(shader == 0)
				return 0;

			ARBShaderObjects.glShaderSourceARB(shader, readFileAsString(filename));
			ARBShaderObjects.glCompileShaderARB(shader);

			if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
				throw new RuntimeException("Error creating shader: " + getLogInfo(shader));

			return shader;
		}
		catch(Exception e) {
			ARBShaderObjects.glDeleteObjectARB(shader);
			e.printStackTrace();
			return -1;
		}
	}

	private static String getLogInfo(int obj) {
		return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}

	private static String readFileAsString(String filename) throws Exception {
		StringBuilder source = new StringBuilder();
		InputStream in = ShaderHelper.class.getResourceAsStream(filename);
		Exception exception = null;
		BufferedReader reader;

		if(in == null)
			return "";

		try {
			reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

			Exception innerExc= null;
			try {
				String line;
				while((line = reader.readLine()) != null)
					source.append(line).append('\n');
			} catch(Exception exc) {
				exception = exc;
			} finally {
				try {
					reader.close();
				} catch(Exception exc) {
					if(innerExc == null)
						innerExc = exc;
					else exc.printStackTrace();
				}
			}

			if(innerExc != null)
				throw innerExc;
		} catch(Exception exc) {
			exception = exc;
		} finally {
			try {
				in.close();
			} catch(Exception exc) {
				if(exception == null)
					exception = exc;
				else exc.printStackTrace();
			}

			if(exception != null)
				throw exception;
		}

		return source.toString();
	}

}
