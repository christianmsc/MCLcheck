package dslmetrics.persistences;

public class Violation {
	
	private String rule;
	private double currentMetricValue;
	
	public Violation(String rule, double currentMetricValue) {
		this.rule = rule;
		this.currentMetricValue = currentMetricValue;
	}
	
	public String getRule() {
		return rule;
	}
	
	public double getCurrentMetricValue() {
		return currentMetricValue;
	}
	
	

}
