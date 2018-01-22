package dslmetrics.persistences;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.core.IJavaProject;

import net.sourceforge.metrics.core.Log;
import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;

public class Rule {
	
	private ArrayList<String> rule;
	private String metric;
	private String levelSource;
	private String operator;
	private double valueDesired;
	private boolean isViolated = true;
	private IJavaProject jp;
	
	public Rule(String[] rule, IJavaProject jp){
		this.rule = new ArrayList<String>(Arrays.asList(rule));
		metric = this.rule.get(0);
		levelSource = this.rule.get(1);
		operator = this.rule.get(2);
		valueDesired = Double.parseDouble(this.rule.get(3));
		this.jp = jp;
		
	}
	
	public ArrayList<String> getRule(){
		return rule;
	}
	
	public boolean isViolated(){
		
		if(getLevelSource().compareTo("project") == 0){
			double metricValue = getProjectMetric(jp,getMetricType());
			if(operator.compareTo("<") == 0){
				if(metricValue < valueDesired){
					isViolated = false;
				}
			}
			else if(operator.compareTo("<=") == 0){
				if(metricValue <= valueDesired){
					isViolated = false;
				}
			}
			else if(operator.compareTo("=") == 0){
				if(metricValue == valueDesired){
					isViolated = false;
				}
			}
			
			else if(operator.compareTo("!=") == 0){
				if(metricValue != valueDesired){
					isViolated = false;
				}
			}
			
			else if(operator.compareTo(">") == 0){
				if(metricValue > valueDesired){
					isViolated = false;
				}
			}
			else if(operator.compareTo(">=") == 0){
				if(metricValue >= valueDesired){
					isViolated = false;
				}
			}
		}
		
		return isViolated;
	}
	
	private String getMetricType(){
		String metricName = "";
		if(metric.compareTo("LCOM") == 0 
				|| metric.compareTo("lcom") == 0 ){
			metricName = "LCOM";
		} else if(metric.compareTo("reusability") == 0){
			metricName = "REU";
		}
		return metricName;
	}
	
	private String getLevelSource(){
		String levelSourceName = "";
		if(levelSource.compareTo("project") == 0){
			levelSourceName = "project";
		}
		return levelSourceName;
	}
	
	private double getProjectMetric(IJavaProject jp, String metric) {

		double metricValue = 0;

		try {

			AbstractMetricSource ms = Dispatcher.getAbstractMetricSource(jp.getPrimaryElement());

			if (ms == null) {
				return metricValue;
			}

			MetricsPlugin plugin = MetricsPlugin.getDefault();
			String[] names = plugin.getMetricIds();
			for (int i = 0; i < names.length; i++) {

				if (names[i].matches(metric)) {

					Metric m = ms.getValue(names[i]);
					metricValue = m.getValue();
					break;
				}
			}

		} catch (Throwable e) {
			//TODO verificar se, quando da nulo, eh pq a metrica não eh de projeto/pacote/classe (por enquanto retorna 0)
			return 0;
			//Log.logError("MetricsTable::setMetrics", e);
		}

		return metricValue;
	}
}
