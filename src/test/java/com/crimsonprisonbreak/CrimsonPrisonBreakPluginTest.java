package com.crimsonprisonbreak;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CrimsonPrisonBreakPluginTest
{
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CrimsonPrisonBreakPlugin.class);
		RuneLite.main(args);
	}
}