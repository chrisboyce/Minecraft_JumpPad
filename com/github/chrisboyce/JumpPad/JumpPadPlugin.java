package com.github.chrisboyce.JumpPad;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class JumpPadPlugin extends JavaPlugin implements Listener{


	private int padTypeId;
	private double horizontalScale,verticalScale;
    Logger log = Logger.getLogger("Minecraft");
    
    
    public void log(String message){
    	log.info("[JumpPad] " + message);
    }
    
	@Override
	public void onDisable() {	
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().equalsIgnoreCase("jp_reload")){
			log("Reloading config");
			initConfig(true);
			return true;
		}
		return false; 
	}
	@Override
	public void onEnable() {
		log("Enabling JumpPad...");
		initConfig(false);
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, this);
		
		log("JumpPad enabled!");
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getAction() == Action.PHYSICAL){
			if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.STONE_PLATE){
				jumpEntity(event.getPlayer(),event.getClickedBlock());
			}
		}
	}
	
	public void onEntityInteract(EntityInteractEvent  event){
		/*
		System.out.println(event.getEventName());
		System.out.println(event.getBlock());
		System.out.println(event.getType());
		System.out.println(event);
		System.out.println(event.getEventName());	
		*/
		if(event.getBlock() != null && event.getBlock().getType() == Material.STONE_PLATE || 
				event.getBlock().getType()== Material.WOOD_PLATE){
			jumpEntity(event.getEntity(),event.getBlock());
		}
	}
	
	public void jumpEntity(Entity entity,Block clickedBlock){
		log("Jumping player..");
		/*
		if(!player.hasPermission("jumppad.jump")){
			log("Player [" + player.getName() + "] doesn't have permission to jump");
		}*/
		
		//Location playerLoc = player.getLocation();
		Location blockLocation = clickedBlock.getLocation();
		//Location playerLoc = blockLocation.clone();
		Location playerLoc = entity.getLocation().clone();
		playerLoc.setX(blockLocation.getX());
		playerLoc.setY(blockLocation.getY());
		playerLoc.setZ(blockLocation.getZ());
		
		blockLocation.setY(blockLocation.getY() - 2);
		int blockCount = 0;
		Block curBlock = entity.getWorld().getBlockAt(blockLocation);
		while(curBlock.getTypeId() == padTypeId){
			blockCount++;
			blockLocation.setY(blockLocation.getY() - 1);
			curBlock = entity.getWorld().getBlockAt(blockLocation);
		}
		
		//We should find a sign underneath the jump pad
		if(curBlock.getType() != Material.WALL_SIGN && curBlock.getType() != Material.SIGN_POST){
			log("No sign found under jump pad");
			log(curBlock.getType().toString());
			return;
		}
		Sign signBlock = (Sign)	curBlock.getState();
		String directionText = signBlock.getLine(1);
		String angleString = signBlock.getLine(2);
		
		float angle = 45;
		try{
			angle = Float.valueOf(angleString);
		} catch(Exception e){
		}
		
		if(angle > 90.0f || angle < 0){
			angle = 90;
		}
		
		
		
		Float heading = Float.valueOf(directionText) * 0.0174532925f;
		Float angleRadians = angle * 0.0174532925f;
		
		Vector headingVector = new Vector(
				Math.sin(heading) * blockCount * horizontalScale * Math.cos(angleRadians),
				blockCount *  verticalScale * Math.sin(angleRadians),
				Math.cos(heading)* blockCount * horizontalScale * Math.cos(angleRadians));
		
		//System.out.println(playerLoc);
		playerLoc.setY(playerLoc.getY() + 1.0);
		playerLoc.setX(playerLoc.getX() + 0.5);
		playerLoc.setZ(playerLoc.getZ() + 0.5);
		/*
		if(playerLoc.getX() > 0){
			playerLoc.setX(Math.floor(playerLoc.getX()) + 0.5);
		} else {
			playerLoc.setX(Math.ceil(playerLoc.getX()) - 0.5);
		}
		if(playerLoc.getZ() > 0){
			playerLoc.setZ(Math.floor(playerLoc.getZ()) - 0.5);
		} else {
			playerLoc.setZ(Math.ceil(playerLoc.getZ()) + 0.5);
		}*/
		//System.out.println(playerLoc);
		entity.teleport(playerLoc);
		entity.setFallDistance(0);
		entity.setVelocity(headingVector);
		
		if(entity instanceof Player){
			((Player)entity).sendMessage("Boing!");
		}
	}
	
	private void initConfig(boolean reload){
		if(reload){
			reloadConfig();
		}
		FileConfiguration config = getConfig();
	
		config.addDefault("pad_item_type", Material.DIAMOND_BLOCK.getId());
		config.addDefault("verticalScale",1.0);
		config.addDefault("horizontalScale", 1.0);

		padTypeId = config.getInt("pad_item_type");
		horizontalScale = config.getDouble("horizontalScale");
		verticalScale = config.getDouble("verticalScale");
		
		this.getConfig().options().copyDefaults(true);
		saveConfig();
		
	}
}