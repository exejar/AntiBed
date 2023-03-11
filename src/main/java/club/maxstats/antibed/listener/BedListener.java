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
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class BedListener {
    private boolean entitySearch = false;
    private IBlockState teamBed;
    private BlockPos bedPosition;

    private long timer = 0;
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        /* Search for nearest bed and set it as team bed */
        if (event.message.getFormattedText().contains("Protect your bed and destroy the enemy beds.") && !event.message.getFormattedText().contains(":")) {
            Main.getInstance().getWhitelist().clear();
            this.teamBed = null;
            this.bedPosition = null;
            this.entitySearch = false;

            Main.getInstance().getWhitelist().add(Minecraft.getMinecraft().thePlayer.getUniqueID());

            this.timer = System.currentTimeMillis();
        } else if (event.message.getUnformattedText().contains("§f§lReward Summary")) {
            this.entitySearch = false;
            this.teamBed = null;
            this.bedPosition = null;
        }
    }

    /* Used to check distance from Player to Bed Position */
    private double distanceTo(BlockPos blockPosition) {
        Vec3 playerPos = Minecraft.getMinecraft().thePlayer.getPositionVector();
        Vec3 bedPos = new Vec3(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
        return playerPos.distanceTo(bedPos);
    }

    private double distanceTo(Entity entity, BlockPos blockPosition) {
        Vec3 playerPos = entity.getPositionVector();
        Vec3 bedPos = new Vec3(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
        return playerPos.distanceTo(bedPos);
    }

    private long afkLoop = 0;
    private int afkLoopStage;
    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (this.timer > 0) {
            if (System.currentTimeMillis() - this.timer >= 5000) {
                this.timer = 0;

                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                BlockPos center = player.getPosition();
                for (int z = center.getZ() - 30; z < center.getZ() + 30; z++) {
                    for (int x = center.getX() - 30; x < center.getX() + 30; x++) {
                        for (int y = center.getY() - 20; y < center.getY() + 20; y++) {
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
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed to Find Team Bed. Trying again..."));
                    this.timer = System.currentTimeMillis() - 4750;
                } else {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Found Team Bed"));
                    /* Whitelist all players within threshold when game first starts */
                    AxisAlignedBB expandedAABB = this.teamBed.getBlock().getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, this.bedPosition).expand(18, 100, 18);
                    List<EntityPlayer> entities = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityPlayer.class, expandedAABB);
                    for (EntityPlayer entity : entities) {
                        Main.getInstance().getWhitelist().add(entity.getUniqueID());
                    }

                    /* Whitelist watchdog bots */
                    Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
                    scoreboard.getTeams().forEach(t -> {
                        if (t.getColorPrefix().contains("§c") && !t.getColorPrefix().contains("R")) {
                            t.getMembershipCollection().forEach(m -> {
                                EntityPlayer member = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(m);
                                if (member != null)
                                    Main.getInstance().getWhitelist().add(member.getUniqueID());
                            });
                        }
                    });
                }
            }
        }

        if (this.entitySearch && this.teamBed != null && this.bedPosition != null) {
            /* Anti AFK using System.currentTime */

            if (System.currentTimeMillis() - this.afkLoop > 500) {
                this.afkLoop = System.currentTimeMillis();
                this.afkLoopStage++;
            }

            switch (this.afkLoopStage) {
                case 1:
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), true);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
                    break;
                case 2:
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), true);
                    break;
                case 3:
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), true);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
                    break;
                case 4:
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), true);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
                    break;
                case 5:
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindForward.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindBack.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindLeft.getKeyCode(), false);
                    KeyBinding.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindRight.getKeyCode(), false);
                    this.afkLoop = System.currentTimeMillis() + 30000;
                    this.afkLoopStage = 0;
                    break;
            }

            AxisAlignedBB expandedAABB = this.teamBed.getBlock().getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, this.bedPosition).expand(18, 100, 18);
            List<Entity> entities = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(Entity.class, expandedAABB);

            /* Bed Threshold Check */
            for (Entity entity : entities) {
                if (entity instanceof EntityPlayer) {
                    if (!Main.getInstance().getWhitelist().contains(entity.getUniqueID()) && !Main.getInstance().getPermaWhiteList().contains(entity.getName().toUpperCase())) {
                        Main.getInstance().broadcastToPlayer("Lobby " + EnumChatFormatting.YELLOW + entity.getName() + EnumChatFormatting.RED + " Found within Bed Threshold. " + EnumChatFormatting.YELLOW + distanceTo(entity, this.bedPosition) + " blocks" + EnumChatFormatting.RED + " away");
                        leaveGame();
                    }
                } else if (entity instanceof EntityEnderPearl) {
                    Main.getInstance().broadcastToPlayer("Lobby " + EnumChatFormatting.YELLOW + "Ender Pearl Entity" + EnumChatFormatting.RED + " Found within Bed Threshold. " + EnumChatFormatting.YELLOW + distanceTo(entity, this.bedPosition) + " blocks" + EnumChatFormatting.RED + " away");
                    leaveGame();
                }
            }

            /* Probably should merge both checks into 1 loop, but I added this way after creating the previous method, so meh */
            try {
                for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
                    try {
                        if (player.getHeldItem().getItem() instanceof ItemEnderPearl && !Main.getInstance().getWhitelist().contains(player.getName().toUpperCase())) {
                            Main.getInstance().broadcastToPlayer("Lobby " + EnumChatFormatting.YELLOW + player.getName() + EnumChatFormatting.RED + " Holding Ender Pearl " + EnumChatFormatting.YELLOW + distanceTo(player, this.bedPosition) + EnumChatFormatting.RED + " Blocks from your Bed.");
                            this.leaveGame();
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            /* Check if player has been hurt */
            if (Minecraft.getMinecraft().thePlayer.maxHurtTime != 0 && (Minecraft.getMinecraft().thePlayer.maxHurtTime == Minecraft.getMinecraft().thePlayer.hurtTime + 1)) {
                Main.getInstance().broadcastToPlayer("Lobby " + EnumChatFormatting.RED + "AntiBed Bot took damage.");
                this.leaveGame();
            }
        }
    }

    private void leaveGame() {
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/lobby");
        this.teamBed = null;
        this.bedPosition = null;
        this.entitySearch = false;
    }

    @SubscribeEvent
    public void renderEvent(RenderWorldLastEvent event) {
        if (this.teamBed != null && this.bedPosition != null) {
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

            double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
            double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
            double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

            AxisAlignedBB expandedAABB = this.teamBed.getBlock().getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, this.bedPosition).expand(18, 100, 18);

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
