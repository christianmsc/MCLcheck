package dslmetrics.persistences;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

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
	private double currentValue;
	private boolean isViolated = true;
	private IJavaProject jp;
	
	public Rule(String[] rule, IJavaProject jp){
		this.rule = new ArrayList<String>(Arrays.asList(rule));
		metric = this.rule.get(0).toLowerCase();
		levelSource = this.rule.get(1);
		operator = this.rule.get(2);
		valueDesired = Double.parseDouble(this.rule.get(3));
		this.jp = jp;
		
	}
	
	public String getRule(){
		return rule.get(0)+" "+rule.get(1)+" "+rule.get(2)+" "+rule.get(3);
	}
	
	public double getCurrentMetricValue(){
		return currentValue;
	}
	
	public boolean isViolated(){
		
		
			
		currentValue = getMetric(jp,getMetricType());
		
		if(operator.compareTo("<") == 0){
			if(currentValue < valueDesired){
				isViolated = false;
			}
		}
		else if(operator.compareTo("<=") == 0){
			if(currentValue <= valueDesired){
				isViolated = false;
			}
		}
		else if(operator.compareTo("=") == 0){
			if(currentValue == valueDesired){
				isViolated = false;
			}
		}
		
		else if(operator.compareTo("!=") == 0){
			if(currentValue != valueDesired){
				isViolated = false;
			}
		}
		
		else if(operator.compareTo(">") == 0){
			if(currentValue > valueDesired){
				isViolated = false;
			}
		}
		else if(operator.compareTo(">=") == 0){
			if(currentValue >= valueDesired){
				isViolated = false;
			}
		}
		
		
		return isViolated;
	}
	
	private String getMetricType(){
		
		String metricName = "";
		
		//McCabe Cyclomatic Complexity (VG)
		if(metric.compareTo("mccabe") == 0 || metric.compareTo("vg") == 0){
			metricName = "VG";
		}
		
		//Number of Parameters (PAR)
		else if(metric.compareTo("par") == 0 || metric.compareTo("params") == 0){
			metricName = "PAR";
		}
		
		//Nested Block Depth (NBD)
		else if(metric.compareTo("nbd") == 0 || metric.compareTo("nestedblockdepth") == 0){
			metricName = "NBD";
		}
		
		//Afferent Coupling (CA)
		else if(metric.compareTo("ca") == 0){
			metricName = "CA";
		}
		
		//Efferent Coupling (CE)
		else if(metric.compareTo("ce") == 0){
			metricName = "CE";
		}
		
		//Instability (RMI)
		else if(metric.compareTo("rmi") == 0){
			metricName = "RMI";
		}
		
		//Abstractness (RMA)
		else if(metric.compareTo("rma") == 0){
			metricName = "RMA";
		}
		
		//Normalized Distance (RMD)
		else if(metric.compareTo("rmd") == 0){
			metricName = "RMD";
		}
		
		//Depth of Inheritance Tree (DIT)
		else if(metric.compareTo("dit") == 0){
			metricName = "DIT";
		}
		
		// Weighted Methods per Class (WMC)
		else if(metric.compareTo("wmc") == 0){
			metricName = "WMC";
		}
		
		// Number of Children (NSC)
		else if(metric.compareTo("nsc") == 0){
			metricName = "NSC";
		}
		
		// Number of Overridden Methods (NORM)
		else if(metric.compareTo("norm") == 0){
			metricName = "NORM";
		}
		
		//Lack of Cohesion of Methods (LCOM)
		else if(metric.compareTo("lcom") == 0 ){
			metricName = "LCOM";
		} 
		
		// Number of Attributes (NOF)
		else if(metric.compareTo("nof") == 0){
			metricName = "NOF";
		}
		
		// Number of Static Attributes/Fields (NSF)
		else if(metric.compareTo("nsf") == 0){
			metricName = "NSF";
		}
		
		// Number of Methods (NOM)
		else if(metric.compareTo("nom") == 0){
			metricName = "NOM";
		}
		
		// Number of Normal Methods - excludes getters and setters (NONM)
		else if(metric.compareTo("nonm") == 0){
			metricName = "NONM";
		}
		
		// Number of Inherited Methods (NMI)
		else if(metric.compareTo("nmi") == 0){
			metricName = "NMI";
		}
		
		// Number of Static Methods (NSM)
		else if(metric.compareTo("nsm") == 0){
			metricName = "NSM";
		}
		
		// Specialization Index (SIX)
		else if(metric.compareTo("six") == 0){
			metricName = "SIX";
		}
		
		// Specialization Index 2 (SIX2)
		else if(metric.compareTo("six2") == 0){
			metricName = "SIX2";
		}
		
		// Number of Classes (NOC)
		else if(metric.compareTo("noc") == 0){
			metricName = "NOC";
		}
		
		// Number of Interfaces (NOI)
		else if(metric.compareTo("noi") == 0){
			metricName = "NOI";
		}
		
		// Number of Packages (NOP)
		else if(metric.compareTo("nop") == 0){
			metricName = "NOP";
		}
		
		// Total Lines of Code (TLOC)
		else if(metric.compareTo("tloc") == 0){
			metricName = "TLOC";
		}
		
		// New Methods Line of Code (MLOC)
		else if(metric.compareTo("mloc") == 0){
			metricName = "MLOC";
		}
		
		// Design Size in Classes (DSC) - Equivalent to the NUM_TYPES (NOC) metric 
		else if(metric.compareTo("dsc") == 0){
			metricName = "DSC";
		}
		
		// Average Number of Ancestors (ANA)
		else if(metric.compareTo("ana") == 0){
			metricName = "ANA";
		}
		
		// Measure of Functional Abstraction (MFA)
		else if(metric.compareTo("mfa") == 0){
			metricName = "MFA";
		}
		
		// Class Interface Size (CIS)
		else if(metric.compareTo("cis") == 0){
			metricName = "CIS";
		}
		
		// Measure of Aggregation (MOA)
		else if(metric.compareTo("moa") == 0){
			metricName = "MOA";
		}
		
		// Data Access Metric (DAM)
		else if(metric.compareTo("dam") == 0){
			metricName = "DAM";
		}
		
		// Number of Polymorhic Methods (NOPM)
		else if(metric.compareTo("nopm") == 0){
			metricName = "NOPM";
		}
		
		// Cohesion Among Methods of Class (CAM)
		else if(metric.compareTo("cam") == 0){
			metricName = "CAM";
		}
		
		// Direct Class Coupling (DCC)
		else if(metric.compareTo("dcc") == 0){
			metricName = "DCC";
		}
		
		//Reusability (REU)
		else if(metric.compareTo("reusability") == 0 || metric.compareTo("reu") == 0){
			metricName = "REU";
		}
		
		// Flexibility (FLE)
		else if(metric.compareTo("fle") == 0 || metric.compareTo("flexibility") == 0){
			metricName = "FLE";
		}
		
		// Effectiveness (EFE)
		else if(metric.compareTo("efe") == 0 || metric.compareTo("effectiveness") == 0){
			metricName = "EFE";
		}
		
		// Extendibility (EXT)
		else if(metric.compareTo("ext") == 0 || metric.compareTo("extendibility") == 0){
			metricName = "EXT";
		}
		
		// Functionality (FUN)
		else if(metric.compareTo("fun") == 0 || metric.compareTo("functionality") == 0){
			metricName = "FUN";
		}
		
		// Understandability (ENT ou UND)
		else if(metric.compareTo("ent") == 0 || metric.compareTo("und") == 0 || metric.compareTo("understandability") == 0){
			metricName = "ENT";
		}
		
		// Number of Hierarchies (NOH)
		else if(metric.compareTo("noh") == 0){
			metricName = "NOH";
		}
		
		return metricName;
	}
	
	private IJavaElement getLevelSource(){
		//String levelSourceName = "";
		try {
			
			//Verifica se eh projeto
			if(levelSource.compareTo("project") == 0 || levelSource.compareTo(jp.getElementName()) == 0){
				//levelSourceName = "project";
				return jp.getPrimaryElement();
			}
			
			//Verifica se eh pacote
			IPackageFragment[] packages = jp.getPackageFragments();
			
			for(IPackageFragment pckg : packages){
				if(pckg.getElementName().compareTo(levelSource) == 0){
					//levelSourceName = "package";
					return pckg.getPrimaryElement();
				}
			}
		
			
			//Verifica se eh classe
			IType type = jp.findType(levelSource);
			if(type != null){
				//levelSourceName = "class";
				return type.getPrimaryElement();
			}
			
			//Vefirica se eh metodo
			int index = levelSource.lastIndexOf(".");
			String className = levelSource.substring(0, index);
			int index2 = levelSource.indexOf("(");
			String methodName = levelSource.substring(index+1,index2);
			int index3 = levelSource.lastIndexOf(")");
			String[] parameters = levelSource.substring(index2+1,index3).split(",");
			
			IMethod[] methods = jp.findType(className).getMethods();

			for (IMethod method : methods) {

				if (method.getElementName().compareTo(methodName) == 0) {
					if (method.getNumberOfParameters() == parameters.length) {
						String[] parametersMethod = method.getParameterTypes();
						boolean todosBatem = true;
						for (int i = 0; i < method.getNumberOfParameters(); i++) {
							if (parametersMethod[i].compareTo(parameters[i]) != 0) {
								todosBatem = false;
							}
						}

						if (todosBatem) {
							//levelSourceName = "method";
							return method.getPrimaryElement();

						}

					}

				}
			}
		
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private double getMetric(IJavaProject jp, String metric) {

		double metricValue = 0;

		try {

			AbstractMetricSource ms = Dispatcher.getAbstractMetricSource(getLevelSource());

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
