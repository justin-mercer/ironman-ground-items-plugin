package com.example;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Test Plugin",
	description = "A simple test plugin to verify sideloading works"
)
public class TestPlugin extends Plugin
{
	@Override
	protected void startUp() throws Exception
	{
		System.out.println("Test Plugin started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		System.out.println("Test Plugin stopped!");
	}
}
