package ShunAnglers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.FishingSpot;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

@ScriptManifest(author = "Shun", category = Category.FISHING, description = "Start with Rod & Bait in inventory. Banks all fish and Clue bottle (elite). Very basic first script looking for ways to improve it.Found it runs best at zoom out maxed and 100% mouse speed", discord = "Shun#2997", name = "ShunAnglers Zaros", servers = {"Zaros"}, version = "1.8")
public class Anglers extends Script {
	protected static boolean started;

	private int totalxp;

	private int startxp;

	private int hourlyxp;

	private int fishinglvl;

	private int fishCaught;

	private int fishPerHour;

	private long startTime;

	long start = System.currentTimeMillis();

	private static GUI gui;

	private final WorldPoint BANK_TILE = new WorldPoint(1807, 3790, 0);

	private WorldArea edge = new WorldArea(new WorldPoint(3064, 3523, 0), new WorldPoint(3134, 3458, 0));

	private WorldArea BANK_AREA = new WorldArea(new WorldPoint(1793, 3796, 0), new WorldPoint(1812, 3783, 0));

	private final WorldArea FISHING_AREA = new WorldArea(new WorldPoint(1823, 3767, 0), new WorldPoint(1842, 3780, 0));

	private final WorldPoint MAP_1 = new WorldPoint(1814, 3779, 0);

	private final WorldPoint MAP_2 = new WorldPoint(1825, 3776, 0);

	private WorldArea EDGE = new WorldArea(new WorldPoint(3073, 3516, 0), new WorldPoint(3108, 3471, 0));

	private WorldPoint[] BANK_PATH = new WorldPoint[] { new WorldPoint(1828, 3773, 0), 
			new WorldPoint(1828, 3773, 0), 
			new WorldPoint(1824, 3775, 0), 
			new WorldPoint(1820, 3776, 0), 
			new WorldPoint(1816, 3776, 0), 
			new WorldPoint(1812, 3778, 0), 
			new WorldPoint(1808, 3781, 0), 
			new WorldPoint(1804, 3783, 0), 
			new WorldPoint(1805, 3787, 0), 
			new WorldPoint(1807, 3789, 0) };

	private WorldPoint[] FISHING_PATH = new WorldPoint[] { new WorldPoint(1809, 3781, 0), 
			new WorldPoint(1809, 3781, 0), 
			new WorldPoint(1813, 3780, 0), 
			new WorldPoint(1817, 3778, 0), 
			new WorldPoint(1821, 3778, 0), 
			new WorldPoint(1825, 3777, 0), 
			new WorldPoint(1829, 3774, 0) };

	public void paint(Graphics Graphs) {

		Graphics2D g = (Graphics2D)Graphs;
		long runTime = System.currentTimeMillis() - startTime;
		fishPerHour = (int)(fishCaught / ((System.currentTimeMillis() - this.startTime) / 3600000.0D));
		g.setColor(Color.DARK_GRAY);
		g.fillRect(5, 257, 200, 80);
		g.setColor(Color.BLACK);
		g.drawRect(5, 257, 200, 80);
		g.setColor(Color.WHITE);
		g.drawString("Shun Anglers", 10, 272);
		g.setColor(Color.WHITE);
		g.drawString("XP per hour: " + formatValue(hourlyxp), 10, 287);
		//g.drawString("XP per hour: " + fishCaught, 10, 287);
		g.drawString("Caught per hour: " + fishPerHour, 10, 302);
		g.drawString("Current Level: " + fishinglvl, 10, 317);
		g.setColor(Color.WHITE);
		g.drawString("Time Fishing Anglers: " + formatTime(runTime), 10, 332);
		totalxp = ctx.skills.experience(SimpleSkills.Skills.FISHING) - startxp;
		long end = System.currentTimeMillis();
		float sec = (float)(end - start) / 1000.0F;
		hourlyxp = (int)(totalxp / sec * 3600.0F);
	}

	private String formatTime(long ms) {
		long s = ms / 1000L, m = s / 60L, h = m / 60L;
		s %= 60L;
		m %= 60L;
		h %= 24L;
		return String.format("%02d:%02d:%02d", new Object[] { Long.valueOf(h), Long.valueOf(m), Long.valueOf(s) });
	}

	public final String formatValue(long l) {
		return (l > 1000000L) ? String.format("%.2fm", new Object[] { Double.valueOf(l / 1000000.0D) }) : ((l > 1000L) ? String.format("%.1fk", new Object[] { Double.valueOf(l / 1000.0D) }) : (new StringBuilder(String.valueOf(l))).toString());
	}

	public void onChatMessage(ChatMessage chatMessage) {
		String message = chatMessage.getMessage().toLowerCase();
		if (message.contains("you catch a anglerfish"))
			fishCaught++; 
	}

	public void onExecute() {
		startxp = ctx.skills.experience(SimpleSkills.Skills.FISHING);
		fishinglvl = ctx.skills.realLevel(SimpleSkills.Skills.FISHING);
		startTime = System.currentTimeMillis();
		gui = new GUI(ctx, this);
		gui.setVisible(true);
		started = false;
	}

	public void onProcess() {

		SimpleObject bank = ctx.objects.populate().filter("Bank booth").nearest().next();

		if(!ctx.game.tab(Game.Tab.INVENTORY)) {
			ctx.game.tab(Game.Tab.INVENTORY);
		}
		if (started) {
			if(!ctx.inventory.populate().filter("Sandworms", "Fishing rod").isEmpty()) {
				if (ctx.inventory.populate().population() < 28) {
					if (FISHING_AREA.containsPoint(ctx.players.getLocal().getLocation())) {
						if (ctx.players.getLocal().getAnimation() == -1) {
							SimpleNpc fishingspot = ctx.npcs.populate().filter(6825).nearest().next();
							if (fishingspot != null && fishingspot.validateInteractable()) {
								fishingspot.click("Bait");
								ctx.onCondition(() -> (ctx.players.getLocal().getAnimation() != -1), 1500);
							} 
						}
					} else {
						if (ctx.game.tab(Game.Tab.MAGIC) && ctx.widgets.getWidget(218, 6) != null) {			
							ctx.widgets.getWidget(218, 6).click(3);
							ctx.onCondition(() -> !edge.containsPoint(ctx.players.getLocal().getLocation()), 250, 10);
						}
					} 
				} else if (!ctx.pathing.inArea(edge)) {
					ctx.magic.castHomeTeleport();
					ctx.onCondition(() -> edge.containsPoint(ctx.players.getLocal().getLocation()), 250, 10);
				} else if (!ctx.bank.bankOpen()) {
					if (bank != null && bank.validateInteractable()) {
						bank.click("bank");
						ctx.onCondition(() -> ctx.bank.bankOpen(), 250,15);
					} 
				} else {
					ctx.bank.depositAllExcept("Fishing rod", "Sandworms");
					ctx.onCondition(() -> ctx.inventory.populate().filter("Raw anglerfish").isEmpty(), 250, 10);
					ctx.bank.closeBank();
					ctx.onCondition(() -> !ctx.bank.bankOpen(), 250, 10);
				} 
			} else {
				getItems();
			}
		}
	}

	private void getItems() {
		SimpleObject bank = ctx.objects.populate().filter("Bank booth").nearest().next();
		if(ctx.pathing.inArea(edge)) {
			if(!ctx.bank.bankOpen()) {
				if (bank != null && bank.validateInteractable()) {
					bank.click(0);
				}
			} else {
				ctx.bank.withdraw("Fishing rod", 1);
				ctx.onCondition(() -> ctx.inventory.populate().filter("Fishing rod").population() == 1);
				ctx.bank.withdraw("Sandworms", 10000);
				ctx.onCondition(() -> ctx.inventory.populate().filter("Sandworms").population() >= 1);
			}
		} else {
			ctx.magic.castHomeTeleport();
			ctx.onCondition(() -> edge.containsPoint(ctx.players.getLocal().getLocation()), 250, 10);
		}
	}

	public void onTerminate() {
		System.out.print("ShunAnglers Stopped");
		gui.dispose();
	}
}
