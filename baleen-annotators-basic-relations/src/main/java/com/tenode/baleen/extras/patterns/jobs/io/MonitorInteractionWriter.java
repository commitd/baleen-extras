package com.tenode.baleen.extras.patterns.jobs.io;

import java.util.Collection;
import java.util.stream.Collectors;

import com.tenode.baleen.extras.patterns.jobs.interactions.data.InteractionDefinition;

import uk.gov.dstl.baleen.uima.UimaMonitor;

/**
 * Writes interactions to a UimaMonitor logger.
 */
public class MonitorInteractionWriter implements InteractionWriter {

	private final UimaMonitor monitor;

	/**
	 * Instantiates a new monitor interaction writer.
	 *
	 * @param monitor
	 *            the monitor
	 */
	public MonitorInteractionWriter(UimaMonitor monitor) {
		this.monitor = monitor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tenode.baleen.extras.jobs.io.InteractionWriter#write(com.tenode.baleen.extras.jobs.
	 * interactions.data.InteractionDefinition, java.util.Collection)
	 */
	@Override
	public void write(InteractionDefinition interaction, Collection<String> alternatives) {
		monitor.info("Interaction {} {} {} {} {}", interaction.getType(), interaction.getSubType(),
				interaction.getSource(), interaction.getTarget(),
				alternatives.stream().collect(Collectors.joining(";")));
	}

}
