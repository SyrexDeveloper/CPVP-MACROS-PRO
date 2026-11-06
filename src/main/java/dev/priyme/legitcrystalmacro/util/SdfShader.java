package dev.priyme.legitcrystalmacro.util;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class SdfShader {
   private static int programId = -1;
   private static int uSizeLoc = -1;
   private static int uRadiusLoc = -1;
   private static int uSoftnessLoc = -1;
   private static int uColorLoc = -1;
   private static int uBorderWidthLoc = -1;
   private static int uBorderColorLoc = -1;
   private static boolean initialized = false;
   private static boolean initFailed = false;
   private static final String VERTEX_SHADER = "#version 150\nin vec3 Position;\nuniform mat4 ProjMat;\nuniform mat4 ModelViewMat;\nout vec2 v_localPos;\nvoid main() {\n    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);\n}\n";
   private static final String FRAGMENT_SHADER = "#version 150\nuniform vec2 u_size;\nuniform float u_radius;\nuniform float u_softness;\nuniform vec4 u_color;\nuniform float u_borderWidth;\nuniform vec4 u_borderColor;\nin vec2 v_localPos;\nout vec4 fragColor;\n\nfloat roundedBoxSDF(vec2 p, vec2 halfSize, float r) {\n    vec2 q = abs(p) - halfSize + vec2(r);\n    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;\n}\n\nvoid main() {\n    float outerDist = roundedBoxSDF(v_localPos, u_size, u_radius);\n    float outerMask = 1.0 - smoothstep(0.0, u_softness, outerDist);\n    if (outerMask <= 0.0) {\n        discard;\n    }\n    vec4 result = u_color;\n    if (u_borderWidth > 0.0) {\n        vec2 innerHalfSize = u_size - vec2(u_borderWidth);\n        float innerRadius = max(u_radius - u_borderWidth, 0.0);\n        float innerDist = roundedBoxSDF(v_localPos, innerHalfSize, innerRadius);\n        float innerMask = 1.0 - smoothstep(0.0, u_softness, innerDist);\n        float borderMask = clamp(outerMask - innerMask, 0.0, 1.0);\n        result = mix(u_color, u_borderColor, borderMask);\n    }\n    fragColor = vec4(result.rgb, result.a * outerMask);\n}\n";

   private SdfShader() {
   }

   public static void init() {
      if (!initialized && !initFailed) {
         try {
            int vertId = GL20.glCreateShader(35633);
            GL20.glShaderSource(
               vertId,
               "#version 150\nin vec3 Position;\nuniform mat4 ProjMat;\nuniform mat4 ModelViewMat;\nout vec2 v_localPos;\nvoid main() {\n    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);\n}\n"
            );
            GL20.glCompileShader(vertId);
            if (GL20.glGetShaderi(vertId, 35713) == 0) {
               System.err.println("SDF Vertex Shader compile failed: " + GL20.glGetShaderInfoLog(vertId));
               initFailed = true;
               return;
            }

            int fragId = GL20.glCreateShader(35632);
            GL20.glShaderSource(
               fragId,
               "#version 150\nuniform vec2 u_size;\nuniform float u_radius;\nuniform float u_softness;\nuniform vec4 u_color;\nuniform float u_borderWidth;\nuniform vec4 u_borderColor;\nin vec2 v_localPos;\nout vec4 fragColor;\n\nfloat roundedBoxSDF(vec2 p, vec2 halfSize, float r) {\n    vec2 q = abs(p) - halfSize + vec2(r);\n    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;\n}\n\nvoid main() {\n    float outerDist = roundedBoxSDF(v_localPos, u_size, u_radius);\n    float outerMask = 1.0 - smoothstep(0.0, u_softness, outerDist);\n    if (outerMask <= 0.0) {\n        discard;\n    }\n    vec4 result = u_color;\n    if (u_borderWidth > 0.0) {\n        vec2 innerHalfSize = u_size - vec2(u_borderWidth);\n        float innerRadius = max(u_radius - u_borderWidth, 0.0);\n        float innerDist = roundedBoxSDF(v_localPos, innerHalfSize, innerRadius);\n        float innerMask = 1.0 - smoothstep(0.0, u_softness, innerDist);\n        float borderMask = clamp(outerMask - innerMask, 0.0, 1.0);\n        result = mix(u_color, u_borderColor, borderMask);\n    }\n    fragColor = vec4(result.rgb, result.a * outerMask);\n}\n"
            );
            GL20.glCompileShader(fragId);
            if (GL20.glGetShaderi(fragId, 35713) == 0) {
               System.err.println("SDF Fragment Shader compile failed: " + GL20.glGetShaderInfoLog(fragId));
               initFailed = true;
               return;
            }

            programId = GL20.glCreateProgram();
            GL20.glAttachShader(programId, vertId);
            GL20.glAttachShader(programId, fragId);
            GL20.glLinkProgram(programId);
            if (GL20.glGetProgrami(programId, 35714) == 0) {
               System.err.println("SDF Program link failed: " + GL20.glGetProgramInfoLog(programId));
               initFailed = true;
               return;
            }

            uSizeLoc = GL20.glGetUniformLocation(programId, "u_size");
            uRadiusLoc = GL20.glGetUniformLocation(programId, "u_radius");
            uSoftnessLoc = GL20.glGetUniformLocation(programId, "u_softness");
            uColorLoc = GL20.glGetUniformLocation(programId, "u_color");
            uBorderWidthLoc = GL20.glGetUniformLocation(programId, "u_borderWidth");
            uBorderColorLoc = GL20.glGetUniformLocation(programId, "u_borderColor");
            initialized = true;
         } catch (Throwable var2) {
            var2.printStackTrace();
            initFailed = true;
         }
      }
   }

   public static boolean isAvailable() {
      if (!initialized && !initFailed) {
         init();
      }

      return initialized;
   }

   public static void drawRoundedRect(
      DrawContext context, float x, float y, float width, float height, float radius, int color, float borderWidth, int borderColor
   ) {
      if (isAvailable()) {
         float halfW = width / 2.0F;
         float halfH = height / 2.0F;
         float r = (color >> 16 & 0xFF) / 255.0F;
         float g = (color >> 8 & 0xFF) / 255.0F;
         float b = (color & 0xFF) / 255.0F;
         float a = (color >> 24 & 0xFF) / 255.0F;
         float br = (borderColor >> 16 & 0xFF) / 255.0F;
         float bg = (borderColor >> 8 & 0xFF) / 255.0F;
         float bb = (borderColor & 0xFF) / 255.0F;
         float ba = (borderColor >> 24 & 0xFF) / 255.0F;
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         GL20.glUseProgram(programId);
         GL20.glUniform2f(uSizeLoc, halfW, halfH);
         GL20.glUniform1f(uRadiusLoc, radius);
         GL20.glUniform1f(uSoftnessLoc, 1.0F);
         GL20.glUniform4f(uColorLoc, r, g, b, a);
         GL20.glUniform1f(uBorderWidthLoc, borderWidth);
         GL20.glUniform4f(uBorderColorLoc, br, bg, bb, ba);
         context.fill((int)x, (int)y, (int)(x + width), (int)(y + height), color);
         GL20.glUseProgram(0);
      }
   }
}
