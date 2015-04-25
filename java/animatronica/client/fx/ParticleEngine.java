package animatronica.client.fx;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

public class ParticleEngine
{
  public static ParticleEngine instance = new ParticleEngine();
  public static final ResourceLocation particleTexture = new ResourceLocation("thaumcraft", "textures/misc/particles.png");
  public static final ResourceLocation particleTexture2 = new ResourceLocation("thaumcraft", "textures/misc/particles2.png");
  protected World worldObj;
  private HashMap<Integer, ArrayList<EntityFX>>[] particles = { new HashMap(), new HashMap(), new HashMap(), new HashMap() };
  private Random rand = new Random();
  
  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void onRenderWorldLast(RenderWorldLastEvent event)
  {
    float frame = event.partialTicks;
    Entity entity = Minecraft.getMinecraft().thePlayer;
    TextureManager renderer = Minecraft.getMinecraft().renderEngine;
    int dim = Minecraft.getMinecraft().theWorld.provider.dimensionId;
    
    renderer.bindTexture(particleTexture);
    
    GL11.glPushMatrix();
    
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    GL11.glDepthMask(false);
    GL11.glEnable(3042);
    GL11.glAlphaFunc(516, 0.003921569F);
    boolean rebound = false;
    for (int layer = 0; layer < 4; layer++) {
      if (this.particles[layer].containsKey(Integer.valueOf(dim)))
      {
        ArrayList<EntityFX> parts = (ArrayList)this.particles[layer].get(Integer.valueOf(dim));
        if (parts.size() != 0)
        {
          if ((!rebound) && (layer >= 2))
          {
            renderer.bindTexture(particleTexture2);
            rebound = true;
          }
          GL11.glPushMatrix();
          switch (layer)
          {
          case 0: 
          case 2: 
            GL11.glBlendFunc(770, 1);
            break;
          case 1: 
          case 3: 
            GL11.glBlendFunc(770, 771);
          }
          float f1 = ActiveRenderInfo.rotationX;
          float f2 = ActiveRenderInfo.rotationZ;
          float f3 = ActiveRenderInfo.rotationYZ;
          float f4 = ActiveRenderInfo.rotationXY;
          float f5 = ActiveRenderInfo.rotationXZ;
          EntityFX.interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * frame;
          EntityFX.interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * frame;
          EntityFX.interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * frame;
          
          Tessellator tessellator = Tessellator.instance;
          tessellator.startDrawingQuads();
          for (int j = 0; j < parts.size(); j++)
          {
            final EntityFX entityfx = (EntityFX)parts.get(j);
            if (entityfx != null)
            {
              tessellator.setBrightness(entityfx.getBrightnessForRender(frame));
              try
              {
                entityfx.renderParticle(tessellator, frame, f1, f5, f2, f3, f4);
              }
              catch (Throwable throwable)
              {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                crashreportcategory.addCrashSectionCallable("Particle", new Callable()
                {
                  public String call()
                  {
                    return entityfx.toString();
                  }
                });
                crashreportcategory.addCrashSectionCallable("Particle Type", new Callable()
                {
                  public String call()
                  {
                    return "ENTITY_PARTICLE_TEXTURE";
                  }
                });
                throw new ReportedException(crashreport);
              }
            }
          }
          tessellator.draw();
          
          GL11.glPopMatrix();
        }
      }
    }
    GL11.glDisable(3042);
    GL11.glDepthMask(true);
    GL11.glAlphaFunc(516, 0.1F);
    GL11.glPopMatrix();
  }
  
  public void addEffect(World world, EntityFX fx)
  {
    if (!this.particles[fx.getFXLayer()].containsKey(Integer.valueOf(world.provider.dimensionId))) {
      this.particles[fx.getFXLayer()].put(Integer.valueOf(world.provider.dimensionId), new ArrayList());
    }
    ArrayList<EntityFX> parts = (ArrayList)this.particles[fx.getFXLayer()].get(Integer.valueOf(world.provider.dimensionId));
    if (parts.size() >= 2000) {
      parts.remove(0);
    }
    parts.add(fx);
    this.particles[fx.getFXLayer()].put(Integer.valueOf(world.provider.dimensionId), parts);
  }
  
  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  public void updateParticles(TickEvent.ClientTickEvent event)
  {
    if (event.side == Side.SERVER) {
      return;
    }
    Minecraft mc = FMLClientHandler.instance().getClient();
    World world = mc.theWorld;
    if (mc.theWorld == null) {
      return;
    }
    int dim = world.provider.dimensionId;
    if (event.phase == TickEvent.Phase.START) {
      for (int layer = 0; layer < 4; layer++) {
        if (this.particles[layer].containsKey(Integer.valueOf(dim)))
        {
          ArrayList<EntityFX> parts = (ArrayList)this.particles[layer].get(Integer.valueOf(dim));
          for (int j = 0; j < parts.size(); j++)
          {
            final EntityFX entityfx = (EntityFX)parts.get(j);
            try
            {
              if (entityfx != null) {
                entityfx.onUpdate();
              }
            }
            catch (Throwable throwable)
            {
              CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking Particle");
              
              CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being ticked");
              
              crashreportcategory.addCrashSectionCallable("Particle", new Callable()
              {
                public String call()
                {
                  return entityfx.toString();
                }
              });
              crashreportcategory.addCrashSectionCallable("Particle Type", new Callable()
              {
                public String call()
                {
                  return "ENTITY_PARTICLE_TEXTURE";
                }
              });
              throw new ReportedException(crashreport);
            }
            if ((entityfx == null) || (entityfx.isDead))
            {
              parts.remove(j--);
              this.particles[layer].put(Integer.valueOf(dim), parts);
            }
          }
        }
      }
    }
  }
}