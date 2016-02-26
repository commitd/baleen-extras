package com.tenode.baleen.extras.jobs.writers;

import java.util.List;
import java.util.stream.Collectors;

import com.tenode.baleen.extras.jobs.interactions.data.InteractionWord;

import uk.gov.dstl.baleen.uima.UimaMonitor;

public class MonitorInteractionWriter implements InteractionWordWriter {

	private final UimaMonitor monitor;

	public MonitorInteractionWriter(UimaMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public void write(InteractionWord word, String relationshipType, String lemma, List<String> alternatives) {
		monitor.info("Interaction {} {}", relationshipType,
				alternatives.stream().collect(Collectors.joining(";")));

		word.getPairs().stream().forEach(p -> {
			monitor.info("Interaction constraints {} {}", p.getSource(), p.getTarget());
		});
	}

}
