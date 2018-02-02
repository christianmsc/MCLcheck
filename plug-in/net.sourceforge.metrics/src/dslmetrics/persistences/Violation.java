package dslmetrics.persistences;

public class Violation {
	
	private String rule;
	private double currentMetricValue;
	private double average;
	private double[] topValues;
	private double[] bottonValues;
	
	//Construtor para violação com operadores de comparação
	public Violation(String rule, double currentMetricValue, double average, double[] topBottonValues) {
		this.rule = rule;
		this.currentMetricValue = currentMetricValue;
		this.average = average;
		
		if(topBottonValues == null){
			topValues = bottonValues = topBottonValues;
		}
		else{
			if(topBottonValues[0] > topBottonValues[1]){
				this.topValues = topBottonValues;
			}
			else{
				this.bottonValues = topBottonValues;
			}
		}
		
	}
	
	public String getRule() {
		return rule;
	}
	
	public double getCurrentMetricValue() {
		return currentMetricValue;
	}

	public double getAverage() {
		return average;
	}

	public double[] getTopValues() {
		return topValues;
	}

	public double[] getBottonValues() {
		return bottonValues;
	}
	
	

}
