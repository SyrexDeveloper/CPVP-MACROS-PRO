package dev.priyme.legitcrystalmacro.macro;

import dev.priyme.legitcrystalmacro.config.MacroConfig;
import dev.priyme.legitcrystalmacro.mixin.TrajectoryFovAccessor;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public final class TrajectoryRenderer {
   private TrajectoryRenderer() {
   }

   public static void register() {
      HudRenderCallback.EVENT.register((HudRenderCallback)(context, ticks) -> render(context, ticks.getTickProgress(false)));
   }

   private static void render(DrawContext context, float delta) {
      MacroConfig config = MacroConfig.get();
      MinecraftClient client = MinecraftClient.getInstance();
      if (config != null
         && config.trajectoriesEnabled
         && client.player != null
         && client.world != null
         && !client.options.hudHidden
         && client.currentScreen == null) {
         Camera camera = client.gameRenderer.getCamera();
         if (camera.isReady()) {
            Vec3d origin = camera.getCameraPos();
            float fov = ((TrajectoryFovAccessor)client.gameRenderer).trajectoryFov(camera, delta, true);
            Matrix4f matrix = new Matrix4f(client.gameRenderer.getBasicProjectionMatrix(fov)).rotate(new Quaternionf(camera.getRotation()).conjugate());
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            context.enableScissor(0, 0, width, height);

            try {
               for (Trajectories.Path path : Trajectories.paths(client, camera, delta)) {
                  List<Vec3d> points = Trajectories.smooth(path.points());
                  double[] previous = null;

                  for (Vec3d point : points) {
                     double[] projected = project(matrix, point.subtract(origin), width, height);
                     if (previous != null && projected != null) {
                        line(context, previous, projected, 1.35F, path.color());
                     }

                     previous = projected;
                  }

                  if (config.trajectoriesShowLanding && path.landed()) {
                     double[] landing = project(matrix, path.points().getLast().subtract(origin), width, height);
                     if (landing != null) {
                        int color = alpha(path.color(), 0.6);
                        line(context, new double[]{landing[0] - 4.0, landing[1]}, new double[]{landing[0] + 4.0, landing[1]}, 1.35F, color);
                        line(context, new double[]{landing[0], landing[1] - 4.0}, new double[]{landing[0], landing[1] + 4.0}, 1.35F, color);
                     }
                  }

                  if (path.target() != null) {
                     targetBox(
                        context,
                        matrix,
                        origin,
                        path.target().getBoundingBox().expand(path.target().getTargetingMargin()),
                        width,
                        height,
                        alpha(path.color(), 0.9)
                     );
                  }
               }
            } finally {
               context.disableScissor();
            }
         }
      }
   }

   static double[] project(Matrix4f matrix, Vec3d point, int width, int height) {
      Vector4f clip = matrix.transform(new Vector4f((float)point.x, (float)point.y, (float)point.z, 1.0F));
      if (Float.isFinite(clip.x) && Float.isFinite(clip.y) && Float.isFinite(clip.z) && Float.isFinite(clip.w) && !(clip.w < 0.05F)) {
         float x = clip.x / clip.w;
         float y = clip.y / clip.w;
         float z = clip.z / clip.w;
         return Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z) && !(z < -1.0F) && !(z > 1.0F)
            ? new double[]{(x * 0.5 + 0.5) * width, (-y * 0.5 + 0.5) * height}
            : null;
      } else {
         return null;
      }
   }

   private static void targetBox(DrawContext context, Matrix4f matrix, Vec3d origin, Box box, int width, int height, int color) {
      double left = Double.POSITIVE_INFINITY;
      double top = left;
      double right = Double.NEGATIVE_INFINITY;
      double bottom = right;

      for (int i = 0; i < 8; i++) {
         Vec3d corner = new Vec3d((i & 1) == 0 ? box.minX : box.maxX, (i & 2) == 0 ? box.minY : box.maxY, (i & 4) == 0 ? box.minZ : box.maxZ);
         double[] point = project(matrix, corner.subtract(origin), width, height);
         if (point == null) {
            return;
         }

         left = Math.min(left, point[0]);
         top = Math.min(top, point[1]);
         right = Math.max(right, point[0]);
         bottom = Math.max(bottom, point[1]);
      }

      double[] a = new double[]{left, top};
      double[] b = new double[]{right, top};
      double[] c = new double[]{right, bottom};
      double[] d = new double[]{left, bottom};
      line(context, a, b, 1.5F, color);
      line(context, b, c, 1.5F, color);
      line(context, c, d, 1.5F, color);
      line(context, d, a, 1.5F, color);
   }

   private static int alpha(int color, double factor) {
      return (int)Math.round((color >>> 24) * factor) << 24 | color & 16777215;
   }

   private static void line(DrawContext context, double[] from, double[] to, float thickness, int color) {
      double dx = to[0] - from[0];
      double dy = to[1] - from[1];
      double length = Math.hypot(dx, dy);
      if (Double.isFinite(length) && !(length < 0.01) && !(length > 100000.0)) {
         float angle = (float)Math.atan2(dy, dx);
         context.getMatrices().pushMatrix();
         context.getMatrices().translate((float)from[0], (float)from[1]);
         context.getMatrices().rotate(angle);
         context.getMatrices().translate(0.0F, -thickness / 2.0F);
         context.getMatrices().scale((float)length, thickness);
         context.fill(0, 0, 1, 1, color);
         context.getMatrices().popMatrix();
      }
   }
}
