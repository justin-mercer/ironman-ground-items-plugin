package com.ironmangrounditems;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ironmangrounditems")
public interface IronmanGroundItemsConfig extends Config
{
	@ConfigItem(
		keyName = "hideOtherPlayerItems",
		name = "Hide Other Player Items",
		description = "Hide items dropped by other players (recommended for Ironman accounts)"
	)
	default boolean hideOtherPlayerItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hidePublicItems",
		name = "Hide Public Items",
		description = "Hide items that are public (no specific owner)"
	)
	default boolean hidePublicItems()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideOwnItems",
		name = "Hide Own Items",
		description = "Hide items that you dropped (not recommended)"
	)
	default boolean hideOwnItems()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideGroupItems",
		name = "Hide Group Items",
		description = "Hide items dropped by group members (for Group Ironman)"
	)
	default boolean hideGroupItems()
	{
		return false;
	}
}
