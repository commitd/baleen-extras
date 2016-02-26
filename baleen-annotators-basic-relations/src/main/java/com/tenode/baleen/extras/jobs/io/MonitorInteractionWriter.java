package com.tenode.baleen.extras.jobs.io;

import java.util.Collection;
import java.util.stream.Collectors;

import com.tenode.baleen.extras.jobs.interactions.data.InteractionRelation;

import uk.gov.dstl.baleen.uima.UimaMonitor;

public class MonitorInteractionWriter implements InteractionWriter {

	private final UimaMonitor monitor;

	public MonitorInteractionWriter(UimaMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public void write(InteractionRelation interaction, Collection<String> alternatives) {
		monitor.info("Interaction {} {} {} {} {}", interaction.getType(), interaction.getSubType(),
				interaction.getSource(), interaction.getTarget(),
				alternatives.stream().collect(Collectors.joining(";")));
	}

}
