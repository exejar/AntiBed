package club.maxstats.antibed.listener;

import club.maxstats.antibed.Main;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class BedListener {
    private boolean entitySearch = false;
    private IBlockState teamBed;
    private BlockPos bedPosition;

    int timer = 0;
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        /* Search for nearest bed and set it as team bed */
        if (event.message.getFormattedText().contains("Protect your bed and destroy the enemy beds.")) {
            Main.getInstance().getWhitelist().clear();
            this.timer++;
        } else if (event.message.getUnformattedText().contains("§fReward Summary")) {
            this.entitySearch = false;
            this.teamBed = null;
            this.bedPosition = null;
        } else if (event.message.getUnformattedText().contains("lorem ipsum")) {
            /* Whitelist generation */

        }
    }

    /* Used to check distance from Player to Bed Position */
    private double distanceTo(BlockPos blockPosition) {
        Vec3 playerPos = Minecraft.getMinecraft().thePlayer.getPositionVector();
        Vec3 bedPos = new Vec3(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
        return playerPos.distanceTo(bedPos);
    }

    private double distanceTo(EntityPlayer player, BlockPos blockPosition) {
        Vec3 playerPos = player.getPositionVector();
        Vec3 bedPos = new Vec3(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
        return playerPos.distanceTo(bedPos);
    }

    int loop = 0;
    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (this.timer > 0) {
            if (this.timer == 80) {
                this.timer = 0;
                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                BlockPos center = player.getPosition();

                for (int z = center.getZ() - 100; z < center.getZ() + 100; z++) {
                    for (int x = center.getX() - 100; x < center.getX() + 100; x++) {
                        for (int y = center.getY() - 10; y < center.getY() + 10; y++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(pos);
                            if (state.getBlock() instanceof BlockBed) {
                                /* Check to see if this Bed is closer than the one currently set */
                                if (this.bedPosition != null && this.teamBed != null) {
                                    if (this.distanceTo(pos) < this.distanceTo(this.bedPosition)) {
                                        this.bedPosition = pos;
                                        this.teamBed = state;
                                    }
                                } else {
                                    this.bedPosition = pos;
                                    this.teamBed = state;
                                    this.entitySearch = true;
                                }
                            }
                        }
                    }
                }

                if (this.bedPosition == null || this.teamBed == null || !this.entitySearch) {
                    this.entitySearch = false;
                    this.teamBed = null;
                    this.bedPosition = null;
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to Find Team Bed"));
                } else {
                    /* Whitelist all players within threshold when game first starts */
                    AxisAlignedBB expandedAABB = this.teamBed.getBlock().getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, this.bedPosition).expand(25, 100, 25);
                    List<EntityPlayer> entities = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityPlayer.class, expandedAABB);
                    for (EntityPlayer entity : entities) {
                        Main.getInstance().getWhitelist().add(entity.getName().toUpperCase());
                    }
                }
            } else
                this.timer++;
        }
        if (this.entitySearch && this.teamBed != null && this.bedPosition != null) {
            switch (this.loop) {
                case 40:
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), true);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
                    this.loop++;
                    break;
                case 80:
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), true);
                    this.loop++;
                    break;
                case 120:
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), true);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
                    this.loop++;
                    break;
                case 160:
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), true);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
                    this.loop++;
                    break;
                case 200:
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
                    this.loop++;
                    break;
                case 3600:
                    this.loop = 0;
                default:
                    this.loop++;
                    break;
            }

            AxisAlignedBB expandedAABB = this.teamBed.getBlock().getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, this.bedPosition).expand(25, 100, 25);
            List<EntityPlayer> entities = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityPlayer.class, expandedAABB);

            /* Bed Threshold Check */
            for (EntityPlayer player : entities) {
                if (!Main.getInstance().getWhitelist().contains(player.getName().toUpperCase())) {
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("/lobby");
                    Main.getInstance().broadcastToPlayer("Lobby " + EnumChatFormatting.YELLOW + player.getName() + EnumChatFormatting.RED + " Found within Bed Threshold. " + EnumChatFormatting.YELLOW + distanceTo(player, this.bedPosition) + " blocks" + EnumChatFormatting.RED + " away");
                    this.teamBed = null;
                    this.bedPosition = null;
                    this.entitySearch = false;
                }
            }

            /* Probably should merge both checks into 1 loop, but I added this way after creating the previous method, so meh */
            try {
                for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
                    try {
                        if (player.getHeldItem().getItem() instanceof ItemEnderPearl && !Main.getInstance().getWhitelist().contains(player.getName().toUpperCase())) {
                            Minecraft.getMinecraft().thePlayer.sendChatMessage("/lobby");
                            Main.getInstance().broadcastToPlayer("Lobby " + EnumChatFormatting.YELLOW + player.getName() + EnumChatFormatting.RED + " Holding Ender Pearl " + EnumChatFormatting.YELLOW + distanceTo(player, this.bedPosition) + EnumChatFormatting.RED + " Blocks from your Bed.");
                            this.teamBed = null;
                            this.bedPosition = null;
                            this.entitySearch = false;
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void renderEvent(RenderWorldLastEvent event) {
        if (this.teamBed != null && this.bedPosition != null) {
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

            double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
            double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
            double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

            AxisAlignedBB expandedAABB = this.teamBed.getBlock().getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, this.bedPosition).expand(25, 100, 25);

            GL11.glPushMatrix();
            GL11.glTranslated(-x, -y, -z); //go from cartesian x,y,z coordinates to in-world x,y,z coordinates
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GL11.glLineWidth(5.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(expandedAABB.minX, expandedAABB.minY, expandedAABB.minZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.maxX, expandedAABB.minY, expandedAABB.minZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.maxX, expandedAABB.minY, expandedAABB.maxZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.minX, expandedAABB.minY, expandedAABB.maxZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.minX, expandedAABB.minY, expandedAABB.minZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            tessellator.draw();
            worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(expandedAABB.minX, expandedAABB.maxY, expandedAABB.minZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.maxX, expandedAABB.maxY, expandedAABB.minZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.maxX, expandedAABB.maxY, expandedAABB.maxZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.minX, expandedAABB.maxY, expandedAABB.maxZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.minX, expandedAABB.maxY, expandedAABB.minZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            tessellator.draw();
            worldrenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(expandedAABB.minX, expandedAABB.minY, expandedAABB.minZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.minX, expandedAABB.maxY, expandedAABB.minZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.maxX, expandedAABB.minY, expandedAABB.minZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.maxX, expandedAABB.maxY, expandedAABB.minZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.maxX, expandedAABB.minY, expandedAABB.maxZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.maxX, expandedAABB.maxY, expandedAABB.maxZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.minX, expandedAABB.minY, expandedAABB.maxZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            worldrenderer.pos(expandedAABB.minX, expandedAABB.maxY, expandedAABB.maxZ).color(1.0f, 0.2f, 0.2f, 0.75f).endVertex();
            tessellator.draw();

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GL11.glPopMatrix();
        }
    }
}
