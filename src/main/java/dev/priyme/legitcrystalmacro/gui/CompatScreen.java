package dev.priyme.legitcrystalmacro.gui;

import dev.priyme.legitcrystalmacro.util.KeyInput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.StyleSpriteSource.Font;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

public abstract class CompatScreen extends Screen {
   public static final Identifier FONT_REGULAR = Identifier.of("legitcrystalmacro", "heading");
   public static final Identifier FONT_BOLD = Identifier.of("legitcrystalmacro", "heading_bold");
   public static final Identifier FONT_MEDIUM = Identifier.of("legitcrystalmacro", "heading_medium");
   public static final Identifier FONT_HEADING = Identifier.of("legitcrystalmacro", "heading_title");
   public static final Identifier FONT_SMALL = Identifier.of("legitcrystalmacro", "heading_small");
   public static final Identifier FONT_SMALL_BOLD = Identifier.of("legitcrystalmacro", "heading_small_bold");
   public static final Identifier FONT_ICONS = Identifier.of("legitcrystalmacro", "icons");
   public static final Identifier TEX_R14 = Identifier.of("legitcrystalmacro", "textures/gui/rect_r14.png");
   public static final Identifier TEX_R9 = Identifier.of("legitcrystalmacro", "textures/gui/rect_r9.png");
   public static final Identifier TEX_R7 = Identifier.of("legitcrystalmacro", "textures/gui/rect_r7.png");
   public static final Identifier TEX_R5 = Identifier.of("legitcrystalmacro", "textures/gui/rect_r5.png");
   public static final Identifier TEX_R4 = Identifier.of("legitcrystalmacro", "textures/gui/rect_r4.png");
   public static final Identifier TEX_R3 = Identifier.of("legitcrystalmacro", "textures/gui/rect_r3.png");
   public static final Identifier TEX_R2 = Identifier.of("legitcrystalmacro", "textures/gui/rect_r2.png");
   public static final Identifier TEX_PILL = Identifier.of("legitcrystalmacro", "textures/gui/rect_pill.png");
   public static final Identifier TEX_BORDER_R9 = Identifier.of("legitcrystalmacro", "textures/gui/border_r9.png");
   public static final Identifier TEX_BORDER_R7 = Identifier.of("legitcrystalmacro", "textures/gui/border_r7.png");
   public static final Identifier TEX_SHADOW = Identifier.of("legitcrystalmacro", "textures/gui/shadow_panel.png");
   public static final Identifier TEX_LOGO = Identifier.of("legitcrystalmacro", "textures/gui/logo.png");
   private static final Identifier TEX_ICONS = Identifier.of("legitcrystalmacro", "textures/gui/crimson-icons.png");
   private static final Identifier TEX_CARD_BODY = Identifier.of("legitcrystalmacro", "textures/gui/card_body.png");
   private static final Identifier TEX_CARD_RING = Identifier.of("legitcrystalmacro", "textures/gui/card_ring.png");
   private static final Identifier TEX_TILE_BODY = Identifier.of("legitcrystalmacro", "textures/gui/tile_body.png");
   private static final Identifier TEX_CONTROLS = Identifier.of("legitcrystalmacro", "textures/gui/controls.png");
   private static final Identifier TEX_PIXEL_LOGO = Identifier.of("legitcrystalmacro", "textures/gui/logo_pixel.png");
   private static final Identifier[] SPRITES = Arrays.stream(NineSliceSprite.values())
      .map(v -> Identifier.of("legitcrystalmacro", "textures/gui/" + v.textureName + ".png"))
      .toArray(Identifier[]::new);
   private final CompatScreen.YarnContextWrapper contextWrapper = new CompatScreen.YarnContextWrapper();

   public static Identifier getTextureId(NineSliceSprite sprite) {
      return SPRITES[(sprite == null ? NineSliceSprite.CARD : sprite).ordinal()];
   }

   protected CompatScreen() {
      super(Text.literal("ClickGUI"));
   }

   public int getScreenWidth() {
      return this.width;
   }

   public int getScreenHeight() {
      return this.height;
   }

   protected void setSearchInputFocused(boolean focused) {
   }

   protected String nativeKeyName(int key) {
      return KeyInput.keyName(key);
   }

   public void closeScreen() {
      this.close();
   }

   public boolean shouldPause() {
      return false;
   }

   public abstract void renderContent(GuiGraphicsContext var1, int var2, int var3, float var4);

   public abstract boolean onMouseClicked(int var1, int var2, int var3);

   public abstract boolean onMouseReleased(int var1, int var2, int var3);

   public abstract boolean onMouseDragged(int var1, int var2, int var3, double var4, double var6);

   public abstract boolean onMouseScrolled(int var1, int var2, double var3, double var5);

   public abstract boolean onKeyPressed(int var1, int var2, int var3);

   public abstract boolean onCharTyped(char var1);

   protected void init() {
      TextureManager textures = MinecraftClient.getInstance().getTextureManager();

      for (Identifier texture : SPRITES) {
         textures.getTexture(texture);
      }

      for (Identifier texture : new Identifier[]{TEX_ICONS, TEX_CARD_BODY, TEX_CARD_RING, TEX_TILE_BODY, TEX_CONTROLS, TEX_PIXEL_LOGO}) {
         textures.getTexture(texture);
      }

      this.contextWrapper.set(null, this.textRenderer);
      this.onGuiInitialized(this.contextWrapper);
   }

   protected void onGuiInitialized(GuiGraphicsContext context) {
   }

   public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
      this.contextWrapper.set(context, this.textRenderer);
      this.renderContent(this.contextWrapper, mouseX, mouseY, deltaTicks);
   }

   public boolean mouseClicked(Click click, boolean doubled) {
      return this.onMouseClicked((int)click.x(), (int)click.y(), click.button());
   }

   public boolean mouseReleased(Click click) {
      return this.onMouseReleased((int)click.x(), (int)click.y(), click.button());
   }

   public boolean mouseDragged(Click click, double offsetX, double offsetY) {
      return this.onMouseDragged((int)click.x(), (int)click.y(), click.button(), offsetX, offsetY) ? true : super.mouseDragged(click, offsetX, offsetY);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      return this.onMouseScrolled((int)mouseX, (int)mouseY, horizontalAmount, verticalAmount)
         ? true
         : super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
      return this.onKeyPressed(input.key(), input.scancode(), input.modifiers()) ? true : super.keyPressed(input);
   }

   public boolean charTyped(CharInput input) {
      int cp = input.codepoint();
      return cp >= 32 && cp != 127 && this.onCharTyped((char)cp) ? true : super.charTyped(input);
   }

   private static class YarnContextWrapper implements GuiGraphicsContext {
      private DrawContext context;
      private final Map<Style, Map<String, Text>> textCache = new HashMap<>();
      private final Map<Text, Integer> widthCache = new IdentityHashMap<>();
      private TextRenderer textRenderer;
      private static final Style STYLE_REG = Style.EMPTY.withFont(new Font(CompatScreen.FONT_REGULAR));
      private static final Style STYLE_B = Style.EMPTY.withFont(new Font(CompatScreen.FONT_BOLD));
      private static final Style STYLE_MED = Style.EMPTY.withFont(new Font(CompatScreen.FONT_MEDIUM));
      private static final Style STYLE_TITLE = Style.EMPTY.withFont(new Font(CompatScreen.FONT_HEADING));
      private static final Style STYLE_SM = Style.EMPTY.withFont(new Font(CompatScreen.FONT_SMALL));
      private static final Style STYLE_SM_B = Style.EMPTY.withFont(new Font(CompatScreen.FONT_SMALL_BOLD));
      private static final Style STYLE_ICONS = Style.EMPTY.withFont(new Font(CompatScreen.FONT_ICONS));
      private int fillCalls = 0;
      private int textureCalls = 0;
      private int drawTextCalls = 0;
      private int matrixPushCalls = 0;
      private float opacity = 1.0F;
      private Identifier cachedSkinId = null;
      private Object lastPlayer = null;
      private long lastSkinCheck = 0L;

      @Override
      public String getClipboard() {
         return MinecraftClient.getInstance().keyboard.getClipboard();
      }

      @Override
      public void setClipboard(String text) {
         MinecraftClient.getInstance().keyboard.setClipboard(text);
      }

      private Text styled(String text, Style style) {
         Map<String, Text> cache = this.textCache.computeIfAbsent(style, key -> new HashMap<>());
         if (cache.size() >= 1024) {
            cache.clear();
            this.widthCache.clear();
         }

         return cache.computeIfAbsent(text, value -> Text.literal(value).setStyle(style));
      }

      private int measured(String text, Style style) {
         Text value = this.styled(text, style);
         return this.widthCache.computeIfAbsent(value, key -> this.textRenderer.getWidth(key));
      }

      private int withOpacity(int color) {
         return color & 16777215 | Math.round((color >>> 24) * this.opacity) << 24;
      }

      @Override
      public void setOpacity(float alpha) {
         this.opacity = Math.max(0.0F, Math.min(1.0F, alpha));
      }

      @Override
      public float getOpacity() {
         return this.opacity;
      }

      @Override
      public int getFps() {
         return MinecraftClient.getInstance().getCurrentFps();
      }

      @Override
      public boolean isDebugVisible() {
         return MinecraftClient.getInstance().getDebugHud().shouldShowDebugHud();
      }

      @Override
      public void drawTextHeading(String text, int x, int y, int color) {
         if (this.context != null && this.textRenderer != null && text != null) {
            this.drawTextCalls++;
            this.context.drawText(this.textRenderer, this.styled(text, STYLE_TITLE), x, y + 8, this.withOpacity(color), false);
         }
      }

      @Override
      public int getTextWidthHeading(String text) {
         return this.textRenderer != null && text != null ? this.measured(text, STYLE_TITLE) : 0;
      }

      @Override
      public int getFillCalls() {
         return this.fillCalls;
      }

      @Override
      public int getTextureCalls() {
         return this.textureCalls;
      }

      @Override
      public int getDrawTextCalls() {
         return this.drawTextCalls;
      }

      @Override
      public int getMatrixPushCalls() {
         return this.matrixPushCalls;
      }

      @Override
      public void resetMetrics() {
         this.fillCalls = 0;
         this.textureCalls = 0;
         this.drawTextCalls = 0;
         this.matrixPushCalls = 0;
      }

      void set(DrawContext context, TextRenderer textRenderer) {
         this.context = context;
         if (this.textRenderer != textRenderer) {
            this.textCache.clear();
            this.widthCache.clear();
         }

         this.textRenderer = textRenderer;
      }

      @Override
      public void fill(int x, int y, int width, int height, int color) {
         if (width > 0 && height > 0 && this.context != null) {
            this.fillCalls++;
            this.context.fill(x, y, x + width, y + height, this.withOpacity(color));
         }
      }

      @Override
      public void drawText(String text, int x, int y, int color, boolean shadow) {
         if (this.context != null && this.textRenderer != null && text != null) {
            this.drawTextCalls++;
            this.context.drawText(this.textRenderer, this.styled(text, STYLE_REG), x, y + 5, this.withOpacity(color), shadow);
         }
      }

      @Override
      public void drawTextBold(String text, int x, int y, int color, boolean shadow) {
         if (this.context != null && this.textRenderer != null && text != null) {
            this.drawTextCalls++;
            this.context.drawText(this.textRenderer, this.styled(text, STYLE_B), x, y + 5, this.withOpacity(color), shadow);
         }
      }

      @Override
      public void drawTextMedium(String text, int x, int y, int color, boolean shadow) {
         if (this.context != null && this.textRenderer != null && text != null) {
            this.drawTextCalls++;
            this.context.drawText(this.textRenderer, this.styled(text, STYLE_MED), x, y + 5, this.withOpacity(color), shadow);
         }
      }

      @Override
      public void drawTextSmall(String text, int x, int y, int color, boolean shadow) {
         if (this.context != null && this.textRenderer != null && text != null) {
            this.drawTextCalls++;
            this.context.drawText(this.textRenderer, this.styled(text, STYLE_SM), x, y + 3, this.withOpacity(color), shadow);
         }
      }

      @Override
      public void drawTextSmallBold(String text, int x, int y, int color, boolean shadow) {
         if (this.context != null && this.textRenderer != null && text != null) {
            this.drawTextCalls++;
            this.context.drawText(this.textRenderer, this.styled(text, STYLE_SM_B), x, y + 3, this.withOpacity(color), shadow);
         }
      }

      @Override
      public int getTextWidth(String text) {
         return this.textRenderer != null && text != null ? this.measured(text, STYLE_REG) : 0;
      }

      @Override
      public int getTextWidthBold(String text) {
         return this.textRenderer != null && text != null ? this.measured(text, STYLE_B) : 0;
      }

      @Override
      public int getTextWidthMedium(String text) {
         return this.textRenderer != null && text != null ? this.measured(text, STYLE_MED) : 0;
      }

      @Override
      public int getTextWidthSmall(String text) {
         return this.textRenderer != null && text != null ? this.measured(text, STYLE_SM) : 0;
      }

      @Override
      public int getTextWidthSmallBold(String text) {
         return this.textRenderer != null && text != null ? this.measured(text, STYLE_SM_B) : 0;
      }

      @Override
      public List<String> wrapText(String text, int maxWidth) {
         List<String> result = new ArrayList<>();
         if (text != null && !text.isEmpty()) {
            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();

            for (String w : words) {
               String candidate = line.length() == 0 ? w : line + " " + w;
               if (this.getTextWidth(candidate) <= maxWidth) {
                  line = new StringBuilder(candidate);
               } else {
                  if (line.length() > 0) {
                     result.add(line.toString());
                  }

                  line = new StringBuilder(w);
               }
            }

            if (line.length() > 0) {
               result.add(line.toString());
            }

            return result;
         } else {
            return result;
         }
      }

      @Override
      public void enableScissor(int x, int y, int width, int height) {
         if (this.context != null) {
            this.context.enableScissor(x, y, x + width, y + height);
         }
      }

      @Override
      public void disableScissor() {
         if (this.context != null) {
            this.context.disableScissor();
         }
      }

      @Override
      public void nextLayer() {
         if (this.context != null) {
            this.context.createNewRootLayer();
         }
      }

      @Override
      public void pushMatrix() {
         this.matrixPushCalls++;
         if (this.context != null) {
            this.context.getMatrices().pushMatrix();
         }
      }

      @Override
      public void scaleMatrix(float sx, float sy) {
         if (this.context != null) {
            this.context.getMatrices().scale(sx, sy);
         }
      }

      @Override
      public void translateMatrix(float dx, float dy) {
         if (this.context != null) {
            this.context.getMatrices().translate(dx, dy);
         }
      }

      @Override
      public void popMatrix() {
         if (this.context != null) {
            this.context.getMatrices().popMatrix();
         }
      }

      @Override
      public float getScaleFactor() {
         return MinecraftClient.getInstance().getWindow().getScaleFactor();
      }

      @Override
      public void drawIcon(IconId icon, int x, int y, int size, int color) {
         if (this.context != null && icon != null && size > 0) {
            this.drawRawQuad(CompatScreen.TEX_ICONS, x, y, size, size, icon.atlasX(), icon.atlasY(), 64, 64, 256, 256, color);
         }
      }

      @Override
      public void drawIcon(String iconChar, int x, int y, int color) {
         if (this.context != null && this.textRenderer != null && iconChar != null) {
            this.drawTextCalls++;
            this.context.drawText(this.textRenderer, this.styled(iconChar, STYLE_ICONS), x, y, this.withOpacity(color), false);
         }
      }

      private void drawRawQuad(
         Identifier tex,
         int x,
         int y,
         int width,
         int height,
         float u,
         float v,
         int regionWidth,
         int regionHeight,
         int textureWidth,
         int textureHeight,
         int color
      ) {
         color = this.withOpacity(color);
         if (color >>> 24 != 0) {
            this.textureCalls++;
            this.context
               .drawTexture(RenderPipelines.GUI_TEXTURED, tex, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight, color);
         }
      }

      @Override
      public void drawNineSlice(NineSliceSprite sprite, int x, int y, int width, int height, int color) {
         if (this.context != null && sprite != null && width > 0 && height > 0) {
            int a = color >>> 24 & 0xFF;
            if (a != 0) {
               Identifier tex = CompatScreen.getTextureId(sprite);
               if (tex != null) {
                  int cardWidth = 222;
                  if (width != cardWidth || height != 44 || sprite != NineSliceSprite.CARD && sprite != NineSliceSprite.BORDER_CARD) {
                     if (width == 26 && height == 26 && sprite == NineSliceSprite.TILE) {
                        this.drawRawQuad(CompatScreen.TEX_TILE_BODY, x, y, 26, 26, 0.0F, 0.0F, 104, 104, 104, 104, color);
                     } else {
                        int u = 0;
                        int v = 0;
                        boolean control = false;
                        if (width != 200 || height != 18 || sprite != NineSliceSprite.KEY_CHIP && sprite != NineSliceSprite.TILE) {
                           if (width == 98 && height == 18 && sprite == NineSliceSprite.TILE) {
                              v = 160;
                              control = true;
                           } else if (width == 10 && height == 10 && sprite == NineSliceSprite.PILL) {
                              u = 800;
                              control = true;
                           } else if (width == 26 && height == 14 && sprite == NineSliceSprite.PILL) {
                              u = 800;
                              v = 48;
                              control = true;
                           } else if (width == 200 && height == 4 && sprite == NineSliceSprite.RECT_R2) {
                              v = 240;
                              control = true;
                           }
                        } else {
                           v = sprite == NineSliceSprite.TILE ? 80 : 0;
                           control = true;
                        }

                        if (control) {
                           this.drawRawQuad(CompatScreen.TEX_CONTROLS, x, y, width, height, u, v, width * 4, height * 4, 1024, 256, color);
                        } else {
                           this.pushMatrix();
                           this.scaleMatrix(0.25F, 0.25F);
                           int X = x * 4;
                           int Y = y * 4;
                           int W = width * 4;
                           int H = height * 4;
                           int origC = sprite.cornerTexels;
                           int cen = sprite.centerTexels;
                           int tot = sprite.totalTexels;
                           int C = Math.min(origC, Math.min(W / 2, H / 2));
                           if (C < 0) {
                              C = 0;
                           }

                           int centerW = W - 2 * C;
                           int centerH = H - 2 * C;
                           if (C > 0) {
                              this.drawRawQuad(tex, X, Y, C, C, 0.0F, 0.0F, origC, origC, tot, tot, color);
                           }

                           if (centerW > 0 && C > 0) {
                              this.drawRawQuad(tex, X + C, Y, centerW, C, origC, 0.0F, cen, origC, tot, tot, color);
                           }

                           if (C > 0) {
                              this.drawRawQuad(tex, X + W - C, Y, C, C, tot - origC, 0.0F, origC, origC, tot, tot, color);
                           }

                           if (centerH > 0 && C > 0) {
                              this.drawRawQuad(tex, X, Y + C, C, centerH, 0.0F, origC, origC, cen, tot, tot, color);
                           }

                           if (centerW > 0 && centerH > 0) {
                              this.drawRawQuad(tex, X + C, Y + C, centerW, centerH, origC, origC, cen, cen, tot, tot, color);
                           }

                           if (centerH > 0 && C > 0) {
                              this.drawRawQuad(tex, X + W - C, Y + C, C, centerH, tot - origC, origC, origC, cen, tot, tot, color);
                           }

                           if (C > 0) {
                              this.drawRawQuad(tex, X, Y + H - C, C, C, 0.0F, tot - origC, origC, origC, tot, tot, color);
                           }

                           if (centerW > 0 && C > 0) {
                              this.drawRawQuad(tex, X + C, Y + H - C, centerW, C, origC, tot - origC, cen, origC, tot, tot, color);
                           }

                           if (C > 0) {
                              this.drawRawQuad(tex, X + W - C, Y + H - C, C, C, tot - origC, tot - origC, origC, origC, tot, tot, color);
                           }

                           this.popMatrix();
                        }
                     }
                  } else {
                     this.drawRawQuad(
                        sprite == NineSliceSprite.CARD ? CompatScreen.TEX_CARD_BODY : CompatScreen.TEX_CARD_RING,
                        x,
                        y,
                        width,
                        height,
                        0.0F,
                        0.0F,
                        width * 4,
                        height * 4,
                        width * 4,
                        height * 4,
                        color
                     );
                  }
               }
            }
         }
      }

      private Identifier getPlayerSkin() {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.player == null) {
            this.lastPlayer = null;
            this.cachedSkinId = null;
            return null;
         } else if (mc.player == this.lastPlayer && this.cachedSkinId != null && System.currentTimeMillis() - this.lastSkinCheck < 2000L) {
            return this.cachedSkinId;
         } else {
            this.lastPlayer = mc.player;
            this.lastSkinCheck = System.currentTimeMillis();
            this.cachedSkinId = mc.player.getSkin().body().texturePath();
            return this.cachedSkinId;
         }
      }

      private void drawPixelQuad(Identifier tex, int x, int y, int size, int u, int v, int sourceSize, int textureSize) {
         Matrix3x2fStack matrix = this.context.getMatrices();
         float sx = matrix.m00();
         float sy = matrix.m11();
         float tx = matrix.m20();
         float ty = matrix.m21();
         float factor = this.getScaleFactor();
         int pixels = Math.max(1, (int)Math.floor(size * Math.min(sx, sy) * factor / sourceSize)) * sourceSize;
         int px = Math.round(((x + size / 2.0F) * sx + tx) * factor - pixels / 2.0F);
         int py = Math.round(((y + size / 2.0F) * sy + ty) * factor - pixels / 2.0F);
         this.pushMatrix();
         this.context.getMatrices().identity();
         this.scaleMatrix(1.0F / factor, 1.0F / factor);
         this.drawRawQuad(tex, px, py, pixels, pixels, u, v, sourceSize, sourceSize, textureSize, textureSize, Theme.WHITE);
         this.popMatrix();
      }

      @Override
      public void drawLogo(int x, int y, int size) {
         this.drawPixelQuad(CompatScreen.TEX_PIXEL_LOGO, x, y, size, 0, 0, 16, 16);
      }

      @Override
      public boolean drawPlayerAvatar(int x, int y, int size) {
         Identifier skin = this.getPlayerSkin();
         if (skin == null) {
            return false;
         } else {
            this.drawPixelQuad(skin, x, y, size, 8, 8, 8, 64);
            this.drawPixelQuad(skin, x, y, size, 40, 8, 8, 64);
            return true;
         }
      }

      @Override
      public String getSessionPlayerName() {
         MinecraftClient mc = MinecraftClient.getInstance();
         return mc.getSession() != null && mc.getSession().getUsername() != null ? mc.getSession().getUsername() : "Player";
      }
   }
}
