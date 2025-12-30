package us.poliscore.legiscan.view;

import java.time.Duration;

import lombok.Data;
import lombok.Getter;

public enum RefreshFrequency {
	HOURLY(Duration.ofHours(1)),
	DAILY(Duration.ofDays(1)),
	WEEKLY(Duration.ofDays(7));
	
	private Duration minFreshness;
	
	RefreshFrequency(Duration minFreshness) {
		this.minFreshness = minFreshness;
	}
	
	public Duration asDuration() { return this.minFreshness; }
}
