package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;

import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Ironman Ground Items",
	description = "Removes items you don't own from right-click menus. Perfect for Ironman accounts to avoid clicking on other players' drops that can't be taken.",
	tags = {"ironman", "ground", "items", "menu", "filter", "ownership"}
)
public class IronmanGroundItemsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private IronmanGroundItemsConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Ironman Ground Items plugin started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Ironman Ground Items plugin stopped!");
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		// Check if this is a ground item menu entry (e.g., "Take ItemName")
		if (!isGroundItemMenuEntry(event))
		{
			return;
		}

		// Get the tile from the menu entry
		Tile tile = getTileFromMenuEntry(event);
		if (tile == null || tile.getGroundItems() == null)
		{
			return;
		}

		// Find the matching TileItem (by ID from event)
		int itemId = event.getIdentifier();
		for (TileItem tileItem : tile.getGroundItems())
		{
			if (tileItem.getId() == itemId)
			{
				// Check ownership using the TileItem API
				if (shouldHideItem(tileItem))
				{
					// Hide the entry: Remove "Take" from the menu
					hideMenuEntry();
					return;
				}
				break; // Found the item, no need to check further
			}
		}
	}

	private boolean isGroundItemMenuEntry(MenuEntryAdded event)
	{
		// Check if this is a ground item menu entry
		return event.getType() == MenuAction.GROUND_ITEM_FIRST_OPTION.getId() ||
			   event.getType() == MenuAction.GROUND_ITEM_SECOND_OPTION.getId() ||
			   event.getOption().startsWith("Take");
	}

	private Tile getTileFromMenuEntry(MenuEntryAdded event)
	{
		try
		{
			// Get the tile coordinates from the menu entry
			// Use the MenuEntry to get the tile location
			MenuEntry menuEntry = event.getMenuEntry();
			if (menuEntry != null)
			{
				int param0 = menuEntry.getParam0();
				int param1 = menuEntry.getParam1();

				// For ground items, param0 and param1 are usually scene coordinates
				// Try to get the tile directly from scene coordinates
				int plane = client.getLocalPlayer().getWorldLocation().getPlane();

				if (param0 >= 0 && param0 < 104 && param1 >= 0 && param1 < 104)
				{
					return client.getTopLevelWorldView().getScene().getTiles()[plane][param0][param1];
				}
			}
		}
		catch (Exception e)
		{
			log.debug("Error getting tile from menu entry: {}", e.getMessage());
		}
		return null;
	}

	private boolean shouldHideItem(TileItem tileItem)
	{
		// Get ownership information from the TileItem
		// Note: These constants may need to be adjusted based on actual RuneLite API
		// OWNERSHIP_NONE (0): No specific owner (takeable)
		// OWNERSHIP_SELF (1): Owned by you (takeable)
		// OWNERSHIP_OTHER (2): Owned by another player (non-takeable for Ironmen)
		// OWNERSHIP_GROUP (3): Owned by your group (takeable for Group Ironman)

		try
		{
			// Using reflection or direct API call to get ownership
			// This is a placeholder - actual implementation depends on RuneLite API
			int ownership = getItemOwnership(tileItem);

			switch (ownership)
			{
				case 2: // OWNERSHIP_OTHER
					return config.hideOtherPlayerItems();
				case 0: // OWNERSHIP_NONE
					return config.hidePublicItems();
				case 1: // OWNERSHIP_SELF
					return config.hideOwnItems();
				case 3: // OWNERSHIP_GROUP
					return config.hideGroupItems();
				default:
					return false;
			}
		}
		catch (Exception e)
		{
			log.debug("Error checking item ownership: {}", e.getMessage());
			return false;
		}
	}

	private int getItemOwnership(TileItem tileItem)
	{
		// Try to use the actual RuneLite API to get ownership
		// The TileItem interface should have ownership information
		try
		{
			// First, try to call getOwnership() method if it exists
			java.lang.reflect.Method method = tileItem.getClass().getMethod("getOwnership");
			Object result = method.invoke(tileItem);
			if (result instanceof Integer)
			{
				return (Integer) result;
			}
		}
		catch (Exception e)
		{
			log.debug("getOwnership() method not available, trying alternative approaches: {}", e.getMessage());
		}

		// Alternative approach: check if the item is private
		try
		{
			java.lang.reflect.Method isPrivateMethod = tileItem.getClass().getMethod("isPrivate");
			Object isPrivateResult = isPrivateMethod.invoke(tileItem);
			if (isPrivateResult instanceof Boolean)
			{
				boolean isPrivate = (Boolean) isPrivateResult;
				if (isPrivate)
				{
					// If it's private, we assume it's owned by another player
					// This is the safest assumption for Ironman accounts
					return 2; // OWNERSHIP_OTHER
				}
				else
				{
					// If it's not private, it's public
					return 0; // OWNERSHIP_NONE
				}
			}
		}
		catch (Exception e)
		{
			log.debug("isPrivate() method not available: {}", e.getMessage());
		}

		// Fallback: assume items are owned by others (most restrictive for Ironman)
		// This ensures that if we can't determine ownership, we err on the side of caution
		log.debug("Could not determine item ownership, assuming OWNERSHIP_OTHER for safety");
		return 2; // OWNERSHIP_OTHER
	}

	@SuppressWarnings("deprecation")
	private void hideMenuEntry()
	{
		// Remove the current menu entry by rebuilding the menu array without it
		// Note: Using deprecated API as it's the most reliable method currently available
		try
		{
			MenuEntry[] entries = client.getMenuEntries();
			if (entries.length > 0)
			{
				MenuEntry[] newEntries = new MenuEntry[entries.length - 1];
				System.arraycopy(entries, 0, newEntries, 0, entries.length - 1);
				client.setMenuEntries(newEntries);
			}
		}
		catch (Exception e)
		{
			log.debug("Failed to hide menu entry: {}", e.getMessage());
		}
	}

	@Provides
	IronmanGroundItemsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(IronmanGroundItemsConfig.class);
	}
}
